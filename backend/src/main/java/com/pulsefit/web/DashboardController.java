package com.pulsefit.web;

import com.pulsefit.service.BookingService;
import com.pulsefit.service.DashboardService;
import com.pulsefit.web.dto.ApiDtos.CancelBookingRequest;
import com.pulsefit.web.dto.ApiDtos.BookingCheckoutResponse;
import com.pulsefit.web.dto.ApiDtos.BookingSummary;
import com.pulsefit.web.dto.ApiDtos.CreateBookingRequest;
import com.pulsefit.web.dto.ApiDtos.DashboardResponse;
import com.pulsefit.web.dto.ApiDtos.ReviewRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class DashboardController {
  private final DashboardService dashboardService;
  private final BookingService bookingService;

  @GetMapping("/dashboard/user")
  @PreAuthorize("hasAnyRole('user','admin')")
  DashboardResponse dashboard(@AuthenticationPrincipal Jwt jwt) {
    return dashboardService.dashboard(jwt);
  }

  @GetMapping("/bookings")
  @PreAuthorize("hasAnyRole('user','admin')")
  List<BookingSummary> bookings(@AuthenticationPrincipal Jwt jwt) {
    return dashboardService.bookings(jwt);
  }

  @PostMapping("/bookings")
  @PreAuthorize("hasRole('user')")
  BookingCheckoutResponse createBooking(@AuthenticationPrincipal Jwt jwt, @RequestBody CreateBookingRequest request) {
    return bookingService.createBooking(jwt, request);
  }

  @PostMapping("/bookings/{bookingId}/cancel")
  @PreAuthorize("hasAnyRole('user','admin')")
  void cancelBooking(@AuthenticationPrincipal Jwt jwt, @PathVariable Long bookingId, @RequestBody CancelBookingRequest request) {
    bookingService.cancelBooking(jwt, bookingId, request, false);
  }

  @PostMapping("/bookings/{bookingId}/reviews")
  @PreAuthorize("hasAnyRole('user','admin')")
  void review(@AuthenticationPrincipal Jwt jwt, @PathVariable Long bookingId, @RequestBody ReviewRequest request) {
    bookingService.addReview(jwt, bookingId, request);
  }
}
