import { keepPreviousData, useQuery } from "@tanstack/react-query";

import { getProjectActivity, listAuditEvents, type AuditListParams } from "./api";

export function useProjectActivity(projectId: string) {
  return useQuery({
    queryKey: ["project", projectId, "activity"],
    queryFn: () => getProjectActivity(projectId),
    enabled: !!projectId,
  });
}

export function useAuditEvents(params: AuditListParams) {
  return useQuery({
    queryKey: ["audit-events", params],
    queryFn: () => listAuditEvents(params),
    placeholderData: keepPreviousData,
  });
}
