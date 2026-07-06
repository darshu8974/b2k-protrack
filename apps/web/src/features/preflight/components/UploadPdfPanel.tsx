import UploadFileIcon from "@mui/icons-material/UploadFile";
import { Alert, Box, Button, Card, Stack, Typography } from "@mui/material";
import { useState } from "react";

import { Can } from "../../../components/auth/Can";
import { DropzoneUpload } from "../../../components/inputs/DropzoneUpload";
import { useHasRole } from "../../../hooks/useHasRole";
import type { AppError } from "../../../types/api";
import { useUploadPdf } from "../hooks";

/** IN_PRODUCTION: the designer uploads the production PDF, which advances the project to PDF_REVIEW. */
export function UploadPdfPanel({ projectId }: { projectId: string }) {
  const [file, setFile] = useState<File | null>(null);
  const [progress, setProgress] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);
  const canUpload = useHasRole("DESIGNER", "ADMIN");
  const upload = useUploadPdf(projectId);

  const submit = async () => {
    if (!file) return;
    setError(null);
    setProgress(0);
    try {
      await upload.mutateAsync({ file, onProgress: setProgress });
      setFile(null);
    } catch (err) {
      setError((err as AppError).message ?? "Upload failed.");
    } finally {
      setProgress(null);
    }
  };

  return (
    <Card sx={{ p: 3 }}>
      <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 2 }}>
        <UploadFileIcon color="primary" />
        <Typography variant="subtitle1">Upload the production PDF</Typography>
      </Stack>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Upload the final PDF produced in InDesign. Submitting it moves the project into PDF Review,
        where AI preflight runs.
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <Can
        roles={["DESIGNER", "ADMIN"]}
        fallback={
          <Alert severity="info" variant="outlined">
            The designer uploads the production PDF for this project.
          </Alert>
        }
      >
        <DropzoneUpload
          accept=".pdf,application/pdf"
          hint="PDF only"
          file={file}
          onFileChange={setFile}
          progress={progress}
          disabled={upload.isPending}
        />
        <Box sx={{ mt: 2 }}>
          <Button
            variant="contained"
            disabled={!file || upload.isPending || !canUpload}
            onClick={submit}
          >
            {upload.isPending ? "Uploading…" : "Upload & start PDF review"}
          </Button>
        </Box>
      </Can>
    </Card>
  );
}
