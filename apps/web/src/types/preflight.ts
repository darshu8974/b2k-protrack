/** Preflight & QA domain types (mirror the api preflight/qa modules). */

export type CheckResult = "PASS" | "REVIEW" | "FAIL";
export type Severity = "HIGH" | "MEDIUM" | "LOW";
export type IssueStatus = "OPEN" | "TRIAGED" | "RESOLVED" | "WAIVED";
export type DecisionType = "ACCEPT_FIX" | "SEND_BACK" | "COMMENT";
export type SignoffDecision = "APPROVED" | "REJECTED";

export interface PreflightCheck {
  key: string;
  result: CheckResult;
  detail?: string | null;
}

/** An issue as embedded in the preflight detail (GET /projects/{id}/preflight). */
export interface PreflightIssueView {
  id: string;
  category?: string | null;
  severity: Severity;
  title: string;
  recommendation?: string | null;
  pageRef?: string | null;
  source: string;
  status: IssueStatus;
  createdAt: string;
}

export interface PreflightDetail {
  id: string;
  projectId: string;
  aiJobId: string;
  pdfVersionId: string;
  standard?: string | null;
  overallScore?: number | null;
  passed?: boolean | null;
  totalIssues: number;
  highSeverity: number;
  status: string;
  ranAt?: string | null;
  createdAt: string;
  checks: PreflightCheck[];
  issues: PreflightIssueView[];
}

/** A QA issue on the triage surface (GET /projects/{id}/issues). */
export interface QaIssue {
  id: string;
  projectId: string;
  preflightRunId: string;
  category?: string | null;
  severity: Severity;
  title: string;
  recommendation?: string | null;
  pageRef?: string | null;
  source: string;
  status: IssueStatus;
  createdAt: string;
}

export interface ProductionPdfResult {
  documentId: string;
  versionId: string;
  fileName: string;
  stage: string;
}

export interface IssueDecision {
  id: string;
  issueId: string;
  decision: DecisionType;
  comment?: string | null;
  decidedBy?: string | null;
  decidedByName?: string | null;
  issueStatus: IssueStatus;
  createdAt: string;
}

export interface BulkDecisionResult {
  decided: number;
  decision: DecisionType;
  issueStatus: IssueStatus;
  issueIds: string[];
}

export interface Signoff {
  id: string;
  projectId: string;
  preflightRunId: string;
  decision: SignoffDecision;
  qualityScore?: number | null;
  signatureHash?: string | null;
  notes?: string | null;
  signedBy?: string | null;
  signedByName?: string | null;
  stage?: string | null;
  createdAt: string;
}

export interface Approval {
  id: string;
  projectId: string;
  stageCode?: string | null;
  approvalType?: string | null;
  decision: string;
  decidedRole?: string | null;
  decidedBy?: string | null;
  decidedByName?: string | null;
  comment?: string | null;
  createdAt: string;
}
