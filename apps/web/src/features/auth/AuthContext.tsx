import {
  createContext,
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ReactNode,
} from "react";

import { registerAuthFailureHandler, setAccessToken } from "../../api/axios";
import { clearRefreshToken, getRefreshToken, setRefreshToken } from "../../api/tokenStorage";
import type { Role, UserSummary } from "../../types/domain";
import * as authApi from "./api";

export type AuthStatus = "loading" | "authenticated" | "unauthenticated";

export interface AuthState {
  user: UserSummary | null;
  status: AuthStatus;
  isAuthenticated: boolean;
  login: (email: string, password: string) => Promise<void>;
  logout: () => Promise<void>;
  /** Demo-only client role switch ("Viewing as"); does not affect server authorization. */
  setRole: (role: Role) => void;
}

export const AuthContext = createContext<AuthState | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserSummary | null>(null);
  const [status, setStatus] = useState<AuthStatus>("loading");

  const clearSession = useCallback(() => {
    setAccessToken(null);
    clearRefreshToken();
    setUser(null);
    setStatus("unauthenticated");
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    const response = await authApi.login({ email, password });
    setAccessToken(response.accessToken);
    setRefreshToken(response.refreshToken);
    setUser(response.user);
    setStatus("authenticated");
  }, []);

  const logout = useCallback(async () => {
    const refreshToken = getRefreshToken();
    if (refreshToken) {
      try {
        await authApi.logout(refreshToken);
      } catch {
        /* best effort — clear locally regardless */
      }
    }
    clearSession();
  }, [clearSession]);

  const setRole = useCallback((role: Role) => {
    setUser((current) => (current ? { ...current, roles: [role] } : current));
  }, []);

  // Let the axios interceptor force a logout when a refresh ultimately fails.
  useEffect(() => {
    registerAuthFailureHandler(clearSession);
  }, [clearSession]);

  // Bootstrap: restore the session from a stored refresh token (rotates it + loads /me).
  const bootstrapped = useRef(false);
  useEffect(() => {
    if (bootstrapped.current) {
      return;
    }
    bootstrapped.current = true;

    const refreshToken = getRefreshToken();
    if (!refreshToken) {
      setStatus("unauthenticated");
      return;
    }

    authApi
      .refresh(refreshToken)
      .then(async (response) => {
        setAccessToken(response.accessToken);
        setRefreshToken(response.refreshToken);
        const currentUser = await authApi.me();
        setUser(currentUser);
        setStatus("authenticated");
      })
      .catch(() => {
        clearSession();
      });
  }, [clearSession]);

  const value = useMemo<AuthState>(
    () => ({
      user,
      status,
      isAuthenticated: status === "authenticated" && user !== null,
      login,
      logout,
      setRole,
    }),
    [user, status, login, logout, setRole],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
