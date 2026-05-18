package com.pulsefit.service;

import com.pulsefit.domain.BookingEntity;
import com.pulsefit.domain.BookingStatus;
import com.pulsefit.domain.SubscriptionStatus;
import com.pulsefit.domain.UserProfileEntity;
import com.pulsefit.repository.BookingRepository;
import com.pulsefit.repository.StudioRepository;
import com.pulsefit.repository.SubscriptionRepository;
import com.pulsefit.web.dto.ApiDtos.BookingSummary;
import com.pulsefit.web.dto.ApiDtos.DashboardResponse;
import com.pulsefit.web.dto.ApiDtos.NextClassResponse;
import com.pulsefit.web.dto.ApiDtos.StudioSummary;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {
  private final ProfileService profileService;
  private final BookingRepository bookings;
  private final SubscriptionRepository subscriptions;
  private final StudioRepository studios;

  @Transactional(readOnly = true)
  public DashboardResponse dashboard(Jwt jwt) {
    UserProfileEntity profile = profileService.ensureProfile(jwt);
    List<BookingEntity> profileBookings = bookings.findByProfileKeycloakIdOrderByCreatedAtDesc(profile.getKeycloakId());
    BookingEntity next = profileBookings.stream().filter(booking -> booking.getStatus() == BookingStatus.CONFIRMED || booking.getStatus() == BookingStatus.PENDING_PAYMENT).findFirst().orElse(null);
    NextClassResponse nextClass = next == null ? null : new NextClassResponse(next.getId(), next.getSession().getTemplate().getStudio().getName(), next.getSession().getTemplate().getTitle(), next.getSession().getStartsAt(), next.getSession().getTemplate().getDiscipline(), next.getSession().getTemplate().getStudio().getNeighborhood(), next.getSession().getPrice(), next.getSession().getCapacity(), next.getSession().getBookedCount());
    int creditsRemaining = subscriptions.findFirstByProfileKeycloakIdAndStatusOrderByCreatedAtDesc(profile.getKeycloakId(), SubscriptionStatus.ACTIVE).map(subscription -> subscription.getCreditsRemaining()).orElse(0);
    List<String> recentActivity = profileBookings.stream().limit(3).map(booking -> booking.getSession().getTemplate().getTitle() + " · " + booking.getStatus().name().toLowerCase()).toList();
    List<StudioSummary> recommended = studios.findByApprovedTrueAndFeaturedTrueOrderByRatingDesc().stream().map(studio -> new StudioSummary(studio.getId(), studio.getSlug(), studio.getName(), studio.getNeighborhood(), studio.getDiscipline(), studio.getImageUrl(), studio.getRating(), studio.getBasePrice(), studio.isFeatured())).toList();
    List<com.pulsefit.web.dto.ApiDtos.SessionSummary> upcomingBookings = profileBookings.stream().limit(5).map(booking -> new com.pulsefit.web.dto.ApiDtos.SessionSummary(booking.getSession().getId(), booking.getSession().getTemplate().getStudio().getName(), booking.getSession().getTemplate().getTitle(), booking.getSession().getStartsAt(), booking.getSession().getTemplate().getDiscipline(), booking.getSession().getTemplate().getStudio().getNeighborhood(), booking.getSession().getPrice(), booking.getSession().getCapacity(), booking.getSession().getBookedCount(), booking.getStatus().name())).toList();
    return new DashboardResponse(nextClass, upcomingBookings, recentActivity, creditsRemaining, recommended);
  }

  @Transactional(readOnly = true)
  public List<BookingSummary> bookings(Jwt jwt) {
    UserProfileEntity profile = profileService.ensureProfile(jwt);
    return bookings.findByProfileKeycloakIdOrderByCreatedAtDesc(profile.getKeycloakId()).stream().map(this::summary).toList();
  }

  private BookingSummary summary(BookingEntity booking) {
    return new BookingSummary(booking.getId(), booking.getSession().getTemplate().getStudio().getName(), booking.getSession().getTemplate().getTitle(), booking.getSession().getStartsAt(), booking.getStatus().name(), booking.getAmount(), booking.getPaymentType().name());
  }
}
