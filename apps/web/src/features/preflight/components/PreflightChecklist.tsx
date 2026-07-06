import CancelIcon from "@mui/icons-material/Cancel";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import WarningAmberIcon from "@mui/icons-material/WarningAmber";
import {
  Box,
  Chip,
  CircularProgress,
  LinearProgress,
  Stack,
  Typography,
} from "@mui/material";

import { CHECK_KEYS, CHECK_RESULT_COLOR, checkKeyLabel } from "../../../lib/labels";
import type { CheckResult, PreflightCheck } from "../../../types/preflight";

interface PreflightChecklistProps {
  /** Final check results (present once the run completes); null while running. */
  checks?: PreflightCheck[] | null;
  running?: boolean;
  progressPct?: number;
  error?: string | null;
}

function ResultIcon({ result }: { result: CheckResult }) {
  if (result === "PASS") return <CheckCircleIcon color="success" fontSize="small" />;
  if (result === "REVIEW") return <WarningAmberIcon color="warning" fontSize="small" />;
  return <CancelIcon color="error" fontSize="small" />;
}

/**
 * The six deterministic preflight checks. While the job runs the rows animate as "scanning"; once
 * results arrive each row resolves to PASS / REVIEW / FAIL. Progress is SSE-driven (polling fallback).
 */
export function PreflightChecklist({
  checks,
  running = false,
  progressPct = 0,
  error,
}: PreflightChecklistProps) {
  const byKey = new Map((checks ?? []).map((check) => [check.key, check]));
  // Preserve the canonical order; fall back to any extra keys the backend returns.
  const keys = checks && checks.length > 0 ? checks.map((c) => c.key) : [...CHECK_KEYS];

  return (
    <Box>
      <Stack spacing={1.25}>
        {keys.map((key) => {
          const check = byKey.get(key);
          return (
            <Stack key={key} direction="row" spacing={1.5} alignItems="center">
              {check ? <ResultIcon result={check.result} /> : <CircularProgress size={16} />}
              <Box sx={{ flex: 1 }}>
                <Typography variant="body2" sx={{ fontWeight: check ? 600 : 400 }}>
                  {checkKeyLabel(key)}
                </Typography>
                {check?.detail && (
                  <Typography variant="caption" color="text.secondary">
                    {check.detail}
                  </Typography>
                )}
              </Box>
              {check && (
                <Chip size="small" color={CHECK_RESULT_COLOR[check.result]} label={check.result} />
              )}
            </Stack>
          );
        })}
      </Stack>

      {running && (
        <Box sx={{ mt: 2 }}>
          <LinearProgress
            variant="determinate"
            value={Math.min(100, Math.max(0, progressPct))}
            color={error ? "error" : "primary"}
          />
          <Typography variant="caption" color="text.secondary">
            {error ? error : `Running preflight… ${progressPct}%`}
          </Typography>
        </Box>
      )}
    </Box>
  );
}
