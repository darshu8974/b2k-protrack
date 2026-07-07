package com.protrack.reporting.service;

import com.protrack.reporting.domain.ReportSnapshot;
import com.protrack.reporting.repository.ReportSnapshotRepository;
import com.protrack.reporting.web.dto.ImprintWorkloadResponse;
import com.protrack.reporting.web.dto.ReportOverviewResponse;
import com.protrack.reporting.web.dto.ThroughputResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Periodically rolls the live report aggregates into {@code report_snapshots} for history/trend
 * (the {@code /reports/*} endpoints serve live values; this is the durable store). Reuses
 * {@link ReportService}'s org-scoped compute methods so there is a single definition of each KPI.
 *
 * <p>Each organization is captured in its own short transaction via {@link TransactionTemplate} (the
 * codebase idiom for scheduled/off-request writes; also sidesteps {@code @Transactional}
 * self-invocation from the scheduler). Writes are idempotent: a snapshot is keyed by
 * (org, metricKey, dimension, periodStart) and upserted, so re-running never duplicates rows.
 */
@Component
public class ReportSnapshotJob {

	static final String TURNAROUND_DAYS = "TURNAROUND_DAYS";
	static final String ON_TIME_PCT = "ON_TIME_PCT";
	static final String AVG_AI_CONFIDENCE = "AVG_AI_CONFIDENCE";
	static final String QA_PASS_PCT = "QA_PASS_PCT";
	static final String THROUGHPUT = "THROUGHPUT";
	static final String WORKLOAD_BY_IMPRINT = "WORKLOAD_BY_IMPRINT";
	private static final String OVERALL = "OVERALL";
	private static final String DEFAULT_RANGE = ReportService.DEFAULT_RANGE_MONTHS + "m";

	private static final Logger log = LoggerFactory.getLogger(ReportSnapshotJob.class);

	private final ReportSnapshotRepository repository;
	private final ReportService reportService;
	private final TransactionTemplate txTemplate;

	public ReportSnapshotJob(ReportSnapshotRepository repository, ReportService reportService,
			PlatformTransactionManager txManager) {
		this.repository = repository;
		this.reportService = reportService;
		this.txTemplate = new TransactionTemplate(txManager);
	}

	/** Scheduled entry point (default daily at 02:00; override via {@code protrack.reporting.snapshot-cron}). */
	@Scheduled(cron = "${protrack.reporting.snapshot-cron:0 0 2 * * *}")
	public void scheduledCapture() {
		int written = captureSnapshots();
		log.info("Report snapshot job wrote {} measurements", written);
	}

	/** Capture snapshots for every organization with projects. Returns the number of rows upserted. */
	public int captureSnapshots() {
		Instant now = Instant.now();
		int total = 0;
		for (UUID organizationId : repository.findOrganizationIdsWithProjects()) {
			Integer written = txTemplate.execute(status -> captureForOrg(organizationId, now));
			total += written == null ? 0 : written;
		}
		return total;
	}

	private int captureForOrg(UUID organizationId, Instant now) {
		int written = 0;

		ReportOverviewResponse overview = reportService.computeOverview(organizationId, DEFAULT_RANGE);
		LocalDate start = overview.periodStart();
		LocalDate end = overview.periodEnd();
		written += upsertIfPresent(organizationId, TURNAROUND_DAYS, OVERALL, start, end,
				overview.turnaroundDays(), now);
		written += upsertIfPresent(organizationId, ON_TIME_PCT, OVERALL, start, end,
				overview.onTimePercentage(), now);
		written += upsertIfPresent(organizationId, AVG_AI_CONFIDENCE, OVERALL, start, end,
				overview.avgAiConfidence(), now);
		written += upsertIfPresent(organizationId, QA_PASS_PCT, OVERALL, start, end,
				overview.qaPassPercentage(), now);

		ThroughputResponse throughput = reportService.computeThroughput(organizationId, DEFAULT_RANGE);
		for (ThroughputResponse.Point point : throughput.points()) {
			YearMonth month = YearMonth.parse(point.month());
			written += upsert(organizationId, THROUGHPUT, point.month(), month.atDay(1),
					month.atEndOfMonth(), BigDecimal.valueOf(point.completed()), now);
		}

		ImprintWorkloadResponse workload = reportService.computeWorkload(organizationId);
		for (ImprintWorkloadResponse.Item item : workload.items()) {
			written += upsert(organizationId, WORKLOAD_BY_IMPRINT, item.imprintName(), start, end,
					BigDecimal.valueOf(item.activeProjects()), now);
		}
		return written;
	}

	private int upsertIfPresent(UUID organizationId, String metricKey, String dimension,
			LocalDate start, LocalDate end, Double value, Instant now) {
		if (value == null) {
			return 0; // no data in the window — don't record a misleading zero
		}
		return upsert(organizationId, metricKey, dimension, start, end, BigDecimal.valueOf(value), now);
	}

	private int upsert(UUID organizationId, String metricKey, String dimension, LocalDate start,
			LocalDate end, BigDecimal value, Instant now) {
		repository.findByOrganizationIdAndMetricKeyAndDimensionAndPeriodStart(
				organizationId, metricKey, dimension, start)
				.ifPresentOrElse(
						existing -> existing.update(start, end, value, now),
						() -> repository.save(new ReportSnapshot(UUID.randomUUID(), organizationId,
								start, end, metricKey, dimension, value, now)));
		return 1;
	}
}
