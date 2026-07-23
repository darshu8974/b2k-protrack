package com.protrack.ai.client.dto;

import java.util.List;

/**
 * Request body sent to the FastAPI {@code /internal/v1/assistant/chat} endpoint: the current
 * question plus light project context and prior conversation turns (RAG-lite). The AI service is
 * stateless — Spring owns the persisted thread and supplies the history each turn.
 */
public record AssistantChatRequest(
		String message,
		ProjectContextDto projectContext,
		List<HistoryMessage> history) {

	public record ProjectContextDto(
			String projectId,
			String title,
			String publicationType,
			String discipline,
			String currentStage) {
	}

	/** A prior turn of the conversation (role = "user" | "assistant"). */
	public record HistoryMessage(String role, String content) {
	}
}
