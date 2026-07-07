import { apiClient } from "../../api/axios";
import type { Page } from "../../types/api";
import type { Comment } from "../../types/comment";

export async function listComments(projectId: string): Promise<Page<Comment>> {
  const { data } = await apiClient.get<Page<Comment>>(`/projects/${projectId}/comments`, {
    params: { page: 0, size: 100 },
  });
  return data;
}

export async function addComment(
  projectId: string,
  body: { body: string; parentId?: string | null },
): Promise<Comment> {
  const { data } = await apiClient.post<Comment>(`/projects/${projectId}/comments`, body);
  return data;
}

export async function editComment(commentId: string, body: string): Promise<Comment> {
  const { data } = await apiClient.patch<Comment>(`/comments/${commentId}`, { body });
  return data;
}

export async function deleteComment(commentId: string): Promise<void> {
  await apiClient.delete(`/comments/${commentId}`);
}
