import { Box, Card, LinearProgress, Stack, Typography } from "@mui/material";

import { ErrorState } from "../../components/feedback/ErrorState";
import { KpiCard } from "../../components/data/KpiCard";
import { LoadingState } from "../../components/feedback/LoadingState";
import { ProjectsTable } from "../../components/data/ProjectsTable";
import { STAGE_LABEL, STATUS_LABEL } from "../../lib/labels";
import type { AppError } from "../../types/api";
import { useAuth } from "../auth/useAuth";
import { useDashboard } from "./hooks";

export function DashboardPage() {
  const { user } = useAuth();
  const { data, isLoading, isError, error } = useDashboard();
  const firstName = user?.fullName?.split(" ")[0] ?? "there";

  if (isLoading) {
    return <LoadingState />;
  }
  if (isError || !data) {
    return <ErrorState message={(error as unknown as AppError | null)?.message} />;
  }

  const maxStage = Math.max(1, ...data.stageCounts.map((s) => s.count));

  return (
    <Stack spacing={3}>
      <Box>
        <Typography variant="h4">Good morning, {firstName}</Typography>
        <Typography color="text.secondary">
          {data.kpis.totalProjects} projects across your organization.
        </Typography>
      </Box>

      {/* KPI cards */}
      <Stack direction="row" spacing={2} flexWrap="wrap" useFlexGap>
        <KpiCard label="Active projects" value={data.kpis.activeProjects} />
        <KpiCard label="In production" value={data.kpis.inProduction} />
        <KpiCard label="Awaiting QA" value={data.kpis.awaitingQa} />
        <KpiCard label="Completed (this month)" value={data.kpis.completedThisMonth} />
      </Stack>

      <Stack direction={{ xs: "column", md: "row" }} spacing={2} alignItems="stretch">
        {/* Pipeline (stage statistics) */}
        <Card sx={{ p: 2.5, flex: 2 }}>
          <Typography variant="subtitle1" sx={{ mb: 1.5 }}>
            Pipeline
          </Typography>
          <Stack spacing={1.25}>
            {data.stageCounts.map((s) => (
              <Box key={s.stage}>
                <Stack direction="row" justifyContent="space-between">
                  <Typography variant="body2">{STAGE_LABEL[s.stage]}</Typography>
                  <Typography variant="body2" sx={{ fontWeight: 600 }}>
                    {s.count}
                  </Typography>
                </Stack>
                <LinearProgress
                  variant="determinate"
                  value={(s.count / maxStage) * 100}
                  sx={{ height: 6, borderRadius: 3 }}
                />
              </Box>
            ))}
          </Stack>
        </Card>

        {/* Status statistics */}
        <Card sx={{ p: 2.5, flex: 1 }}>
          <Typography variant="subtitle1" sx={{ mb: 1.5 }}>
            By status
          </Typography>
          <Stack spacing={1}>
            {data.statusCounts.map((s) => (
              <Stack key={s.status} direction="row" justifyContent="space-between">
                <Typography variant="body2">{STATUS_LABEL[s.status]}</Typography>
                <Typography variant="body2" sx={{ fontWeight: 600 }}>
                  {s.count}
                </Typography>
              </Stack>
            ))}
          </Stack>
        </Card>
      </Stack>

      {/* Recent projects */}
      <Card sx={{ p: 1 }}>
        <Typography variant="subtitle1" sx={{ p: 1.5, pb: 0.5 }}>
          Recent projects
        </Typography>
        <ProjectsTable projects={data.recentProjects} />
      </Card>

      {/* My projects */}
      <Card sx={{ p: 1 }}>
        <Typography variant="subtitle1" sx={{ p: 1.5, pb: 0.5 }}>
          My projects
        </Typography>
        <ProjectsTable projects={data.myProjects} />
      </Card>
    </Stack>
  );
}
