import AddIcon from "@mui/icons-material/Add";
import SearchOffIcon from "@mui/icons-material/SearchOff";
import {
  Alert,
  Button,
  Card,
  Chip,
  FormControlLabel,
  MenuItem,
  Stack,
  Switch,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TablePagination,
  TableRow,
  TableSortLabel,
  TextField,
  Typography,
} from "@mui/material";
import { useState } from "react";
import { useNavigate } from "react-router-dom";

import { paths } from "../../../app/router/paths";
import { Can } from "../../../components/auth/Can";
import { EmptyState } from "../../../components/feedback/EmptyState";
import { TableSkeleton } from "../../../components/feedback/Skeletons";
import { useDebounce } from "../../../hooks/useDebounce";
import {
  PRIORITY_COLOR,
  PRIORITY_LABEL,
  STAGE_LABEL,
  STATUS_COLOR,
  STATUS_LABEL,
} from "../../../lib/labels";
import type { AppError } from "../../../types/api";
import type { Priority, ProjectStatus, Stage } from "../../../types/project";
import { useWorkflowStages } from "../../reference/hooks";
import { useProjects } from "../hooks";

const STATUSES: ProjectStatus[] = ["ACTIVE", "ON_HOLD", "COMPLETED", "ARCHIVED"];
const PRIORITIES: Priority[] = ["LOW", "MEDIUM", "HIGH"];

export function ProjectsListPage() {
  const navigate = useNavigate();
  const { data: stages } = useWorkflowStages();

  const [page, setPage] = useState(0);
  const [size, setSize] = useState(10);
  const [stage, setStage] = useState<Stage | "">("");
  const [status, setStatus] = useState<ProjectStatus | "">("");
  const [priority, setPriority] = useState<Priority | "">("");
  const [mine, setMine] = useState(false);
  const [search, setSearch] = useState("");
  const [sortField, setSortField] = useState("dueDate");
  const [sortDir, setSortDir] = useState<"asc" | "desc">("asc");

  const q = useDebounce(search);

  const { data, isLoading, isError, error } = useProjects({
    page,
    size,
    sort: `${sortField},${sortDir}`,
    stage,
    status,
    priority,
    mine,
    q,
  });

  const toggleSort = (field: string) => {
    if (sortField === field) {
      setSortDir((d) => (d === "asc" ? "desc" : "asc"));
    } else {
      setSortField(field);
      setSortDir("asc");
    }
    setPage(0);
  };

  return (
    <Stack spacing={2}>
      <Stack direction="row" alignItems="center" justifyContent="space-between">
        <Typography variant="h4">Projects</Typography>
        <Can roles={["PROJECT_MANAGER", "ADMIN"]}>
          <Button variant="contained" startIcon={<AddIcon />} onClick={() => navigate(paths.projectNew)}>
            New project
          </Button>
        </Can>
      </Stack>

      {/* Filters */}
      <Card sx={{ p: 2 }}>
        <Stack direction="row" spacing={2} flexWrap="wrap" useFlexGap alignItems="center">
          <TextField
            size="small"
            label="Search"
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(0);
            }}
            sx={{ minWidth: 220 }}
          />
          <TextField
            size="small"
            select
            label="Stage"
            value={stage}
            onChange={(e) => {
              setStage(e.target.value as Stage | "");
              setPage(0);
            }}
            sx={{ minWidth: 160 }}
          >
            <MenuItem value="">All stages</MenuItem>
            {(stages ?? []).map((s) => (
              <MenuItem key={s.code} value={s.code}>
                {s.name}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            size="small"
            select
            label="Status"
            value={status}
            onChange={(e) => {
              setStatus(e.target.value as ProjectStatus | "");
              setPage(0);
            }}
            sx={{ minWidth: 140 }}
          >
            <MenuItem value="">All</MenuItem>
            {STATUSES.map((s) => (
              <MenuItem key={s} value={s}>
                {STATUS_LABEL[s]}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            size="small"
            select
            label="Priority"
            value={priority}
            onChange={(e) => {
              setPriority(e.target.value as Priority | "");
              setPage(0);
            }}
            sx={{ minWidth: 140 }}
          >
            <MenuItem value="">All</MenuItem>
            {PRIORITIES.map((p) => (
              <MenuItem key={p} value={p}>
                {PRIORITY_LABEL[p]}
              </MenuItem>
            ))}
          </TextField>
          <FormControlLabel
            control={
              <Switch
                checked={mine}
                onChange={(e) => {
                  setMine(e.target.checked);
                  setPage(0);
                }}
              />
            }
            label="Mine"
          />
        </Stack>
      </Card>

      <Card>
        {isError && <Alert severity="warning">{(error as unknown as AppError | null)?.message}</Alert>}
        {isLoading && <TableSkeleton columns={6} />}
        {data && (
          <>
            <TableContainer sx={{ overflowX: "auto" }}>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell sortDirection={sortField === "title" ? sortDir : false}>
                    <TableSortLabel
                      active={sortField === "title"}
                      direction={sortField === "title" ? sortDir : "asc"}
                      onClick={() => toggleSort("title")}
                    >
                      Project
                    </TableSortLabel>
                  </TableCell>
                  <TableCell>Stage</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>
                    <TableSortLabel
                      active={sortField === "priority"}
                      direction={sortField === "priority" ? sortDir : "asc"}
                      onClick={() => toggleSort("priority")}
                    >
                      Priority
                    </TableSortLabel>
                  </TableCell>
                  <TableCell>
                    <TableSortLabel
                      active={sortField === "dueDate"}
                      direction={sortField === "dueDate" ? sortDir : "asc"}
                      onClick={() => toggleSort("dueDate")}
                    >
                      Due
                    </TableSortLabel>
                  </TableCell>
                  <TableCell>Owner</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {data.content.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={6} sx={{ borderBottom: 0 }}>
                      <EmptyState
                        icon={<SearchOffIcon />}
                        title="No projects found"
                        message="No projects match your current filters. Try clearing them or create a new project."
                      />
                    </TableCell>
                  </TableRow>
                )}
                {data.content.map((project) => (
                  <TableRow
                    key={project.id}
                    hover
                    sx={{ cursor: "pointer" }}
                    onClick={() => navigate(paths.project(project.id))}
                  >
                    <TableCell>
                      <Typography variant="body2" sx={{ fontWeight: 600 }}>
                        {project.title}
                      </Typography>
                      {project.isbn && (
                        <Typography variant="caption" color="text.secondary">
                          ISBN {project.isbn}
                        </Typography>
                      )}
                    </TableCell>
                    <TableCell>
                      <Chip size="small" label={STAGE_LABEL[project.currentStage]} />
                    </TableCell>
                    <TableCell>
                      <Chip size="small" color={STATUS_COLOR[project.status]} label={STATUS_LABEL[project.status]} />
                    </TableCell>
                    <TableCell>
                      <Chip
                        size="small"
                        variant="outlined"
                        color={PRIORITY_COLOR[project.priority]}
                        label={PRIORITY_LABEL[project.priority]}
                      />
                    </TableCell>
                    <TableCell>{project.dueDate ?? "—"}</TableCell>
                    <TableCell>{project.ownerName ?? "—"}</TableCell>
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
              rowsPerPageOptions={[10, 20, 50]}
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
