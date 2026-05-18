package com.pulsefit.service;

import com.pulsefit.domain.PaymentEntity;
import com.pulsefit.domain.StudioEntity;
import com.pulsefit.exception.NotFoundException;
import com.pulsefit.repository.PaymentRepository;
import com.pulsefit.repository.StudioRepository;
import com.pulsefit.web.dto.ApiDtos.AdminMetricsResponse;
import com.pulsefit.web.dto.ApiDtos.AdminUserPageResponse;
import com.pulsefit.web.dto.ApiDtos.AdminUserResponse;
import com.pulsefit.web.dto.ApiDtos.ApproveStudioRequest;
import com.pulsefit.web.dto.ApiDtos.FeaturedStudioRequest;
import com.pulsefit.web.dto.ApiDtos.RoleUpdateRequest;
import com.pulsefit.web.dto.ApiDtos.SuspendUserRequest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {
  private final KeycloakAdminService keycloakAdminService;
  private final PaymentRepository payments;
  private final StudioRepository studios;

  @Transactional(readOnly = true)
  public AdminMetricsResponse metrics() {
    List<PaymentEntity> allPayments = payments.findAll();
    BigDecimal revenue = allPayments.stream().map(PaymentEntity::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    long bookings = allPayments.stream().filter(payment -> payment.getBooking() != null).count();
    long refunds = allPayments.stream().filter(payment -> payment.getStatus().name().equals("REFUNDED")).count();
    long approvedStudios = studios.findAll().stream().filter(StudioEntity::isApproved).count();
    long pendingStudios = studios.findAll().stream().filter(studio -> !studio.isApproved()).count();
    BigDecimal utilization = bookings == 0 ? BigDecimal.ZERO : BigDecimal.valueOf(100).min(BigDecimal.valueOf(bookings * 100).divide(BigDecimal.valueOf(Math.max(1, allPayments.size())), 2, RoundingMode.HALF_UP));
    return new AdminMetricsResponse(revenue, bookings, approvedStudios, pendingStudios, refunds, utilization);
  }

  @Transactional(readOnly = true)
  public AdminUserPageResponse users(String search, int page, int size) {
    List<AdminUserResponse> users = keycloakAdminService.listUsers(search, page, size);
    long total = keycloakAdminService.countUsers();
    return new AdminUserPageResponse(users, total);
  }

  @Transactional
  public void updateRole(String userId, RoleUpdateRequest request, boolean promote) {
    keycloakAdminService.setRole(userId, request.role(), promote);
  }

  @Transactional
  public void suspendUser(String userId, SuspendUserRequest request) {
    keycloakAdminService.setSuspended(userId, request.suspended());
  }

  @Transactional
  public StudioEntity approveStudio(Long studioId, ApproveStudioRequest request) {
    StudioEntity studio = studios.findById(studioId).orElseThrow(() -> new NotFoundException("Studio not found"));
    studio.setApproved(request.approved());
    return studios.save(studio);
  }

  @Transactional
  public StudioEntity featureStudio(Long studioId, FeaturedStudioRequest request) {
    StudioEntity studio = studios.findById(studioId).orElseThrow(() -> new NotFoundException("Studio not found"));
    studio.setFeatured(request.featured());
    return studios.save(studio);
  }
}
