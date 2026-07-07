import { Box, Stack, Typography } from "@mui/material";

import type { ImprintWorkloadItem } from "../../../types/report";

/** Horizontal share bars of active projects by imprint. */
export function ImprintBars({ items }: { items: ImprintWorkloadItem[] }) {
  if (items.length === 0) {
    return (
      <Typography variant="body2" color="text.secondary">
        No active projects.
      </Typography>
    );
  }

  return (
    <Stack spacing={1.5}>
      {items.map((item) => (
        <Box key={item.imprintId}>
          <Stack direction="row" justifyContent="space-between" sx={{ mb: 0.5 }}>
            <Typography variant="body2" sx={{ fontWeight: 500 }}>
              {item.imprintName}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {item.activeProjects} · {item.percentage}%
            </Typography>
          </Stack>
          <Box sx={{ height: 8, borderRadius: 1, bgcolor: "action.hover", overflow: "hidden" }}>
            <Box
              sx={{
                width: `${Math.max(2, item.percentage)}%`,
                height: "100%",
                bgcolor: "primary.main",
                borderRadius: 1,
              }}
            />
          </Box>
        </Box>
      ))}
    </Stack>
  );
}
