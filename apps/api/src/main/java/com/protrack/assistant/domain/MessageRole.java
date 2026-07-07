package com.protrack.assistant.domain;

/** The author of an assistant-thread message. Stored as its {@code name()} in a VARCHAR column. */
public enum MessageRole {
	USER,
	ASSISTANT
}
