/**
 * Refresh-token persistence. Per the approved architecture, the access token is held in memory
 * only; the refresh token persists here so the session survives reloads.
 *
 * Phase-1 storage = localStorage (the documented fallback). The hardened option — an httpOnly,
 * Secure cookie set by the backend — is a future enhancement that requires backend changes.
 */
const REFRESH_TOKEN_KEY = "protrack.refreshToken";

export function getRefreshToken(): string | null {
  try {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  } catch {
    return null;
  }
}

export function setRefreshToken(token: string): void {
  try {
    localStorage.setItem(REFRESH_TOKEN_KEY, token);
  } catch {
    /* storage unavailable — session simply won't persist across reloads */
  }
}

export function clearRefreshToken(): void {
  try {
    localStorage.removeItem(REFRESH_TOKEN_KEY);
  } catch {
    /* ignore */
  }
}
