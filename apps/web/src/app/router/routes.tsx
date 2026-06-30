import { createBrowserRouter, Navigate } from "react-router-dom";

import { AppShell } from "../../components/layout/AppShell";
import { RouteErrorBoundary } from "../../components/feedback/RouteErrorBoundary";
import { LoginPage } from "../../features/auth/LoginPage";
import { HealthPage } from "../../features/health/HealthPage";
import { NotFoundPage } from "../../features/health/NotFoundPage";
import { ProtectedRoute } from "./ProtectedRoute";

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
          { index: true, element: <Navigate to="/health" replace /> },
          { path: "health", element: <HealthPage /> },
        ],
      },
    ],
  },
  { path: "*", element: <NotFoundPage /> },
]);
