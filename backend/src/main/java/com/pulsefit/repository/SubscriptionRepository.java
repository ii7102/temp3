package com.pulsefit.repository;

import com.pulsefit.domain.SubscriptionEntity;
import com.pulsefit.domain.SubscriptionStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {
  Optional<SubscriptionEntity> findByStripeSubscriptionId(String stripeSubscriptionId);
  Optional<SubscriptionEntity> findFirstByProfileKeycloakIdAndStatusOrderByCreatedAtDesc(String keycloakId, SubscriptionStatus status);
}
