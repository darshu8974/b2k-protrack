import { fetchEventSource } from "@microsoft/fetch-event-source";

import { env } from "../config/env";

export interface SseEvent {
  event?: string;
  data: string;
}

export interface SseHandlers {
  onMessage: (event: SseEvent) => void;
  onError?: (error: unknown) => void;
  onOpen?: () => void;
}

/**
 * Subscribe to a project's server-sent event stream for live AI-job/stage progress.
 *
 * Uses fetch-event-source (not native EventSource) so the JWT can be sent as a Bearer header.
 * Concrete event handling + query-cache updates are wired in Sprint 4.
 */
export function subscribeToProjectEvents(
  projectId: string,
  token: string | null,
  handlers: SseHandlers,
  signal: AbortSignal,
): Promise<void> {
  return fetchEventSource(`${env.apiBase}/projects/${projectId}/events`, {
    signal,
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    onopen: async () => {
      handlers.onOpen?.();
    },
    onmessage: (message) => {
      handlers.onMessage({ event: message.event, data: message.data });
    },
    onerror: (error) => {
      handlers.onError?.(error);
      throw error; // stop retrying; callers fall back to polling
    },
  });
}
