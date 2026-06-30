import { useContext } from "react";

import { AuthContext, type AuthState } from "./AuthContext";

/** Access the auth context; throws if used outside <AuthProvider>. */
export function useAuth(): AuthState {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider");
  }
  return context;
}
