import FolderOpenOutlinedIcon from "@mui/icons-material/FolderOpenOutlined";
import {
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from "@mui/material";
import type { ReactNode } from "react";
import { useNavigate } from "react-router-dom";

import { EmptyState } from "../feedback/EmptyState";
import {
  PRIORITY_COLOR,
  PRIORITY_LABEL,
  STAGE_COLOR,
  STAGE_LABEL,
  STATUS_COLOR,
  STATUS_LABEL,
} from "../../lib/labels";
import { paths } from "../../app/router/paths";
import type { ProjectSummary } from "../../types/project";

/** Reusable, clickable table of project summaries. Callers can supply a tailored empty state. */
export function ProjectsTable({
  projects,
  emptyState,
}: {
  projects: ProjectSummary[];
  emptyState?: ReactNode;
}) {
  const navigate = useNavigate();

  if (projects.length === 0) {
    return (
      <>{emptyState ?? <EmptyState icon={<FolderOpenOutlinedIcon />} message="No projects to show." />}</>
    );
  }

  return (
    <TableContainer sx={{ overflowX: "auto" }}>
      <Table
        size="small"
        sx={{
          "& thead .MuiTableCell-root": {
            fontSize: 11,
            fontWeight: 600,
            letterSpacing: 0.6,
            textTransform: "uppercase",
            color: "text.secondary",
            borderBottomColor: "divider",
          },
          "& tbody .MuiTableCell-root": { py: 1.25 },
        }}
      >
        <TableHead>
          <TableRow>
            <TableCell>Project</TableCell>
            <TableCell>Stage</TableCell>
            <TableCell>Status</TableCell>
            <TableCell>Priority</TableCell>
            <TableCell>Due</TableCell>
            <TableCell>Owner</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {projects.map((project) => (
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
                <Chip
                  size="small"
                  color={STAGE_COLOR[project.currentStage]}
                  label={STAGE_LABEL[project.currentStage]}
                />
              </TableCell>
              <TableCell>
                <Chip
                  size="small"
                  color={STATUS_COLOR[project.status]}
                  label={STATUS_LABEL[project.status]}
                />
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
  );
}
