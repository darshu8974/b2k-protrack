import {
  Box,
  Drawer,
  List,
  ListItemButton,
  ListItemText,
  Toolbar,
  Typography,
} from "@mui/material";
import { useLocation, useNavigate } from "react-router-dom";

import { useAuth } from "../../features/auth/useAuth";
import { navConfig } from "../../lib/navConfig";
import { RoleSwitcher } from "./RoleSwitcher";

const DRAWER_WIDTH = 248;

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
      <Toolbar sx={{ minHeight: 60 }}>
        <Typography variant="h6" color="primary">
          Protrack
        </Typography>
      </Toolbar>

      <Box component="nav" aria-label="Main navigation" sx={{ overflow: "auto", flex: 1 }}>
        {sections.map((section) => (
          <Box key={section.group} sx={{ px: 1, pt: 1 }}>
            <Typography
              variant="caption"
              sx={{ px: 1, color: "text.secondary", fontWeight: 600 }}
            >
              {section.group}
            </Typography>
            <List dense>
              {section.items.map((item, index) => (
                <ListItemButton
                  key={`${item.label}-${index}`}
                  selected={pathname === item.path}
                  onClick={() => navigate(item.path)}
                  sx={{ borderRadius: 1.5, mx: 0.5 }}
                >
                  <ListItemText primary={item.label} />
                </ListItemButton>
              ))}
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
