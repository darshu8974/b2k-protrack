import { Box, Button, Stack, Typography } from "@mui/material";

import { useAuth } from "../../features/auth/useAuth";
import { ROLE_LABELS } from "../../lib/constants";
import type { Role } from "../../types/domain";

const ROLES: Role[] = ["PM", "DESIGNER", "QA", "ADMIN"];

/**
 * "Viewing as" role switcher — a Sprint-0 demo affordance that reshapes the client UI only.
 * Real authorization is enforced server-side; impersonation is a future enhancement.
 */
export function RoleSwitcher() {
  const { user, setRole } = useAuth();
  const activeRole = user?.roles[0] ?? "PM";

  return (
    <Box>
      <Typography variant="caption" sx={{ px: 1, color: "text.secondary", fontWeight: 600 }}>
        VIEWING AS
      </Typography>
      <Stack spacing={0.5} sx={{ mt: 0.5 }}>
        {ROLES.map((role) => (
          <Button
            key={role}
            size="small"
            variant={role === activeRole ? "contained" : "text"}
            onClick={() => setRole(role)}
            sx={{ justifyContent: "flex-start" }}
          >
            {ROLE_LABELS[role]}
          </Button>
        ))}
      </Stack>
    </Box>
  );
}
