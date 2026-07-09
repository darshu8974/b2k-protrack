import { Box } from "@mui/material";
import { Suspense } from "react";
import { Outlet } from "react-router-dom";

import { LoadingState } from "../feedback/LoadingState";
import { Sidebar } from "./Sidebar";
import { TopBar } from "./TopBar";

/** The authenticated app shell: fixed sidebar + top bar + routed content. */
export function AppShell() {
  return (
    <Box sx={{ display: "flex", minHeight: "100vh", bgcolor: "background.default" }}>
      {/* Accessibility: skip straight to the page content, bypassing the sidebar/top bar. */}
      <a href="#main-content" className="skip-link">
        Skip to content
      </a>
      <Sidebar />
      <Box sx={{ flex: 1, display: "flex", flexDirection: "column", minWidth: 0 }}>
        <TopBar />
        <Box component="main" id="main-content" sx={{ p: 3, flex: 1 }}>
          {/* Lazy pages suspend while their chunk loads; show a spinner rather than a blank. */}
          <Suspense fallback={<LoadingState />}>
            <Outlet />
          </Suspense>
        </Box>
      </Box>
    </Box>
  );
}
