import { lazy, Suspense } from "react";
import { createBrowserRouter, Navigate } from "react-router-dom";

import { AppShell } from "../../components/layout/AppShell";
import { LoadingState } from "../../components/feedback/LoadingState";
import { RouteErrorBoundary } from "../../components/feedback/RouteErrorBoundary";
import { ProtectedRoute } from "./ProtectedRoute";
import { RoleRoute } from "./RoleRoute";

// Route-level code-splitting: each page is a lazy chunk (Frontend Architecture §8.4/§18), so the
// initial bundle stays small and features load on demand. Layout/guards stay eager.
const LoginPage = lazy(() =>
  import("../../features/auth/LoginPage").then((m) => ({ default: m.LoginPage })),
);
const DashboardPage = lazy(() =>
  import("../../features/dashboard/DashboardPage").then((m) => ({ default: m.DashboardPage })),
);
const HealthPage = lazy(() =>
  import("../../features/health/HealthPage").then((m) => ({ default: m.HealthPage })),
);
const NotFoundPage = lazy(() =>
  import("../../features/health/NotFoundPage").then((m) => ({ default: m.NotFoundPage })),
);
const ProjectsListPage = lazy(() =>
  import("../../features/projects/pages/ProjectsListPage").then((m) => ({
    default: m.ProjectsListPage,
  })),
);
const CreateProjectPage = lazy(() =>
  import("../../features/projects/pages/CreateProjectPage").then((m) => ({
    default: m.CreateProjectPage,
  })),
);
const ProjectDetailsPage = lazy(() =>
  import("../../features/projects/pages/ProjectDetailsPage").then((m) => ({
    default: m.ProjectDetailsPage,
  })),
);
const ReportsPage = lazy(() =>
  import("../../features/reports/ReportsPage").then((m) => ({ default: m.ReportsPage })),
);
const AdminUsersPage = lazy(() =>
  import("../../features/admin/AdminUsersPage").then((m) => ({ default: m.AdminUsersPage })),
);
const AuditLogPage = lazy(() =>
  import("../../features/admin/AuditLogPage").then((m) => ({ default: m.AuditLogPage })),
);

/** Wraps lazy route elements in a Suspense fallback so chunk loads show a spinner, not a blank. */
function Lazy({ children }: { children: React.ReactNode }) {
  return <Suspense fallback={<LoadingState />}>{children}</Suspense>;
}

/**
 * Route tree. Pages are lazy-loaded for code-splitting; RoleRoute wraps role-restricted sections.
 * RouteErrorBoundary catches render/loader errors per branch.
 */
export const router = createBrowserRouter([
  {
    path: "/login",
    element: (
      <Lazy>
        <LoginPage />
      </Lazy>
    ),
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
  {
    path: "*",
    element: (
      <Lazy>
        <NotFoundPage />
      </Lazy>
    ),
  },
]);
