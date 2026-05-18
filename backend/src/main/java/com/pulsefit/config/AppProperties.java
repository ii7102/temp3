package com.pulsefit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(
    String publicBaseUrl,
    KeycloakProperties keycloak,
    StripeProperties stripe) {

  public record KeycloakProperties(
      String realm,
      String frontendClientId,
      String backendClientId,
      String backendClientSecret,
      String adminUsername,
      String adminPassword) {}

  public record StripeProperties(
      String secretKey,
      String publishableKey,
      String webhookSecret,
      String sessionPriceId,
      String classPackPriceId,
      double commissionRate) {}
}
