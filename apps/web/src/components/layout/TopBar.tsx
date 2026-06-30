import { AppBar, Avatar, Box, IconButton, Toolbar, Tooltip, Typography } from "@mui/material";

import { useAuth } from "../../features/auth/useAuth";
import { ROLE_COLORS, ROLE_LABELS } from "../../lib/constants";

/** Top bar: breadcrumb/search/notifications land in later sprints; user menu + logout now. */
export function TopBar() {
  const { user, logout } = useAuth();
  const role = user?.roles[0] ?? "PM";

  return (
    <AppBar
      position="sticky"
      color="inherit"
      sx={{ borderBottom: 1, borderColor: "divider" }}
    >
      <Toolbar sx={{ minHeight: 60, gap: 1 }}>
        <Box sx={{ flex: 1 }} />
        <Box sx={{ textAlign: "right", mr: 1 }}>
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
