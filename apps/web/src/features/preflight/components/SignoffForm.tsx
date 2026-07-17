import {
  Alert,
  Box,
  Button,
  Card,
  FormControlLabel,
  Radio,
  RadioGroup,
  Slider,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import { useState } from "react";

import type { AppError } from "../../../types/api";
import type { SignoffDecision } from "../../../types/preflight";
import { useSignoff } from "../hooks";

interface SignoffFormProps {
  projectId: string;
  /** HIGH-severity issues still untriaged — approval is blocked until this is zero. */
  highOpenCount: number;
  defaultScore?: number | null;
}

/** The formal QA e-signature: decision, quality score, and a typed signature (non-repudiation). */
export function SignoffForm({ projectId, highOpenCount, defaultScore }: SignoffFormProps) {
  const [decision, setDecision] = useState<SignoffDecision>("APPROVED");
  const [score, setScore] = useState<number>(defaultScore ?? 80);
  const [signature, setSignature] = useState("");
  const [notes, setNotes] = useState("");
  const [error, setError] = useState<string | null>(null);

  const signoff = useSignoff(projectId);

  const approveBlocked = decision === "APPROVED" && highOpenCount > 0;
  const canSubmit = signature.trim().length > 0 && !approveBlocked && !signoff.isPending;

  const submit = async () => {
    setError(null);
    try {
      await signoff.mutateAsync({
        decision,
        qualityScore: score,
        signature: signature.trim(),
        notes: notes.trim() || undefined,
      });
      // On success the project transitions (COMPLETED / IN_PRODUCTION); the panel re-renders.
    } catch (err) {
      const e = err as AppError;
      setError(
        e.status === 409
          ? (e.message ?? "All HIGH-severity issues must be triaged before sign-off.")
          : (e.message ?? "Could not record the sign-off."),
      );
    }
  };

  return (
    <Card sx={{ p: 2.5 }}>
      <Typography variant="subtitle1" sx={{ mb: 1.5 }}>
        QA sign-off
      </Typography>
      <Stack spacing={2}>
        {error && <Alert severity="error">{error}</Alert>}
        {approveBlocked && (
          <Alert severity="warning">
            Triage all {highOpenCount} HIGH-severity issue(s) before approving.
          </Alert>
        )}

        <RadioGroup
          row
          value={decision}
          onChange={(e) => setDecision(e.target.value as SignoffDecision)}
        >
          <FormControlLabel value="APPROVED" control={<Radio />} label="Approve → Completed" />
          <FormControlLabel value="REJECTED" control={<Radio />} label="Reject → send back" />
        </RadioGroup>

        <Box>
          <Typography variant="body2" color="text.secondary" gutterBottom>
            Quality score: <strong>{score}</strong> / 100
          </Typography>
          <Slider
            value={score}
            onChange={(_, value) => setScore(value as number)}
            min={0}
            max={100}
            valueLabelDisplay="auto"
          />
        </Box>

        <TextField
          label="Signature (type your full name)"
          value={signature}
          onChange={(e) => setSignature(e.target.value)}
          required
          placeholder="e.g. QA Approver"
        />
        <TextField
          label="Notes (optional)"
          value={notes}
          onChange={(e) => setNotes(e.target.value)}
          multiline
          minRows={2}
        />

        <Box>
          <Button
            variant="contained"
            color={decision === "REJECTED" ? "warning" : "primary"}
            disabled={!canSubmit}
            onClick={submit}
          >
            {signoff.isPending
              ? "Submitting…"
              : decision === "APPROVED"
                ? "Approve & e-sign"
                : "Reject & send back"}
          </Button>
        </Box>
      </Stack>
    </Card>
  );
}
