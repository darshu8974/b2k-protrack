package com.protrack.reporting.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A pre-computed KPI measurement for an organization: one ({@code metricKey}, {@code dimension},
 * period) value. Written periodically by {@link com.protrack.reporting.service.ReportSnapshotJob}
 * as the historical/trend store behind the Reports screen; the {@code /reports/*} endpoints serve
 * live aggregates. The organization reference is a plain UUID (no JPA association — snapshots never
 * navigate to the org aggregate).
 *
 * <p>{@code generatedAt} is set on the application clock so an upserted snapshot reflects the run
 * time in the same unit of work (the DB {@code DEFAULT now()} is a fallback).
 */
@Entity
@Table(name = "report_snapshots")
public class ReportSnapshot {

	@Id
	private UUID id;

	@Column(name = "organization_id", nullable = false)
	private UUID organizationId;

	@Column(name = "period_start", nullable = false)
	private LocalDate periodStart;

	@Column(name = "period_end", nullable = false)
	private LocalDate periodEnd;

	@Column(name = "metric_key", nullable = false)
	private String metricKey;

	@Column(nullable = false)
	private String dimension;

	@Column(name = "metric_value", nullable = false)
	private BigDecimal metricValue;

	@Column(name = "generated_at", nullable = false)
	private Instant generatedAt;

	protected ReportSnapshot() {
	}

	public ReportSnapshot(UUID id, UUID organizationId, LocalDate periodStart, LocalDate periodEnd,
			String metricKey, String dimension, BigDecimal metricValue, Instant generatedAt) {
		this.id = id;
		this.organizationId = organizationId;
		this.periodStart = periodStart;
		this.periodEnd = periodEnd;
		this.metricKey = metricKey;
		this.dimension = dimension;
		this.metricValue = metricValue;
		this.generatedAt = generatedAt;
	}

	/** Overwrite the measured value and its period/timestamp on re-capture (idempotent upsert). */
	public void update(LocalDate periodStart, LocalDate periodEnd, BigDecimal metricValue,
			Instant generatedAt) {
		this.periodStart = periodStart;
		this.periodEnd = periodEnd;
		this.metricValue = metricValue;
		this.generatedAt = generatedAt;
	}

	public UUID getId() {
		return id;
	}

	public UUID getOrganizationId() {
		return organizationId;
	}

	public LocalDate getPeriodStart() {
		return periodStart;
	}

	public LocalDate getPeriodEnd() {
		return periodEnd;
	}

	public String getMetricKey() {
		return metricKey;
	}

	public String getDimension() {
		return dimension;
	}

	public BigDecimal getMetricValue() {
		return metricValue;
	}

	public Instant getGeneratedAt() {
		return generatedAt;
	}
}
