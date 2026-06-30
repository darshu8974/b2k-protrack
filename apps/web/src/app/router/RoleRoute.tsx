import { Navigate, Outlet } from "react-router-dom";

import { useAuth } from "../../features/auth/useAuth";
import type { Role } from "../../types/domain";
import { paths } from "./paths";

/**
 * Gate for role-restricted routes. Used to wrap admin/QA/designer-only sections as they land.
 * A dedicated 403 page replaces the redirect in a later sprint.
 */
export function RoleRoute({ allow }: { allow: Role[] }) {
  const { user } = useAuth();

  if (!user) {
    return <Navigate to={paths.login} replace />;
  }
  const permitted = user.roles.some((role) => allow.includes(role));
  return permitted ? <Outlet /> : <Navigate to={paths.health} replace />;
}
