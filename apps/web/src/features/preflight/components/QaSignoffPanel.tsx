import { Card, Stack, Typography } from "@mui/material";

import { Can } from "../../../components/auth/Can";
import { ErrorState } from "../../../components/feedback/ErrorState";
import { LoadingState } from "../../../components/feedback/LoadingState";
import type { AppError } from "../../../types/api";
import { useIssues, usePreflight } from "../hooks";
import { IssuesTable } from "./IssuesTable";
import { PreflightSummary } from "./PreflightSummary";
import { SignoffForm } from "./SignoffForm";

/** QA_SIGNOFF: preflight results, the QC-decided issues (read-only), and the QA e-signature form. */
export function QaSignoffPanel({ projectId }: { projectId: string }) {
  const { data: preflight, isLoading, isError, error } = usePreflight(projectId);
  const { data: issues } = useIssues(projectId);

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

  return (
    <Stack spacing={2}>
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
        <IssuesTable projectId={projectId} issues={issues ?? []} canDecide={false} />
      </Card>

      <Can
        roles={["QA", "ADMIN"]}
        fallback={
          <Card sx={{ p: 2.5 }}>
            <Typography variant="body2" color="text.secondary">
              QA reviews the issues and signs off to complete the project.
            </Typography>
          </Card>
        }
      >
        <SignoffForm
          projectId={projectId}
          highOpenCount={highOpenCount}
          defaultScore={preflight?.overallScore}
        />
      </Can>
    </Stack>
  );
}
