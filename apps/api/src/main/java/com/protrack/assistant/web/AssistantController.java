package com.protrack.assistant.web;

import com.protrack.assistant.service.AssistantService;
import com.protrack.assistant.web.dto.AssistantMessageRequest;
import com.protrack.assistant.web.dto.AssistantMessageResponse;
import com.protrack.assistant.web.dto.AssistantThreadResponse;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The scoped AI Assistant (API Specification §3.10). Authorization is contextual and enforced in
 * {@link AssistantService}: read/ask require project membership (ADMIN overrides). The chat turn is
 * synchronous — the reply is returned inline (200).
 */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/assistant")
public class AssistantController {

	private final AssistantService assistantService;

	public AssistantController(AssistantService assistantService) {
		this.assistantService = assistantService;
	}

	@GetMapping("/thread")
	public AssistantThreadResponse thread(@PathVariable UUID projectId, Principal principal) {
		return assistantService.getThread(currentUser(principal), projectId);
	}

	@PostMapping("/messages")
	public AssistantMessageResponse ask(@PathVariable UUID projectId,
			@Valid @RequestBody AssistantMessageRequest request, Principal principal) {
		return assistantService.ask(currentUser(principal), projectId, request.content());
	}

	private static UUID currentUser(Principal principal) {
		return UUID.fromString(principal.getName());
	}
}
