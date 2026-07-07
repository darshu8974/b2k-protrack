package com.protrack.audit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.protrack.audit.domain.AuditEvent;
import com.protrack.audit.repository.AuditEventRepository;
import com.protrack.identity.spi.IdentityFacade;
import com.protrack.identity.spi.IdentityFacade.UserBrief;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

/** Unit tests for {@link AuditService#exportCsv} formatting + RFC-4180 escaping (no Docker). */
class AuditServiceCsvTest {

	private AuditEventRepository auditEventRepository;
	private IdentityFacade identityFacade;
	private AuditService service;

	private final UUID admin = UUID.randomUUID();
	private final UUID orgId = UUID.randomUUID();
	private final UUID actorId = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		auditEventRepository = mock(AuditEventRepository.class);
		identityFacade = mock(IdentityFacade.class);
		service = new AuditService(auditEventRepository, identityFacade, new ObjectMapper());
		when(identityFacade.findOrganizationId(admin)).thenReturn(Optional.of(orgId));
	}

	@Test
	void exportsAHeaderRowAndEscapesCommasAndQuotes() {
		AuditEvent event = mock(AuditEvent.class);
		when(event.getCreatedAt()).thenReturn(Instant.parse("2026-07-07T05:00:00Z"));
		when(event.getEventType()).thenReturn("PROJECT_CREATED");
		when(event.getEntityType()).thenReturn("PROJECT");
		when(event.getEntityId()).thenReturn(null);
		// Summary contains a comma and a quote -> must be wrapped and inner quotes doubled.
		when(event.getSummary()).thenReturn("Created, \"Quantum\"");
		when(event.getActorId()).thenReturn(actorId);
		when(event.getActorType()).thenReturn("USER");
		when(event.getProjectId()).thenReturn(null);
		when(event.getCorrelationId()).thenReturn("trace-1");

		Page<AuditEvent> page = new PageImpl<>(List.of(event), PageRequest.of(0, 500), 1);
		when(auditEventRepository.findAll(any(Specification.class), any(Pageable.class)))
				.thenReturn(page);
		when(identityFacade.findBriefs(any())).thenReturn(Map.of(actorId,
				new UserBrief(actorId, "Priya Anand", "priya@protrack.io", "PA", "#000")));

		String csv = new String(service.exportCsv(admin, null, null), StandardCharsets.UTF_8);
		List<String> lines = csv.lines().toList();

		assertThat(lines.get(0))
				.isEqualTo("Time,Event,Entity Type,Entity Id,Summary,Actor,Actor Type,Project Id,Correlation Id");
		assertThat(lines.get(1)).contains("PROJECT_CREATED");
		assertThat(lines.get(1)).contains("\"Created, \"\"Quantum\"\"\""); // RFC-4180 escaping
		assertThat(lines.get(1)).contains("Priya Anand");
		assertThat(lines).hasSize(2);
	}

	@Test
	void anEmptyLogStillProducesTheHeaderRow() {
		Page<AuditEvent> empty = new PageImpl<>(List.of(), PageRequest.of(0, 500), 0);
		when(auditEventRepository.findAll(any(Specification.class), any(Pageable.class)))
				.thenReturn(empty);

		String csv = new String(service.exportCsv(admin, null, null), StandardCharsets.UTF_8);

		assertThat(csv.lines().toList()).hasSize(1);
		assertThat(csv).startsWith("Time,Event,Entity Type");
	}
}
