import AddIcon from "@mui/icons-material/Add";
import GroupIcon from "@mui/icons-material/Group";
import LogoutIcon from "@mui/icons-material/Logout";
import NotificationsNoneIcon from "@mui/icons-material/NotificationsNone";
import SearchIcon from "@mui/icons-material/Search";
import {
  AppBar,
  Avatar,
  Badge,
  Box,
  Button,
  ButtonBase,
  Divider,
  IconButton,
  ListItemIcon,
  Menu,
  MenuItem,
  Toolbar,
  Tooltip,
  Typography,
} from "@mui/material";
import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";

import { paths } from "../../app/router/paths";
import { useAuth } from "../../features/auth/useAuth";
import { NotificationsPanel } from "../../features/notifications/NotificationsPanel";
import { useUnreadCount } from "../../features/notifications/useUnreadCount";
import { ROLE_COLORS, ROLE_LABELS } from "../../lib/constants";
import { Can } from "../auth/Can";
import { QuickSearch } from "../search/QuickSearch";

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

/** Shared dark palette so the top bar reads as one surface with the sidebar rail. */
const BAR = {
  bg: "#16294A",
  text: "rgba(255,255,255,0.92)",
  muted: "rgba(255,255,255,0.60)",
  border: "rgba(255,255,255,0.12)",
};

/** Top bar (dark): breadcrumb, project quick-search, role-gated actions and the notifications bell. */
export function TopBar() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const role = user?.roles[0] ?? "PROJECT_MANAGER";

  const [notifAnchor, setNotifAnchor] = useState<HTMLElement | null>(null);
  const [accountAnchor, setAccountAnchor] = useState<HTMLElement | null>(null);
  const [searchOpen, setSearchOpen] = useState(false);
  const { data: unread } = useUnreadCount();
  const unreadCount = unread?.count ?? 0;

  // ⌘K / Ctrl+K opens the project quick-search from anywhere.
  useEffect(() => {
    const onKey = (e: KeyboardEvent) => {
      if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === "k") {
        e.preventDefault();
        setSearchOpen(true);
      }
    };
    window.addEventListener("keydown", onKey);
    return () => window.removeEventListener("keydown", onKey);
  }, []);

  return (
    <AppBar
      position="sticky"
      color="inherit"
      sx={{
        bgcolor: BAR.bg,
        color: BAR.text,
        backgroundImage: "none",
        borderBottom: `1px solid ${BAR.border}`,
      }}
    >
      <Toolbar sx={{ minHeight: 60, gap: 1.5 }}>
        <Typography variant="body2" sx={{ fontWeight: 500, color: BAR.muted }} noWrap>
          {breadcrumb(pathname)}
        </Typography>

        <Box sx={{ flex: 1 }} />

        {/* Global project quick-search (also opens via ⌘K / Ctrl+K). */}
        <ButtonBase
          onClick={() => setSearchOpen(true)}
          aria-label="Search projects"
          sx={{
            display: { xs: "none", md: "flex" },
            alignItems: "center",
            gap: 1,
            width: 360,
            px: 1.5,
            py: 0.75,
            borderRadius: 2,
            border: `1px solid ${BAR.border}`,
            bgcolor: "rgba(255,255,255,0.06)",
            color: BAR.muted,
            justifyContent: "flex-start",
            "&:hover": { borderColor: "rgba(255,255,255,0.3)", bgcolor: "rgba(255,255,255,0.1)" },
          }}
        >
          <SearchIcon sx={{ fontSize: 18 }} />
          <Typography variant="body2" sx={{ flex: 1, textAlign: "left" }} noWrap>
            Search projects, manuscripts…
          </Typography>
          <Box
            component="span"
            sx={{
              px: 0.75,
              py: 0.1,
              borderRadius: 1,
              border: `1px solid ${BAR.border}`,
              fontSize: 11,
              fontWeight: 600,
              lineHeight: 1.6,
            }}
          >
            ⌘K
          </Box>
        </ButtonBase>
        {searchOpen && <QuickSearch onClose={() => setSearchOpen(false)} />}

        <Box sx={{ flex: 1 }} />

        {/* Project-manager-only action (project creation arrives in a later sprint). */}
        <Can roles={["PROJECT_MANAGER"]}>
          <Tooltip title="Project creation arrives in a later sprint">
            <span>
              <Button
                variant="contained"
                size="small"
                startIcon={<AddIcon />}
                disabled
                sx={{
                  "&.Mui-disabled": {
                    bgcolor: "rgba(255,255,255,0.12)",
                    color: "rgba(255,255,255,0.5)",
                  },
                }}
              >
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
            sx={{
              color: BAR.text,
              borderColor: "rgba(255,255,255,0.28)",
              "&:hover": { borderColor: "#fff", bgcolor: "rgba(255,255,255,0.08)" },
            }}
          >
            Manage users
          </Button>
        </Can>

        <Tooltip title="Notifications">
          <IconButton
            onClick={(e) => setNotifAnchor(e.currentTarget)}
            aria-label="Notifications"
            sx={{ color: BAR.muted, "&:hover": { color: "#fff", bgcolor: "rgba(255,255,255,0.08)" } }}
          >
            <Badge badgeContent={unreadCount} color="error" max={99}>
              <NotificationsNoneIcon />
            </Badge>
          </IconButton>
        </Tooltip>
        <NotificationsPanel anchorEl={notifAnchor} onClose={() => setNotifAnchor(null)} />

        {/* Account menu: name/role + avatar open a dropdown with Sign out. */}
        <Tooltip title="Account">
          <ButtonBase
            onClick={(e) => setAccountAnchor(e.currentTarget)}
            aria-label="Account"
            aria-haspopup="menu"
            aria-expanded={Boolean(accountAnchor)}
            sx={{
              gap: 1,
              borderRadius: 2,
              px: 0.5,
              py: 0.25,
              "&:hover": { bgcolor: "rgba(255,255,255,0.08)" },
            }}
          >
            <Box sx={{ textAlign: "right", display: { xs: "none", sm: "block" } }}>
              <Typography variant="body2" sx={{ fontWeight: 600, lineHeight: 1.1, color: BAR.text }}>
                {user?.fullName ?? "—"}
              </Typography>
              <Typography variant="caption" sx={{ color: BAR.muted }}>
                {ROLE_LABELS[role]}
              </Typography>
            </Box>
            <Avatar sx={{ width: 32, height: 32, bgcolor: ROLE_COLORS[role], fontSize: 14 }}>
              {user?.avatarInitials ?? "?"}
            </Avatar>
          </ButtonBase>
        </Tooltip>
        <Menu
          anchorEl={accountAnchor}
          open={Boolean(accountAnchor)}
          onClose={() => setAccountAnchor(null)}
          anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
          transformOrigin={{ vertical: "top", horizontal: "right" }}
          slotProps={{ paper: { sx: { minWidth: 220, mt: 0.5 } } }}
        >
          <Box sx={{ px: 2, py: 1 }}>
            <Typography variant="body2" sx={{ fontWeight: 600 }} noWrap>
              {user?.fullName ?? "—"}
            </Typography>
            <Typography variant="caption" color="text.secondary" noWrap sx={{ display: "block" }}>
              {user?.email}
            </Typography>
          </Box>
          <Divider />
          <MenuItem
            onClick={() => {
              setAccountAnchor(null);
              void logout();
            }}
          >
            <ListItemIcon>
              <LogoutIcon fontSize="small" />
            </ListItemIcon>
            Sign out
          </MenuItem>
        </Menu>
      </Toolbar>
    </AppBar>
  );
}
