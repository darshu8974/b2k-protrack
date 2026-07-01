package com.protrack.packaging.repository;

import com.protrack.packaging.domain.ProductionPackage;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link ProductionPackage}. One package per project (the latest is authoritative). */
public interface PackageRepository extends JpaRepository<ProductionPackage, UUID> {

	@EntityGraph(attributePaths = "items")
	Optional<ProductionPackage> findFirstByProjectIdOrderByCreatedAtDesc(UUID projectId);
}
