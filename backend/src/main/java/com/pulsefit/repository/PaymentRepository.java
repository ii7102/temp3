package com.pulsefit.repository;

import com.pulsefit.domain.PaymentEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
  Optional<PaymentEntity> findByStripeCheckoutSessionId(String checkoutSessionId);
  Optional<PaymentEntity> findByStripePaymentIntentId(String paymentIntentId);
  Optional<PaymentEntity> findByStripeSubscriptionId(String stripeSubscriptionId);
}
