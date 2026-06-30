const apiUrl = import.meta.env.VITE_API_URL ?? "http://localhost:8080";

/** Centralized, typed access to build-time environment configuration. */
export const env = {
  apiUrl,
  apiBase: `${apiUrl}/api/v1`,
} as const;
