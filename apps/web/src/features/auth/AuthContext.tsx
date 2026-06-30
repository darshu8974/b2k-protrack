import { createContext, useCallback, useMemo, useState, type ReactNode } from "react";

import { setAccessToken } from "../../api/axios";
import type { Role, UserSummary } from "../../types/domain";

export interface AuthState {
  user: UserSummary | null;
  isAuthenticated: boolean;
  login: (user: UserSummary, token: string) => void;
  logout: () => void;
  /** Demo-only client role switch ("Viewing as"); does not affect server authorization. */
  setRole: (role: Role) => void;
}

export const AuthContext = createContext<AuthState | undefined>(undefined);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserSummary | null>(null);

  const login = useCallback((nextUser: UserSummary, token: string) => {
    setAccessToken(token);
    setUser(nextUser);
  }, []);

  const logout = useCallback(() => {
    setAccessToken(null);
    setUser(null);
  }, []);

  const setRole = useCallback((role: Role) => {
    setUser((current) => (current ? { ...current, roles: [role] } : current));
  }, []);

  const value = useMemo<AuthState>(
    () => ({ user, isAuthenticated: user !== null, login, logout, setRole }),
    [user, login, logout, setRole],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
