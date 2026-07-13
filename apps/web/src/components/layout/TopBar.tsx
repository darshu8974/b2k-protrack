import AddIcon from "@mui/icons-material/Add";
import GroupIcon from "@mui/icons-material/Group";
import NotificationsNoneIcon from "@mui/icons-material/NotificationsNone";
import SearchIcon from "@mui/icons-material/Search";
import {
  AppBar,
  Avatar,
  Badge,
  Box,
  Button,
  IconButton,
  Toolbar,
  Tooltip,
  Typography,
} from "@mui/material";
import { useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";

import { paths } from "../../app/router/paths";
import { useAuth } from "../../features/auth/useAuth";
import { NotificationsPanel } from "../../features/notifications/NotificationsPanel";
import { useUnreadCount } from "../../features/notifications/useUnreadCount";
import { ROLE_COLORS, ROLE_LABELS } from "../../lib/constants";
import { Can } from "../auth/Can";

/** Derive a simple breadcrumb from the current path (presentational). */
function breadcrumb(pathname: string): string {
  if (pathname.startsWith("/dashboard")) return "Dashboard";
  if (pathname === paths.projectNew) return "Projects › New project";
  if (pathname.startsWith("/projects/")) return "Projects › Workspace";
  if (pathname.startsWith("/projects")) return "Projects";
  if (pathname.startsWith("/reports")) return "Reports";
  if (pathname.startsWith("/admin/users")) return "Administration › Users & roles";
  if (pathname.startsWith("/admin/audit")) return "Administration › Audit log";
  return "Protrack";
}

/** Top bar: breadcrumb, global search affordance, role-gated actions and the notifications bell. */
export function TopBar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const role = user?.roles[0] ?? "PM";

  const [notifAnchor, setNotifAnchor] = useState<HTMLElement | null>(null);
  const { data: unread } = useUnreadCount();
  const unreadCount = unread?.count ?? 0;

  return (
    <AppBar position="sticky" color="inherit" sx={{ borderBottom: 1, borderColor: "divider" }}>
      <Toolbar sx={{ minHeight: 60, gap: 1.5 }}>
        <Typography variant="body2" color="text.secondary" sx={{ fontWeight: 500 }} noWrap>
          {breadcrumb(pathname)}
        </Typography>

        <Box sx={{ flex: 1 }} />

        {/* Global search affordance (presentational, mirrors the design). */}
        <Box
          sx={{
            display: { xs: "none", md: "flex" },
            alignItems: "center",
            gap: 1,
            width: 360,
            px: 1.5,
            py: 0.75,
            borderRadius: 2,
            border: 1,
            borderColor: "divider",
            bgcolor: "background.default",
            color: "text.secondary",
          }}
        >
          <SearchIcon sx={{ fontSize: 18 }} />
          <Typography variant="body2" sx={{ flex: 1 }} noWrap>
            Search projects, manuscripts…
          </Typography>
          <Box
            sx={{
              px: 0.75,
              py: 0.1,
              borderRadius: 1,
              border: 1,
              borderColor: "divider",
              fontSize: 11,
              fontWeight: 600,
              lineHeight: 1.6,
            }}
          >
            ⌘K
          </Box>
        </Box>

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

        <Tooltip title="Notifications">
          <IconButton onClick={(e) => setNotifAnchor(e.currentTarget)} aria-label="Notifications">
            <Badge badgeContent={unreadCount} color="error" max={99}>
              <NotificationsNoneIcon />
            </Badge>
          </IconButton>
        </Tooltip>
        <NotificationsPanel anchorEl={notifAnchor} onClose={() => setNotifAnchor(null)} />

        <Box sx={{ textAlign: "right", mx: 0.5, display: { xs: "none", sm: "block" } }}>
          <Typography variant="body2" sx={{ fontWeight: 600, lineHeight: 1.1 }}>
            {user?.fullName ?? "—"}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            {ROLE_LABELS[role]}
          </Typography>
        </Box>
        <Tooltip title="Sign out">
          <IconButton onClick={() => void logout()} aria-label="Sign out">
            <Avatar sx={{ width: 32, height: 32, bgcolor: ROLE_COLORS[role], fontSize: 14 }}>
              {user?.avatarInitials ?? "?"}
            </Avatar>
          </IconButton>
        </Tooltip>
      </Toolbar>
    </AppBar>
  );
}
