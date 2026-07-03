import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { queryKeys } from "../../api/keys";
import type { AppError } from "../../types/api";
import type { AiJob } from "../../types/analysis";
import { getAiJob, getAnalysis, startAnalysis, startProduction } from "./api";

const ACTIVE_STATUSES = new Set(["QUEUED", "RUNNING"]);

export function useAnalysis(projectId: string) {
  return useQuery({
    queryKey: queryKeys.analysis(projectId),
    queryFn: () => getAnalysis(projectId),
    enabled: !!projectId,
    // 404 means "no analysis yet" — surface immediately without retrying.
    retry: (failureCount, error) =>
      (error as unknown as AppError)?.status !== 404 && failureCount < 2,
  });
}

/** Poll a job while it is active (SSE gives faster updates; this is the fallback). */
export function useAiJob(jobId: string | null) {
  return useQuery({
    queryKey: queryKeys.aiJob(jobId ?? "none"),
    queryFn: () => getAiJob(jobId as string),
    enabled: !!jobId,
    refetchInterval: (query) => {
      const status = (query.state.data as AiJob | undefined)?.status;
      return status && ACTIVE_STATUSES.has(status) ? 2000 : false;
    },
  });
}

export function useStartAnalysis(projectId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => startAnalysis(projectId),
    onSuccess: (job) => {
      queryClient.setQueryData(queryKeys.aiJob(job.jobId), job);
    },
  });
}

export function useStartProduction(projectId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => startProduction(projectId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.project(projectId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.projectTimeline(projectId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.projectActivity(projectId) });
    },
  });
}
