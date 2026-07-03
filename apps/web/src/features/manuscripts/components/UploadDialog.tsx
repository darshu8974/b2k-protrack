import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  Stack,
  TextField,
} from "@mui/material";
import { useState } from "react";

import { DropzoneUpload } from "../../../components/inputs/DropzoneUpload";
import { DOC_TYPE_LABEL } from "../../../lib/labels";
import type { AppError } from "../../../types/api";
import type { DocType } from "../../../types/files";
import { useAddVersion, useUploadDocument } from "../hooks";

const DOC_TYPES: DocType[] = [
  "MANUSCRIPT",
  "PRODUCTION_PDF",
  "STRUCTURED_XML",
  "FIGURES_MANIFEST",
  "OTHER",
];

const ACCEPT: Record<DocType, string> = {
  MANUSCRIPT: ".docx,.pdf",
  PRODUCTION_PDF: ".pdf",
  STRUCTURED_XML: ".xml",
  FIGURES_MANIFEST: ".json,.csv,.zip",
  OTHER: "",
};

const HINT: Record<DocType, string> = {
  MANUSCRIPT: "DOCX or PDF, up to 100 MB",
  PRODUCTION_PDF: "PDF, up to 100 MB",
  STRUCTURED_XML: "XML, up to 100 MB",
  FIGURES_MANIFEST: "JSON, CSV or ZIP, up to 100 MB",
  OTHER: "Any file, up to 100 MB",
};

interface UploadDialogProps {
  open: boolean;
  onClose: () => void;
  projectId: string;
  /** "document" creates a new document; "version" adds a version to an existing one. */
  mode: "document" | "version";
  /** Required in "version" mode. */
  documentId?: string;
  /** Fixes the accepted formats in "version" mode. */
  versionDocType?: DocType;
}

export function UploadDialog({
  open,
  onClose,
  projectId,
  mode,
  documentId,
  versionDocType,
}: UploadDialogProps) {
  const [file, setFile] = useState<File | null>(null);
  const [docType, setDocType] = useState<DocType>("MANUSCRIPT");
  const [title, setTitle] = useState("");
  const [progress, setProgress] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  const uploadDocument = useUploadDocument(projectId);
  const addVersion = useAddVersion(projectId);

  const effectiveType = mode === "version" ? (versionDocType ?? "OTHER") : docType;
  const busy = progress != null;

  const reset = () => {
    setFile(null);
    setDocType("MANUSCRIPT");
    setTitle("");
    setProgress(null);
    setError(null);
  };

  const close = () => {
    if (!busy) {
      reset();
      onClose();
    }
  };

  const submit = async () => {
    if (!file) {
      return;
    }
    setError(null);
    setProgress(0);
    try {
      if (mode === "version") {
        await addVersion.mutateAsync({ documentId: documentId!, file, onProgress: setProgress });
      } else {
        await uploadDocument.mutateAsync({
          file,
          docType,
          title: title.trim() || undefined,
          onProgress: setProgress,
        });
      }
      reset();
      onClose();
    } catch (err) {
      setProgress(null);
      setError((err as AppError).message ?? "Upload failed.");
    }
  };

  return (
    <Dialog open={open} onClose={close} fullWidth maxWidth="sm">
      <DialogTitle>{mode === "version" ? "Upload new version" : "Upload document"}</DialogTitle>
      <DialogContent>
        <Stack spacing={2.5} sx={{ mt: 0.5 }}>
          {error && <Alert severity="error">{error}</Alert>}

          {mode === "document" && (
            <>
              <TextField
                select
                label="Document type"
                value={docType}
                onChange={(e) => setDocType(e.target.value as DocType)}
                fullWidth
                disabled={busy}
              >
                {DOC_TYPES.map((t) => (
                  <MenuItem key={t} value={t}>
                    {DOC_TYPE_LABEL[t]}
                  </MenuItem>
                ))}
              </TextField>
              <TextField
                label="Title (optional)"
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                fullWidth
                disabled={busy}
                helperText="Defaults to the file name."
              />
            </>
          )}

          <DropzoneUpload
            accept={ACCEPT[effectiveType] || undefined}
            hint={HINT[effectiveType]}
            file={file}
            onFileChange={setFile}
            progress={progress}
            disabled={busy}
          />
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={close} disabled={busy}>
          Cancel
        </Button>
        <Button variant="contained" onClick={submit} disabled={!file || busy}>
          {busy ? "Uploading…" : "Upload"}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
