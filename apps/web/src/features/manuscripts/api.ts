import type { AxiosProgressEvent } from "axios";

import { apiClient } from "../../api/axios";
import type { DocType, DocumentDetail, DocumentSummary, FileVersion } from "../../types/files";

export type ProgressHandler = (percent: number) => void;

function progressConfig(onProgress?: ProgressHandler) {
  return {
    // Override the client's default JSON content-type; axios fills in the multipart boundary.
    headers: { "Content-Type": "multipart/form-data" },
    onUploadProgress: onProgress
      ? (event: AxiosProgressEvent) => {
          if (event.total) {
            onProgress(Math.round((event.loaded / event.total) * 100));
          }
        }
      : undefined,
  };
}

export async function listDocuments(
  projectId: string,
  docType?: string,
): Promise<DocumentSummary[]> {
  const { data } = await apiClient.get<DocumentSummary[]>(`/projects/${projectId}/documents`, {
    params: { docType: docType || undefined },
  });
  return data;
}

export async function getDocument(documentId: string): Promise<DocumentDetail> {
  const { data } = await apiClient.get<DocumentDetail>(`/documents/${documentId}`);
  return data;
}

export async function listVersions(documentId: string): Promise<FileVersion[]> {
  const { data } = await apiClient.get<FileVersion[]>(`/documents/${documentId}/versions`);
  return data;
}

export async function uploadManuscript(
  projectId: string,
  file: File,
  title?: string,
  onProgress?: ProgressHandler,
): Promise<DocumentDetail> {
  const form = new FormData();
  form.append("file", file);
  if (title) {
    form.append("title", title);
  }
  const { data } = await apiClient.post<DocumentDetail>(
    `/projects/${projectId}/manuscript`,
    form,
    progressConfig(onProgress),
  );
  return data;
}

export async function uploadDocument(
  projectId: string,
  docType: DocType,
  file: File,
  title?: string,
  onProgress?: ProgressHandler,
): Promise<DocumentDetail> {
  const form = new FormData();
  form.append("file", file);
  form.append("docType", docType);
  if (title) {
    form.append("title", title);
  }
  const { data } = await apiClient.post<DocumentDetail>(
    `/projects/${projectId}/documents`,
    form,
    progressConfig(onProgress),
  );
  return data;
}

export async function addVersion(
  documentId: string,
  file: File,
  onProgress?: ProgressHandler,
): Promise<FileVersion> {
  const form = new FormData();
  form.append("file", file);
  const { data } = await apiClient.post<FileVersion>(
    `/documents/${documentId}/versions`,
    form,
    progressConfig(onProgress),
  );
  return data;
}

export async function setCurrentVersion(
  documentId: string,
  versionId: string,
): Promise<DocumentDetail> {
  const { data } = await apiClient.post<DocumentDetail>(
    `/documents/${documentId}/versions/${versionId}:setCurrent`,
  );
  return data;
}

/** Relative URL for downloading a version's bytes (used with useDownload). */
export function versionDownloadUrl(versionId: string): string {
  return `/file-versions/${versionId}/download`;
}
