import { apiClient } from "../../api/axios";
import type { UserSummary } from "../../types/domain";

export interface LoginRequest {
  email: string;
  password: string;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
  expiresIn: number;
  user: UserSummary;
}

/** POST /auth/login — wired into LoginPage in Sprint 1 (backend auth). */
export async function login(body: LoginRequest): Promise<TokenResponse> {
  const { data } = await apiClient.post<TokenResponse>("/auth/login", body);
  return data;
}
