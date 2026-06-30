import {
  Chip,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
} from "@mui/material";
import { useNavigate } from "react-router-dom";

import {
  PRIORITY_COLOR,
  PRIORITY_LABEL,
  STAGE_LABEL,
  STATUS_COLOR,
  STATUS_LABEL,
} from "../../lib/labels";
import { paths } from "../../app/router/paths";
import type { ProjectSummary } from "../../types/project";

/** Reusable, clickable table of project summaries. */
export function ProjectsTable({ projects }: { projects: ProjectSummary[] }) {
  const navigate = useNavigate();

  if (projects.length === 0) {
    return (
      <Typography variant="body2" color="text.secondary" sx={{ p: 2 }}>
        No projects.
      </Typography>
    );
  }

  return (
    <Table size="small">
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
              <Chip size="small" label={STAGE_LABEL[project.currentStage]} />
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
  );
}
