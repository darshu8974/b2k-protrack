import { Navigate, Outlet } from "react-router-dom";

import { ForbiddenPage } from "../../components/feedback/ForbiddenPage";
import { useAuth } from "../../features/auth/useAuth";
import type { Role } from "../../types/domain";
import { paths } from "./paths";

/**
 * Gate for role-restricted routes. Unauthenticated users go to login; authenticated users without
 * an allowed role get a 403 page (the URL is preserved).
 */
export function RoleRoute({ allow }: { allow: Role[] }) {
  const { user } = useAuth();

  if (!user) {
    return <Navigate to={paths.login} replace />;
  }
  const permitted = user.roles.some((role) => allow.includes(role));
  return permitted ? <Outlet /> : <ForbiddenPage />;
}
