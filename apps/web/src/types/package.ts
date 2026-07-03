/** Production-package domain types (mirrors the api packaging module). */

export type PackageStatus = "DRAFT" | "ASSEMBLING" | "ASSEMBLED" | "FAILED";

export interface PackageItem {
  id: string;
  documentId?: string | null;
  itemType: string;
  label: string;
  sizeBytes?: number | null;
  sortOrder: number;
}

export interface ProductionPackage {
  id: string;
  projectId: string;
  status: PackageStatus;
  totalSizeBytes: number;
  itemCount: number;
  downloadCount: number;
  assembledAt?: string | null;
  assembledById?: string | null;
  assembledByName?: string | null;
  createdAt: string;
  updatedAt: string;
  items: PackageItem[];
}
