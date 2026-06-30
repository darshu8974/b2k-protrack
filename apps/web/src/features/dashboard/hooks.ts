import { useQuery } from "@tanstack/react-query";

import { apiClient } from "../../api/axios";
import { queryKeys } from "../../api/keys";
import type { DashboardData } from "../../types/project";

async function fetchDashboard(): Promise<DashboardData> {
  const { data } = await apiClient.get<DashboardData>("/dashboard");
  return data;
}

export function useDashboard() {
  return useQuery({ queryKey: queryKeys.dashboard, queryFn: fetchDashboard });
}
