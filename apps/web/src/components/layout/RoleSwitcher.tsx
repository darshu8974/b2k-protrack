import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import { Avatar, Box, ButtonBase, Stack, Typography } from "@mui/material";

import { useAuth } from "../../features/auth/useAuth";
import { ROLE_COLORS, ROLE_LABELS } from "../../lib/constants";
import type { Role } from "../../types/domain";

/** Demo personas shown in the "Viewing as" switcher (matches the seeded demo users). */
const PERSONAS: Record<Role, { name: string; initials: string }> = {
  PM: { name: "Priya Anand", initials: "PA" },
  DESIGNER: { name: "Marcus Reed", initials: "MR" },
  QA: { name: "Lena Ortiz", initials: "LO" },
  ADMIN: { name: "David Cho", initials: "DC" },
};

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
      <Typography
        variant="caption"
        sx={{ px: 1, color: "text.secondary", fontWeight: 600, letterSpacing: 0.6, fontSize: 10.5 }}
      >
        VIEWING AS
      </Typography>
      <Stack spacing={0.25} sx={{ mt: 0.5 }}>
        {ROLES.map((role) => {
          const persona = PERSONAS[role];
          const active = role === activeRole;
          return (
            <ButtonBase
              key={role}
              onClick={() => setRole(role)}
              sx={{
                justifyContent: "flex-start",
                width: "100%",
                borderRadius: 1.5,
                px: 1,
                py: 0.75,
                gap: 1,
                bgcolor: active ? "action.hover" : "transparent",
                "&:hover": { bgcolor: "action.hover" },
              }}
            >
              <Avatar
                sx={{ width: 28, height: 28, bgcolor: ROLE_COLORS[role], fontSize: 12, fontWeight: 600 }}
              >
                {persona.initials}
              </Avatar>
              <Box sx={{ flex: 1, textAlign: "left", minWidth: 0 }}>
                <Typography variant="body2" sx={{ fontWeight: 600, lineHeight: 1.15 }} noWrap>
                  {persona.name}
                </Typography>
                <Typography variant="caption" color="text.secondary" noWrap sx={{ display: "block" }}>
                  {ROLE_LABELS[role]}
                </Typography>
              </Box>
              {active && <CheckCircleIcon sx={{ fontSize: 16, color: "primary.main" }} />}
            </ButtonBase>
          );
        })}
      </Stack>
    </Box>
  );
}
