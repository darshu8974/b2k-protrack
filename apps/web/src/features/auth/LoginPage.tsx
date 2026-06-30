import { zodResolver } from "@hookform/resolvers/zod";
import { Alert, Box, Button, Card, Stack, TextField, Typography } from "@mui/material";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { Navigate, useLocation, useNavigate } from "react-router-dom";
import { z } from "zod";

import { paths } from "../../app/router/paths";
import type { AppError } from "../../types/api";
import { useAuth } from "./useAuth";

const loginSchema = z.object({
  email: z.string().email("Enter a valid work email"),
  password: z.string().min(8, "Password must be at least 8 characters"),
});

type LoginFormValues = z.infer<typeof loginSchema>;

interface FromState {
  from?: string;
}

/** Login screen wired to POST /api/v1/auth/login. On success, redirects to the dashboard. */
export function LoginPage() {
  const { login, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [submitError, setSubmitError] = useState<string | null>(null);

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: "priya.anand@protrack.io", password: "password" },
  });

  // Authenticated users shouldn't see the login page.
  if (isAuthenticated) {
    return <Navigate to={paths.dashboard} replace />;
  }

  const onSubmit = handleSubmit(async (values) => {
    setSubmitError(null);
    try {
      await login(values.email, values.password);
      const from = (location.state as FromState | null)?.from;
      navigate(from && from !== paths.login ? from : paths.dashboard, { replace: true });
    } catch (error) {
      const appError = error as AppError;
      setSubmitError(appError?.message ?? "Sign in failed. Please try again.");
    }
  });

  return (
    <Box
      sx={{
        minHeight: "100vh",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        bgcolor: "background.default",
        p: 2,
      }}
    >
      <Card sx={{ p: 4, width: 420, maxWidth: "100%" }}>
        <Stack spacing={1} sx={{ mb: 3 }}>
          <Typography variant="h5">Sign in to Protrack</Typography>
          <Typography variant="body2" color="text.secondary">
            Welcome back. Use your enterprise credentials.
          </Typography>
        </Stack>

        <form onSubmit={onSubmit} noValidate>
          <Stack spacing={2}>
            {submitError && <Alert severity="error">{submitError}</Alert>}
            <TextField
              label="Work email"
              type="email"
              fullWidth
              error={Boolean(errors.email)}
              helperText={errors.email?.message}
              {...register("email")}
            />
            <TextField
              label="Password"
              type="password"
              fullWidth
              error={Boolean(errors.password)}
              helperText={errors.password?.message}
              {...register("password")}
            />
            <Button type="submit" variant="contained" size="large" disabled={isSubmitting}>
              {isSubmitting ? "Signing in…" : "Sign in"}
            </Button>
          </Stack>
        </form>
      </Card>
    </Box>
  );
}
