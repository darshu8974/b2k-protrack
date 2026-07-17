import DownloadIcon from "@mui/icons-material/Download";
import TaskAltIcon from "@mui/icons-material/TaskAlt";
import {
  Box,
  Button,
  Card,
  Chip,
  CircularProgress,
  Stack,
  Typography,
} from "@mui/material";
import { useQueryClient } from "@tanstack/react-query";

import { queryKeys } from "../../../api/keys";
import { Can } from "../../../components/auth/Can";
import { QualityRing } from "../../../components/data/QualityRing";
import { useDownload } from "../../../hooks/useDownload";
import { useHasRole } from "../../../hooks/useHasRole";
import { packageDownloadUrl } from "../../package/api";
import { usePackage } from "../../package/hooks";
import { useApprovals, usePreflight, useSignoffs } from "../hooks";

function StatRow({ label, value }: { label: string; value?: string | number | null }) {
  return (
    <Stack direction="row" justifyContent="space-between" sx={{ py: 0.4 }}>
      <Typography variant="body2" color="text.secondary">
        {label}
      </Typography>
      <Typography variant="body2" sx={{ fontWeight: 500, textAlign: "right", maxWidth: 320 }} noWrap>
        {value ?? "—"}
      </Typography>
    </Stack>
  );
}

/** COMPLETED: the sign-off certificate, approval history, and the deliverable package download. */
export function CompletedPanel({ projectId }: { projectId: string }) {
  const queryClient = useQueryClient();
  const canPackage = useHasRole("PAGINATOR", "PROJECT_MANAGER", "ADMIN");
  const { data: preflight } = usePreflight(projectId);
  const { data: signoffs } = useSignoffs(projectId);
  const { data: approvals } = useApprovals(projectId);
  // The package endpoints are Paginator/PM/Admin only — don't query them for QA (avoids a 403).
  const { data: pkg } = usePackage(projectId, canPackage);
  const { download, downloading } = useDownload();

  const signoff = signoffs?.[0];

  const handleDownload = async () => {
    await download(packageDownloadUrl(projectId), "production-package.zip", "package");
    queryClient.invalidateQueries({ queryKey: queryKeys.projectPackage(projectId) });
  };

  return (
    <Stack spacing={2}>
      <Card sx={{ p: 2.5 }}>
        <Stack direction={{ xs: "column", md: "row" }} spacing={3} alignItems={{ md: "center" }}>
          {preflight && <QualityRing score={preflight.overallScore} passed={preflight.passed} />}
          <Box sx={{ flex: 1 }}>
            <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 1 }}>
              <TaskAltIcon color="success" />
              <Typography variant="subtitle1">Completed</Typography>
              {signoff && (
                <Chip
                  size="small"
                  color={signoff.decision === "APPROVED" ? "success" : "warning"}
                  label={signoff.decision}
                />
              )}
            </Stack>
            {signoff ? (
              <>
                <StatRow label="Signed by" value={signoff.signedByName} />
                <StatRow label="Quality score" value={signoff.qualityScore} />
                <StatRow label="Signed at" value={new Date(signoff.createdAt).toLocaleString()} />
                <StatRow label="Signature" value={signoff.signatureHash?.slice(0, 24)} />
                {signoff.notes && <StatRow label="Notes" value={signoff.notes} />}
              </>
            ) : (
              <Typography variant="body2" color="text.secondary">
                No sign-off record found.
              </Typography>
            )}
          </Box>
        </Stack>
      </Card>

      <Card sx={{ p: 2.5 }}>
        <Typography variant="subtitle1" sx={{ mb: 1 }}>
          Deliverables
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          Download the assembled production package for this title.
        </Typography>
        <Can
          roles={["PAGINATOR", "PROJECT_MANAGER", "ADMIN"]}
          fallback={
            <Typography variant="body2" color="text.secondary">
              The production package is available to the pagination and project-management team.
            </Typography>
          }
        >
          <Button
            variant="contained"
            startIcon={
              downloading === "package" ? <CircularProgress size={16} /> : <DownloadIcon />
            }
            disabled={!pkg || pkg.itemCount === 0 || downloading === "package"}
            onClick={handleDownload}
          >
            Download production package
          </Button>
          {(!pkg || pkg.itemCount === 0) && (
            <Typography variant="caption" color="text.secondary" sx={{ display: "block", mt: 1 }}>
              Assemble the package in the Package tab to enable the download.
            </Typography>
          )}
        </Can>
      </Card>

      <Card sx={{ p: 2.5 }}>
        <Typography variant="subtitle1" sx={{ mb: 1.5 }}>
          Approval history
        </Typography>
        {approvals && approvals.length > 0 ? (
          <Stack spacing={1}>
            {approvals.map((approval) => (
              <Stack key={approval.id} direction="row" spacing={1} alignItems="baseline">
                <Chip
                  size="small"
                  color={approval.decision === "APPROVED" ? "success" : "warning"}
                  label={approval.decision}
                />
                <Typography variant="body2" sx={{ flex: 1 }}>
                  {approval.approvalType ?? approval.stageCode}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  {approval.decidedByName ?? "—"}
                  {approval.decidedRole ? ` (${approval.decidedRole})` : ""} ·{" "}
                  {new Date(approval.createdAt).toLocaleString()}
                </Typography>
              </Stack>
            ))}
          </Stack>
        ) : (
          <Typography variant="body2" color="text.secondary">
            No approvals recorded.
          </Typography>
        )}
      </Card>
    </Stack>
  );
}
