import react from "@vitejs/plugin-react";
import { defineConfig } from "vite";

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
  },
  build: {
    rollupOptions: {
      output: {
        // Split large, rarely-changing vendor libraries into their own long-cached chunks so the
        // app entry stays small and dependency updates don't invalidate the whole bundle.
        manualChunks: {
          "vendor-react": ["react", "react-dom", "react-router-dom"],
          "vendor-mui": ["@mui/material", "@emotion/react", "@emotion/styled"],
          "vendor-data": ["@tanstack/react-query", "axios"],
        },
      },
    },
  },
});
