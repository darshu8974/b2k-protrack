import { useEffect, useRef } from "react";

import { getAccessToken } from "../api/axios";
import { subscribeToProjectEvents } from "../api/sse";

/** Shape of the JSON payload carried by the AI progress/terminal SSE events. */
export interface SseEventData {
  jobId?: string;
  progressPct?: number;
  status?: string;
  error?: string;
}

export interface SseCallbacks {
  onProgress?: (data: SseEventData) => void;
  onCompleted?: (data: SseEventData) => void;
  onFailed?: (data: SseEventData) => void;
}

/**
 * Subscribe to a project's SSE stream while {@code enabled}. Sends the JWT via
 * {@link subscribeToProjectEvents} (fetch-event-source). Callbacks are kept in a ref so the
 * subscription isn't torn down on every render. Errors are swallowed — the caller polls as fallback.
 */
export function useSse(projectId: string, enabled: boolean, callbacks: SseCallbacks): void {
  const callbacksRef = useRef(callbacks);
  callbacksRef.current = callbacks;

  useEffect(() => {
    if (!enabled || !projectId) {
      return;
    }
    const controller = new AbortController();
    subscribeToProjectEvents(
      projectId,
      getAccessToken(),
      {
        onMessage: ({ event, data }) => {
          let parsed: SseEventData = {};
          try {
            parsed = data ? (JSON.parse(data) as SseEventData) : {};
          } catch {
            parsed = {};
          }
          if (event === "progress") {
            callbacksRef.current.onProgress?.(parsed);
          } else if (event === "completed") {
            callbacksRef.current.onCompleted?.(parsed);
          } else if (event === "failed") {
            callbacksRef.current.onFailed?.(parsed);
          }
        },
      },
      controller.signal,
    ).catch(() => {
      // Aborted or the server closed the stream; polling keeps the UI current.
    });
    return () => controller.abort();
  }, [projectId, enabled]);
}
