#!/bin/sh
set -eu

cat >/usr/share/nginx/html/config.js <<EOF
window.__APP_CONFIG__ = {
  publicBaseUrl: "${APP_PUBLIC_BASE_URL}",
  keycloakUrl: "${APP_PUBLIC_BASE_URL}/auth",
  keycloakRealm: "${KEYCLOAK_REALM}",
  keycloakClientId: "${KEYCLOAK_FRONTEND_CLIENT_ID}",
  apiBaseUrl: "${APP_PUBLIC_BASE_URL}/api",
  stripePublishableKey: "${STRIPE_PUBLISHABLE_KEY}"
};
EOF

exec "$@"
