package com.pulsefit.web;

import com.pulsefit.service.StripeWebhookService;
import com.pulsefit.web.dto.ApiDtos.WebhookResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stripe")
public class StripeWebhookController {
  private final StripeWebhookService webhookService;

  @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
  WebhookResponse webhook(@RequestBody String payload, @RequestHeader("Stripe-Signature") String signature) {
    webhookService.handleWebhook(payload, signature);
    return new WebhookResponse("ok");
  }
}
