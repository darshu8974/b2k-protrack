import AutoAwesomeOutlinedIcon from "@mui/icons-material/AutoAwesomeOutlined";
import BrushOutlinedIcon from "@mui/icons-material/BrushOutlined";
import FactCheckOutlinedIcon from "@mui/icons-material/FactCheckOutlined";
import FolderOutlinedIcon from "@mui/icons-material/FolderOutlined";
import GridViewOutlinedIcon from "@mui/icons-material/GridViewOutlined";
import GroupOutlinedIcon from "@mui/icons-material/GroupOutlined";
import HistoryOutlinedIcon from "@mui/icons-material/HistoryOutlined";
import InsightsOutlinedIcon from "@mui/icons-material/InsightsOutlined";
import ManageAccountsOutlinedIcon from "@mui/icons-material/ManageAccountsOutlined";
import RateReviewOutlinedIcon from "@mui/icons-material/RateReviewOutlined";
import TaskAltOutlinedIcon from "@mui/icons-material/TaskAltOutlined";
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

import { tokens } from "../../app/theme/palette";
import { useAuth } from "../../features/auth/useAuth";
import { navConfig } from "../../lib/navConfig";
import { RoleSwitcher } from "./RoleSwitcher";

const DRAWER_WIDTH = 248;

const ICONS: Record<string, ReactNode> = {
  dashboard: <GridViewOutlinedIcon fontSize="small" />,
  folder: <FolderOutlinedIcon fontSize="small" />,
  review: <RateReviewOutlinedIcon fontSize="small" />,
  team: <GroupOutlinedIcon fontSize="small" />,
  assistant: <AutoAwesomeOutlinedIcon fontSize="small" />,
  reports: <InsightsOutlinedIcon fontSize="small" />,
  tasks: <TaskAltOutlinedIcon fontSize="small" />,
  production: <BrushOutlinedIcon fontSize="small" />,
  qa: <FactCheckOutlinedIcon fontSize="small" />,
  users: <ManageAccountsOutlinedIcon fontSize="small" />,
  audit: <HistoryOutlinedIcon fontSize="small" />,
};

/** Fixed left navigation; items are shaped by the active role. */
export function Sidebar() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { pathname } = useLocation();

  const role = user?.roles[0] ?? "PM";
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
          borderRight: 1,
          borderColor: "divider",
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
          <Typography variant="subtitle1" sx={{ fontWeight: 700, lineHeight: 1.1 }}>
            Protrack
          </Typography>
          <Typography
            variant="caption"
            sx={{ color: "text.secondary", letterSpacing: 0.6, fontSize: 9.5 }}
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
                color: "text.secondary",
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
                      "&.Mui-selected": {
                        bgcolor: tokens.primaryTint,
                        color: "primary.main",
                        "& .MuiListItemIcon-root": { color: "primary.main" },
                        "&:hover": { bgcolor: tokens.primaryTint },
                      },
                    }}
                  >
                    <ListItemIcon sx={{ minWidth: 34, color: "text.secondary" }}>
                      {ICONS[item.icon]}
                    </ListItemIcon>
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

      <Box sx={{ p: 1, borderTop: 1, borderColor: "divider" }}>
        <RoleSwitcher />
      </Box>
    </Drawer>
  );
}
