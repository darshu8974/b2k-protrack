import axios, { AxiosError, type InternalAxiosRequestConfig } from "axios";

import { env } from "../config/env";
import { toAppError } from "./problem";
import { clearRefreshToken, getRefreshToken, setRefreshToken } from "./tokenStorage";

/**
 * Shared axios instance. The access token lives in memory and is attached per request. On a 401,
 * the response interceptor transparently refreshes the access token (single-flight) and retries
 * the original request once; if refresh fails, it clears the session and notifies the app.
 */
export const apiClient = axios.create({
  baseURL: env.apiBase,
  headers: { "Content-Type": "application/json" },
});

// Bare client for the refresh call, so refreshing never re-triggers the interceptor.
const refreshClient = axios.create({
  baseURL: env.apiBase,
  headers: { "Content-Type": "application/json" },
});

let accessToken: string | null = null;

export function setAccessToken(token: string | null): void {
  accessToken = token;
}

export function getAccessToken(): string | null {
  return accessToken;
}

let authFailureHandler: (() => void) | null = null;

/** Registered by the auth context so the interceptor can force a logout when refresh fails. */
export function registerAuthFailureHandler(handler: () => void): void {
  authFailureHandler = handler;
}

apiClient.interceptors.request.use((config) => {
  if (accessToken) {
    config.headers.set("Authorization", `Bearer ${accessToken}`);
  }
  return config;
});

let refreshPromise: Promise<string> | null = null;

/** Refresh the access token, deduplicating concurrent calls into one in-flight request. */
async function refreshAccessToken(): Promise<string> {
  const storedRefreshToken = getRefreshToken();
  if (!storedRefreshToken) {
    throw new Error("No refresh token");
  }
  if (!refreshPromise) {
    refreshPromise = refreshClient
      .post<{ accessToken: string; refreshToken: string }>("/auth/refresh", {
        refreshToken: storedRefreshToken,
      })
      .then((response) => {
        setAccessToken(response.data.accessToken);
        setRefreshToken(response.data.refreshToken);
        return response.data.accessToken;
      })
      .finally(() => {
        refreshPromise = null;
      });
  }
  return refreshPromise;
}

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const original = error.config as
      | (InternalAxiosRequestConfig & { _retry?: boolean })
      | undefined;
    const url = original?.url ?? "";
    const isAuthEndpoint = url.includes("/auth/");

    if (
      error.response?.status === 401 &&
      original &&
      !original._retry &&
      !isAuthEndpoint &&
      getRefreshToken()
    ) {
      original._retry = true;
      try {
        const newToken = await refreshAccessToken();
        original.headers.set("Authorization", `Bearer ${newToken}`);
        return apiClient(original);
      } catch {
        setAccessToken(null);
        clearRefreshToken();
        authFailureHandler?.();
      }
    }
    return Promise.reject(toAppError(error));
  },
);
