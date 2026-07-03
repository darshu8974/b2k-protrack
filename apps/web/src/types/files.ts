/** Document & file-version domain types (mirrors the api files module). */

export type DocType =
  | "MANUSCRIPT"
  | "PRODUCTION_PDF"
  | "STRUCTURED_XML"
  | "FIGURES_MANIFEST"
  | "OTHER";

export type DocumentStatus = "ACTIVE" | "ARCHIVED";

export interface FileVersion {
  id: string;
  versionNo: number;
  fileName: string;
  mimeType: string;
  sizeBytes: number;
  checksumSha256?: string | null;
  current: boolean;
  uploadedById?: string | null;
  uploadedByName?: string | null;
  createdAt: string;
}

export interface DocumentSummary {
  id: string;
  docType: string;
  title?: string | null;
  status: DocumentStatus;
  versionCount: number;
  currentVersion?: FileVersion | null;
  createdAt: string;
  updatedAt: string;
}

export interface DocumentDetail {
  id: string;
  projectId: string;
  docType: string;
  title?: string | null;
  status: DocumentStatus;
  currentVersion?: FileVersion | null;
  createdAt: string;
  updatedAt: string;
}
