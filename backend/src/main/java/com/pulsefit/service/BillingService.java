package com.pulsefit.service;

import com.pulsefit.config.AppProperties;
import com.pulsefit.domain.BookingEntity;
import com.pulsefit.domain.PaymentEntity;
import com.pulsefit.domain.PaymentStatus;
import com.pulsefit.domain.PaymentType;
import com.pulsefit.domain.SubscriptionEntity;
import com.pulsefit.domain.SubscriptionStatus;
import com.pulsefit.domain.UserProfileEntity;
import com.pulsefit.exception.ConflictException;
import com.pulsefit.repository.InvoiceRepository;
import com.pulsefit.repository.PaymentRepository;
import com.pulsefit.repository.SubscriptionRepository;
import com.pulsefit.web.dto.ApiDtos.BillingSummaryResponse;
import com.pulsefit.web.dto.ApiDtos.CreateClassPackCheckoutResponse;
import com.pulsefit.web.dto.ApiDtos.PaymentReceiptResponse;
import com.stripe.exception.StripeException;
import com.stripe.model.Refund;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.param.RefundCreateParams;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BillingService {
  private final AppProperties properties;
  private final SubscriptionRepository subscriptions;
  private final PaymentRepository payments;
  private final InvoiceRepository invoices;

  private String lastCheckoutSessionId;

  @Transactional(readOnly = true)
  public BillingSummaryResponse summary(UserProfileEntity profile) {
    SubscriptionEntity active = subscriptions.findFirstByProfileKeycloakIdAndStatusOrderByCreatedAtDesc(profile.getKeycloakId(), SubscriptionStatus.ACTIVE).orElse(null);
    return new BillingSummaryResponse(
        active != null,
        active != null ? active.getCreditsRemaining() : 0,
        active != null ? active.getRenewsAt() : null,
        active != null ? "Managed in Stripe" : "No saved payment method",
        properties.publicBaseUrl() + "/billing",
        payments.findAll().stream().filter(payment -> payment.getBooking() != null && payment.getBooking().getProfile().getId().equals(profile.getId())).map(this::receipt).toList());
  }

  public String createSessionCheckout(BookingEntity booking) {
    try {
      SessionCreateParams params = SessionCreateParams.builder()
          .setMode(SessionCreateParams.Mode.PAYMENT)
          .setSuccessUrl(buildSuccessUrl("/billing/success"))
          .setCancelUrl(buildSuccessUrl("/billing/cancel"))
          .addLineItem(SessionCreateParams.LineItem.builder()
              .setQuantity(1L)
              .setPriceData(SessionCreateParams.LineItem.PriceData.builder()
                  .setCurrency("eur")
                  .setUnitAmount(booking.getAmount().movePointRight(2).longValue())
                  .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder().setName(booking.getSession().getTemplate().getTitle()).build())
                  .build())
              .build())
          .putMetadata("bookingId", String.valueOf(booking.getId()))
          .putMetadata("paymentType", PaymentType.PER_SESSION.name())
          .build();
      Session session = Session.create(params);
      lastCheckoutSessionId = session.getId();
      return session.getUrl();
    } catch (StripeException exception) {
      throw new IllegalStateException("Unable to create Stripe checkout session", exception);
    }
  }

  @Transactional
  public CreateClassPackCheckoutResponse createClassPackCheckout(UserProfileEntity profile) {
    try {
      SessionCreateParams params = SessionCreateParams.builder()
          .setMode(SessionCreateParams.Mode.SUBSCRIPTION)
          .setSuccessUrl(buildSuccessUrl("/billing/subscription/success"))
          .setCancelUrl(buildSuccessUrl("/billing/subscription/cancel"))
          .addLineItem(SessionCreateParams.LineItem.builder().setQuantity(1L).setPrice(properties.stripe().classPackPriceId()).build())
          .putMetadata("profileId", String.valueOf(profile.getId()))
          .putMetadata("paymentType", PaymentType.CLASS_PACK.name())
          .build();
      Session session = Session.create(params);
      lastCheckoutSessionId = session.getId();
      return new CreateClassPackCheckoutResponse(session.getUrl());
    } catch (StripeException exception) {
      throw new IllegalStateException("Unable to create Stripe subscription session", exception);
    }
  }

  @Transactional
  public String createCustomerPortal(UserProfileEntity profile) {
    SubscriptionEntity active = subscriptions.findFirstByProfileKeycloakIdAndStatusOrderByCreatedAtDesc(profile.getKeycloakId(), SubscriptionStatus.ACTIVE).orElse(null);
    if (active == null || active.getStripeCustomerId() == null) {
      throw new ConflictException("No Stripe customer is associated with this account");
    }
    try {
      com.stripe.param.billingportal.SessionCreateParams params = com.stripe.param.billingportal.SessionCreateParams.builder()
          .setCustomer(active.getStripeCustomerId())
          .setReturnUrl(buildSuccessUrl("/billing"))
          .build();
      return com.stripe.model.billingportal.Session.create(params).getUrl();
    } catch (StripeException exception) {
      throw new IllegalStateException("Unable to open Stripe customer portal", exception);
    }
  }

  @Transactional
  public void cancelSubscription(Long subscriptionId) {
    SubscriptionEntity subscription = subscriptions.findById(subscriptionId).orElseThrow(() -> new ConflictException("Subscription not found"));
    if (subscription.getStripeSubscriptionId() != null) {
      try {
        Subscription.retrieve(subscription.getStripeSubscriptionId()).cancel();
      } catch (StripeException ignored) {
        // Fall back to local state below.
      }
    }
    subscription.setStatus(SubscriptionStatus.CANCELLED);
    subscription.setCancelledAt(LocalDateTime.now());
    subscriptions.save(subscription);
  }

  @Transactional
  public PaymentReceiptResponse refund(Long paymentId, String reason) {
    PaymentEntity payment = payments.findById(paymentId).orElseThrow(() -> new ConflictException("Payment not found"));
    if (payment.getStripePaymentIntentId() != null) {
      try {
        Refund.create(RefundCreateParams.builder().setPaymentIntent(payment.getStripePaymentIntentId()).setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER).build());
      } catch (StripeException exception) {
        throw new IllegalStateException("Unable to issue Stripe refund", exception);
      }
    }
    payment.setStatus(PaymentStatus.REFUNDED);
    payment.setRefundReason(reason);
    payment.setRefundedAt(LocalDateTime.now());
    payments.save(payment);
    return receipt(payment);
  }

  public String getLastCheckoutSessionId() {
    return lastCheckoutSessionId;
  }

  private String buildSuccessUrl(String path) {
    return properties.publicBaseUrl() + path + "?session_id={CHECKOUT_SESSION_ID}";
  }

  private PaymentReceiptResponse receipt(PaymentEntity payment) {
    return new PaymentReceiptResponse(
        payment.getId(),
        payment.getBooking() != null ? payment.getBooking().getSession().getTemplate().getTitle() : "Stripe charge",
        payment.getAmount(),
        payment.getStatus().name().toLowerCase(),
        payment.getCreatedAt() != null ? payment.getCreatedAt().atZone(java.time.ZoneOffset.UTC).toLocalDateTime() : LocalDateTime.now());
  }
}
