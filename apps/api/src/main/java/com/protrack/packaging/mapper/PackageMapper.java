package com.protrack.packaging.mapper;

import com.protrack.packaging.domain.PackageItem;
import com.protrack.packaging.domain.ProductionPackage;
import com.protrack.packaging.web.dto.PackageItemResponse;
import com.protrack.packaging.web.dto.PackageResponse;
import org.springframework.stereotype.Component;

/** Maps packaging entities to response DTOs (the assembler's display name is supplied by the service). */
@Component
public class PackageMapper {

	public PackageResponse toResponse(ProductionPackage pkg, String assembledByName) {
		return new PackageResponse(
				pkg.getId().toString(),
				pkg.getProjectId().toString(),
				pkg.getStatus(),
				pkg.getTotalSizeBytes(),
				pkg.getItemCount(),
				pkg.getDownloadCount(),
				pkg.getAssembledAt(),
				pkg.getAssembledBy() == null ? null : pkg.getAssembledBy().toString(),
				assembledByName,
				pkg.getCreatedAt(),
				pkg.getUpdatedAt(),
				pkg.getItems().stream().map(PackageMapper::toItemResponse).toList());
	}

	private static PackageItemResponse toItemResponse(PackageItem item) {
		return new PackageItemResponse(
				item.getId().toString(),
				item.getDocumentId() == null ? null : item.getDocumentId().toString(),
				item.getItemType(),
				item.getLabel(),
				item.getSizeBytes(),
				item.getSortOrder());
	}
}
