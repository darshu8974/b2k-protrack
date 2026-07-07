package com.protrack.reporting.web;

import com.protrack.reporting.service.ReportService;
import com.protrack.reporting.web.dto.ImprintWorkloadResponse;
import com.protrack.reporting.web.dto.ReportOverviewResponse;
import com.protrack.reporting.web.dto.ThroughputResponse;
import java.security.Principal;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Reports endpoints (API Specification §3.15): headline KPIs, monthly throughput, and workload by
 * imprint. Restricted to ADMIN / PM / QA (designers excluded); values are live org-scoped aggregates.
 */
@RestController
@RequestMapping("/api/v1/reports")
@PreAuthorize("hasAnyRole('ADMIN', 'PM', 'QA')")
public class ReportController {

	private final ReportService reportService;

	public ReportController(ReportService reportService) {
		this.reportService = reportService;
	}

	@GetMapping("/overview")
	public ReportOverviewResponse overview(
			@RequestParam(required = false, defaultValue = "6m") String range, Principal principal) {
		return reportService.overview(currentUser(principal), range);
	}

	@GetMapping("/throughput")
	public ThroughputResponse throughput(
			@RequestParam(required = false, defaultValue = "6m") String range, Principal principal) {
		return reportService.throughput(currentUser(principal), range);
	}

	@GetMapping("/workload-by-imprint")
	public ImprintWorkloadResponse workloadByImprint(Principal principal) {
		return reportService.workloadByImprint(currentUser(principal));
	}

	private static UUID currentUser(Principal principal) {
		return UUID.fromString(principal.getName());
	}
}
