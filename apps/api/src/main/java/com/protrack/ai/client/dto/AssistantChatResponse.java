package com.protrack.ai.client.dto;

import java.util.List;

/**
 * The scoped assistant reply returned by the FastAPI AI service (camelCase JSON, deserialized 1:1).
 * The assistant module persists {@code reply} + {@code usage.outputTokens} into assistant_messages.
 */
public record AssistantChatResponse(
		String reply,
		List<String> citations,
		UsageDto usage) {

	public record UsageDto(Integer inputTokens, Integer outputTokens, String model) {
	}
}
