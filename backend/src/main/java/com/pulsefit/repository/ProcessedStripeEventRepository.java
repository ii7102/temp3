package com.pulsefit.repository;

import com.pulsefit.domain.ProcessedStripeEventEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedStripeEventRepository extends JpaRepository<ProcessedStripeEventEntity, Long> {
  Optional<ProcessedStripeEventEntity> findByEventId(String eventId);
}
