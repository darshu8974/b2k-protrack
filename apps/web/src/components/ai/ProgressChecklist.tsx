import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import ErrorOutlineIcon from "@mui/icons-material/ErrorOutline";
import RadioButtonUncheckedIcon from "@mui/icons-material/RadioButtonUnchecked";
import {
  Box,
  CircularProgress,
  LinearProgress,
  Stack,
  Typography,
} from "@mui/material";

import type { JobStatus } from "../../types/analysis";

interface ProgressChecklistProps {
  progressPct: number;
  status: JobStatus;
  error?: string | null;
}

/** The pipeline milestones, keyed to the progress percentages the AI service reports. */
const STEPS: { label: string; at: number }[] = [
  { label: "Loading manuscript", at: 5 },
  { label: "Parsing document", at: 25 },
  { label: "Building analysis prompt", at: 40 },
  { label: "Analyzing with AI", at: 75 },
  { label: "Finalizing results", at: 95 },
];

/** Animated Scanning → Done checklist driven by the live SSE progress (with polling fallback). */
export function ProgressChecklist({ progressPct, status, error }: ProgressChecklistProps) {
  const failed = status === "FAILED";
  const activeIndex = STEPS.findIndex((step) => progressPct < step.at);

  return (
    <Box>
      <Stack spacing={1.25}>
        {STEPS.map((step, index) => {
          const done = progressPct >= step.at && !failed;
          const active = !failed && index === activeIndex;
          return (
            <Stack key={step.label} direction="row" spacing={1.5} alignItems="center">
              {failed && active ? (
                <ErrorOutlineIcon color="error" fontSize="small" />
              ) : done ? (
                <CheckCircleIcon color="success" fontSize="small" />
              ) : active ? (
                <CircularProgress size={16} />
              ) : (
                <RadioButtonUncheckedIcon color="disabled" fontSize="small" />
              )}
              <Typography
                variant="body2"
                color={done || active ? "text.primary" : "text.secondary"}
                sx={{ fontWeight: active ? 600 : 400 }}
              >
                {step.label}
              </Typography>
            </Stack>
          );
        })}
      </Stack>

      <Box sx={{ mt: 2 }}>
        <LinearProgress
          variant="determinate"
          value={Math.min(100, Math.max(0, progressPct))}
          color={failed ? "error" : "primary"}
        />
        <Typography variant="caption" color="text.secondary">
          {failed ? (error ?? "Analysis failed") : `Analyzing… ${progressPct}%`}
        </Typography>
      </Box>
    </Box>
  );
}
