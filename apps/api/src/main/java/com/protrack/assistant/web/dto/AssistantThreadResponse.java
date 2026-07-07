package com.protrack.assistant.web.dto;

import java.util.List;

/**
 * A user's scoped assistant thread for a project, with its messages in order. {@code threadId} is
 * null when no conversation has started yet (the thread is created lazily on the first question).
 */
public record AssistantThreadResponse(
		String threadId,
		List<AssistantMessageResponse> messages) {

	/** An empty thread (nothing asked yet). */
	public static AssistantThreadResponse empty() {
		return new AssistantThreadResponse(null, List.of());
	}
}
