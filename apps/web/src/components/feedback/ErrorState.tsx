import { Alert, AlertTitle, Box } from "@mui/material";

export function ErrorState({ message }: { message?: string }) {
  return (
    <Box sx={{ p: 3 }}>
      <Alert severity="error">
        <AlertTitle>Something went wrong</AlertTitle>
        {message ?? "An unexpected error occurred. Please try again."}
      </Alert>
    </Box>
  );
}
