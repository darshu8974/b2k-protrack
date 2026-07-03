import CloudUploadIcon from "@mui/icons-material/CloudUpload";
import InsertDriveFileIcon from "@mui/icons-material/InsertDriveFile";
import { Box, LinearProgress, Stack, Typography } from "@mui/material";
import { useRef, useState, type DragEvent } from "react";

import { formatBytes } from "../../lib/format";

interface DropzoneUploadProps {
  /** Accept attribute for the file input, e.g. ".docx,.pdf". */
  accept?: string;
  /** Helper text describing accepted formats. */
  hint?: string;
  /** Currently selected file (controlled). */
  file: File | null;
  onFileChange: (file: File | null) => void;
  /** Upload progress 0–100 while an upload is in flight. */
  progress?: number | null;
  disabled?: boolean;
}

/** A drag-and-drop / click-to-browse file picker with an inline upload progress bar. */
export function DropzoneUpload({
  accept,
  hint,
  file,
  onFileChange,
  progress = null,
  disabled = false,
}: DropzoneUploadProps) {
  const inputRef = useRef<HTMLInputElement>(null);
  const [dragging, setDragging] = useState(false);
  const uploading = progress != null;

  const openPicker = () => {
    if (!disabled && !uploading) {
      inputRef.current?.click();
    }
  };

  const handleDrop = (event: DragEvent<HTMLDivElement>) => {
    event.preventDefault();
    setDragging(false);
    if (disabled || uploading) {
      return;
    }
    const dropped = event.dataTransfer.files?.[0];
    if (dropped) {
      onFileChange(dropped);
    }
  };

  return (
    <Box>
      <Box
        role="button"
        tabIndex={0}
        onClick={openPicker}
        onKeyDown={(e) => (e.key === "Enter" || e.key === " ") && openPicker()}
        onDragOver={(e) => {
          e.preventDefault();
          if (!disabled && !uploading) setDragging(true);
        }}
        onDragLeave={() => setDragging(false)}
        onDrop={handleDrop}
        sx={{
          border: "2px dashed",
          borderColor: dragging ? "primary.main" : "divider",
          borderRadius: 2,
          p: 4,
          textAlign: "center",
          cursor: disabled || uploading ? "default" : "pointer",
          bgcolor: dragging ? "action.hover" : "background.default",
          transition: "border-color 120ms, background-color 120ms",
          opacity: disabled ? 0.6 : 1,
        }}
      >
        <input
          ref={inputRef}
          type="file"
          accept={accept}
          hidden
          onChange={(e) => onFileChange(e.target.files?.[0] ?? null)}
        />
        {file ? (
          <Stack spacing={0.5} alignItems="center">
            <InsertDriveFileIcon color="primary" fontSize="large" />
            <Typography variant="body2" sx={{ fontWeight: 600 }}>
              {file.name}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              {formatBytes(file.size)} · click to choose a different file
            </Typography>
          </Stack>
        ) : (
          <Stack spacing={0.5} alignItems="center">
            <CloudUploadIcon color="action" fontSize="large" />
            <Typography variant="body2">Drag &amp; drop a file here, or click to browse</Typography>
            {hint && (
              <Typography variant="caption" color="text.secondary">
                {hint}
              </Typography>
            )}
          </Stack>
        )}
      </Box>
      {uploading && (
        <Box sx={{ mt: 1.5 }}>
          <LinearProgress variant="determinate" value={progress ?? 0} />
          <Typography variant="caption" color="text.secondary">
            Uploading… {Math.round(progress ?? 0)}%
          </Typography>
        </Box>
      )}
    </Box>
  );
}
