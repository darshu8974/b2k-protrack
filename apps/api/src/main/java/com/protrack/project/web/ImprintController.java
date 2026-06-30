package com.protrack.project.web;

import com.protrack.identity.spi.IdentityFacade;
import com.protrack.project.repository.ImprintRepository;
import com.protrack.project.web.dto.ImprintResponse;
import com.protrack.shared.error.ApiException;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Read-only imprint list (reference data for the project-creation form). */
@RestController
@RequestMapping("/api/v1/imprints")
public class ImprintController {

	private final ImprintRepository imprintRepository;
	private final IdentityFacade identityFacade;

	public ImprintController(ImprintRepository imprintRepository, IdentityFacade identityFacade) {
		this.imprintRepository = imprintRepository;
		this.identityFacade = identityFacade;
	}

	@GetMapping
	public List<ImprintResponse> list(Principal principal) {
		UUID organizationId = identityFacade.findOrganizationId(UUID.fromString(principal.getName()))
				.orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED",
						"Authenticated user no longer exists."));
		return imprintRepository.findByOrganizationIdOrderByNameAsc(organizationId).stream()
				.map(imprint -> new ImprintResponse(
						imprint.getId().toString(), imprint.getName(), imprint.getCode()))
				.toList();
	}
}
