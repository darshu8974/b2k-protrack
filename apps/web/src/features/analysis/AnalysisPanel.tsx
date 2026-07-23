import AutoAwesomeIcon from "@mui/icons-material/AutoAwesome";
import {
  Alert,
  Box,
  Button,
  Card,
  Chip,
  Divider,
  Stack,
  Typography,
} from "@mui/material";
import { useState, type ReactNode } from "react";
import { useQueryClient } from "@tanstack/react-query";

import { queryKeys } from "../../api/keys";
import { Can } from "../../components/auth/Can";
import { ConfidenceChip } from "../../components/ai/ConfidenceChip";
import { ProgressChecklist } from "../../components/ai/ProgressChecklist";
import { ErrorState } from "../../components/feedback/ErrorState";
import { LoadingState } from "../../components/feedback/LoadingState";
import { useSse } from "../../hooks/useSse";
import type { AppError } from "../../types/api";
import type { AiJob } from "../../types/analysis";
import { CompositionDonut } from "./components/CompositionDonut";
import { ComplexityGauge } from "./components/ComplexityGauge";
import { Headings } from "./components/Headings";
import { MetricCards } from "./components/MetricCards";
import { Risks } from "./components/Risks";
import { SuggestedTeam } from "./components/SuggestedTeam";
import { useAiJob, useAnalysis, useStartAnalysis, useStartProduction } from "./hooks";

interface AnalysisPanelProps {
  projectId: string;
  currentStage: string;
}

function Section({ title, children }: { title: string; children: ReactNode }) {
  return (
    <Card sx={{ p: 2.5 }}>
      <Typography variant="subtitle1" sx={{ mb: 1.5 }}>
        {title}
      </Typography>
      {children}
    </Card>
  );
}

export function AnalysisPanel({ projectId, currentStage }: AnalysisPanelProps) {
  const queryClient = useQueryClient();
  const [activeJobId, setActiveJobId] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);

  const { data: analysis, isLoading, isError, error } = useAnalysis(projectId);
  const { data: job } = useAiJob(activeJobId);
  const startAnalysis = useStartAnalysis(projectId);
  const startProduction = useStartProduction(projectId);

  const jobActive = job != null && (job.status === "QUEUED" || job.status === "RUNNING");
  const jobFailed = job?.status === "FAILED";

  useSseUpdates(projectId, activeJobId, jobActive, queryClient);

  const notFound = (error as unknown as AppError | null)?.status === 404;

  const runAnalysis = async () => {
    setActionError(null);
    try {
      const started = await startAnalysis.mutateAsync();
      setActiveJobId(started.jobId);
    } catch (err) {
      setActionError((err as AppError).message ?? "Could not start analysis.");
    }
  };

  const startProductionNow = async () => {
    setActionError(null);
    try {
      await startProduction.mutateAsync();
    } catch (err) {
      setActionError((err as AppError).message ?? "Could not start production.");
    }
  };

  if (isLoading && !notFound) {
    return <LoadingState />;
  }
  if (isError && !notFound) {
    return <ErrorState message="Could not load the analysis." />;
  }

  // A job is running (or just started) — show the animated checklist.
  if (startAnalysis.isPending || jobActive || (activeJobId && jobFailed)) {
    return (
      <Card sx={{ p: 3 }}>
        <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 2 }}>
          <AutoAwesomeIcon color="primary" />
          <Typography variant="subtitle1">Analyzing manuscript</Typography>
        </Stack>
        <ProgressChecklist
          progressPct={job?.progressPct ?? 0}
          status={(job?.status as AiJob["status"]) ?? "QUEUED"}
          error={job?.errorMessage}
        />
        {jobFailed && (
          <Can roles={["PROJECT_MANAGER", "ADMIN"]}>
            <Button sx={{ mt: 2 }} variant="contained" onClick={runAnalysis}>
              Try again
            </Button>
          </Can>
        )}
      </Card>
    );
  }

  // Job succeeded but the persisted result hasn't loaded yet.
  if (activeJobId && job?.status === "SUCCEEDED" && !analysis) {
    return <LoadingState />;
  }

  // No analysis yet — offer to run it.
  if (!analysis) {
    return (
      <Card sx={{ p: 4, textAlign: "center" }}>
        <AutoAwesomeIcon color="disabled" sx={{ fontSize: 40, mb: 1 }} />
        <Typography sx={{ mb: 0.5 }}>No AI analysis yet</Typography>
        <Typography color="text.secondary" variant="body2" sx={{ mb: 2 }}>
          Run AI analysis on the current manuscript to extract structure, complexity, and risks.
        </Typography>
        {actionError && (
          <Alert severity="error" sx={{ mb: 2, textAlign: "left" }}>
            {actionError}
          </Alert>
        )}
        <Can
          roles={["PROJECT_MANAGER", "ADMIN"]}
          fallback={
            <Typography variant="caption" color="text.secondary">
              A project manager can run the analysis.
            </Typography>
          }
        >
          <Button
            variant="contained"
            startIcon={<AutoAwesomeIcon />}
            onClick={runAnalysis}
            disabled={startAnalysis.isPending}
          >
            Run AI analysis
          </Button>
        </Can>
      </Card>
    );
  }

  // Results.
  return (
    <Stack spacing={2}>
      {actionError && <Alert severity="error">{actionError}</Alert>}

      <Card sx={{ p: 2.5 }}>
        <Stack
          direction={{ xs: "column", md: "row" }}
          spacing={2}
          justifyContent="space-between"
          alignItems={{ md: "center" }}
        >
          <Box sx={{ flex: 1 }}>
            <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 1 }}>
              <Typography variant="subtitle1">AI analysis</Typography>
              <ConfidenceChip value={analysis.overallConfidence} label="confidence" />
              {analysis.language && (
                <Chip size="small" variant="outlined" label={analysis.language.toUpperCase()} />
              )}
              {analysis.estimatedWorkingDays != null && (
                <Chip
                  size="small"
                  variant="outlined"
                  label={`~${analysis.estimatedWorkingDays} working days`}
                />
              )}
            </Stack>
            {analysis.summary && <Typography variant="body2">{analysis.summary}</Typography>}
          </Box>
          <Stack direction="row" spacing={1}>
            <Can roles={["PROJECT_MANAGER", "ADMIN"]}>
              <Button variant="outlined" onClick={runAnalysis} disabled={startAnalysis.isPending}>
                Re-run
              </Button>
            </Can>
            {currentStage === "AI_ANALYSIS" && (
              <Can roles={["PROJECT_MANAGER", "ADMIN"]}>
                <Button
                  variant="contained"
                  onClick={startProductionNow}
                  disabled={startProduction.isPending}
                >
                  {startProduction.isPending ? "Starting…" : "Start Production"}
                </Button>
              </Can>
            )}
          </Stack>
        </Stack>
      </Card>

      <Section title="Metrics">
        <MetricCards metrics={analysis.metrics} />
      </Section>

      <Stack direction={{ xs: "column", md: "row" }} spacing={2} alignItems="stretch">
        <Card sx={{ p: 2.5, flex: 1 }}>
          <Typography variant="subtitle1" sx={{ mb: 1.5 }}>
            Composition
          </Typography>
          <CompositionDonut segments={analysis.composition} />
        </Card>
        <Card sx={{ p: 2.5, flex: 1 }}>
          <Typography variant="subtitle1" sx={{ mb: 1.5 }}>
            Complexity
          </Typography>
          <Stack alignItems="center">
            <ComplexityGauge score={analysis.complexityScore} label={analysis.complexityLabel} />
          </Stack>
          <Divider sx={{ my: 2 }} />
          <Typography variant="subtitle2" sx={{ mb: 1 }}>
            Headings
          </Typography>
          <Headings headings={analysis.headings} />
        </Card>
      </Stack>

      <Stack direction={{ xs: "column", md: "row" }} spacing={2} alignItems="flex-start">
        <Card sx={{ p: 2.5, flex: 1 }}>
          <Typography variant="subtitle1" sx={{ mb: 1.5 }}>
            Potential risks
          </Typography>
          <Risks risks={analysis.risks} />
        </Card>
        <Card sx={{ p: 2.5, flex: 1 }}>
          <Typography variant="subtitle1" sx={{ mb: 1.5 }}>
            Suggested team
          </Typography>
          <SuggestedTeam team={analysis.suggestedTeam} />
        </Card>
      </Stack>
    </Stack>
  );
}

/** Wires SSE events to cache updates while a job is active. */
function useSseUpdates(
  projectId: string,
  activeJobId: string | null,
  jobActive: boolean,
  queryClient: ReturnType<typeof useQueryClient>,
) {
  useSse(projectId, !!activeJobId && jobActive, {
    onProgress: (data) => {
      if (!activeJobId) return;
      queryClient.setQueryData<AiJob>(queryKeys.aiJob(activeJobId), (prev) =>
        prev
          ? {
              ...prev,
              progressPct: data.progressPct ?? prev.progressPct,
              status: (data.status as AiJob["status"]) ?? prev.status,
            }
          : prev,
      );
    },
    onCompleted: () => {
      if (activeJobId) {
        queryClient.invalidateQueries({ queryKey: queryKeys.aiJob(activeJobId) });
      }
      queryClient.invalidateQueries({ queryKey: queryKeys.analysis(projectId) });
    },
    onFailed: () => {
      if (activeJobId) {
        queryClient.invalidateQueries({ queryKey: queryKeys.aiJob(activeJobId) });
      }
    },
  });
}
