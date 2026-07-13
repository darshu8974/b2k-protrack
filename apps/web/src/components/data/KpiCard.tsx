import { Box, Card, Stack, Typography } from "@mui/material";
import type { ReactNode } from "react";

import { tokens } from "../../app/theme/palette";

/**
 * KPI stat card: a tinted icon chip above a large figure and its label — matching the
 * dashboard/reports design. Icon + accent are optional so existing call sites stay valid.
 */
export function KpiCard({
  label,
  value,
  icon,
  tint = tokens.primaryTint,
  iconColor = tokens.primary,
}: {
  label: string;
  value: number | string;
  icon?: ReactNode;
  tint?: string;
  iconColor?: string;
}) {
  return (
    <Card sx={{ p: 2.5, minWidth: 168, flex: 1 }}>
      <Stack spacing={1.5}>
        {icon && (
          <Box
            sx={{
              width: 40,
              height: 40,
              borderRadius: 2,
              bgcolor: tint,
              color: iconColor,
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              "& .MuiSvgIcon-root": { fontSize: 22 },
            }}
          >
            {icon}
          </Box>
        )}
        <Box>
          <Typography variant="h4" sx={{ fontWeight: 800, lineHeight: 1.1 }}>
            {value}
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.5 }}>
            {label}
          </Typography>
        </Box>
      </Stack>
    </Card>
  );
}
