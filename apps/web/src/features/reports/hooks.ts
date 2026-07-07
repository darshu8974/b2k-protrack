import { useQuery } from "@tanstack/react-query";

import { queryKeys } from "../../api/keys";
import { getOverview, getThroughput, getWorkloadByImprint } from "./api";

export function useReportOverview(range: string) {
  return useQuery({
    queryKey: queryKeys.reportsOverview(range),
    queryFn: () => getOverview(range),
  });
}

export function useReportThroughput(range: string) {
  return useQuery({
    queryKey: queryKeys.reportsThroughput(range),
    queryFn: () => getThroughput(range),
  });
}

export function useReportWorkload() {
  return useQuery({
    queryKey: queryKeys.reportsWorkload,
    queryFn: getWorkloadByImprint,
  });
}
