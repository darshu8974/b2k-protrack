import { Box, Card, Chip, Stack, Typography } from "@mui/material";

import { ROLE_LABELS } from "../../lib/constants";
import type { Role } from "../../types/domain";
import { useAuth } from "../auth/useAuth";

/**
 * Sprint-1 dashboard placeholder. Confirms the authenticated session end-to-end; the real
 * role-aware dashboard (KPIs, active projects, AI insights) is built in a later sprint.
 */
export function DashboardPage() {
  const { user } = useAuth();
  const firstName = user?.fullName?.split(" ")[0] ?? "there";
  const role = (user?.roles[0] ?? "PM") as Role;

  return (
    <Stack spacing={3}>
      <Box>
        <Typography variant="h4">Good morning, {firstName}</Typography>
        <Typography color="text.secondary">
          You are signed in to Protrack. Dashboard features arrive in a later sprint.
        </Typography>
      </Box>

      <Card sx={{ p: 3, maxWidth: 640 }}>
        <Stack spacing={2}>
          <Box>
            <Typography variant="subtitle2" color="text.secondary">
              Signed in as
            </Typography>
            <Typography variant="subtitle1">{user?.fullName}</Typography>
            <Typography variant="body2" color="text.secondary">
              {user?.email}
            </Typography>
          </Box>
          <Box>
            <Typography variant="subtitle2" color="text.secondary" sx={{ mb: 0.5 }}>
              Role
            </Typography>
            <Chip label={ROLE_LABELS[role]} color="primary" size="small" />
          </Box>
        </Stack>
      </Card>
    </Stack>
  );
}
