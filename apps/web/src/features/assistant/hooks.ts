import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { queryKeys } from "../../api/keys";
import { askAssistant, getAssistantThread } from "./api";

export function useAssistantThread(projectId: string) {
  return useQuery({
    queryKey: queryKeys.assistantThread(projectId),
    queryFn: () => getAssistantThread(projectId),
    enabled: !!projectId,
  });
}

export function useAskAssistant(projectId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (content: string) => askAssistant(projectId, content),
    // The persisted user + assistant messages are the source of truth — refetch the thread.
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.assistantThread(projectId) });
    },
  });
}
