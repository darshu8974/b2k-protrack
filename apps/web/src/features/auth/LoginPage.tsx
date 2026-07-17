import { zodResolver } from "@hookform/resolvers/zod";
import AutoAwesomeIcon from "@mui/icons-material/AutoAwesome";
import FactCheckIcon from "@mui/icons-material/FactCheck";
import LockIcon from "@mui/icons-material/Lock";
import { Alert, Box, Button, Stack, TextField, Typography } from "@mui/material";
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

const FEATURES = [
  { icon: <AutoAwesomeIcon fontSize="small" />, text: "AI manuscript structuring & confidence scoring" },
  { icon: <FactCheckIcon fontSize="small" />, text: "Automated PDF preflight & QA gates" },
  { icon: <LockIcon fontSize="small" />, text: "SOC 2 · 21 CFR Part 11 audit trails" },
];

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
    defaultValues: { email: "admin@protrack.io", password: "password" },
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
    <Box sx={{ minHeight: "100vh", display: "flex" }}>
      {/* Brand panel (hidden on small screens). */}
      <Box
        sx={{
          display: { xs: "none", md: "flex" },
          flexDirection: "column",
          width: "44%",
          maxWidth: 560,
          p: 6,
          bgcolor: "#12213B",
          color: "common.white",
        }}
      >
        <Stack direction="row" spacing={1.25} alignItems="center">
          <Box
            sx={{
              width: 32,
              height: 32,
              borderRadius: 1.5,
              bgcolor: "primary.main",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              fontWeight: 800,
            }}
          >
            P
          </Box>
          <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
            Protrack
          </Typography>
          <Box
            sx={{
              px: 1,
              py: 0.25,
              borderRadius: 1,
              border: "1px solid rgba(255,255,255,0.25)",
              fontSize: 10,
              letterSpacing: 0.6,
              color: "rgba(255,255,255,0.7)",
            }}
          >
            PUBLISHING OS
          </Box>
        </Stack>

        <Box sx={{ my: "auto", maxWidth: 420 }}>
          <Typography
            variant="overline"
            sx={{ color: "#7FA6E0", fontWeight: 700, letterSpacing: 1 }}
          >
            AI-ASSISTED PUBLISHING WORKFLOW
          </Typography>
          <Typography variant="h3" sx={{ fontWeight: 800, mt: 1.5, mb: 2, lineHeight: 1.15 }}>
            From manuscript to print-ready, orchestrated end to end.
          </Typography>
          <Typography sx={{ color: "rgba(255,255,255,0.7)", mb: 3.5, lineHeight: 1.6 }}>
            Intake, AI structural analysis, production hand-off, PDF review and QA sign-off — all in
            one place. Your designers keep working in InDesign; Protrack handles everything around it.
          </Typography>
          <Stack spacing={1.75}>
            {FEATURES.map((f) => (
              <Stack key={f.text} direction="row" spacing={1.5} alignItems="center">
                <Box sx={{ color: "#7FA6E0", display: "flex" }}>{f.icon}</Box>
                <Typography variant="body2" sx={{ color: "rgba(255,255,255,0.85)" }}>
                  {f.text}
                </Typography>
              </Stack>
            ))}
          </Stack>
        </Box>

        <Typography variant="caption" sx={{ color: "rgba(255,255,255,0.45)" }}>
          © 2026 Protrack, Inc. · Enterprise edition
        </Typography>
      </Box>

      {/* Form panel. */}
      <Box
        sx={{
          flex: 1,
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
          bgcolor: "background.paper",
          p: { xs: 3, md: 6 },
        }}
      >
        <Box sx={{ width: 380, maxWidth: "100%" }}>
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

          <Box
            sx={{
              mt: 3,
              p: 1.5,
              borderRadius: 2,
              bgcolor: "background.default",
              border: 1,
              borderColor: "divider",
            }}
          >
            <Typography variant="caption" color="text.secondary">
              <strong>Demo</strong> — credentials are pre-filled. Sign in, then switch roles (Project
              Manager / Paginator / QC / QA / Admin) from the sidebar.
            </Typography>
          </Box>
        </Box>
      </Box>
    </Box>
  );
}
