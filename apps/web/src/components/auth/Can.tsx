import type { ReactNode } from "react";

import { useAuth } from "../../features/auth/useAuth";
import type { Role } from "../../types/domain";

interface CanProps {
  /** Require any of these roles. */
  roles?: Role[];
  /** Require this permission code. */
  permission?: string;
  /** Rendered when not permitted (default: nothing). */
  fallback?: ReactNode;
  children: ReactNode;
}

/**
 * Conditionally renders children based on the current user's roles/permissions. UX gating only —
 * the backend (@PreAuthorize) remains the source of truth. When both props are given, both must
 * pass; when neither is given, it acts as an authenticated gate.
 */
export function Can({ roles, permission, fallback = null, children }: CanProps) {
  const { user } = useAuth();

  if (!user) {
    return <>{fallback}</>;
  }

  const roleOk = roles ? user.roles.some((role) => roles.includes(role)) : true;
  const permissionOk = permission ? user.permissions.includes(permission) : true;
  const allowed = roleOk && permissionOk;

  return <>{allowed ? children : fallback}</>;
}
