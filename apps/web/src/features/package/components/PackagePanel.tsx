import DeleteOutlineIcon from "@mui/icons-material/DeleteOutline";
import DownloadIcon from "@mui/icons-material/Download";
import Inventory2Icon from "@mui/icons-material/Inventory2";
import {
  Alert,
  Box,
  Button,
  Card,
  Chip,
  CircularProgress,
  Divider,
  IconButton,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Tooltip,
  Typography,
} from "@mui/material";

import { useQueryClient } from "@tanstack/react-query";

import { queryKeys } from "../../../api/keys";
import { Can } from "../../../components/auth/Can";
import { ErrorState } from "../../../components/feedback/ErrorState";
import { LoadingState } from "../../../components/feedback/LoadingState";
import { useDownload } from "../../../hooks/useDownload";
import { formatBytes } from "../../../lib/format";
import { docTypeLabel, PACKAGE_STATUS_COLOR, PACKAGE_STATUS_LABEL } from "../../../lib/labels";
import type { AppError } from "../../../types/api";
import { packageDownloadUrl } from "../api";
import { useAssemblePackage, usePackage, useRemovePackageItem } from "../hooks";

interface PackagePanelProps {
  projectId: string;
}

function StatRow({ label, value }: { label: string; value: string }) {
  return (
    <Stack direction="row" justifyContent="space-between" sx={{ py: 0.25 }}>
      <Typography variant="body2" color="text.secondary">
        {label}
      </Typography>
      <Typography variant="body2" sx={{ fontWeight: 500 }}>
        {value}
      </Typography>
    </Stack>
  );
}

export function PackagePanel({ projectId }: PackagePanelProps) {
  const queryClient = useQueryClient();
  const { data: pkg, isLoading, isError, error } = usePackage(projectId);
  const assemble = useAssemblePackage(projectId);
  const removeItem = useRemovePackageItem(projectId);
  const { download, downloading } = useDownload();

  const notFound = (error as unknown as AppError | null)?.status === 404;

  if (isLoading) {
    return <LoadingState />;
  }

  if (isError && !notFound) {
    return <ErrorState message="Could not load the production package." />;
  }

  // No package assembled yet.
  if (!pkg) {
    return (
      <Card sx={{ p: 4, textAlign: "center" }}>
        <Inventory2Icon color="disabled" sx={{ fontSize: 40, mb: 1 }} />
        <Typography sx={{ mb: 0.5 }}>No production package yet</Typography>
        <Typography color="text.secondary" variant="body2" sx={{ mb: 2 }}>
          Assemble a hand-off package from the project's current document versions.
        </Typography>
        <Can
          roles={["PROJECT_MANAGER", "ADMIN"]}
          fallback={
            <Typography variant="caption" color="text.secondary">
              A project manager can assemble the package.
            </Typography>
          }
        >
          <Button
            variant="contained"
            disabled={assemble.isPending}
            onClick={() => assemble.mutate()}
          >
            {assemble.isPending ? "Assembling…" : "Assemble package"}
          </Button>
        </Can>
        {assemble.isError && (
          <Alert severity="error" sx={{ mt: 2 }}>
            {(assemble.error as unknown as AppError).message ?? "Failed to assemble."}
          </Alert>
        )}
      </Card>
    );
  }

  const handleDownload = async () => {
    await download(packageDownloadUrl(projectId), "production-package.zip", "package");
    // The download bumps the server-side count; refresh so the UI reflects it.
    queryClient.invalidateQueries({ queryKey: queryKeys.projectPackage(projectId) });
  };

  return (
    <Stack spacing={2}>
      <Card sx={{ p: 2.5 }}>
        <Stack
          direction={{ xs: "column", md: "row" }}
          spacing={2}
          justifyContent="space-between"
          alignItems={{ md: "center" }}
        >
          <Stack direction="row" spacing={1.5} alignItems="center">
            <Inventory2Icon color="primary" />
            <Box>
              <Stack direction="row" spacing={1} alignItems="center">
                <Typography variant="subtitle1">Production package</Typography>
                <Chip
                  size="small"
                  color={PACKAGE_STATUS_COLOR[pkg.status]}
                  label={PACKAGE_STATUS_LABEL[pkg.status]}
                />
              </Stack>
              <Typography variant="caption" color="text.secondary">
                {pkg.assembledByName
                  ? `Assembled by ${pkg.assembledByName}`
                  : "Assembled"}
                {pkg.assembledAt ? ` · ${new Date(pkg.assembledAt).toLocaleString()}` : ""}
              </Typography>
            </Box>
          </Stack>
          <Stack direction="row" spacing={1}>
            <Can roles={["PROJECT_MANAGER", "ADMIN"]}>
              <Button
                variant="outlined"
                disabled={assemble.isPending}
                onClick={() => assemble.mutate()}
              >
                {assemble.isPending ? "Re-assembling…" : "Re-assemble"}
              </Button>
            </Can>
            <Button
              variant="contained"
              startIcon={
                downloading === "package" ? <CircularProgress size={16} /> : <DownloadIcon />
              }
              disabled={downloading === "package" || pkg.itemCount === 0}
              onClick={handleDownload}
            >
              Download .zip
            </Button>
          </Stack>
        </Stack>

        <Divider sx={{ my: 2 }} />

        <Stack direction={{ xs: "column", sm: "row" }} spacing={4}>
          <Box sx={{ flex: 1 }}>
            <StatRow label="Items" value={String(pkg.itemCount)} />
            <StatRow label="Total size" value={formatBytes(pkg.totalSizeBytes)} />
            <StatRow label="Downloads" value={String(pkg.downloadCount)} />
          </Box>
        </Stack>
      </Card>

      <Card>
        <Box sx={{ p: 2 }}>
          <Typography variant="subtitle2">Contents</Typography>
        </Box>
        {pkg.items.length === 0 ? (
          <Box sx={{ px: 2, pb: 2 }}>
            <Typography variant="body2" color="text.secondary">
              This package has no items. Upload documents, then re-assemble.
            </Typography>
          </Box>
        ) : (
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>Label</TableCell>
                <TableCell>Type</TableCell>
                <TableCell>Size</TableCell>
                <TableCell align="right" />
              </TableRow>
            </TableHead>
            <TableBody>
              {pkg.items.map((item) => (
                <TableRow key={item.id} hover>
                  <TableCell>{item.label}</TableCell>
                  <TableCell>
                    <Chip size="small" variant="outlined" label={docTypeLabel(item.itemType)} />
                  </TableCell>
                  <TableCell>{formatBytes(item.sizeBytes)}</TableCell>
                  <TableCell align="right">
                    <Can roles={["PROJECT_MANAGER", "ADMIN"]}>
                      <Tooltip title="Remove from package">
                        <span>
                          <IconButton
                            size="small"
                            aria-label="Remove from package"
                            disabled={removeItem.isPending}
                            onClick={() => removeItem.mutate(item.id)}
                          >
                            <DeleteOutlineIcon fontSize="small" />
                          </IconButton>
                        </span>
                      </Tooltip>
                    </Can>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </Card>
    </Stack>
  );
}
