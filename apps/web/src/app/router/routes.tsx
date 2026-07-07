import { createBrowserRouter, Navigate } from "react-router-dom";

import { AppShell } from "../../components/layout/AppShell";
import { RouteErrorBoundary } from "../../components/feedback/RouteErrorBoundary";
import { AdminUsersPage } from "../../features/admin/AdminUsersPage";
import { AuditLogPage } from "../../features/admin/AuditLogPage";
import { LoginPage } from "../../features/auth/LoginPage";
import { DashboardPage } from "../../features/dashboard/DashboardPage";
import { HealthPage } from "../../features/health/HealthPage";
import { NotFoundPage } from "../../features/health/NotFoundPage";
import { CreateProjectPage } from "../../features/projects/pages/CreateProjectPage";
import { ProjectDetailsPage } from "../../features/projects/pages/ProjectDetailsPage";
import { ProjectsListPage } from "../../features/projects/pages/ProjectsListPage";
import { ReportsPage } from "../../features/reports/ReportsPage";
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
          { path: "projects", element: <ProjectsListPage /> },
          {
            element: <RoleRoute allow={["PM", "ADMIN"]} />,
            children: [{ path: "projects/new", element: <CreateProjectPage /> }],
          },
          { path: "projects/:id", element: <ProjectDetailsPage /> },
          {
            element: <RoleRoute allow={["ADMIN", "PM", "QA"]} />,
            children: [{ path: "reports", element: <ReportsPage /> }],
          },
          {
            element: <RoleRoute allow={["ADMIN"]} />,
            children: [
              { path: "admin/users", element: <AdminUsersPage /> },
              { path: "admin/audit", element: <AuditLogPage /> },
            ],
          },
        ],
      },
    ],
  },
  { path: "*", element: <NotFoundPage /> },
]);
