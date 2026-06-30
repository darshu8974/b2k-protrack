import { Navigate, Outlet, useLocation } from "react-router-dom";

import { useAuth } from "../../features/auth/useAuth";
import { paths } from "./paths";

/** Gate for authenticated routes; redirects to login, preserving the intended path. */
export function ProtectedRoute() {
  const { isAuthenticated } = useAuth();
  const location = useLocation();

  if (!isAuthenticated) {
    return <Navigate to={paths.login} replace state={{ from: location.pathname }} />;
  }
  return <Outlet />;
}
