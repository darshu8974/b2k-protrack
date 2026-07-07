package com.protrack.assistant.web.dto;

import com.protrack.assistant.domain.AssistantMessage;
import java.time.Instant;
import java.util.List;

/** A single assistant-thread message (user question or assistant reply). */
public record AssistantMessageResponse(
		String id,
		String role,
		String content,
		Integer tokens,
		List<String> citations,
		Instant createdAt) {

	/** Map a persisted message (no citations — those are transient on the live reply). */
	public static AssistantMessageResponse from(AssistantMessage message) {
		return new AssistantMessageResponse(
				message.getId().toString(),
				message.getRole(),
				message.getContent(),
				message.getTokens(),
				null,
				message.getCreatedAt());
	}

	/** Map a persisted message, attaching citations from the live AI reply. */
	public static AssistantMessageResponse from(AssistantMessage message, List<String> citations) {
		return new AssistantMessageResponse(
				message.getId().toString(),
				message.getRole(),
				message.getContent(),
				message.getTokens(),
				citations,
				message.getCreatedAt());
	}
}
