import { useAuth } from "../features/auth/useAuth";

/** UI permission check (UX gating only; the server remains the source of truth). */
export function useCan(permission: string): boolean {
  const { user } = useAuth();
  return user?.permissions.includes(permission) ?? false;
}
