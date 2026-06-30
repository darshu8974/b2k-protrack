import { apiClient } from "../../api/axios";
import type { Page } from "../../types/api";
import type {
  Priority,
  ProjectDetail,
  ProjectStatus,
  ProjectSummary,
  PublicationType,
  Stage,
  TimelineEntry,
} from "../../types/project";

export interface ProjectListParams {
  page: number;
  size: number;
  sort?: string;
  stage?: Stage | "";
  status?: ProjectStatus | "";
  priority?: Priority | "";
  mine?: boolean;
  q?: string;
}

export interface CreateProjectBody {
  title: string;
  isbn?: string;
  imprintId: string;
  publicationType: PublicationType;
  discipline?: string;
  brief?: string;
  pageExtent?: number;
  trimSize?: string;
  priority?: Priority;
  dueDate?: string;
}

export async function listProjects(params: ProjectListParams): Promise<Page<ProjectSummary>> {
  const query: Record<string, unknown> = {
    page: params.page,
    size: params.size,
    sort: params.sort,
    mine: params.mine ? true : undefined,
    stage: params.stage || undefined,
    status: params.status || undefined,
    priority: params.priority || undefined,
    q: params.q || undefined,
  };
  const { data } = await apiClient.get<Page<ProjectSummary>>("/projects", { params: query });
  return data;
}

export async function getProject(id: string): Promise<ProjectDetail> {
  const { data } = await apiClient.get<ProjectDetail>(`/projects/${id}`);
  return data;
}

export async function getProjectTimeline(id: string): Promise<TimelineEntry[]> {
  const { data } = await apiClient.get<TimelineEntry[]>(`/projects/${id}/timeline`);
  return data;
}

export async function createProject(body: CreateProjectBody): Promise<ProjectDetail> {
  const { data } = await apiClient.post<ProjectDetail>("/projects", body);
  return data;
}
