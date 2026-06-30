import { createBrowserRouter, Navigate } from "react-router-dom";

import { AppShell } from "../../components/layout/AppShell";
import { RouteErrorBoundary } from "../../components/feedback/RouteErrorBoundary";
import { AdminUsersPage } from "../../features/admin/AdminUsersPage";
import { LoginPage } from "../../features/auth/LoginPage";
import { DashboardPage } from "../../features/dashboard/DashboardPage";
import { HealthPage } from "../../features/health/HealthPage";
import { NotFoundPage } from "../../features/health/NotFoundPage";
import { ProtectedRoute } from "./ProtectedRoute";
import { RoleRoute } from "./RoleRoute";

/**
 * Route tree. Feature routes (dashboard, projects, workspace, qa, admin, ...) are added under
 * the AppShell as they are built; RoleRoute wraps role-restricted sections.
 */
export const router = createBrowserRouter([
  {
    path: "/login",
    element: <LoginPage />,
    errorElement: <RouteErrorBoundary />,
  },
  {
    path: "/",
    element: <ProtectedRoute />,
    errorElement: <RouteErrorBoundary />,
    children: [
      {
        element: <AppShell />,
        children: [
          { index: true, element: <Navigate to="/dashboard" replace /> },
          { path: "dashboard", element: <DashboardPage /> },
          { path: "health", element: <HealthPage /> },
          {
            element: <RoleRoute allow={["ADMIN"]} />,
            children: [{ path: "admin/users", element: <AdminUsersPage /> }],
          },
        ],
      },
    ],
  },
  { path: "*", element: <NotFoundPage /> },
]);
