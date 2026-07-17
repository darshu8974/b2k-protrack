import { Avatar, Box, Typography } from "@mui/material";

import { useAuth } from "../../features/auth/useAuth";
import { ROLE_COLORS, ROLE_LABELS } from "../../lib/constants";

/**
 * "Signed in as" — shows the currently authenticated user and their role. Read-only: the role is
 * determined server-side from the JWT, so it is displayed, not switched, in the client. Styled for
 * the dark sidebar rail (light text on the dark background).
 */
export function RoleSwitcher() {
  const { user } = useAuth();
  if (!user) {
    return null;
  }

  const role = user.roles[0] ?? "PROJECT_MANAGER";

  return (
    <Box>
      <Typography
        variant="caption"
        sx={{
          px: 1,
          color: "rgba(255,255,255,0.5)",
          fontWeight: 600,
          letterSpacing: 0.6,
          fontSize: 10.5,
        }}
      >
        SIGNED IN AS
      </Typography>
      <Box
        sx={{
          mt: 0.5,
          display: "flex",
          alignItems: "center",
          gap: 1,
          borderRadius: 1.5,
          px: 1,
          py: 0.75,
          bgcolor: "rgba(255,255,255,0.08)",
        }}
      >
        <Avatar
          sx={{
            width: 28,
            height: 28,
            bgcolor: user.avatarColor ?? ROLE_COLORS[role],
            fontSize: 12,
            fontWeight: 600,
          }}
        >
          {user.avatarInitials}
        </Avatar>
        <Box sx={{ flex: 1, minWidth: 0 }}>
          <Typography
            variant="body2"
            sx={{ fontWeight: 600, lineHeight: 1.15, color: "rgba(255,255,255,0.92)" }}
            noWrap
          >
            {user.fullName}
          </Typography>
          <Typography variant="caption" noWrap sx={{ display: "block", color: "rgba(255,255,255,0.55)" }}>
            {ROLE_LABELS[role]}
          </Typography>
        </Box>
      </Box>
    </Box>
  );
}
