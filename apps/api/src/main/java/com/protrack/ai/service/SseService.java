package com.protrack.ai.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Server-Sent Events relay. Clients subscribe per project; the AI progress callback and completion
 * events publish here so the browser sees live progress. Emitters are cleaned up on
 * completion/timeout/error. State is in-memory (single instance in Phase 1).
 */
@Service
public class SseService {

	private static final Logger log = LoggerFactory.getLogger(SseService.class);
	private static final long TIMEOUT_MS = 30 * 60 * 1000L;

	private final ConcurrentHashMap<UUID, List<SseEmitter>> emitters = new ConcurrentHashMap<>();

	public SseEmitter subscribe(UUID projectId) {
		SseEmitter emitter = new SseEmitter(TIMEOUT_MS);
		List<SseEmitter> list = emitters.computeIfAbsent(projectId, key -> new CopyOnWriteArrayList<>());
		list.add(emitter);
		emitter.onCompletion(() -> remove(projectId, emitter));
		emitter.onTimeout(() -> remove(projectId, emitter));
		emitter.onError(ex -> remove(projectId, emitter));
		try {
			emitter.send(SseEmitter.event().name("subscribed").data("{\"projectId\":\"" + projectId + "\"}"));
		} catch (IOException ex) {
			remove(projectId, emitter);
		}
		return emitter;
	}

	/** Publish an event to all subscribers of a project. Dead emitters are pruned. */
	public void publish(UUID projectId, String eventName, Object data) {
		List<SseEmitter> list = emitters.get(projectId);
		if (list == null) {
			return;
		}
		for (SseEmitter emitter : list) {
			try {
				emitter.send(SseEmitter.event().name(eventName).data(data));
			} catch (Exception ex) {
				log.debug("SSE send failed; removing emitter for project {}", projectId);
				remove(projectId, emitter);
			}
		}
	}

	private void remove(UUID projectId, SseEmitter emitter) {
		List<SseEmitter> list = emitters.get(projectId);
		if (list != null) {
			list.remove(emitter);
			if (list.isEmpty()) {
				emitters.remove(projectId, list);
			}
		}
	}
}
