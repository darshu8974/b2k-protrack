import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  FormControlLabel,
  Radio,
  RadioGroup,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import { useEffect, useState } from "react";

import { DECISION_LABEL } from "../../../lib/labels";
import type { DecisionType } from "../../../types/preflight";

interface DecisionDialogProps {
  open: boolean;
  /** Number of issues the decision applies to (>1 = bulk). */
  count: number;
  title?: string;
  pending?: boolean;
  error?: string | null;
  onClose: () => void;
  onSubmit: (decision: DecisionType, comment?: string) => void;
}

const OPTIONS: DecisionType[] = ["ACCEPT_FIX", "SEND_BACK", "COMMENT"];

/** Captures a triage decision; a comment is required for SEND_BACK / COMMENT (single issues only). */
export function DecisionDialog({
  open,
  count,
  title,
  pending = false,
  error,
  onClose,
  onSubmit,
}: DecisionDialogProps) {
  const [decision, setDecision] = useState<DecisionType>("ACCEPT_FIX");
  const [comment, setComment] = useState("");
  const bulk = count > 1;
  const needsComment = !bulk && decision !== "ACCEPT_FIX";

  useEffect(() => {
    if (open) {
      setDecision("ACCEPT_FIX");
      setComment("");
    }
  }, [open]);

  const submit = () => onSubmit(decision, comment.trim() || undefined);
  const commentMissing = needsComment && comment.trim().length === 0;

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>{bulk ? `Decide ${count} issues` : (title ?? "Decide issue")}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 0.5 }}>
          {error && <Alert severity="error">{error}</Alert>}
          <FormControl>
            <RadioGroup value={decision} onChange={(e) => setDecision(e.target.value as DecisionType)}>
              {OPTIONS.map((option) => (
                <FormControlLabel
                  key={option}
                  value={option}
                  control={<Radio />}
                  label={DECISION_LABEL[option]}
                />
              ))}
            </RadioGroup>
          </FormControl>
          {bulk ? (
            <Typography variant="caption" color="text.secondary">
              A bulk decision applies to all selected issues without a per-issue comment.
            </Typography>
          ) : (
            <TextField
              label={needsComment ? "Comment (required)" : "Comment (optional)"}
              multiline
              minRows={2}
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              error={commentMissing}
              helperText={commentMissing ? "A comment is required for this decision." : " "}
            />
          )}
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={pending}>
          Cancel
        </Button>
        <Button variant="contained" onClick={submit} disabled={pending || commentMissing}>
          {pending ? "Saving…" : "Confirm"}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
