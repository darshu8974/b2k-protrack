/**
 * Centralized query-key factory. Keeps cache invalidation consistent across the app.
 */
export const queryKeys = {
  health: ["health"] as const,
  dashboard: ["dashboard"] as const,
  imprints: ["imprints"] as const,
  workflowStages: ["workflow-stages"] as const,
  projects: (params: unknown) => ["projects", params] as const,
  project: (id: string) => ["project", id] as const,
  projectTimeline: (id: string) => ["project", id, "timeline"] as const,
  projectActivity: (id: string) => ["project", id, "activity"] as const,
  projectDocuments: (id: string, docType?: string) =>
    ["project", id, "documents", docType ?? "all"] as const,
  documentVersions: (documentId: string) => ["document", documentId, "versions"] as const,
  projectPackage: (id: string) => ["project", id, "package"] as const,
};
