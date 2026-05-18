package com.pulsefit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.pulsefit.config.AppProperties;
import com.pulsefit.domain.BookingEntity;
import com.pulsefit.domain.BookingStatus;
import com.pulsefit.domain.InvoiceEntity;
import com.pulsefit.domain.PaymentEntity;
import com.pulsefit.domain.PaymentStatus;
import com.pulsefit.domain.PaymentType;
import com.pulsefit.domain.ProcessedStripeEventEntity;
import com.pulsefit.domain.SubscriptionEntity;
import com.pulsefit.domain.SubscriptionStatus;
import com.pulsefit.exception.NotFoundException;
import com.pulsefit.repository.BookingRepository;
import com.pulsefit.repository.InvoiceRepository;
import com.pulsefit.repository.PaymentRepository;
import com.pulsefit.repository.ProcessedStripeEventRepository;
import com.pulsefit.repository.SubscriptionRepository;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.Charge;
import com.stripe.model.Invoice;
import com.stripe.model.Subscription;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StripeWebhookService {
  private final AppProperties properties;
  private final ProcessedStripeEventRepository events;
  private final BookingRepository bookings;
  private final PaymentRepository payments;
  private final SubscriptionRepository subscriptions;
  private final InvoiceRepository invoices;

  @Transactional
  public void handleWebhook(String payload, String signature) {
    Event event;
    try {
      event = Webhook.constructEvent(payload, signature, properties.stripe().webhookSecret());
    } catch (SignatureVerificationException e) {
      throw new IllegalArgumentException("Invalid Stripe signature", e);
    }

    if (events.findByEventId(event.getId()).isPresent()) {
      return;
    }

    ProcessedStripeEventEntity processed = new ProcessedStripeEventEntity();
    processed.setEventId(event.getId());
    processed.setEventType(event.getType());
    processed.setProcessedAt(LocalDateTime.now());
    events.save(processed);

    switch (event.getType()) {
      case "checkout.session.completed" -> handleCheckoutSessionCompleted(event);
      case "invoice.paid" -> handleInvoicePaid(event);
      case "customer.subscription.deleted" -> handleSubscriptionDeleted(event);
      case "charge.refunded" -> handleChargeRefunded(event);
      default -> { }
    }
  }

  private void handleCheckoutSessionCompleted(Event event) {
    Session session = (Session) event.getDataObjectDeserializer().getObject().orElseThrow(() -> new NotFoundException("Stripe session missing"));
    String bookingId = session.getMetadata().get("bookingId");
    String paymentType = session.getMetadata().getOrDefault("paymentType", PaymentType.PER_SESSION.name());
    PaymentEntity payment = session.getPaymentIntent() != null ? payments.findByStripePaymentIntentId(session.getPaymentIntent()).orElseGet(PaymentEntity::new) : new PaymentEntity();
    payment.setStripeCheckoutSessionId(session.getId());
    payment.setStripePaymentIntentId(session.getPaymentIntent());
    payment.setStripeCustomerId(session.getCustomer());
    payment.setAmount(BigDecimal.valueOf((session.getAmountTotal() == null ? 0L : session.getAmountTotal()) / 100.0));
    payment.setCurrency(session.getCurrency() == null ? "eur" : session.getCurrency());
    payment.setStatus(PaymentStatus.PAID);
    payment.setPaymentType(PaymentType.valueOf(paymentType));

    if (bookingId != null) {
      BookingEntity booking = bookings.findById(Long.valueOf(bookingId)).orElseThrow(() -> new NotFoundException("Booking not found"));
      booking.setStatus(BookingStatus.CONFIRMED);
      booking.setStripeCheckoutSessionId(session.getId());
      booking.setStripePaymentIntentId(session.getPaymentIntent());
      booking.getSession().setBookedCount(booking.getSession().getBookedCount() + 1);
      bookings.save(booking);
      payment.setBooking(booking);
    } else if (payment.getPaymentType() == PaymentType.CLASS_PACK) {
      SubscriptionEntity subscription = subscriptions.findByStripeSubscriptionId(session.getSubscription()).orElseGet(SubscriptionEntity::new);
      subscription.setStripeSubscriptionId(session.getSubscription());
      subscription.setStripeCustomerId(session.getCustomer());
      subscription.setCreditsRemaining(10);
      subscription.setStatus(SubscriptionStatus.ACTIVE);
      subscription.setPlanName("10-class pack");
      subscriptions.save(subscription);
      payment.setStripeSubscriptionId(session.getSubscription());
    }

    payments.save(payment);
  }

  private void handleInvoicePaid(Event event) {
    Invoice invoice = (Invoice) event.getDataObjectDeserializer().getObject().orElseThrow(() -> new NotFoundException("Stripe invoice missing"));
    PaymentEntity payment = invoice.getSubscription() == null ? null : payments.findByStripeSubscriptionId(invoice.getSubscription()).orElse(null);
    if (payment != null) {
      payment.setStatus(PaymentStatus.PAID);
      payments.save(payment);
    }
    InvoiceEntity entity = new InvoiceEntity();
    entity.setPayment(payment);
    entity.setStripeInvoiceId(invoice.getId());
    entity.setInvoiceNumber(invoice.getNumber());
    entity.setHostedInvoiceUrl(invoice.getHostedInvoiceUrl());
    entity.setCurrency(invoice.getCurrency());
    entity.setAmount(BigDecimal.valueOf((invoice.getAmountPaid() == null ? 0L : invoice.getAmountPaid()) / 100.0));
    entity.setStatus(invoice.getStatus());
    invoices.save(entity);

    if (invoice.getSubscription() != null) {
      subscriptions.findByStripeSubscriptionId(invoice.getSubscription()).ifPresent(subscription -> {
        subscription.setCreditsRemaining(10);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscriptions.save(subscription);
      });
    }
  }

  private void handleSubscriptionDeleted(Event event) {
    Subscription subscription = (Subscription) event.getDataObjectDeserializer().getObject().orElseThrow(() -> new NotFoundException("Stripe subscription missing"));
    subscriptions.findByStripeSubscriptionId(subscription.getId()).ifPresent(entity -> {
      entity.setStatus(SubscriptionStatus.CANCELLED);
      entity.setCancelledAt(LocalDateTime.now());
      subscriptions.save(entity);
    });
  }

  private void handleChargeRefunded(Event event) {
    Charge charge = (Charge) event.getDataObjectDeserializer().getObject().orElse(null);
    if (charge == null || charge.getPaymentIntent() == null) {
      return;
    }
    payments.findByStripePaymentIntentId(charge.getPaymentIntent()).ifPresent(payment -> {
      payment.setStatus(PaymentStatus.REFUNDED);
      payment.setRefundedAt(LocalDateTime.now());
      payments.save(payment);
    });
  }
}
