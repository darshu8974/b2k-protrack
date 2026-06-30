import { Box, Button, Stack, Typography } from "@mui/material";
import { useNavigate } from "react-router-dom";

import { paths } from "../../app/router/paths";

export function NotFoundPage() {
  const navigate = useNavigate();
  return (
    <Box sx={{ display: "flex", minHeight: "100vh", alignItems: "center", justifyContent: "center" }}>
      <Stack spacing={2} alignItems="center">
        <Typography variant="h4">404</Typography>
        <Typography color="text.secondary">This page does not exist.</Typography>
        <Button variant="contained" onClick={() => navigate(paths.health)}>
          Go home
        </Button>
      </Stack>
    </Box>
  );
}
