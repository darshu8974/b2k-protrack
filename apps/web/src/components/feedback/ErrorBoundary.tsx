import { Component, type ErrorInfo, type ReactNode } from "react";

import { ErrorState } from "./ErrorState";

interface Props {
  children: ReactNode;
}

interface State {
  hasError: boolean;
  message?: string;
}

/** Top-level React error boundary that catches render-time errors below it. */
export class ErrorBoundary extends Component<Props, State> {
  state: State = { hasError: false };

  static getDerivedStateFromError(error: unknown): State {
    return { hasError: true, message: error instanceof Error ? error.message : undefined };
  }

  componentDidCatch(error: Error, info: ErrorInfo): void {
    // TODO (later): forward to an error-reporting sink.
    console.error("Unhandled UI error:", error, info.componentStack);
  }

  render(): ReactNode {
    if (this.state.hasError) {
      return <ErrorState message={this.state.message} />;
    }
    return this.props.children;
  }
}
