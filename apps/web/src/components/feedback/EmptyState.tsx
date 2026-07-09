import { Box, Typography } from "@mui/material";
import type { ReactNode } from "react";

/**
 * Standard "no data yet" placeholder for empty lists/panels (Frontend Architecture §13). Keeps the
 * empty-state copy and spacing consistent across features; an optional action slot (e.g. a button)
 * renders below the message.
 */
export function EmptyState({
  message,
  action,
}: {
  message: string;
  action?: ReactNode;
}) {
  return (
    <Box sx={{ py: 4, px: 2, textAlign: "center" }}>
      <Typography variant="body2" color="text.secondary">
        {message}
      </Typography>
      {action && <Box sx={{ mt: 2 }}>{action}</Box>}
    </Box>
  );
}
