import { Alert, Snackbar } from "@mui/material";
import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from "react";

type Severity = "success" | "error" | "info";

interface Toast {
  key: number;
  message: string;
  severity: Severity;
}

interface ToastContextValue {
  success: (message: string) => void;
  error: (message: string) => void;
  info: (message: string) => void;
}

const ToastContext = createContext<ToastContextValue | null>(null);

/**
 * App-wide success/error/info notifications. Renders as a single transient Snackbar in the
 * bottom-left corner; queued toasts show one at a time so messages never overlap. Purely additive
 * — no existing layout changes shape when a toast appears or is dismissed.
 */
export function ToastProvider({ children }: { children: ReactNode }) {
  const [queue, setQueue] = useState<Toast[]>([]);
  const [current, setCurrent] = useState<Toast | null>(null);

  const push = useCallback((message: string, severity: Severity) => {
    setQueue((q) => [...q, { key: Date.now() + Math.random(), message, severity }]);
  }, []);

  // Pop the next queued toast once the current one has finished closing.
  if (!current && queue.length > 0) {
    setCurrent(queue[0]);
    setQueue((q) => q.slice(1));
  }

  const handleClose = (_: unknown, reason?: string) => {
    if (reason === "clickaway") return;
    setCurrent(null);
  };

  const value = useMemo<ToastContextValue>(
    () => ({
      success: (message: string) => push(message, "success"),
      error: (message: string) => push(message, "error"),
      info: (message: string) => push(message, "info"),
    }),
    [push],
  );

  return (
    <ToastContext.Provider value={value}>
      {children}
      <Snackbar
        key={current?.key}
        open={!!current}
        autoHideDuration={4000}
        onClose={handleClose}
        anchorOrigin={{ vertical: "bottom", horizontal: "left" }}
        TransitionProps={{ onExited: () => setCurrent(null) }}
      >
        {current ? (
          <Alert onClose={() => setCurrent(null)} severity={current.severity} variant="filled" sx={{ minWidth: 280 }}>
            {current.message}
          </Alert>
        ) : undefined}
      </Snackbar>
    </ToastContext.Provider>
  );
}

/** Fire a transient success/error/info toast from anywhere in the app. */
export function useToast(): ToastContextValue {
  const ctx = useContext(ToastContext);
  if (!ctx) {
    throw new Error("useToast must be used within a ToastProvider");
  }
  return ctx;
}
