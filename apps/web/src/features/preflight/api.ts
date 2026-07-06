import type { AxiosProgressEvent } from "axios";

import { apiClient } from "../../api/axios";
import type { AiJob } from "../../types/analysis";
import type {
  Approval,
  BulkDecisionResult,
  DecisionType,
  IssueDecision,
  PreflightDetail,
  ProductionPdfResult,
  QaIssue,
  Signoff,
  SignoffDecision,
} from "../../types/preflight";

export type ProgressHandler = (percent: number) => void;

/** Upload the production PDF (multipart) — the backend advances the project to PDF_REVIEW. */
export async function uploadProductionPdf(
  projectId: string,
  file: File,
  onProgress?: ProgressHandler,
): Promise<ProductionPdfResult> {
  const form = new FormData();
  form.append("file", file);
  const { data } = await apiClient.post<ProductionPdfResult>(`/projects/${projectId}/pdf`, form, {
    // Override the client's default JSON content-type; axios fills in the multipart boundary.
    headers: { "Content-Type": "multipart/form-data" },
    onUploadProgress: onProgress
      ? (event: AxiosProgressEvent) => {
          if (event.total) {
            onProgress(Math.round((event.loaded / event.total) * 100));
          }
        }
      : undefined,
  });
  return data;
}

/** Start a PDF preflight (202 + job). */
export async function startPreflight(projectId: string): Promise<AiJob> {
  const { data } = await apiClient.post<AiJob>(`/projects/${projectId}/preflight`);
  return data;
}

/** Latest persisted preflight for a project (404 if none yet). */
export async function getPreflight(projectId: string): Promise<PreflightDetail> {
  const { data } = await apiClient.get<PreflightDetail>(`/projects/${projectId}/preflight`);
  return data;
}

export async function listIssues(projectId: string): Promise<QaIssue[]> {
  const { data } = await apiClient.get<QaIssue[]>(`/projects/${projectId}/issues`);
  return data;
}

export async function decideIssue(
  issueId: string,
  decision: DecisionType,
  comment?: string,
): Promise<IssueDecision> {
  const { data } = await apiClient.post<IssueDecision>(`/issues/${issueId}/decision`, {
    decision,
    comment,
  });
  return data;
}

export async function bulkDecide(
  issueIds: string[],
  decision: DecisionType,
): Promise<BulkDecisionResult> {
  const { data } = await apiClient.post<BulkDecisionResult>(`/issues:bulk-decision`, {
    issueIds,
    decision,
  });
  return data;
}

export interface SignoffBody {
  decision: SignoffDecision;
  qualityScore: number;
  signature: string;
  notes?: string;
}

export async function signOff(projectId: string, body: SignoffBody): Promise<Signoff> {
  const { data } = await apiClient.post<Signoff>(`/projects/${projectId}/signoff`, body);
  return data;
}

export async function listApprovals(projectId: string): Promise<Approval[]> {
  const { data } = await apiClient.get<Approval[]>(`/projects/${projectId}/approvals`);
  return data;
}

export async function listSignoffs(projectId: string): Promise<Signoff[]> {
  const { data } = await apiClient.get<Signoff[]>(`/projects/${projectId}/signoffs`);
  return data;
}

/** Advance the workflow PDF_REVIEW → QA_SIGNOFF (SEND_TO_QA transition). */
export async function sendToQa(projectId: string): Promise<void> {
  await apiClient.post(`/projects/${projectId}/transitions`, { toStage: "QA_SIGNOFF" });
}
