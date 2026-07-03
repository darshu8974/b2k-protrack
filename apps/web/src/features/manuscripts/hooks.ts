import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { queryKeys } from "../../api/keys";
import type { DocType } from "../../types/files";
import {
  addVersion,
  listDocuments,
  listVersions,
  setCurrentVersion,
  uploadDocument,
  uploadManuscript,
  type ProgressHandler,
} from "./api";

export function useDocuments(projectId: string, docType?: string) {
  return useQuery({
    queryKey: queryKeys.projectDocuments(projectId, docType),
    queryFn: () => listDocuments(projectId, docType),
    enabled: !!projectId,
  });
}

export function useDocumentVersions(documentId: string, enabled = true) {
  return useQuery({
    queryKey: queryKeys.documentVersions(documentId),
    queryFn: () => listVersions(documentId),
    enabled: enabled && !!documentId,
  });
}

interface UploadVars {
  file: File;
  docType: DocType;
  title?: string;
  onProgress?: ProgressHandler;
}

/** Create a new document (uses the manuscript convenience endpoint for MANUSCRIPT, else generic). */
export function useUploadDocument(projectId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ file, docType, title, onProgress }: UploadVars) =>
      docType === "MANUSCRIPT"
        ? uploadManuscript(projectId, file, title, onProgress)
        : uploadDocument(projectId, docType, file, title, onProgress),
    onSuccess: () => invalidateFiles(queryClient, projectId),
  });
}

interface AddVersionVars {
  documentId: string;
  file: File;
  onProgress?: ProgressHandler;
}

export function useAddVersion(projectId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ documentId, file, onProgress }: AddVersionVars) =>
      addVersion(documentId, file, onProgress),
    onSuccess: (_data, { documentId }) => {
      invalidateFiles(queryClient, projectId);
      queryClient.invalidateQueries({ queryKey: queryKeys.documentVersions(documentId) });
    },
  });
}

export function useSetCurrentVersion(projectId: string) {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ documentId, versionId }: { documentId: string; versionId: string }) =>
      setCurrentVersion(documentId, versionId),
    onSuccess: (_data, { documentId }) => {
      invalidateFiles(queryClient, projectId);
      queryClient.invalidateQueries({ queryKey: queryKeys.documentVersions(documentId) });
    },
  });
}

function invalidateFiles(
  queryClient: ReturnType<typeof useQueryClient>,
  projectId: string,
): void {
  // Documents list is keyed by docType filter; invalidate every variant by prefix.
  queryClient.invalidateQueries({ queryKey: ["project", projectId, "documents"] });
  queryClient.invalidateQueries({ queryKey: queryKeys.projectActivity(projectId) });
  queryClient.invalidateQueries({ queryKey: queryKeys.projectPackage(projectId) });
}
