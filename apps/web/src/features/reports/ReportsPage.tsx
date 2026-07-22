import AutoAwesomeIcon from "@mui/icons-material/AutoAwesome";
import EventAvailableOutlinedIcon from "@mui/icons-material/EventAvailableOutlined";
import FolderOpenOutlinedIcon from "@mui/icons-material/FolderOpenOutlined";
import TimerOutlinedIcon from "@mui/icons-material/TimerOutlined";
import VerifiedOutlinedIcon from "@mui/icons-material/VerifiedOutlined";
import {
  Box,
  Card,
  Skeleton,
  Stack,
  ToggleButton,
  ToggleButtonGroup,
  Typography,
} from "@mui/material";
import { useState } from "react";

import { KpiCard } from "../../components/data/KpiCard";
import { ErrorState } from "../../components/feedback/ErrorState";
import { CardSkeleton } from "../../components/feedback/Skeletons";
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
        <Stack spacing={2}>
          <Stack direction="row" spacing={2} flexWrap="wrap" useFlexGap>
            {Array.from({ length: 5 }).map((_, i) => (
              <Box key={i} sx={{ flex: 1, minWidth: 180 }}>
                <CardSkeleton height={40} />
              </Box>
            ))}
          </Stack>
          <CardSkeleton height={200} />
          <CardSkeleton height={160} />
        </Stack>
      ) : (
        <>
          {/* KPI cards */}
          <Stack direction="row" spacing={2} flexWrap="wrap" useFlexGap>
            <KpiCard
              label="Avg turnaround"
              value={days(overview.data?.turnaroundDays ?? null)}
              icon={<TimerOutlinedIcon />}
              tint="#EAF2FD"
              iconColor="#0B63CE"
            />
            <KpiCard
              label="On-time delivery"
              value={pct(overview.data?.onTimePercentage ?? null)}
              icon={<EventAvailableOutlinedIcon />}
              tint="#E7F5EC"
              iconColor="#1F9D57"
            />
            <KpiCard
              label="Avg AI confidence"
              value={pct(overview.data?.avgAiConfidence ?? null)}
              icon={<AutoAwesomeIcon />}
              tint="#EFEDFE"
              iconColor="#6D5EF0"
            />
            <KpiCard
              label="QA pass rate"
              value={pct(overview.data?.qaPassPercentage ?? null)}
              icon={<VerifiedOutlinedIcon />}
              tint="#E7F5EC"
              iconColor="#1F9D57"
            />
            <KpiCard
              label="Completed (range)"
              value={overview.data?.completedProjects ?? 0}
              icon={<FolderOpenOutlinedIcon />}
              tint="#EAF2FD"
              iconColor="#0B63CE"
            />
          </Stack>

          {/* Throughput */}
          <Card sx={{ p: 2.5 }}>
            <Typography variant="subtitle1" sx={{ mb: 1.5 }}>
              Titles completed per month
            </Typography>
            {throughput.isLoading || !throughput.data ? (
              <Skeleton variant="rounded" height={200} />
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
              <Skeleton variant="rounded" height={160} />
            ) : (
              <ImprintBars items={workload.data.items} />
            )}
          </Card>
        </>
      )}
    </Stack>
  );
}
