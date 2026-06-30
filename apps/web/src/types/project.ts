export type Stage =
  | "INTAKE"
  | "AI_ANALYSIS"
  | "DESIGN_PREP"
  | "IN_PRODUCTION"
  | "PDF_REVIEW"
  | "QA_SIGNOFF"
  | "COMPLETED";

export type ProjectStatus = "ACTIVE" | "ON_HOLD" | "COMPLETED" | "ARCHIVED";
export type Priority = "LOW" | "MEDIUM" | "HIGH";
export type PublicationType = "STEM_TEXTBOOK" | "MONOGRAPH" | "JOURNAL" | "REFERENCE";

export interface Imprint {
  id: string;
  name: string;
  code: string;
}

export interface WorkflowStage {
  code: Stage;
  name: string;
  sequence: number;
  description?: string;
}

export interface ProjectSummary {
  id: string;
  title: string;
  isbn?: string | null;
  publicationType: PublicationType;
  discipline?: string | null;
  imprintName?: string | null;
  currentStage: Stage;
  status: ProjectStatus;
  priority: Priority;
  dueDate?: string | null;
  ownerName?: string | null;
}

export interface ProjectMember {
  userId: string;
  fullName?: string | null;
  email?: string | null;
  avatarInitials?: string | null;
  roleInProject?: string | null;
  owner: boolean;
  matchScore?: number | null;
}

export interface ProjectDetail {
  id: string;
  title: string;
  isbn?: string | null;
  publicationType: PublicationType;
  discipline?: string | null;
  brief?: string | null;
  pageExtent?: number | null;
  trimSize?: string | null;
  priority: Priority;
  currentStage: Stage;
  status: ProjectStatus;
  dueDate?: string | null;
  createdDate?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
  imprint?: Imprint | null;
  owner?: { id: string; fullName: string; email: string } | null;
  members: ProjectMember[];
}

export interface TimelineEntry {
  fromStage?: string | null;
  toStage: string;
  triggeredRole?: string | null;
  triggeredByName?: string | null;
  note?: string | null;
  occurredAt: string;
}

export interface DashboardData {
  kpis: {
    activeProjects: number;
    inProduction: number;
    awaitingQa: number;
    completedThisMonth: number;
    totalProjects: number;
  };
  stageCounts: { stage: Stage; count: number }[];
  statusCounts: { status: ProjectStatus; count: number }[];
  recentProjects: ProjectSummary[];
  myProjects: ProjectSummary[];
}
