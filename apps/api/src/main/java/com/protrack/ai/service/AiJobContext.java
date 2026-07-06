package com.protrack.ai.service;

import com.protrack.ai.domain.JobType;
import java.util.UUID;

/**
 * Immutable snapshot of a job read inside the worker's first transaction, so the external call and
 * result persistence run without holding a JPA entity across transaction boundaries.
 */
public record AiJobContext(UUID projectId, UUID inputVersionId, UUID createdBy, JobType jobType) {
}
