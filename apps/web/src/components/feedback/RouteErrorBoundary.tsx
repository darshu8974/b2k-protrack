import { isRouteErrorResponse, useRouteError } from "react-router-dom";

import { ErrorState } from "./ErrorState";

/** errorElement for routes — renders router/loader errors via ErrorState. */
export function RouteErrorBoundary() {
  const error = useRouteError();

  let message = "Unexpected error";
  if (isRouteErrorResponse(error)) {
    message = `${error.status} ${error.statusText}`;
  } else if (error instanceof Error) {
    message = error.message;
  }

  return <ErrorState message={message} />;
}
