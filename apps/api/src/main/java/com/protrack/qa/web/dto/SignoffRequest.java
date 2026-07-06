package com.protrack.qa.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/** The QA e-signature payload: the decision, an assessed quality score, and a typed signature. */
public record SignoffRequest(
		@NotBlank String decision,
		@Min(0) @Max(100) Integer qualityScore,
		@NotBlank String signature,
		String notes) {
}
