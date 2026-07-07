package com.protrack.reporting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.protrack.reporting.domain.ReportSnapshot;
import com.protrack.reporting.repository.ReportSnapshotRepository;
import com.protrack.reporting.web.dto.ImprintWorkloadResponse;
import com.protrack.reporting.web.dto.ReportOverviewResponse;
import com.protrack.reporting.web.dto.ThroughputResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

/** Unit tests for {@link ReportSnapshotJob} upsert/idempotency behaviour (Mockito; no Docker). */
class ReportSnapshotJobTest {

	private ReportSnapshotRepository repository;
	private ReportService reportService;
	private ReportSnapshotJob job;
	private final UUID org = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		repository = mock(ReportSnapshotRepository.class);
		reportService = mock(ReportService.class);
		PlatformTransactionManager txManager = mock(PlatformTransactionManager.class);
		when(txManager.getTransaction(any())).thenReturn(mock(TransactionStatus.class));
		job = new ReportSnapshotJob(repository, reportService, txManager);

		when(repository.findOrganizationIdsWithProjects()).thenReturn(List.of(org));
		LocalDate start = LocalDate.of(2026, 2, 1);
		LocalDate end = LocalDate.of(2026, 7, 7);
		// One KPI (onTime) is null -> it must be skipped, not written as a misleading zero.
		when(reportService.computeOverview(eq(org), any())).thenReturn(new ReportOverviewResponse(
				"6m", start, end, 8.0, null, 85.0, 75.0, 5, 4));
		when(reportService.computeThroughput(eq(org), any())).thenReturn(new ThroughputResponse("6m",
				List.of(new ThroughputResponse.Point("2026-06", 1),
						new ThroughputResponse.Point("2026-07", 2))));
		when(reportService.computeWorkload(eq(org))).thenReturn(new ImprintWorkloadResponse(4,
				List.of(new ImprintWorkloadResponse.Item("id-1", "Physical Sciences", 4, 100.0))));
	}

	@Test
	void insertsOneRowPerMeasurementAndSkipsNullKpis() {
		when(repository.findByOrganizationIdAndMetricKeyAndDimensionAndPeriodStart(
				any(), any(), any(), any())).thenReturn(Optional.empty());

		int written = job.captureSnapshots();

		// 3 non-null overall KPIs (turnaround, AI confidence, QA pass — on-time is null) + 2 throughput
		// months + 1 imprint = 6 upserts.
		assertThat(written).isEqualTo(6);
		verify(repository, times(6)).save(any(ReportSnapshot.class));
	}

	@Test
	void existingMeasurementsAreUpdatedInPlaceNotDuplicated() {
		ReportSnapshot existing = spy(new ReportSnapshot(UUID.randomUUID(), org,
				LocalDate.of(2026, 2, 1), LocalDate.of(2026, 7, 7), "TURNAROUND_DAYS", "OVERALL",
				BigDecimal.ZERO, Instant.now()));
		when(repository.findByOrganizationIdAndMetricKeyAndDimensionAndPeriodStart(
				any(), any(), any(), any())).thenReturn(Optional.of(existing));

		int written = job.captureSnapshots();

		assertThat(written).isEqualTo(6);
		verify(repository, never()).save(any()); // never inserts when the measurement already exists
		verify(existing, atLeastOnce()).update(any(), any(), any(), any());
	}
}
