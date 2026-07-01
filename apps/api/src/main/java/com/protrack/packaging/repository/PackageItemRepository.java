package com.protrack.packaging.repository;

import com.protrack.packaging.domain.PackageItem;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for {@link PackageItem}. Items normally cascade through their package. */
public interface PackageItemRepository extends JpaRepository<PackageItem, UUID> {
}
