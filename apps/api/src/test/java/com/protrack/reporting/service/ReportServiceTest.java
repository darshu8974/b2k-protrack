package com.protrack.reporting.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.protrack.identity.spi.IdentityFacade;
import com.protrack.reporting.repository.ImprintWorkloadRow;
import com.protrack.reporting.repository.ReportSnapshotRepository;
import com.protrack.reporting.repository.ThroughputRow;
import com.protrack.reporting.service.ReportService.Window;
import com.protrack.reporting.web.dto.ImprintWorkloadResponse;
import com.protrack.reporting.web.dto.ReportOverviewResponse;
import com.protrack.reporting.web.dto.ThroughputResponse;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link ReportService} window parsing and live aggregation math (no Docker). */
class ReportServiceTest {

	private ReportSnapshotRepository repository;
	private ReportService service;
	private final UUID org = UUID.randomUUID();

	@BeforeEach
	void setUp() {
		repository = mock(ReportSnapshotRepository.class);
		IdentityFacade identityFacade = mock(IdentityFacade.class);
		service = new ReportService(repository, identityFacade);
	}

	// ── range window ─────────────────────────────────────────────────────────

	@Test
	void windowSpansTheRequestedNumberOfMonthsEndingThisMonth() {
		Window window = ReportService.parseWindow("6m");
		assertThat(window.label()).isEqualTo("6m");
		assertThat(window.months()).hasSize(6);
		assertThat(window.months().get(5)).isEqualTo(YearMonth.now(ZoneOffset.UTC).toString());
		assertThat(window.periodEnd()).isEqualTo(LocalDate.now(ZoneOffset.UTC));
	}

	@Test
	void yearsAreConvertedToMonths() {
		assertThat(ReportService.parseWindow("1y").months()).hasSize(12);
	}

	@Test
	void blankOrGarbageRangeFallsBackToTheDefault() {
		assertThat(ReportService.parseWindow(null).months()).hasSize(6);
		assertThat(ReportService.parseWindow("").months()).hasSize(6);
		assertThat(ReportService.parseWindow("nonsense").months()).hasSize(6);
	}

	// ── throughput zero-fill ─────────────────────────────────────────────────

	@Test
	void throughputEmitsOnePointPerMonthWithGapsAsZero() {
		String thisMonth = YearMonth.now(ZoneOffset.UTC).toString();
		ThroughputRow row = mock(ThroughputRow.class);
		when(row.getMonth()).thenReturn(thisMonth);
		when(row.getCompleted()).thenReturn(2L);
		when(repository.throughputByMonth(eq(org), any())).thenReturn(List.of(row));

		ThroughputResponse response = service.computeThroughput(org, "6m");

		assertThat(response.points()).hasSize(6);
		assertThat(response.points().get(5).month()).isEqualTo(thisMonth);
		assertThat(response.points().get(5).completed()).isEqualTo(2L);
		assertThat(response.points().get(0).completed()).isZero(); // gap filled with 0
	}

	// ── workload share ─────────────────────────────────────────────────────────

	@Test
	void workloadComputesEachImprintsSharePercentage() {
		ImprintWorkloadRow physics = workloadRow("Physical Sciences", 3);
		ImprintWorkloadRow maths = workloadRow("Mathematics", 1);
		when(repository.workloadByImprint(org)).thenReturn(List.of(physics, maths));

		ImprintWorkloadResponse response = service.computeWorkload(org);

		assertThat(response.totalActive()).isEqualTo(4);
		assertThat(response.items().get(0).percentage()).isEqualTo(75.0);
		assertThat(response.items().get(1).percentage()).isEqualTo(25.0);
	}

	// ── overview KPIs ─────────────────────────────────────────────────────────

	@Test
	void overviewComputesPercentagesFromNumeratorsAndDenominators() {
		when(repository.avgTurnaroundDays(eq(org), any())).thenReturn(10.0);
		when(repository.countCompletedWithDueDate(eq(org), any())).thenReturn(4L);
		when(repository.countOnTime(eq(org), any())).thenReturn(3L);
		when(repository.avgAiConfidence(eq(org), any())).thenReturn(88.0);
		when(repository.countQaSignoffs(eq(org), any())).thenReturn(4L);
		when(repository.countQaApproved(eq(org), any())).thenReturn(3L);
		when(repository.countCompleted(eq(org), any())).thenReturn(5L);

		ReportOverviewResponse overview = service.computeOverview(org, "6m");

		assertThat(overview.turnaroundDays()).isEqualTo(10.0);
		assertThat(overview.onTimePercentage()).isEqualTo(75.0);
		assertThat(overview.avgAiConfidence()).isEqualTo(88.0);
		assertThat(overview.qaPassPercentage()).isEqualTo(75.0);
		assertThat(overview.completedProjects()).isEqualTo(5L);
	}

	@Test
	void overviewReturnsNullKpisWhenThereIsNoDataToDivideBy() {
		when(repository.avgTurnaroundDays(eq(org), any())).thenReturn(null);
		when(repository.countCompletedWithDueDate(eq(org), any())).thenReturn(0L);
		when(repository.countOnTime(eq(org), any())).thenReturn(0L);
		when(repository.avgAiConfidence(eq(org), any())).thenReturn(null);
		when(repository.countQaSignoffs(eq(org), any())).thenReturn(0L);
		when(repository.countQaApproved(eq(org), any())).thenReturn(0L);
		when(repository.countCompleted(eq(org), any())).thenReturn(0L);

		ReportOverviewResponse overview = service.computeOverview(org, "6m");

		assertThat(overview.turnaroundDays()).isNull();
		assertThat(overview.onTimePercentage()).isNull(); // avoids divide-by-zero
		assertThat(overview.avgAiConfidence()).isNull();
		assertThat(overview.qaPassPercentage()).isNull();
	}

	private static ImprintWorkloadRow workloadRow(String name, long count) {
		ImprintWorkloadRow row = mock(ImprintWorkloadRow.class);
		when(row.getImprintId()).thenReturn(UUID.randomUUID().toString());
		when(row.getImprintName()).thenReturn(name);
		when(row.getActiveProjects()).thenReturn(count);
		return row;
	}
}
