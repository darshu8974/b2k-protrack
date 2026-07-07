import { apiClient } from "../../api/axios";
import type { AssistantMessage, AssistantThread } from "../../types/assistant";

export async function getAssistantThread(projectId: string): Promise<AssistantThread> {
  const { data } = await apiClient.get<AssistantThread>(
    `/projects/${projectId}/assistant/thread`,
  );
  return data;
}

export async function askAssistant(
  projectId: string,
  content: string,
): Promise<AssistantMessage> {
  const { data } = await apiClient.post<AssistantMessage>(
    `/projects/${projectId}/assistant/messages`,
    { content },
  );
  return data;
}
