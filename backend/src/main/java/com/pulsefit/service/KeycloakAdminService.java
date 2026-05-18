package com.pulsefit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.pulsefit.config.AppProperties;
import com.pulsefit.exception.NotFoundException;
import com.pulsefit.web.dto.ApiDtos.AdminUserResponse;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
@RequiredArgsConstructor
public class KeycloakAdminService {
  private final RestClient restClient = RestClient.create();
  private final AppProperties properties;

  public List<AdminUserResponse> listUsers(String search, int page, int size) {
    JsonNode payload = adminClient().get()
      .uri(uriBuilder -> uriBuilder.path("/users")
            .queryParamIfPresent("search", java.util.Optional.ofNullable(search).filter(value -> !value.isBlank()))
            .queryParam("first", page * size)
            .queryParam("max", size)
            .build())
        .retrieve()
        .body(JsonNode.class);

    List<AdminUserResponse> results = new ArrayList<>();
    if (payload != null && payload.isArray()) {
      for (JsonNode user : payload) {
        results.add(new AdminUserResponse(
            user.path("id").asText(),
            user.path("username").asText(),
            user.path("email").asText(),
            user.path("enabled").asBoolean(true),
            userRoles(user.path("id").asText())));
      }
    }
    return results;
  }

  public long countUsers() {
    JsonNode payload = adminClient().get().uri(uriBuilder -> uriBuilder.path("/users/count").build()).retrieve().body(JsonNode.class);
    return payload == null ? 0 : payload.asLong();
  }

  public void updateUser(String userId, String displayName, String email) {
    adminClient().put().uri("/users/{userId}", userId).contentType(MediaType.APPLICATION_JSON).body(java.util.Map.of(
        "firstName", displayName,
        "username", displayName,
        "email", email)).retrieve().toBodilessEntity();
  }

  public void setRole(String userId, String role, boolean add) {
    JsonNode rolePayload = adminClient().get().uri("/roles/{role}", role).retrieve().body(JsonNode.class);
    if (rolePayload == null) {
      throw new NotFoundException("Role not found: " + role);
    }
    if (add) {
      adminClient().post().uri("/users/{userId}/role-mappings/realm", userId).contentType(MediaType.APPLICATION_JSON).body(List.of(rolePayload)).retrieve().toBodilessEntity();
    } else {
      adminClient().method(HttpMethod.DELETE).uri("/users/{userId}/role-mappings/realm", userId).contentType(MediaType.APPLICATION_JSON).body(List.of(rolePayload)).retrieve().toBodilessEntity();
    }
  }

  public void setSuspended(String userId, boolean suspended) {
    adminClient().put().uri("/users/{userId}", userId).contentType(MediaType.APPLICATION_JSON).body(java.util.Map.of("enabled", !suspended)).retrieve().toBodilessEntity();
  }

  private List<String> userRoles(String userId) {
    JsonNode payload = adminClient().get().uri("/users/{userId}/role-mappings/realm", userId).retrieve().body(JsonNode.class);
    if (payload == null || !payload.isArray()) {
      return List.of();
    }
    List<String> roles = new ArrayList<>();
    for (JsonNode role : payload) {
      roles.add(role.path("name").asText());
    }
    return roles;
  }

  public String getAccessToken() {
    MultiValueMap<String, String> credentials = new LinkedMultiValueMap<>();
    credentials.add("client_id", properties.keycloak().backendClientId());
    credentials.add("client_secret", properties.keycloak().backendClientSecret());
    credentials.add("grant_type", "client_credentials");
    try {
      JsonNode token = restClient.post()
          .uri(keycloakTokenUrl())
          .contentType(MediaType.APPLICATION_FORM_URLENCODED)
          .body(credentials)
          .retrieve()
          .body(JsonNode.class);
      if (token != null && token.hasNonNull("access_token")) {
        return token.get("access_token").asText();
      }
    } catch (Exception ignored) {
      // fallback below
    }

    MultiValueMap<String, String> fallback = new LinkedMultiValueMap<>();
    fallback.add("client_id", "admin-cli");
    fallback.add("grant_type", "password");
    fallback.add("username", properties.keycloak().adminUsername());
    fallback.add("password", properties.keycloak().adminPassword());
    JsonNode token = restClient.post()
        .uri(masterTokenUrl())
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(fallback)
        .retrieve()
        .body(JsonNode.class);
    if (token == null || !token.hasNonNull("access_token")) {
      throw new IllegalStateException("Unable to obtain Keycloak admin token");
    }
    return token.get("access_token").asText();
  }

  private RestClient adminClient() {
    return restClient.mutate()
        .defaultHeader("Authorization", "Bearer " + getAccessToken())
        .baseUrl("http://keycloak:8080/auth/admin/realms/" + properties.keycloak().realm())
        .build();
  }

  private String keycloakTokenUrl() {
    return "http://keycloak:8080/auth/realms/" + properties.keycloak().realm() + "/protocol/openid-connect/token";
  }

  private String masterTokenUrl() {
    return "http://keycloak:8080/auth/realms/master/protocol/openid-connect/token";
  }
}
