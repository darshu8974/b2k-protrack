import axios from "axios";

import { env } from "../config/env";
import { toAppError } from "./problem";

/**
 * Shared axios instance. Components must not call this directly — use feature `api.ts`
 * modules and query/mutation hooks.
 *
 * The access token is held in memory and attached per request. Silent refresh on 401 is
 * wired in Sprint 1 alongside the auth backend.
 */
export const apiClient = axios.create({
  baseURL: env.apiBase,
  headers: { "Content-Type": "application/json" },
});

let accessToken: string | null = null;

/** Set/clear the in-memory bearer token (called by the auth context). */
export function setAccessToken(token: string | null): void {
  accessToken = token;
}

export function getAccessToken(): string | null {
  return accessToken;
}

apiClient.interceptors.request.use((config) => {
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  // TODO (Sprint 1): attempt one silent /auth/refresh on 401, then logout on failure.
  (error) => Promise.reject(toAppError(error)),
);
