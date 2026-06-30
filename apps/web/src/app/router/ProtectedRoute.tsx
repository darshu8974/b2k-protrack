import { Box, CircularProgress } from "@mui/material";
import { Navigate, Outlet, useLocation } from "react-router-dom";

import { useAuth } from "../../features/auth/useAuth";
import { paths } from "./paths";

/**
 * Gate for authenticated routes. While the session is being restored ("loading") it shows a
 * spinner rather than bouncing to login; otherwise unauthenticated users are redirected.
 */
export function ProtectedRoute() {
  const { status } = useAuth();
  const location = useLocation();

  if (status === "loading") {
    return (
      <Box sx={{ display: "flex", minHeight: "100vh", alignItems: "center", justifyContent: "center" }}>
        <CircularProgress />
      </Box>
    );
  }

  if (status !== "authenticated") {
    return <Navigate to={paths.login} replace state={{ from: location.pathname }} />;
  }

  return <Outlet />;
}
