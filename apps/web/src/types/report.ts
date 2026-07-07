/** Headline KPIs for the Reports screen. Numeric KPIs are null when there is no data in the range. */
export interface ReportOverview {
  range: string;
  periodStart: string;
  periodEnd: string;
  turnaroundDays: number | null;
  onTimePercentage: number | null;
  avgAiConfidence: number | null;
  qaPassPercentage: number | null;
  completedProjects: number;
  qaSignoffs: number;
}

export interface ThroughputPoint {
  month: string; // YYYY-MM
  completed: number;
}

export interface Throughput {
  range: string;
  points: ThroughputPoint[];
}

export interface ImprintWorkloadItem {
  imprintId: string;
  imprintName: string;
  activeProjects: number;
  percentage: number;
}

export interface ImprintWorkload {
  totalActive: number;
  items: ImprintWorkloadItem[];
}
