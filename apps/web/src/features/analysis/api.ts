import { apiClient } from "../../api/axios";
import type { AiJob, AnalysisDetail } from "../../types/analysis";

/** Start a manuscript analysis (202 + job). */
export async function startAnalysis(projectId: string): Promise<AiJob> {
  const { data } = await apiClient.post<AiJob>(`/projects/${projectId}/analysis`);
  return data;
}

/** Latest persisted analysis for a project (404 if none yet). */
export async function getAnalysis(projectId: string): Promise<AnalysisDetail> {
  const { data } = await apiClient.get<AnalysisDetail>(`/projects/${projectId}/analysis`);
  return data;
}

/** Poll a job's status/progress (SSE fallback). */
export async function getAiJob(jobId: string): Promise<AiJob> {
  const { data } = await apiClient.get<AiJob>(`/ai-jobs/${jobId}`);
  return data;
}

/** "Start Production": advance the workflow AI_ANALYSIS → DESIGN_PREP. */
export async function startProduction(projectId: string): Promise<void> {
  await apiClient.post(`/projects/${projectId}/transitions`, { toStage: "DESIGN_PREP" });
}
