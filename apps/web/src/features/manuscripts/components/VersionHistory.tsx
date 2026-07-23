import DownloadIcon from "@mui/icons-material/Download";
import {
  Box,
  Button,
  Chip,
  CircularProgress,
  IconButton,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Tooltip,
  Typography,
} from "@mui/material";

import { Can } from "../../../components/auth/Can";
import { useToast } from "../../../components/feedback/ToastProvider";
import { useDownload } from "../../../hooks/useDownload";
import { formatBytes } from "../../../lib/format";
import type { AppError } from "../../../types/api";
import type { FileVersion } from "../../../types/files";
import { versionDownloadUrl } from "../api";
import { useDocumentVersions, useSetCurrentVersion } from "../hooks";

interface VersionHistoryProps {
  projectId: string;
  documentId: string;
}

export function VersionHistory({ projectId, documentId }: VersionHistoryProps) {
  const toast = useToast();
  const { data: versions, isLoading } = useDocumentVersions(documentId);
  const setCurrent = useSetCurrentVersion(projectId);
  const { download, downloading } = useDownload();

  if (isLoading) {
    return (
      <Box sx={{ p: 2 }}>
        <CircularProgress size={20} />
      </Box>
    );
  }
  if (!versions || versions.length === 0) {
    return (
      <Typography variant="body2" color="text.secondary" sx={{ p: 2 }}>
        No versions yet.
      </Typography>
    );
  }

  const handleDownload = (version: FileVersion) =>
    download(versionDownloadUrl(version.id), version.fileName, version.id);

  return (
    <TableContainer sx={{ overflowX: "auto" }}>
    <Table size="small">
      <TableHead>
        <TableRow>
          <TableCell>Version</TableCell>
          <TableCell>File</TableCell>
          <TableCell>Size</TableCell>
          <TableCell>Uploaded by</TableCell>
          <TableCell>When</TableCell>
          <TableCell align="right">Actions</TableCell>
        </TableRow>
      </TableHead>
      <TableBody>
        {versions.map((version) => (
          <TableRow key={version.id} hover>
            <TableCell>
              <Stack direction="row" spacing={1} alignItems="center">
                <Typography variant="body2" sx={{ fontWeight: 600 }}>
                  v{version.versionNo}
                </Typography>
                {version.current && <Chip size="small" color="success" label="Current" />}
              </Stack>
            </TableCell>
            <TableCell>
              <Typography variant="body2">{version.fileName}</Typography>
            </TableCell>
            <TableCell>{formatBytes(version.sizeBytes)}</TableCell>
            <TableCell>{version.uploadedByName ?? "—"}</TableCell>
            <TableCell>{new Date(version.createdAt).toLocaleString()}</TableCell>
            <TableCell align="right">
              <Stack direction="row" spacing={1} justifyContent="flex-end" alignItems="center">
                {!version.current && (
                  <Can roles={["PROJECT_MANAGER", "ADMIN"]}>
                    <Button
                      size="small"
                      variant="outlined"
                      disabled={setCurrent.isPending}
                      onClick={() =>
                        setCurrent.mutate(
                          { documentId, versionId: version.id },
                          {
                            onSuccess: () => toast.success(`v${version.versionNo} is now the current version.`),
                            onError: (e) =>
                              toast.error(
                                (e as unknown as AppError)?.message ?? "Could not switch versions.",
                              ),
                          },
                        )
                      }
                    >
                      Make current
                    </Button>
                  </Can>
                )}
                <Tooltip title="Download">
                  <span>
                    <IconButton
                      size="small"
                      aria-label="Download this version"
                      disabled={downloading === version.id}
                      onClick={() => handleDownload(version)}
                    >
                      {downloading === version.id ? (
                        <CircularProgress size={16} aria-label="Downloading" />
                      ) : (
                        <DownloadIcon fontSize="small" />
                      )}
                    </IconButton>
                  </span>
                </Tooltip>
              </Stack>
            </TableCell>
          </TableRow>
        ))}
      </TableBody>
    </Table>
    </TableContainer>
  );
}
