package com.protrack.ai.web;

import com.protrack.ai.service.SseService;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/** Server-Sent Events stream for a project's AI progress/stage updates. */
@RestController
@RequestMapping("/api/v1/projects/{projectId}/events")
public class SseController {

	private final SseService sseService;

	public SseController(SseService sseService) {
		this.sseService = sseService;
	}

	@GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter stream(@PathVariable UUID projectId) {
		return sseService.subscribe(projectId);
	}
}
