package com.pulsefit.service;

import com.pulsefit.domain.BookingEntity;
import com.pulsefit.domain.BookingStatus;
import com.pulsefit.domain.ClassSessionEntity;
import com.pulsefit.domain.PaymentEntity;
import com.pulsefit.domain.PaymentStatus;
import com.pulsefit.domain.PaymentType;
import com.pulsefit.domain.SessionStatus;
import com.pulsefit.domain.SubscriptionEntity;
import com.pulsefit.domain.SubscriptionStatus;
import com.pulsefit.domain.UserProfileEntity;
import com.pulsefit.exception.ConflictException;
import com.pulsefit.exception.NotFoundException;
import com.pulsefit.repository.BookingRepository;
import com.pulsefit.repository.ClassSessionRepository;
import com.pulsefit.repository.PaymentRepository;
import com.pulsefit.repository.ReviewRepository;
import com.pulsefit.repository.SubscriptionRepository;
import com.pulsefit.domain.ReviewEntity;
import com.pulsefit.web.dto.ApiDtos.BookingCheckoutResponse;
import com.pulsefit.web.dto.ApiDtos.CancelBookingRequest;
import com.pulsefit.web.dto.ApiDtos.CreateBookingRequest;
import com.pulsefit.web.dto.ApiDtos.ReviewRequest;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookingService {
  private final BookingRepository bookings;
  private final ClassSessionRepository sessions;
  private final SubscriptionRepository subscriptions;
  private final PaymentRepository payments;
  private final ReviewRepository reviews;
  private final ProfileService profileService;
  private final BillingService billingService;

  @Transactional
  public BookingCheckoutResponse createBooking(Jwt jwt, CreateBookingRequest request) {
    UserProfileEntity profile = profileService.ensureProfile(jwt);
    ClassSessionEntity session = sessions.findById(request.sessionId()).orElseThrow(() -> new NotFoundException("Session not found"));
    if (session.getBookedCount() >= session.getCapacity()) {
      throw new ConflictException("The class is sold out");
    }

    SubscriptionEntity subscription = subscriptions.findFirstByProfileKeycloakIdAndStatusOrderByCreatedAtDesc(profile.getKeycloakId(), SubscriptionStatus.ACTIVE).orElse(null);
    if (subscription != null && subscription.getCreditsRemaining() > 0) {
      subscription.setCreditsRemaining(subscription.getCreditsRemaining() - 1);
      subscriptions.save(subscription);

      BookingEntity booking = new BookingEntity();
      booking.setProfile(profile);
      booking.setSession(session);
      booking.setStatus(BookingStatus.CONFIRMED);
      booking.setPaymentType(PaymentType.CLASS_PACK);
      booking.setAmount(session.getPrice());
      booking.setCreditsUsed(1);
      bookings.save(booking);
      session.setBookedCount(session.getBookedCount() + 1);
      if (session.getBookedCount() >= session.getCapacity()) {
        session.setStatus(SessionStatus.SOLD_OUT);
      }
      sessions.save(session);

      PaymentEntity payment = new PaymentEntity();
      payment.setBooking(booking);
      payment.setAmount(BigDecimal.ZERO);
      payment.setPlatformCommission(BigDecimal.ZERO);
      payment.setCurrency("EUR");
      payment.setStatus(PaymentStatus.PAID);
      payment.setPaymentType(PaymentType.CLASS_PACK);
      payments.save(payment);
      return new BookingCheckoutResponse(booking.getId(), null, booking.getStatus().name(), false);
    }

    BookingEntity booking = new BookingEntity();
    booking.setProfile(profile);
    booking.setSession(session);
    booking.setStatus(BookingStatus.PENDING_PAYMENT);
    booking.setPaymentType(PaymentType.PER_SESSION);
    booking.setAmount(session.getPrice());
    booking.setCreditsUsed(0);
    bookings.save(booking);

    String checkoutUrl = billingService.createSessionCheckout(booking);
    booking.setStripeCheckoutSessionId(billingService.getLastCheckoutSessionId());
    bookings.save(booking);

    PaymentEntity payment = new PaymentEntity();
    payment.setBooking(booking);
    payment.setStripeCheckoutSessionId(booking.getStripeCheckoutSessionId());
    payment.setAmount(session.getPrice());
    payment.setPlatformCommission(session.getPrice().multiply(BigDecimal.valueOf(0.12)));
    payment.setCurrency("EUR");
    payment.setStatus(PaymentStatus.PENDING);
    payment.setPaymentType(PaymentType.PER_SESSION);
    payments.save(payment);

    return new BookingCheckoutResponse(booking.getId(), checkoutUrl, booking.getStatus().name(), true);
  }

  @Transactional
  public void cancelBooking(Jwt jwt, Long bookingId, CancelBookingRequest request, boolean adminOverride) {
    UserProfileEntity profile = profileService.ensureProfile(jwt);
    BookingEntity booking = bookings.findById(bookingId).orElseThrow(() -> new NotFoundException("Booking not found"));
    if (!adminOverride && !booking.getProfile().getKeycloakId().equals(profile.getKeycloakId())) {
      throw new ConflictException("Cannot cancel someone else\'s booking");
    }

    LocalDateTime cutoff = booking.getSession().getStartsAt().minusHours(booking.getSession().getTemplate().getCancellationWindowHours());
    if (!adminOverride && LocalDateTime.now().isAfter(cutoff)) {
      throw new ConflictException("Cancellation window has closed");
    }

    booking.setStatus(BookingStatus.CANCELLED);
    booking.setCancellationReason(request.reason());
    booking.setCancelledAt(LocalDateTime.now());
    bookings.save(booking);
  }

  @Transactional
  public void addReview(Jwt jwt, Long bookingId, ReviewRequest request) {
    UserProfileEntity profile = profileService.ensureProfile(jwt);
    BookingEntity booking = bookings.findById(bookingId).orElseThrow(() -> new NotFoundException("Booking not found"));
    if (booking.getStatus() != BookingStatus.ATTENDED) {
      throw new ConflictException("You can only review after attending");
    }
    ReviewEntity review = new ReviewEntity();
    review.setBooking(booking);
    review.setProfile(profile);
    review.setStudio(booking.getSession().getTemplate().getStudio());
    review.setRating(request.rating());
    review.setBody(request.body());
    reviews.save(review);
  }
}
