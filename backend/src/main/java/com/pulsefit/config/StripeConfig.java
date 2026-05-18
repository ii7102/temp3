package com.pulsefit.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {
  private final AppProperties properties;

  public StripeConfig(AppProperties properties) {
    this.properties = properties;
  }

  @PostConstruct
  void init() {
    Stripe.apiKey = properties.stripe().secretKey();
  }
}
