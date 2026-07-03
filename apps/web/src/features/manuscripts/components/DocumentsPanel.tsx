import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import UploadFileIcon from "@mui/icons-material/UploadFile";
import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Box,
  Button,
  Card,
  Chip,
  Stack,
  Typography,
} from "@mui/material";
import { useState } from "react";

import { Can } from "../../../components/auth/Can";
import { ErrorState } from "../../../components/feedback/ErrorState";
import { LoadingState } from "../../../components/feedback/LoadingState";
import { docTypeLabel } from "../../../lib/labels";
import { formatBytes } from "../../../lib/format";
import type { DocType, DocumentSummary } from "../../../types/files";
import { useDocuments } from "../hooks";
import { UploadDialog } from "./UploadDialog";
import { VersionHistory } from "./VersionHistory";

interface DialogState {
  mode: "document" | "version";
  documentId?: string;
  versionDocType?: DocType;
}

interface DocumentsPanelProps {
  projectId: string;
}

export function DocumentsPanel({ projectId }: DocumentsPanelProps) {
  const { data: documents, isLoading, isError } = useDocuments(projectId);
  const [dialog, setDialog] = useState<DialogState | null>(null);

  return (
    <Stack spacing={2}>
      <Stack direction="row" alignItems="center" justifyContent="space-between">
        <Typography variant="subtitle1">Documents</Typography>
        <Can roles={["PM", "DESIGNER", "ADMIN"]}>
          <Button
            variant="contained"
            startIcon={<UploadFileIcon />}
            onClick={() => setDialog({ mode: "document" })}
          >
            Upload document
          </Button>
        </Can>
      </Stack>

      {isLoading && <LoadingState />}
      {isError && <ErrorState message="Could not load documents." />}

      {documents && documents.length === 0 && (
        <Card sx={{ p: 4, textAlign: "center" }}>
          <Typography color="text.secondary">
            No documents yet. Upload a manuscript to get started.
          </Typography>
        </Card>
      )}

      {documents?.map((doc) => (
        <DocumentRow
          key={doc.id}
          doc={doc}
          projectId={projectId}
          onUploadVersion={() =>
            setDialog({
              mode: "version",
              documentId: doc.id,
              versionDocType: doc.docType as DocType,
            })
          }
        />
      ))}

      {dialog && (
        <UploadDialog
          open
          onClose={() => setDialog(null)}
          projectId={projectId}
          mode={dialog.mode}
          documentId={dialog.documentId}
          versionDocType={dialog.versionDocType}
        />
      )}
    </Stack>
  );
}

function DocumentRow({
  doc,
  projectId,
  onUploadVersion,
}: {
  doc: DocumentSummary;
  projectId: string;
  onUploadVersion: () => void;
}) {
  const current = doc.currentVersion;
  return (
    <Accordion disableGutters>
      <AccordionSummary expandIcon={<ExpandMoreIcon />}>
        <Stack
          direction={{ xs: "column", sm: "row" }}
          spacing={{ xs: 0.5, sm: 2 }}
          alignItems={{ sm: "center" }}
          sx={{ width: "100%", pr: 2 }}
        >
          <Chip size="small" label={docTypeLabel(doc.docType)} />
          <Typography variant="body2" sx={{ fontWeight: 600, flex: 1 }}>
            {doc.title ?? current?.fileName ?? "Untitled document"}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            {current ? `v${current.versionNo} · ${current.fileName} · ${formatBytes(current.sizeBytes)}` : "No versions"}
          </Typography>
          <Chip size="small" variant="outlined" label={`${doc.versionCount} version(s)`} />
        </Stack>
      </AccordionSummary>
      <AccordionDetails sx={{ bgcolor: "background.default" }}>
        <Stack spacing={1.5}>
          <Box sx={{ display: "flex", justifyContent: "flex-end" }}>
            <Can roles={["PM", "DESIGNER", "ADMIN"]}>
              <Button size="small" startIcon={<UploadFileIcon />} onClick={onUploadVersion}>
                Upload new version
              </Button>
            </Can>
          </Box>
          <VersionHistory projectId={projectId} documentId={doc.id} />
        </Stack>
      </AccordionDetails>
    </Accordion>
  );
}
