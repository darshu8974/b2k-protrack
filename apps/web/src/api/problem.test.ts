import { AxiosError, AxiosHeaders } from "axios";
import { describe, expect, it } from "vitest";

import type { Problem } from "../types/api";
import { toAppError } from "./problem";

function axiosErrorWith(data: Problem, status: number): AxiosError<Problem> {
  const error = new AxiosError<Problem>("Request failed");
  error.response = {
    data,
    status,
    statusText: "",
    headers: {},
    config: { headers: new AxiosHeaders() },
  };
  return error;
}

describe("toAppError", () => {
  it("maps a Problem body, preferring detail over title and copying field errors", () => {
    const problem: Problem = {
      title: "Validation failed",
      status: 422,
      code: "VALIDATION_ERROR",
      detail: "ISBN is invalid",
      traceId: "abc-123",
      fieldErrors: [{ field: "isbn", code: "invalid", message: "Bad checksum" }],
    };

    const result = toAppError(axiosErrorWith(problem, 422));

    expect(result).toEqual({
      status: 422,
      code: "VALIDATION_ERROR",
      message: "ISBN is invalid",
      traceId: "abc-123",
      fieldErrors: [{ field: "isbn", message: "Bad checksum" }],
    });
  });

  it("falls back to the Problem title when detail is absent", () => {
    const problem: Problem = { title: "Forbidden", status: 403, code: "FORBIDDEN" };
    const result = toAppError(axiosErrorWith(problem, 403));
    expect(result.message).toBe("Forbidden");
    expect(result.fieldErrors).toBeUndefined();
  });

  it("produces a NETWORK_ERROR for a response without a Problem code", () => {
    const error = new AxiosError("Network Error");
    const result = toAppError(error);
    expect(result.code).toBe("NETWORK_ERROR");
    expect(result.status).toBe(0);
    expect(result.message).toBe("Network Error");
  });
});
