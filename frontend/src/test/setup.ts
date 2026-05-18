import '@testing-library/jest-dom/vitest';
import { vi } from 'vitest';

window.__APP_CONFIG__ = {
  publicBaseUrl: 'http://example.com',
  keycloakUrl: 'http://example.com/auth',
  keycloakRealm: 'pulsefit',
  keycloakClientId: 'pulsefit-frontend',
  apiBaseUrl: 'http://example.com/api',
  stripePublishableKey: 'pk_test'
};

vi.mock('keycloak-js', () => {
  return {
    default: class MockKeycloak {
      token: string | null = null;
      authenticated = false;
      tokenParsed = { preferred_username: 'guest', email: 'guest@example.com', realm_access: { roles: [] } };
      init = vi.fn(async () => false);
      login = vi.fn();
      logout = vi.fn();
      updateToken = vi.fn(async () => false);
    }
  };
});
