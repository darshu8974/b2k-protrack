import { useAuth } from "../features/auth/useAuth";
import type { Role } from "../types/domain";

/** True if the current user holds any of the given roles (UX gating; server is the source of truth). */
export function useHasRole(...roles: Role[]): boolean {
  const { user } = useAuth();
  if (!user) {
    return false;
  }
  return user.roles.some((role) => roles.includes(role));
}
