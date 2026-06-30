import { apiClient } from "../../api/axios";
import type { UserSummary } from "../../types/domain";

export interface LoginRequest {
  email: string;
  password: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  expiresIn: number;
  user: UserSummary;
}

/** POST /auth/login — exchange credentials for tokens + user. */
export async function login(body: LoginRequest): Promise<TokenResponse> {
  const { data } = await apiClient.post<TokenResponse>("/auth/login", body);
  return data;
}

/** POST /auth/refresh — rotate the refresh token and obtain a new access token. */
export async function refresh(refreshToken: string): Promise<TokenResponse> {
  const { data } = await apiClient.post<TokenResponse>("/auth/refresh", { refreshToken });
  return data;
}

/** POST /auth/logout — revoke the refresh token (best effort). */
export async function logout(refreshToken: string): Promise<void> {
  await apiClient.post("/auth/logout", { refreshToken });
}

/** GET /auth/me — the currently authenticated user. */
export async function me(): Promise<UserSummary> {
  const { data } = await apiClient.get<UserSummary>("/auth/me");
  return data;
}
