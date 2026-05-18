package com.pulsefit.web;

import com.pulsefit.service.AdminService;
import com.pulsefit.service.BillingService;
import com.pulsefit.web.dto.ApiDtos.AdminMetricsResponse;
import com.pulsefit.web.dto.ApiDtos.AdminUserPageResponse;
import com.pulsefit.web.dto.ApiDtos.ApproveStudioRequest;
import com.pulsefit.web.dto.ApiDtos.FeaturedStudioRequest;
import com.pulsefit.web.dto.ApiDtos.RefundRequest;
import com.pulsefit.web.dto.ApiDtos.RoleUpdateRequest;
import com.pulsefit.web.dto.ApiDtos.SuspendUserRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AdminController {
  private final AdminService adminService;
  private final BillingService billingService;

  @GetMapping("/metrics")
  @PreAuthorize("hasRole('admin')")
  AdminMetricsResponse metrics() {
    return adminService.metrics();
  }

  @GetMapping("/users")
  @PreAuthorize("hasRole('admin')")
  AdminUserPageResponse users(@RequestParam(required = false) String search, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "8") int size) {
    return adminService.users(search, page, size);
  }

  @PatchMapping("/users/{userId}/role")
  @PreAuthorize("hasRole('admin')")
  void changeRole(@PathVariable String userId, @RequestBody RoleUpdateRequest request, @RequestParam(defaultValue = "true") boolean promote) {
    adminService.updateRole(userId, request, promote);
  }

  @PatchMapping("/users/{userId}/suspend")
  @PreAuthorize("hasRole('admin')")
  void suspend(@PathVariable String userId, @RequestBody SuspendUserRequest request) {
    adminService.suspendUser(userId, request);
  }

  @PostMapping("/payments/{paymentId}/refund")
  @PreAuthorize("hasRole('admin')")
  void refund(@PathVariable Long paymentId, @RequestBody RefundRequest request) {
    billingService.refund(paymentId, request.reason());
  }

  @PatchMapping("/studios/{studioId}/approve")
  @PreAuthorize("hasRole('admin')")
  void approveStudio(@PathVariable Long studioId, @RequestBody ApproveStudioRequest request) {
    adminService.approveStudio(studioId, request);
  }

  @PatchMapping("/studios/{studioId}/featured")
  @PreAuthorize("hasRole('admin')")
  void featureStudio(@PathVariable Long studioId, @RequestBody FeaturedStudioRequest request) {
    adminService.featureStudio(studioId, request);
  }
}
