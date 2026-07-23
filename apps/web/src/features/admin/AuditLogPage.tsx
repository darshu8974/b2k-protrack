import DownloadIcon from "@mui/icons-material/Download";
import {
  Button,
  Card,
  Chip,
  MenuItem,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  TextField,
  Typography,
} from "@mui/material";
import { useState } from "react";

import { EmptyState } from "../../components/feedback/EmptyState";
import { ErrorState } from "../../components/feedback/ErrorState";
import { TableSkeleton } from "../../components/feedback/Skeletons";
import { useDownload } from "../../hooks/useDownload";
import { AUDIT_EVENT_LABEL, auditEventLabel } from "../../lib/labels";
import { useAuditEvents } from "../audit/hooks";

// Derived from the shared label map so a new backend event type can never silently disappear
// from this filter again (previously hardcoded to 4 of 14+ real event types).
const EVENT_TYPES = Object.keys(AUDIT_EVENT_LABEL);

export function AuditLogPage() {
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [eventType, setEventType] = useState("");
  const { download, downloading } = useDownload();

  const { data, isLoading, isError } = useAuditEvents({
    page,
    size,
    sort: "createdAt,desc",
    eventType: eventType || undefined,
  });

  function exportCsv() {
    const query = eventType ? `&eventType=${encodeURIComponent(eventType)}` : "";
    void download(`/audit-events:export?format=csv${query}`, "audit-log.csv", "audit-csv");
  }

  return (
    <Stack spacing={2}>
      <Stack direction="row" justifyContent="space-between" alignItems="center" flexWrap="wrap" useFlexGap>
        <Typography variant="h4">Audit log</Typography>
        <Button
          variant="outlined"
          startIcon={<DownloadIcon />}
          onClick={exportCsv}
          disabled={downloading === "audit-csv"}
        >
          Export CSV
        </Button>
      </Stack>

      <Card sx={{ p: 2 }}>
        <TextField
          size="small"
          select
          label="Event type"
          value={eventType}
          onChange={(e) => {
            setEventType(e.target.value);
            setPage(0);
          }}
          sx={{ minWidth: 200 }}
        >
          <MenuItem value="">All events</MenuItem>
          {EVENT_TYPES.map((t) => (
            <MenuItem key={t} value={t}>
              {auditEventLabel(t)}
            </MenuItem>
          ))}
        </TextField>
      </Card>

      <Card>
        {isError && <ErrorState message="Unable to load the audit log." />}
        {isLoading && <TableSkeleton columns={4} />}
        {data && (
          <>
            <TableContainer sx={{ overflowX: "auto" }}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>Time</TableCell>
                  <TableCell>Event</TableCell>
                  <TableCell>Summary</TableCell>
                  <TableCell>Actor</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {data.content.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={4} sx={{ borderBottom: 0 }}>
                      <EmptyState
                        title="No activity yet"
                        message="No audit events match your current filters."
                      />
                    </TableCell>
                  </TableRow>
                )}
                {data.content.map((entry) => (
                  <TableRow key={entry.id}>
                    <TableCell>{new Date(entry.createdAt).toLocaleString()}</TableCell>
                    <TableCell>
                      <Chip size="small" variant="outlined" label={auditEventLabel(entry.eventType)} />
                    </TableCell>
                    <TableCell>{entry.summary}</TableCell>
                    <TableCell>{entry.actorName ?? "—"}</TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
            </TableContainer>
            <TablePagination
              component="div"
              count={data.totalElements}
              page={data.page}
              rowsPerPage={data.size}
              rowsPerPageOptions={[20, 50, 100]}
              onPageChange={(_, next) => setPage(next)}
              onRowsPerPageChange={(e) => {
                setSize(parseInt(e.target.value, 10));
                setPage(0);
              }}
            />
          </>
        )}
      </Card>
    </Stack>
  );
}
