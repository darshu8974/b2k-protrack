import { apiClient } from "../../api/axios";
import type { ImprintWorkload, ReportOverview, Throughput } from "../../types/report";

export async function getOverview(range: string): Promise<ReportOverview> {
  const { data } = await apiClient.get<ReportOverview>("/reports/overview", { params: { range } });
  return data;
}

export async function getThroughput(range: string): Promise<Throughput> {
  const { data } = await apiClient.get<Throughput>("/reports/throughput", { params: { range } });
  return data;
}

export async function getWorkloadByImprint(): Promise<ImprintWorkload> {
  const { data } = await apiClient.get<ImprintWorkload>("/reports/workload-by-imprint");
  return data;
}
