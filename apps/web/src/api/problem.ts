import { AxiosError } from "axios";

import type { AppError, Problem } from "../types/api";

/** Convert an unknown axios/network error into a normalized AppError. */
export function toAppError(error: unknown): AppError {
  const axiosError = error as AxiosError<Problem>;
  const data = axiosError.response?.data;

  if (data && typeof data === "object" && "code" in data) {
    return {
      status: data.status,
      code: data.code,
      message: data.detail ?? data.title,
      fieldErrors: data.fieldErrors?.map((f) => ({ field: f.field, message: f.message })),
      traceId: data.traceId,
    };
  }

  return {
    status: axiosError.response?.status ?? 0,
    code: "NETWORK_ERROR",
    message: axiosError.message ?? "Network error",
  };
}
