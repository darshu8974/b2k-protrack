import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { queryKeys } from "../../api/keys";
import type { AppError } from "../../types/api";
import type { DecisionType, SignoffDecision } from "../../types/preflight";
import {
  bulkDecide,
  decideIssue,
  getPreflight,
  listApprovals,
  listIssues,
  listSignoffs,
  qcApprove,
  qcReject,
  sendToQc,
  signOff,
  startPreflight,
  uploadProductionPdf,
  type ProgressHandler,
} from "./api";

export function usePreflight(projectId: string) {
  return useQuery({
    queryKey: queryKeys.preflight(projectId),
    queryFn: () => getPreflight(projectId),
    enabled: !!projectId,
    // 404 means "no preflight yet" — surface immediately without retrying.
    retry: (failureCount, error) =>
      (error as unknown as AppError)?.status !== 404 && failureCount < 2,
  });
}

export function useIssues(projectId: string, enabled = true) {
  return useQuery({
    queryKey: queryKeys.issues(projectId),
    queryFn: () => listIssues(projectId),
    enabled: enabled && !!projectId,
  });
}

export function useApprovals(projectId: string, enabled = true) {
  return useQuery({
    queryKey: queryKeys.approvals(projectId),
    queryFn: () => listApprovals(projectId),
    enabled: enabled && !!projectId,
  });
}

export function useSignoffs(projectId: string, enabled = true) {
  return useQuery({
    queryKey: queryKeys.signoffs(projectId),
    queryFn: () => listSignoffs(projectId),
    enabled: enabled && !!projectId,
  });
}

export function useUploadPdf(projectId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ file, onProgress }: { file: File; onProgress?: ProgressHandler }) =>
      uploadProductionPdf(projectId, file, onProgress),
    onSuccess: () => {
      // The upload auto-transitions the project to PDF_REVIEW.
      queryClient.invalidateQueries({ queryKey: queryKeys.project(projectId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.projectTimeline(projectId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.projectActivity(projectId) });
      queryClient.invalidateQueries({ queryKey: ["project", projectId, "documents"] });
    },
  });
}

export function useStartPreflight(projectId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => startPreflight(projectId),
    onSuccess: (job) => {
      queryClient.setQueryData(queryKeys.aiJob(job.jobId), job);
    },
  });
}

export function useDecideIssue(projectId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({
      issueId,
      decision,
      comment,
    }: {
      issueId: string;
      decision: DecisionType;
      comment?: string;
    }) => decideIssue(issueId, decision, comment),
    onSuccess: () => invalidateQa(queryClient, projectId),
  });
}

export function useBulkDecide(projectId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ issueIds, decision }: { issueIds: string[]; decision: DecisionType }) =>
      bulkDecide(issueIds, decision),
    onSuccess: () => invalidateQa(queryClient, projectId),
  });
}

export function useSignoff(projectId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (body: {
      decision: SignoffDecision;
      qualityScore: number;
      signature: string;
      notes?: string;
    }) => signOff(projectId, body),
    onSuccess: () => {
      // Sign-off atomically moves the project (COMPLETED or IN_PRODUCTION) and writes history.
      queryClient.invalidateQueries({ queryKey: queryKeys.project(projectId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.projectTimeline(projectId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.projectActivity(projectId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.approvals(projectId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.signoffs(projectId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.issues(projectId) });
    },
  });
}

export function useSendToQc(projectId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => sendToQc(projectId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.project(projectId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.projectTimeline(projectId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.projectActivity(projectId) });
    },
  });
}

/** QC → advance QC_REVIEW to QA sign-off (approve) or back to production (reject). */
function useQcTransition(projectId: string, action: (projectId: string) => Promise<void>) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => action(projectId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.project(projectId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.projectTimeline(projectId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.projectActivity(projectId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.issues(projectId) });
    },
  });
}

export function useQcApprove(projectId: string) {
  return useQcTransition(projectId, qcApprove);
}

export function useQcReject(projectId: string) {
  return useQcTransition(projectId, qcReject);
}

function invalidateQa(queryClient: ReturnType<typeof useQueryClient>, projectId: string): void {
  queryClient.invalidateQueries({ queryKey: queryKeys.issues(projectId) });
  queryClient.invalidateQueries({ queryKey: queryKeys.preflight(projectId) });
  queryClient.invalidateQueries({ queryKey: queryKeys.projectActivity(projectId) });
}
