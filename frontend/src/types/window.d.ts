export {};

declare global {
  interface Window {
    __APP_CONFIG__?: {
      publicBaseUrl: string;
      keycloakUrl: string;
      keycloakRealm: string;
      keycloakClientId: string;
      apiBaseUrl: string;
      stripePublishableKey?: string;
    };
  }
}
