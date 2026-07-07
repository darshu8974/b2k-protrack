/** A single message in the scoped assistant thread. */
export interface AssistantMessage {
  id: string;
  role: string; // "USER" | "ASSISTANT"
  content: string;
  tokens?: number | null;
  citations?: string[] | null;
  createdAt: string;
}

/** The current user's assistant conversation for a project. */
export interface AssistantThread {
  threadId: string | null;
  messages: AssistantMessage[];
}
