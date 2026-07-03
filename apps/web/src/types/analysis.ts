/** AI analysis domain types (mirror the api ai/analysis modules). */

export type JobStatus = "QUEUED" | "RUNNING" | "SUCCEEDED" | "FAILED";

export interface AiJob {
  jobId: string;
  projectId: string;
  jobType: string;
  status: JobStatus;
  progressPct: number;
  provider?: string | null;
  model?: string | null;
  errorMessage?: string | null;
  createdAt?: string | null;
  finishedAt?: string | null;
}

export interface AnalysisMetric {
  key: string;
  value: number | null;
  confidence: number | null;
}

export interface AnalysisCompositionSegment {
  segment: string;
  percentage: number | null;
}

export interface AnalysisHeadingCount {
  level: string;
  count: number;
}

export interface AnalysisRisk {
  severity: string;
  title: string;
  description?: string | null;
}

export interface AnalysisTeamSuggestion {
  userId?: string | null;
  role?: string | null;
  matchScore?: number | null;
  rationale?: string | null;
}

export interface AnalysisDetail {
  id: string;
  projectId: string;
  aiJobId: string;
  overallConfidence?: number | null;
  summary?: string | null;
  language?: string | null;
  complexityScore?: number | null;
  complexityLabel?: string | null;
  estimatedWorkingDays?: number | null;
  metrics: AnalysisMetric[];
  composition: AnalysisCompositionSegment[];
  headings: AnalysisHeadingCount[];
  risks: AnalysisRisk[];
  suggestedTeam: AnalysisTeamSuggestion[];
  createdAt: string;
}
