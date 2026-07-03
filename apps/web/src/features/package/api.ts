import { apiClient } from "../../api/axios";
import type { ProductionPackage } from "../../types/package";

export async function getPackage(projectId: string): Promise<ProductionPackage> {
  const { data } = await apiClient.get<ProductionPackage>(`/projects/${projectId}/package`);
  return data;
}

export async function assemblePackage(projectId: string): Promise<ProductionPackage> {
  const { data } = await apiClient.post<ProductionPackage>(`/projects/${projectId}/package`);
  return data;
}

export async function removePackageItem(projectId: string, itemId: string): Promise<void> {
  await apiClient.delete(`/projects/${projectId}/package/items/${itemId}`);
}

/** Relative URL for downloading the assembled package zip (used with useDownload). */
export function packageDownloadUrl(projectId: string): string {
  return `/projects/${projectId}/package/download`;
}
