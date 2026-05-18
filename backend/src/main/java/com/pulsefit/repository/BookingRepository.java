package com.pulsefit.repository;

import com.pulsefit.domain.BookingEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<BookingEntity, Long> {
  List<BookingEntity> findByProfileKeycloakIdOrderByCreatedAtDesc(String keycloakId);
  Optional<BookingEntity> findByStripeCheckoutSessionId(String sessionId);
}
