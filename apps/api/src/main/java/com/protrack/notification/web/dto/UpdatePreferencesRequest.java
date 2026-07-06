package com.protrack.notification.web.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/** Upsert one or more per-type channel preferences for the current user. */
public record UpdatePreferencesRequest(
		@NotEmpty @Valid List<Item> preferences) {

	/** A single per-type preference override. */
	public record Item(
			@NotNull String type,
			@NotNull Boolean inAppEnabled,
			@NotNull Boolean emailEnabled) {
	}
}
