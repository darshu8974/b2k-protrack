import FactCheckIcon from "@mui/icons-material/FactCheck";
import { Alert, Button, Card, Stack, Typography } from "@mui/material";
import { useQueryClient } from "@tanstack/react-query";
import { useState } from "react";

import { queryKeys } from "../../../api/keys";
import { Can } from "../../../components/auth/Can";
import { useSse } from "../../../hooks/useSse";
import type { AppError } from "../../../types/api";
import type { AiJob } from "../../../types/analysis";
import { useAiJob } from "../../analysis/hooks";
import { usePreflight, useSendToQc, useStartPreflight } from "../hooks";
import { PreflightChecklist } from "./PreflightChecklist";
import { PreflightSummary } from "./PreflightSummary";

const ACTIVE = new Set(["QUEUED", "RUNNING"]);

/** PDF_REVIEW: run preflight (animated), show results, and send the project to QC review. */
export function PreflightPanel({ projectId }: { projectId: string }) {
  const queryClient = useQueryClient();
  const [activeJobId, setActiveJobId] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  const { data: preflight } = usePreflight(projectId);
  const { data: job } = useAiJob(activeJobId);
  const startPreflight = useStartPreflight(projectId);
  const sendToQc = useSendToQc(projectId);

  const jobActive = job != null && ACTIVE.has(job.status);
  const jobFailed = job?.status === "FAILED";

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
      if (activeJobId) queryClient.invalidateQueries({ queryKey: queryKeys.aiJob(activeJobId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.preflight(projectId) });
      queryClient.invalidateQueries({ queryKey: queryKeys.issues(projectId) });
    },
    onFailed: () => {
      if (activeJobId) queryClient.invalidateQueries({ queryKey: queryKeys.aiJob(activeJobId) });
    },
  });

  const run = async () => {
    setError(null);
    try {
      const started = await startPreflight.mutateAsync();
      setActiveJobId(started.jobId);
    } catch (err) {
      setError((err as AppError).message ?? "Could not start preflight.");
    }
  };

  const advance = async () => {
    setError(null);
    try {
      await sendToQc.mutateAsync();
    } catch (err) {
      setError((err as AppError).message ?? "Could not send to QC.");
    }
  };

  // A preflight run is in progress — show the animated checklist.
  if (startPreflight.isPending || jobActive || (activeJobId && jobFailed)) {
    return (
      <Card sx={{ p: 3 }}>
        <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 2 }}>
          <FactCheckIcon color="primary" />
          <Typography variant="subtitle1">Running PDF preflight</Typography>
        </Stack>
        <PreflightChecklist
          running={!jobFailed}
          progressPct={job?.progressPct ?? 0}
          error={jobFailed ? (job?.errorMessage ?? "Preflight failed") : null}
        />
        {jobFailed && (
          <Can roles={["QC", "PROJECT_MANAGER", "ADMIN"]}>
            <Button sx={{ mt: 2 }} variant="contained" onClick={run}>
              Try again
            </Button>
          </Can>
        )}
      </Card>
    );
  }

  // No successful run yet — offer to run preflight.
  if (!preflight) {
    return (
      <Card sx={{ p: 4, textAlign: "center" }}>
        <FactCheckIcon color="disabled" sx={{ fontSize: 40, mb: 1 }} />
        <Typography sx={{ mb: 0.5 }}>PDF uploaded — ready for preflight</Typography>
        <Typography color="text.secondary" variant="body2" sx={{ mb: 2 }}>
          Run AI preflight to check the production PDF for geometry, fonts, image resolution,
          overflow, placement, and accessibility.
        </Typography>
        {error && (
          <Alert severity="error" sx={{ mb: 2, textAlign: "left" }}>
            {error}
          </Alert>
        )}
        <Can
          roles={["QC", "PROJECT_MANAGER", "ADMIN"]}
          fallback={
            <Typography variant="caption" color="text.secondary">
              QC or a project manager can run preflight.
            </Typography>
          }
        >
          <Button variant="contained" startIcon={<FactCheckIcon />} onClick={run}>
            Run preflight
          </Button>
        </Can>
      </Card>
    );
  }

  // Results.
  return (
    <Stack spacing={2}>
      {error && <Alert severity="error">{error}</Alert>}
      <PreflightSummary preflight={preflight} />
      <Card sx={{ p: 2.5 }}>
        <Stack
          direction={{ xs: "column", sm: "row" }}
          spacing={1.5}
          justifyContent="space-between"
          alignItems={{ sm: "center" }}
        >
          <Typography variant="body2" color="text.secondary">
            Review the results, then send the project to QC review.
          </Typography>
          <Stack direction="row" spacing={1}>
            <Can roles={["QC", "PROJECT_MANAGER", "ADMIN"]}>
              <Button variant="outlined" onClick={run} disabled={startPreflight.isPending}>
                Re-run
              </Button>
            </Can>
            <Can roles={["QC", "PROJECT_MANAGER", "ADMIN"]}>
              <Button variant="contained" onClick={advance} disabled={sendToQc.isPending}>
                {sendToQc.isPending ? "Sending…" : "Send to QC"}
              </Button>
            </Can>
          </Stack>
        </Stack>
      </Card>
    </Stack>
  );
}
