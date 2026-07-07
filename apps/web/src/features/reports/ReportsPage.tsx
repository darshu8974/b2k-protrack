import {
  Box,
  Card,
  Stack,
  ToggleButton,
  ToggleButtonGroup,
  Typography,
} from "@mui/material";
import { useState } from "react";

import { KpiCard } from "../../components/data/KpiCard";
import { ErrorState } from "../../components/feedback/ErrorState";
import { LoadingState } from "../../components/feedback/LoadingState";
import { ImprintBars } from "./components/ImprintBars";
import { ThroughputChart } from "./components/ThroughputChart";
import { useReportOverview, useReportThroughput, useReportWorkload } from "./hooks";

const RANGES = [
  { value: "3m", label: "3 months" },
  { value: "6m", label: "6 months" },
  { value: "12m", label: "12 months" },
];

function pct(value: number | null): string {
  return value == null ? "—" : `${value}%`;
}

function days(value: number | null): string {
  return value == null ? "—" : `${value} d`;
}

export function ReportsPage() {
  const [range, setRange] = useState("6m");

  const overview = useReportOverview(range);
  const throughput = useReportThroughput(range);
  const workload = useReportWorkload();

  const isError = overview.isError || throughput.isError || workload.isError;

  return (
    <Stack spacing={3}>
      <Stack direction="row" justifyContent="space-between" alignItems="center" flexWrap="wrap" useFlexGap>
        <Box>
          <Typography variant="h4">Reports</Typography>
          <Typography variant="body2" color="text.secondary">
            Production KPIs and throughput across your organization.
          </Typography>
        </Box>
        <ToggleButtonGroup
          size="small"
          exclusive
          value={range}
          onChange={(_, value) => value && setRange(value)}
        >
          {RANGES.map((r) => (
            <ToggleButton key={r.value} value={r.value}>
              {r.label}
            </ToggleButton>
          ))}
        </ToggleButtonGroup>
      </Stack>

      {isError ? (
        <ErrorState message="Unable to load reports." />
      ) : overview.isLoading ? (
        <LoadingState />
      ) : (
        <>
          {/* KPI cards */}
          <Stack direction="row" spacing={2} flexWrap="wrap" useFlexGap>
            <KpiCard label="Avg turnaround" value={days(overview.data?.turnaroundDays ?? null)} />
            <KpiCard label="On-time delivery" value={pct(overview.data?.onTimePercentage ?? null)} />
            <KpiCard label="Avg AI confidence" value={pct(overview.data?.avgAiConfidence ?? null)} />
            <KpiCard label="QA pass rate" value={pct(overview.data?.qaPassPercentage ?? null)} />
            <KpiCard label="Completed (range)" value={overview.data?.completedProjects ?? 0} />
          </Stack>

          {/* Throughput */}
          <Card sx={{ p: 2.5 }}>
            <Typography variant="subtitle1" sx={{ mb: 1.5 }}>
              Titles completed per month
            </Typography>
            {throughput.isLoading || !throughput.data ? (
              <LoadingState />
            ) : (
              <ThroughputChart points={throughput.data.points} />
            )}
          </Card>

          {/* Workload by imprint */}
          <Card sx={{ p: 2.5 }}>
            <Typography variant="subtitle1" sx={{ mb: 0.5 }}>
              Active projects by imprint
            </Typography>
            <Typography variant="caption" color="text.secondary" sx={{ display: "block", mb: 2 }}>
              {workload.data?.totalActive ?? 0} active projects
            </Typography>
            {workload.isLoading || !workload.data ? (
              <LoadingState />
            ) : (
              <ImprintBars items={workload.data.items} />
            )}
          </Card>
        </>
      )}
    </Stack>
  );
}
