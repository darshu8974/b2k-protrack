import { apiClient } from "../../api/axios";
import type { Page } from "../../types/api";

export interface AuditEvent {
  id: string;
  eventType: string;
  entityType: string;
  entityId?: string | null;
  summary: string;
  actorId?: string | null;
  actorName?: string | null;
  actorType: string;
  projectId?: string | null;
  metadata?: unknown;
  correlationId?: string | null;
  createdAt: string;
}

export interface AuditListParams {
  page: number;
  size: number;
  sort?: string;
  projectId?: string;
  eventType?: string;
}

export async function getProjectActivity(projectId: string): Promise<AuditEvent[]> {
  const { data } = await apiClient.get<AuditEvent[]>(`/projects/${projectId}/activity`);
  return data;
}

export async function listAuditEvents(params: AuditListParams): Promise<Page<AuditEvent>> {
  const { data } = await apiClient.get<Page<AuditEvent>>("/audit-events", {
    params: {
      page: params.page,
      size: params.size,
      sort: params.sort,
      projectId: params.projectId || undefined,
      eventType: params.eventType || undefined,
    },
  });
  return data;
}
