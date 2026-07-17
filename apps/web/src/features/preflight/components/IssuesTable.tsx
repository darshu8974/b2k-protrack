import {
  Alert,
  Box,
  Button,
  Checkbox,
  Chip,
  MenuItem,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from "@mui/material";
import { useMemo, useState } from "react";

import { Can } from "../../../components/auth/Can";
import {
  ISSUE_STATUS_COLOR,
  ISSUE_STATUS_LABEL,
  SEVERITY_COLOR,
  SEVERITY_LABEL,
} from "../../../lib/labels";
import type { AppError } from "../../../types/api";
import type { DecisionType, QaIssue } from "../../../types/preflight";
import { useBulkDecide, useDecideIssue } from "../hooks";
import { DecisionDialog } from "./DecisionDialog";

interface IssuesTableProps {
  projectId: string;
  issues: QaIssue[];
  /** When false (e.g. after completion), decisions are hidden and the table is read-only. */
  canDecide?: boolean;
}

const SEVERITIES = ["HIGH", "MEDIUM", "LOW"];
const STATUSES = ["OPEN", "TRIAGED", "RESOLVED", "WAIVED"];

/** QC triage table: filter by severity/status, decide per-row, and bulk-decide a selection. */
export function IssuesTable({ projectId, issues, canDecide = true }: IssuesTableProps) {
  const [severity, setSeverity] = useState("");
  const [status, setStatus] = useState("");
  const [selected, setSelected] = useState<Set<string>>(new Set());
  const [dialog, setDialog] = useState<{ ids: string[]; title?: string } | null>(null);
  const [error, setError] = useState<string | null>(null);

  const decide = useDecideIssue(projectId);
  const bulk = useBulkDecide(projectId);

  const filtered = useMemo(
    () =>
      issues.filter(
        (issue) =>
          (!severity || issue.severity === severity) && (!status || issue.status === status),
      ),
    [issues, severity, status],
  );

  const toggle = (id: string) =>
    setSelected((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });

  const allSelected = filtered.length > 0 && filtered.every((i) => selected.has(i.id));
  const toggleAll = () =>
    setSelected(allSelected ? new Set() : new Set(filtered.map((i) => i.id)));

  const submit = async (decision: DecisionType, comment?: string) => {
    if (!dialog) return;
    setError(null);
    try {
      if (dialog.ids.length === 1) {
        await decide.mutateAsync({ issueId: dialog.ids[0], decision, comment });
      } else {
        await bulk.mutateAsync({ issueIds: dialog.ids, decision });
      }
      setDialog(null);
      setSelected(new Set());
    } catch (err) {
      setError((err as AppError).message ?? "Could not record the decision.");
    }
  };

  if (issues.length === 0) {
    return (
      <Alert severity="success" variant="outlined">
        No issues were raised by preflight.
      </Alert>
    );
  }

  return (
    <Box>
      <Stack
        direction={{ xs: "column", sm: "row" }}
        spacing={1.5}
        alignItems={{ sm: "center" }}
        sx={{ mb: 2 }}
      >
        <TextField
          select
          size="small"
          label="Severity"
          value={severity}
          onChange={(e) => setSeverity(e.target.value)}
          sx={{ minWidth: 140 }}
        >
          <MenuItem value="">All</MenuItem>
          {SEVERITIES.map((s) => (
            <MenuItem key={s} value={s}>
              {SEVERITY_LABEL[s]}
            </MenuItem>
          ))}
        </TextField>
        <TextField
          select
          size="small"
          label="Status"
          value={status}
          onChange={(e) => setStatus(e.target.value)}
          sx={{ minWidth: 140 }}
        >
          <MenuItem value="">All</MenuItem>
          {STATUSES.map((s) => (
            <MenuItem key={s} value={s}>
              {ISSUE_STATUS_LABEL[s]}
            </MenuItem>
          ))}
        </TextField>
        <Box sx={{ flex: 1 }} />
        {canDecide && (
          <Can roles={["QC", "ADMIN"]}>
            <Button
              variant="outlined"
              size="small"
              disabled={selected.size === 0}
              onClick={() => setDialog({ ids: [...selected] })}
            >
              Bulk decide ({selected.size})
            </Button>
          </Can>
        )}
      </Stack>

      <Box sx={{ overflowX: "auto" }}>
        <Table size="small">
          <TableHead>
            <TableRow>
              {canDecide && (
                <TableCell padding="checkbox">
                  <Can roles={["QC", "ADMIN"]}>
                    <Checkbox
                      checked={allSelected}
                      indeterminate={selected.size > 0 && !allSelected}
                      onChange={toggleAll}
                    />
                  </Can>
                </TableCell>
              )}
              <TableCell>Severity</TableCell>
              <TableCell>Issue</TableCell>
              <TableCell>Page</TableCell>
              <TableCell>Status</TableCell>
              {canDecide && <TableCell align="right">Action</TableCell>}
            </TableRow>
          </TableHead>
          <TableBody>
            {filtered.map((issue) => (
              <TableRow key={issue.id} hover>
                {canDecide && (
                  <TableCell padding="checkbox">
                    <Can roles={["QC", "ADMIN"]}>
                      <Checkbox checked={selected.has(issue.id)} onChange={() => toggle(issue.id)} />
                    </Can>
                  </TableCell>
                )}
                <TableCell>
                  <Chip size="small" color={SEVERITY_COLOR[issue.severity]} label={SEVERITY_LABEL[issue.severity]} />
                </TableCell>
                <TableCell>
                  <Typography variant="body2" sx={{ fontWeight: 600 }}>
                    {issue.title}
                  </Typography>
                  {issue.recommendation && (
                    <Typography variant="caption" color="text.secondary">
                      {issue.recommendation}
                    </Typography>
                  )}
                  {issue.category && (
                    <Chip
                      size="small"
                      variant="outlined"
                      label={issue.category}
                      sx={{ ml: 1, height: 18 }}
                    />
                  )}
                </TableCell>
                <TableCell>
                  <Typography variant="caption" color="text.secondary">
                    {issue.pageRef ?? "—"}
                  </Typography>
                </TableCell>
                <TableCell>
                  <Chip
                    size="small"
                    color={ISSUE_STATUS_COLOR[issue.status]}
                    label={ISSUE_STATUS_LABEL[issue.status]}
                  />
                </TableCell>
                {canDecide && (
                  <TableCell align="right">
                    <Can roles={["QC", "ADMIN"]}>
                      <Button size="small" onClick={() => setDialog({ ids: [issue.id], title: issue.title })}>
                        Decide
                      </Button>
                    </Can>
                  </TableCell>
                )}
              </TableRow>
            ))}
            {filtered.length === 0 && (
              <TableRow>
                <TableCell colSpan={6}>
                  <Typography variant="body2" color="text.secondary" sx={{ py: 1 }}>
                    No issues match the current filters.
                  </Typography>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </Box>

      <DecisionDialog
        open={dialog != null}
        count={dialog?.ids.length ?? 0}
        title={dialog?.title}
        pending={decide.isPending || bulk.isPending}
        error={error}
        onClose={() => {
          setDialog(null);
          setError(null);
        }}
        onSubmit={submit}
      />
    </Box>
  );
}
