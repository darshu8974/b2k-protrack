import AddIcon from "@mui/icons-material/Add";
import GroupIcon from "@mui/icons-material/Group";
import { AppBar, Avatar, Box, Button, IconButton, Toolbar, Tooltip, Typography } from "@mui/material";
import { useNavigate } from "react-router-dom";

import { paths } from "../../app/router/paths";
import { useAuth } from "../../features/auth/useAuth";
import { ROLE_COLORS, ROLE_LABELS } from "../../lib/constants";
import { Can } from "../auth/Can";

/** Top bar with role-gated actions; breadcrumb/search/notifications land in later sprints. */
export function TopBar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const role = user?.roles[0] ?? "PM";

  return (
    <AppBar position="sticky" color="inherit" sx={{ borderBottom: 1, borderColor: "divider" }}>
      <Toolbar sx={{ minHeight: 60, gap: 1 }}>
        <Box sx={{ flex: 1 }} />

        {/* PM-only action (project creation arrives in a later sprint). */}
        <Can roles={["PM"]}>
          <Tooltip title="Project creation arrives in a later sprint">
            <span>
              <Button variant="contained" size="small" startIcon={<AddIcon />} disabled>
                New project
              </Button>
            </span>
          </Tooltip>
        </Can>

        {/* Admin-only action. */}
        <Can roles={["ADMIN"]}>
          <Button
            variant="outlined"
            size="small"
            startIcon={<GroupIcon />}
            onClick={() => navigate(paths.adminUsers)}
          >
            Manage users
          </Button>
        </Can>

        <Box sx={{ textAlign: "right", mx: 1 }}>
          <Typography variant="body2" sx={{ fontWeight: 600, lineHeight: 1.1 }}>
            {user?.fullName ?? "—"}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            {ROLE_LABELS[role]}
          </Typography>
        </Box>
        <Tooltip title="Sign out">
          <IconButton onClick={() => void logout()}>
            <Avatar sx={{ width: 32, height: 32, bgcolor: ROLE_COLORS[role], fontSize: 14 }}>
              {user?.avatarInitials ?? "?"}
            </Avatar>
          </IconButton>
        </Tooltip>
      </Toolbar>
    </AppBar>
  );
}
