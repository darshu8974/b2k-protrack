/** Standard paginated list envelope returned by the API. */
export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  sort?: string;
  hasNext: boolean;
}

/** RFC 9457-style Problem body returned by the API on errors. */
export interface Problem {
  type?: string;
  title: string;
  status: number;
  code: string;
  detail?: string;
  instance?: string;
  traceId?: string;
  timestamp?: string;
  fieldErrors?: { field: string; code: string; message: string }[];
}

/** Normalized client-side error shape produced from a Problem or network failure. */
export interface AppError {
  status: number;
  code: string;
  message: string;
  fieldErrors?: { field: string; message: string }[];
  traceId?: string;
}
