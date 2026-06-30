package com.protrack.project.web;

import com.protrack.project.service.DashboardService;
import com.protrack.project.web.dto.DashboardResponse;
import java.security.Principal;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Dashboard summary endpoint (available to every authenticated role). */
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

	private final DashboardService dashboardService;

	public DashboardController(DashboardService dashboardService) {
		this.dashboardService = dashboardService;
	}

	@GetMapping
	public DashboardResponse get(Principal principal) {
		return dashboardService.getDashboard(UUID.fromString(principal.getName()));
	}
}
