import { Alert, Button, Card, Stack, Typography } from "@mui/material";
import { useState } from "react";

import { Can } from "../../../components/auth/Can";
import { ErrorState } from "../../../components/feedback/ErrorState";
import { LoadingState } from "../../../components/feedback/LoadingState";
import type { AppError } from "../../../types/api";
import { useIssues, usePreflight, useQcApprove, useQcReject } from "../hooks";
import { IssuesTable } from "./IssuesTable";
import { PreflightSummary } from "./PreflightSummary";

/**
 * QC_REVIEW: QC reviews the paginator's pages through the preflight issues, resolves them, then
 * either approves the project to QA sign-off or rejects it back to production.
 */
export function QcReviewPanel({ projectId }: { projectId: string }) {
  const { data: preflight, isLoading, isError, error } = usePreflight(projectId);
  const { data: issues } = useIssues(projectId);
  const approve = useQcApprove(projectId);
  const reject = useQcReject(projectId);
  const [actionError, setActionError] = useState<string | null>(null);

  const notFound = (error as unknown as AppError | null)?.status === 404;
  if (isLoading && !notFound) {
    return <LoadingState />;
  }
  if (isError && !notFound) {
    return <ErrorState message="Could not load the preflight results." />;
  }

  const highOpenCount = (issues ?? []).filter(
    (issue) => issue.severity === "HIGH" && issue.status === "OPEN",
  ).length;

  const onError = (e: unknown) =>
    setActionError((e as AppError)?.message ?? "Something went wrong.");

  return (
    <Stack spacing={2}>
      {actionError && <Alert severity="error">{actionError}</Alert>}

      {preflight ? (
        <PreflightSummary preflight={preflight} />
      ) : (
        <Card sx={{ p: 2.5 }}>
          <Typography variant="body2" color="text.secondary">
            No preflight results for this project.
          </Typography>
        </Card>
      )}

      <Card sx={{ p: 2.5 }}>
        <Typography variant="subtitle1" sx={{ mb: 1.5 }}>
          Issues
        </Typography>
        <IssuesTable projectId={projectId} issues={issues ?? []} />
      </Card>

      <Can
        roles={["QC", "ADMIN"]}
        fallback={
          <Card sx={{ p: 2.5 }}>
            <Typography variant="body2" color="text.secondary">
              QC reviews the paginator's work, then approves it to QA sign-off or rejects it back to
              production.
            </Typography>
          </Card>
        }
      >
        <Card sx={{ p: 2.5 }}>
          <Stack
            direction={{ xs: "column", sm: "row" }}
            spacing={1.5}
            justifyContent="space-between"
            alignItems={{ sm: "center" }}
          >
            <Typography variant="body2" color="text.secondary">
              {highOpenCount > 0
                ? `${highOpenCount} high-severity issue(s) still open — reject to production, or approve to QA.`
                : "Approve to send the project to QA sign-off, or reject it back to production."}
            </Typography>
            <Stack direction="row" spacing={1}>
              <Button
                variant="outlined"
                color="error"
                disabled={reject.isPending}
                onClick={() => {
                  setActionError(null);
                  reject.mutate(undefined, { onError });
                }}
              >
                {reject.isPending ? "Rejecting…" : "Reject to production"}
              </Button>
              <Button
                variant="contained"
                disabled={approve.isPending}
                onClick={() => {
                  setActionError(null);
                  approve.mutate(undefined, { onError });
                }}
              >
                {approve.isPending ? "Approving…" : "Approve to QA"}
              </Button>
            </Stack>
          </Stack>
        </Card>
      </Can>
    </Stack>
  );
}
