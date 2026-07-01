import {
  Card,
  Chip,
  MenuItem,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TablePagination,
  TableRow,
  TextField,
  Typography,
} from "@mui/material";
import { useState } from "react";

import { LoadingState } from "../../components/feedback/LoadingState";
import { auditEventLabel } from "../../lib/labels";
import { useAuditEvents } from "../audit/hooks";

const EVENT_TYPES = ["PROJECT_CREATED", "PROJECT_UPDATED", "MEMBERS_ASSIGNED", "STAGE_CHANGED"];

export function AuditLogPage() {
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [eventType, setEventType] = useState("");

  const { data, isLoading } = useAuditEvents({
    page,
    size,
    sort: "createdAt,desc",
    eventType: eventType || undefined,
  });

  return (
    <Stack spacing={2}>
      <Typography variant="h4">Audit log</Typography>

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
        {isLoading && <LoadingState />}
        {data && (
          <>
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
