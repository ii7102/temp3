package com.pulsefit.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class ApiDtos {
  private ApiDtos() {}

  public record StudioSummary(Long id, String slug, String name, String neighborhood, String discipline, String imageUrl, BigDecimal rating, BigDecimal basePrice, boolean featured) {}

  public record SessionSummary(Long id, String studioName, String title, LocalDateTime startsAt, String discipline, String neighborhood, BigDecimal price, int capacity, int bookedCount, String status) {}

  public record StudioDetailResponse(Long id, String slug, String name, String neighborhood, String discipline, String description, String imageUrl, String instructorName, BigDecimal rating, BigDecimal priceFrom, List<SessionSummary> sessions) {}

  public record LandingResponse(List<StudioSummary> featuredStudios, List<SessionSummary> upcomingSessions) {}

  public record NextClassResponse(Long id, String studioName, String title, LocalDateTime startsAt, String discipline, String neighborhood, BigDecimal price, int capacity, int bookedCount) {}

  public record DashboardResponse(NextClassResponse nextClass, List<SessionSummary> upcomingBookings, List<String> recentActivity, int creditsRemaining, List<StudioSummary> recommendedStudios) {}

  public record BookingSummary(Long id, String studioName, String title, LocalDateTime startsAt, String status, BigDecimal amount, String paymentType) {}

  public record BookingCheckoutResponse(Long bookingId, String checkoutUrl, String status, boolean requiresPayment) {}

  public record BillingSummaryResponse(boolean subscriptionActive, int creditsRemaining, LocalDateTime nextRenewalAt, String savedPaymentMethod, String customerPortalUrl, List<PaymentReceiptResponse> receipts) {}

  public record PaymentReceiptResponse(Long id, String label, BigDecimal amount, String status, LocalDateTime createdAt) {}

  public record ProfileResponse(String displayName, String email, boolean marketingOptIn) {}

  public record UpdateProfileRequest(@NotBlank String displayName, @NotBlank String email, boolean marketingOptIn) {}

  public record CreateBookingRequest(@NotNull Long sessionId) {}

  public record CancelBookingRequest(@NotBlank String reason) {}

  public record ReviewRequest(@Min(1) @Max(5) int rating, @NotBlank String body) {}

  public record CreateClassPackCheckoutResponse(String checkoutUrl) {}

  public record CustomerPortalResponse(String url) {}

  public record RefundRequest(@NotBlank String reason) {}

  public record AdminMetricsResponse(BigDecimal revenue, long bookings, long approvedStudios, long pendingStudios, long refunds, BigDecimal utilization) {}

  public record AdminUserResponse(String id, String username, String email, boolean enabled, List<String> roles) {}

  public record AdminUserPageResponse(List<AdminUserResponse> content, long total) {}

  public record RoleUpdateRequest(@NotBlank String role) {}

  public record SuspendUserRequest(boolean suspended) {}

  public record ApproveStudioRequest(boolean approved) {}

  public record FeaturedStudioRequest(boolean featured) {}

  public record WebhookResponse(String status) {}
}
