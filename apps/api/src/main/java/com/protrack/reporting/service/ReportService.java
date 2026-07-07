package com.protrack.reporting.service;

import com.protrack.identity.spi.IdentityFacade;
import com.protrack.reporting.repository.ImprintWorkloadRow;
import com.protrack.reporting.repository.ReportSnapshotRepository;
import com.protrack.reporting.repository.ThroughputRow;
import com.protrack.reporting.web.dto.ImprintWorkloadResponse;
import com.protrack.reporting.web.dto.ReportOverviewResponse;
import com.protrack.reporting.web.dto.ThroughputResponse;
import com.protrack.shared.error.ApiException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * Computes the Reports KPIs as LIVE aggregates over the operational tables (the endpoints always
 * reflect current data). The same org-scoped compute methods are reused by
 * {@link ReportSnapshotJob} to persist periodic history. Org scoping goes through the identity SPI —
 * reporting never reaches into another module's entities.
 */
@Service
public class ReportService {

	static final int DEFAULT_RANGE_MONTHS = 6;
	private static final int MAX_RANGE_MONTHS = 60;

	private final ReportSnapshotRepository repository;
	private final IdentityFacade identityFacade;

	public ReportService(ReportSnapshotRepository repository, IdentityFacade identityFacade) {
		this.repository = repository;
		this.identityFacade = identityFacade;
	}

	// ── user-facing (resolve the caller's organization) ─────────────────────────

	@Transactional(readOnly = true)
	public ReportOverviewResponse overview(UUID currentUserId, String range) {
		return computeOverview(organizationOf(currentUserId), range);
	}

	@Transactional(readOnly = true)
	public ThroughputResponse throughput(UUID currentUserId, String range) {
		return computeThroughput(organizationOf(currentUserId), range);
	}

	@Transactional(readOnly = true)
	public ImprintWorkloadResponse workloadByImprint(UUID currentUserId) {
		return computeWorkload(organizationOf(currentUserId));
	}

	// ── org-scoped compute (reused by the snapshot job) ─────────────────────────

	public ReportOverviewResponse computeOverview(UUID organizationId, String range) {
		Window window = parseWindow(range);
		Instant since = window.since();

		Double turnaround = round1(repository.avgTurnaroundDays(organizationId, since));

		long completedWithDue = repository.countCompletedWithDueDate(organizationId, since);
		long onTime = repository.countOnTime(organizationId, since);
		Double onTimePct = completedWithDue > 0
				? round1(100.0 * onTime / completedWithDue) : null;

		Double avgConfidence = round1(repository.avgAiConfidence(organizationId, since));

		long qaSignoffs = repository.countQaSignoffs(organizationId, since);
		long qaApproved = repository.countQaApproved(organizationId, since);
		Double qaPassPct = qaSignoffs > 0 ? round1(100.0 * qaApproved / qaSignoffs) : null;

		long completed = repository.countCompleted(organizationId, since);

		return new ReportOverviewResponse(window.label(), window.periodStart(), window.periodEnd(),
				turnaround, onTimePct, avgConfidence, qaPassPct, completed, qaSignoffs);
	}

	public ThroughputResponse computeThroughput(UUID organizationId, String range) {
		Window window = parseWindow(range);
		Map<String, Long> counts = repository.throughputByMonth(organizationId, window.since())
				.stream().collect(Collectors.toMap(ThroughputRow::getMonth, ThroughputRow::getCompleted));

		// Emit one point per month across the window so gaps render as zero bars.
		List<ThroughputResponse.Point> points = window.months().stream()
				.map(month -> new ThroughputResponse.Point(month, counts.getOrDefault(month, 0L)))
				.toList();
		return new ThroughputResponse(window.label(), points);
	}

	public ImprintWorkloadResponse computeWorkload(UUID organizationId) {
		List<ImprintWorkloadRow> rows = repository.workloadByImprint(organizationId);
		long total = rows.stream().mapToLong(ImprintWorkloadRow::getActiveProjects).sum();
		List<ImprintWorkloadResponse.Item> items = rows.stream()
				.map(row -> new ImprintWorkloadResponse.Item(
						row.getImprintId(), row.getImprintName(), row.getActiveProjects(),
						total > 0 ? round1(100.0 * row.getActiveProjects() / total) : 0.0))
				.toList();
		return new ImprintWorkloadResponse(total, items);
	}

	// ── helpers ─────────────────────────────────────────────────────────────────

	private UUID organizationOf(UUID currentUserId) {
		return identityFacade.findOrganizationId(currentUserId)
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
						"Authenticated user no longer exists."));
	}

	/**
	 * Parse a range like {@code 6m} / {@code 12m} / {@code 1y} into a window of trailing whole
	 * months ending with the current month. Unknown/blank ranges fall back to the default.
	 */
	static Window parseWindow(String range) {
		int months = monthsFrom(range);
		YearMonth current = YearMonth.now(ZoneOffset.UTC);
		YearMonth start = current.minusMonths(months - 1L);
		LocalDate periodStart = start.atDay(1);
		LocalDate periodEnd = LocalDate.now(ZoneOffset.UTC);
		Instant since = periodStart.atStartOfDay(ZoneOffset.UTC).toInstant();

		List<String> labels = new java.util.ArrayList<>();
		for (YearMonth m = start; !m.isAfter(current); m = m.plusMonths(1)) {
			labels.add(m.toString()); // YYYY-MM
		}
		return new Window(normalizeLabel(range, months), periodStart, periodEnd, since, labels);
	}

	private static int monthsFrom(String range) {
		if (!StringUtils.hasText(range)) {
			return DEFAULT_RANGE_MONTHS;
		}
		String value = range.trim().toLowerCase();
		try {
			char unit = value.charAt(value.length() - 1);
			int amount = Integer.parseInt(value.substring(0, value.length() - 1).trim());
			int months = unit == 'y' ? amount * 12 : amount; // 'm' (or anything else) = months
			return Math.max(1, Math.min(MAX_RANGE_MONTHS, months));
		} catch (NumberFormatException | IndexOutOfBoundsException ex) {
			return DEFAULT_RANGE_MONTHS;
		}
	}

	private static String normalizeLabel(String range, int months) {
		return StringUtils.hasText(range) ? range.trim().toLowerCase() : months + "m";
	}

	private static Double round1(Double value) {
		if (value == null) {
			return null;
		}
		return Math.round(value * 10.0) / 10.0;
	}

	/** The resolved reporting window: label, period bounds, the "since" instant, and month labels. */
	record Window(String label, LocalDate periodStart, LocalDate periodEnd, Instant since,
			List<String> months) {
	}
}
