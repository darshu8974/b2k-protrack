import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { queryKeys } from "../../api/keys";
import type { AppError } from "../../types/api";
import { assemblePackage, getPackage, removePackageItem } from "./api";

export function usePackage(projectId: string, enabled = true) {
  return useQuery({
    queryKey: queryKeys.projectPackage(projectId),
    queryFn: () => getPackage(projectId),
    // Package access is Designer/PM/Admin only; callers gate `enabled` to avoid a 403 for QA.
    enabled: enabled && !!projectId,
    // A 404 means "not assembled yet" — surface it immediately without retrying.
    retry: (failureCount, error) =>
      (error as unknown as AppError)?.status !== 404 && failureCount < 2,
  });
}

export function useAssemblePackage(projectId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: () => assemblePackage(projectId),
    onSuccess: (data) => {
      queryClient.setQueryData(queryKeys.projectPackage(projectId), data);
      queryClient.invalidateQueries({ queryKey: queryKeys.projectActivity(projectId) });
    },
  });
}

export function useRemovePackageItem(projectId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (itemId: string) => removePackageItem(projectId, itemId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: queryKeys.projectPackage(projectId) });
    },
  });
}
