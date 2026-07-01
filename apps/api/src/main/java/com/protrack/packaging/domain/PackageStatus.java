package com.protrack.packaging.domain;

/** Lifecycle status of a production package (matches the {@code production_packages.status} CHECK). */
public enum PackageStatus {
	DRAFT,
	ASSEMBLING,
	ASSEMBLED,
	FAILED
}
