package com.protrack.assistant.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** A question posted to the scoped assistant (API Specification §3.10). */
public record AssistantMessageRequest(
		@NotBlank @Size(max = 4000) String content) {
}
