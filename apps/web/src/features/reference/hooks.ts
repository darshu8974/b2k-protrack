import { useQuery } from "@tanstack/react-query";

import { apiClient } from "../../api/axios";
import { queryKeys } from "../../api/keys";
import type { Imprint, WorkflowStage } from "../../types/project";

async function fetchImprints(): Promise<Imprint[]> {
  const { data } = await apiClient.get<Imprint[]>("/imprints");
  return data;
}

async function fetchWorkflowStages(): Promise<WorkflowStage[]> {
  const { data } = await apiClient.get<WorkflowStage[]>("/workflow-stages");
  return data;
}

export function useImprints() {
  return useQuery({ queryKey: queryKeys.imprints, queryFn: fetchImprints, staleTime: 5 * 60_000 });
}

export function useWorkflowStages() {
  return useQuery({
    queryKey: queryKeys.workflowStages,
    queryFn: fetchWorkflowStages,
    staleTime: 5 * 60_000,
  });
}
