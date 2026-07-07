package com.protrack.identity.web.dto;

/** Outcome of a bulk user operation: how many were changed and how many were skipped (e.g. self). */
public record BulkUserResult(int updated, int skipped) {
}
