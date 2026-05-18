import Keycloak from 'keycloak-js';
import React, { createContext, useContext, useEffect, useMemo, useState } from 'react';
import { getConfig } from '../config';

type AuthContextValue = {
  ready: boolean;
  authenticated: boolean;
  token: string | null;
  userName: string;
  email: string;
  roles: string[];
  login: () => void;
  register: () => void;
  logout: () => void;
  hasRole: (role: string) => boolean;
};

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function rolesFromToken(tokenParsed: unknown): string[] {
  const realmAccess = (tokenParsed as { realm_access?: { roles?: string[] } } | undefined)?.realm_access;
  return realmAccess?.roles ?? [];
}

export function AuthProvider({ children }: { children: React.ReactNode }) {
  const config = getConfig();
  const keycloak = useMemo(
    () =>
      new Keycloak({
        url: config.keycloakUrl,
        realm: config.keycloakRealm,
        clientId: config.keycloakClientId
      }),
    [config.keycloakClientId, config.keycloakRealm, config.keycloakUrl]
  );

  const [ready, setReady] = useState(false);
  const [authenticated, setAuthenticated] = useState(false);
  const [token, setToken] = useState<string | null>(null);
  const [roles, setRoles] = useState<string[]>([]);
  const [userName, setUserName] = useState('Guest');
  const [email, setEmail] = useState('');

  useEffect(() => {
    let mounted = true;

    keycloak
      .init({ onLoad: 'check-sso', pkceMethod: 'S256' })
      .then((isAuthenticated) => {
        if (!mounted) {
          return;
        }
        setAuthenticated(isAuthenticated);
        setToken(isAuthenticated ? keycloak.token ?? null : null);
        setRoles(isAuthenticated ? rolesFromToken(keycloak.tokenParsed) : []);
        setUserName((keycloak.tokenParsed as { preferred_username?: string })?.preferred_username ?? 'Guest');
        setEmail((keycloak.tokenParsed as { email?: string })?.email ?? '');
        setReady(true);
      })
      .catch(() => {
        if (mounted) {
          setReady(true);
        }
      });

    const refresh = window.setInterval(() => {
      if (keycloak.authenticated) {
        void keycloak.updateToken(30).then((refreshed) => {
          if (refreshed) {
            setToken(keycloak.token ?? null);
          }
        });
      }
    }, 30000);

    return () => {
      mounted = false;
      window.clearInterval(refresh);
    };
  }, [keycloak]);

  const value = useMemo<AuthContextValue>(
    () => ({
      ready,
      authenticated,
      token,
      userName,
      email,
      roles,
      login: () => keycloak.login({ redirectUri: window.location.href }),
      register: () => keycloak.login({ action: 'register', redirectUri: window.location.href }),
      logout: () => keycloak.logout({ redirectUri: getConfig().publicBaseUrl }),
      hasRole: (role: string) => roles.includes(role)
    }),
    [authenticated, email, keycloak, ready, roles, token, userName]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error('useAuth must be used inside AuthProvider');
  }
  return context;
}

export { AuthContext };
