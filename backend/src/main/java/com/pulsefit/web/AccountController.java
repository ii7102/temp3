package com.pulsefit.web;

import com.pulsefit.service.ProfileService;
import com.pulsefit.web.dto.ApiDtos.ProfileResponse;
import com.pulsefit.web.dto.ApiDtos.UpdateProfileRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/account")
public class AccountController {
  private final ProfileService profileService;

  @GetMapping("/profile")
  @PreAuthorize("hasAnyRole('user','admin')")
  ProfileResponse profile(@AuthenticationPrincipal Jwt jwt) {
    return profileService.getProfile(jwt);
  }

  @PatchMapping("/profile")
  @PreAuthorize("hasAnyRole('user','admin')")
  ProfileResponse update(@AuthenticationPrincipal Jwt jwt, @RequestBody UpdateProfileRequest request) {
    return profileService.updateProfile(jwt, request);
  }
}
