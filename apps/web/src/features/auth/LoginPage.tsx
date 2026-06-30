import { zodResolver } from "@hookform/resolvers/zod";
import { Box, Button, Card, Stack, TextField, Typography } from "@mui/material";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router-dom";
import { z } from "zod";

import { paths } from "../../app/router/paths";
import { ROLE_COLORS } from "../../lib/constants";
import { useAuth } from "./useAuth";

const loginSchema = z.object({
  email: z.string().email("Enter a valid work email"),
  password: z.string().min(8, "Password must be at least 8 characters"),
});

type LoginFormValues = z.infer<typeof loginSchema>;

/**
 * Login screen. The form (react-hook-form + zod) is in place; Sprint 0 performs a demo sign-in
 * since the auth backend lands in Sprint 1, where this is wired to POST /auth/login.
 */
export function LoginPage() {
  const { login } = useAuth();
  const navigate = useNavigate();

  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<LoginFormValues>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: "demo@protrack.io", password: "demo-password" },
  });

  const onSubmit = handleSubmit(() => {
    // Sprint-0 demo sign-in (replaced by real auth in Sprint 1).
    login(
      {
        id: "demo-user",
        fullName: "Demo User",
        email: "demo@protrack.io",
        roles: ["PM"],
        permissions: [],
        avatarInitials: "DU",
        avatarColor: ROLE_COLORS.PM,
      },
      "demo-access-token",
    );
    navigate(paths.health, { replace: true });
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
              Sign in
            </Button>
          </Stack>
        </form>
      </Card>
    </Box>
  );
}
