import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { queryKeys } from "../../api/keys";
import { addComment, deleteComment, editComment, listComments } from "./api";

export function useComments(projectId: string) {
  return useQuery({
    queryKey: queryKeys.comments(projectId),
    queryFn: () => listComments(projectId),
    enabled: !!projectId,
  });
}

export function useAddComment(projectId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (body: { body: string; parentId?: string | null }) => addComment(projectId, body),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.comments(projectId) });
    },
  });
}

export function useEditComment(projectId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: string }) => editComment(id, body),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.comments(projectId) });
    },
  });
}

export function useDeleteComment(projectId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (id: string) => deleteComment(id),
    onSuccess: () => {
      void queryClient.invalidateQueries({ queryKey: queryKeys.comments(projectId) });
    },
  });
}
