package com.pulsefit.web;

import com.pulsefit.service.BillingService;
import com.pulsefit.service.ProfileService;
import com.pulsefit.web.dto.ApiDtos.BillingSummaryResponse;
import com.pulsefit.web.dto.ApiDtos.CreateClassPackCheckoutResponse;
import com.pulsefit.web.dto.ApiDtos.CustomerPortalResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/billing")
public class BillingController {
  private final BillingService billingService;
  private final ProfileService profileService;

  @GetMapping("/summary")
  @PreAuthorize("hasAnyRole('user','admin')")
  BillingSummaryResponse summary(@AuthenticationPrincipal Jwt jwt) {
    return billingService.summary(profileService.ensureProfile(jwt));
  }

  @PostMapping("/class-pack/checkout")
  @PreAuthorize("hasAnyRole('user','admin')")
  CreateClassPackCheckoutResponse checkout(@AuthenticationPrincipal Jwt jwt) {
    return billingService.createClassPackCheckout(profileService.ensureProfile(jwt));
  }

  @PostMapping("/customer-portal")
  @PreAuthorize("hasAnyRole('user','admin')")
  CustomerPortalResponse customerPortal(@AuthenticationPrincipal Jwt jwt) {
    return new CustomerPortalResponse(billingService.createCustomerPortal(profileService.ensureProfile(jwt)));
  }

  @PostMapping("/subscription/{subscriptionId}/cancel")
  @PreAuthorize("hasAnyRole('user','admin')")
  void cancelSubscription(@PathVariable Long subscriptionId) {
    billingService.cancelSubscription(subscriptionId);
  }
}
