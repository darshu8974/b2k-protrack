import { Box, Button, Stack, Typography } from "@mui/material";
import { useNavigate } from "react-router-dom";

import { paths } from "../../app/router/paths";

/** Shown when an authenticated user lacks the role required for a route. */
export function ForbiddenPage() {
  const navigate = useNavigate();
  return (
    <Box sx={{ p: 6, display: "flex", justifyContent: "center" }}>
      <Stack spacing={2} alignItems="center">
        <Typography variant="h4">403</Typography>
        <Typography color="text.secondary">
          You don&apos;t have permission to view this page.
        </Typography>
        <Button variant="contained" onClick={() => navigate(paths.dashboard)}>
          Back to dashboard
        </Button>
      </Stack>
    </Box>
  );
}
