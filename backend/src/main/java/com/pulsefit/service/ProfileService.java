package com.pulsefit.service;

import com.pulsefit.domain.UserProfileEntity;
import com.pulsefit.repository.UserProfileRepository;
import com.pulsefit.web.dto.ApiDtos.ProfileResponse;
import com.pulsefit.web.dto.ApiDtos.UpdateProfileRequest;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {
  private final UserProfileRepository profiles;
  private final KeycloakAdminService keycloakAdminService;

  @Transactional
  public UserProfileEntity ensureProfile(Jwt jwt) {
    return profiles.findByKeycloakId(jwt.getSubject()).orElseGet(() -> {
      UserProfileEntity profile = new UserProfileEntity();
      profile.setKeycloakId(jwt.getSubject());
      profile.setEmail(Optional.ofNullable(jwt.getClaimAsString("email")).orElse(jwt.getSubject() + "@example.com"));
      profile.setFullName(Optional.ofNullable(jwt.getClaimAsString("name")).orElse(Optional.ofNullable(jwt.getClaimAsString("preferred_username")).orElse("PulseFit member")));
      Object realmAccess = jwt.getClaim("realm_access");
      boolean admin = realmAccess instanceof java.util.Map<?, ?> map && map.get("roles") instanceof java.util.Collection<?> roles && roles.stream().anyMatch(role -> "admin".equals(String.valueOf(role)));
      profile.setRoleSnapshot(admin ? "admin" : "user");
      profile.setMarketingOptIn(false);
      profile.setSuspended(false);
      return profiles.save(profile);
    });
  }

  @Transactional(readOnly = true)
  public ProfileResponse getProfile(Jwt jwt) {
    UserProfileEntity profile = ensureProfile(jwt);
    return new ProfileResponse(profile.getFullName(), profile.getEmail(), profile.isMarketingOptIn());
  }

  @Transactional
  public ProfileResponse updateProfile(Jwt jwt, UpdateProfileRequest request) {
    UserProfileEntity profile = ensureProfile(jwt);
    profile.setFullName(request.displayName());
    profile.setEmail(request.email());
    profile.setMarketingOptIn(request.marketingOptIn());
    keycloakAdminService.updateUser(profile.getKeycloakId(), request.displayName(), request.email());
    profiles.save(profile);
    return new ProfileResponse(profile.getFullName(), profile.getEmail(), profile.isMarketingOptIn());
  }
}
