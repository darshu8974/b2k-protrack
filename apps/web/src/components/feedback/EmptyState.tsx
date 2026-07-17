import { Box, Typography } from "@mui/material";
import type { ReactNode } from "react";

/**
 * Standard "no data yet" placeholder for empty lists/panels. An optional muted icon and bold title
 * sit above the message; an optional action slot (e.g. a button) renders below. Keeps empty-state
 * copy, spacing, and styling consistent across features.
 */
export function EmptyState({
  icon,
  title,
  message,
  action,
}: {
  icon?: ReactNode;
  title?: string;
  message: string;
  action?: ReactNode;
}) {
  return (
    <Box sx={{ py: 5, px: 2, textAlign: "center" }}>
      {icon && (
        <Box sx={{ color: "text.disabled", mb: 1, "& .MuiSvgIcon-root": { fontSize: 46 } }}>
          {icon}
        </Box>
      )}
      {title && (
        <Typography variant="subtitle1" sx={{ fontWeight: 700, mb: 0.5 }}>
          {title}
        </Typography>
      )}
      <Typography variant="body2" color="text.secondary" sx={{ maxWidth: 360, mx: "auto" }}>
        {message}
      </Typography>
      {action && <Box sx={{ mt: 2 }}>{action}</Box>}
    </Box>
  );
}
