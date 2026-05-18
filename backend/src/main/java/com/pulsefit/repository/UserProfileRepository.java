package com.pulsefit.repository;

import com.pulsefit.domain.UserProfileEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfileEntity, Long> {
  Optional<UserProfileEntity> findByKeycloakId(String keycloakId);
  Optional<UserProfileEntity> findByEmail(String email);
}
