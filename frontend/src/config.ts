export type AppConfig = {
  publicBaseUrl: string;
  keycloakUrl: string;
  keycloakRealm: string;
  keycloakClientId: string;
  apiBaseUrl: string;
  stripePublishableKey?: string;
};

export function getConfig(): AppConfig {
  const config = window.__APP_CONFIG__;
  if (!config) {
    throw new Error('Runtime config is missing');
  }
  return config;
}
