import FolderOutlinedIcon from "@mui/icons-material/FolderOutlined";
import GridViewOutlinedIcon from "@mui/icons-material/GridViewOutlined";
import HistoryOutlinedIcon from "@mui/icons-material/HistoryOutlined";
import InsightsOutlinedIcon from "@mui/icons-material/InsightsOutlined";
import ManageAccountsOutlinedIcon from "@mui/icons-material/ManageAccountsOutlined";
import {
  Box,
  Drawer,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Toolbar,
  Typography,
} from "@mui/material";
import type { ReactNode } from "react";
import { useLocation, useNavigate } from "react-router-dom";

import { useAuth } from "../../features/auth/useAuth";
import { navConfig } from "../../lib/navConfig";
import { RoleSwitcher } from "./RoleSwitcher";

const DRAWER_WIDTH = 248;

/** Dark-rail palette: the sidebar is the app's dark accent against the light workspace. */
const RAIL = {
  bg: "linear-gradient(180deg, #16294A 0%, #101A2E 100%)",
  text: "rgba(255,255,255,0.92)",
  muted: "rgba(255,255,255,0.52)",
  icon: "rgba(255,255,255,0.70)",
  hover: "rgba(255,255,255,0.07)",
  selBg: "rgba(96,165,250,0.18)",
  selText: "#FFFFFF",
  selIcon: "#7FB2FF",
  border: "rgba(255,255,255,0.08)",
};

const ICONS: Record<string, ReactNode> = {
  dashboard: <GridViewOutlinedIcon fontSize="small" />,
  folder: <FolderOutlinedIcon fontSize="small" />,
  reports: <InsightsOutlinedIcon fontSize="small" />,
  users: <ManageAccountsOutlinedIcon fontSize="small" />,
  audit: <HistoryOutlinedIcon fontSize="small" />,
};

/** Fixed left navigation (dark rail); items are shaped by the active role. */
export function Sidebar() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { pathname } = useLocation();

  const role = user?.roles[0] ?? "PROJECT_MANAGER";
  const sections = navConfig[role];

  return (
    <Drawer
      variant="permanent"
      sx={{
        width: DRAWER_WIDTH,
        flexShrink: 0,
        "& .MuiDrawer-paper": {
          width: DRAWER_WIDTH,
          boxSizing: "border-box",
          border: "none",
          background: RAIL.bg,
          color: RAIL.text,
        },
      }}
    >
      {/* Brand lockup */}
      <Toolbar sx={{ minHeight: 60, gap: 1.25 }}>
        <Box
          sx={{
            width: 30,
            height: 30,
            borderRadius: 1.5,
            bgcolor: "primary.main",
            color: "common.white",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            fontWeight: 800,
            fontSize: 16,
          }}
        >
          P
        </Box>
        <Box>
          <Typography variant="subtitle1" sx={{ fontWeight: 700, lineHeight: 1.1, color: RAIL.text }}>
            Protrack
          </Typography>
          <Typography
            variant="caption"
            sx={{ color: RAIL.muted, letterSpacing: 0.6, fontSize: 9.5 }}
          >
            PUBLISHING OS
          </Typography>
        </Box>
      </Toolbar>

      <Box component="nav" aria-label="Main navigation" sx={{ overflow: "auto", flex: 1 }}>
        {sections.map((section) => (
          <Box key={section.group} sx={{ px: 1, pt: 1.5 }}>
            <Typography
              variant="caption"
              sx={{
                px: 1.5,
                color: RAIL.muted,
                fontWeight: 600,
                letterSpacing: 0.6,
                fontSize: 10.5,
              }}
            >
              {section.group}
            </Typography>
            <List dense sx={{ mt: 0.5 }}>
              {section.items.map((item, index) => {
                const selected = pathname === item.path;
                return (
                  <ListItemButton
                    key={`${item.label}-${index}`}
                    selected={selected}
                    onClick={() => navigate(item.path)}
                    sx={{
                      borderRadius: 1.5,
                      mx: 0.5,
                      py: 0.75,
                      color: RAIL.text,
                      "& .MuiListItemIcon-root": { color: RAIL.icon },
                      "&:hover": { bgcolor: RAIL.hover },
                      "&.Mui-selected": {
                        bgcolor: RAIL.selBg,
                        color: RAIL.selText,
                        "& .MuiListItemIcon-root": { color: RAIL.selIcon },
                        "&:hover": { bgcolor: RAIL.selBg },
                      },
                    }}
                  >
                    <ListItemIcon sx={{ minWidth: 34 }}>{ICONS[item.icon]}</ListItemIcon>
                    <ListItemText
                      primary={item.label}
                      primaryTypographyProps={{
                        fontSize: 13.5,
                        fontWeight: selected ? 600 : 500,
                      }}
                    />
                  </ListItemButton>
                );
              })}
            </List>
          </Box>
        ))}
      </Box>

      <Box sx={{ p: 1, borderTop: `1px solid ${RAIL.border}` }}>
        <RoleSwitcher />
      </Box>
    </Drawer>
  );
}
