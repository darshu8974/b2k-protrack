import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  Stack,
  TextField,
} from "@mui/material";
import { useState } from "react";

import { useToast } from "../../components/feedback/ToastProvider";
import type { AppError } from "../../types/api";
import type { AdminUser, Role } from "./api";
import { useCreateUser, useUpdateUser } from "./hooks";

const STATUSES = ["ACTIVE", "INACTIVE", "SUSPENDED"];

interface Props {
  open: boolean;
  onClose: () => void;
  roles: Role[];
  /** Present → edit mode; absent → create mode. */
  user?: AdminUser | null;
}

/** Create a new user or edit an existing user's profile/status. */
export function UserFormDialog({ open, onClose, roles, user }: Props) {
  const editing = !!user;
  const createUser = useCreateUser();
  const updateUser = useUpdateUser();
  const toast = useToast();

  const [email, setEmail] = useState("");
  const [fullName, setFullName] = useState(user?.fullName ?? "");
  const [roleId, setRoleId] = useState<number | "">(roles[0]?.id ?? "");
  const [password, setPassword] = useState("");
  const [status, setStatus] = useState(user?.status ?? "ACTIVE");
  const [error, setError] = useState<string | null>(null);

  // Re-seed the form whenever the dialog opens for a (different) user.
  const [seededFor, setSeededFor] = useState<string | null>(null);
  const key = user?.id ?? "new";
  if (open && seededFor !== key) {
    setEmail("");
    setFullName(user?.fullName ?? "");
    setRoleId(roles[0]?.id ?? "");
    setPassword("");
    setStatus(user?.status ?? "ACTIVE");
    setError(null);
    setSeededFor(key);
  }

  const pending = createUser.isPending || updateUser.isPending;

  function close() {
    setSeededFor(null);
    onClose();
  }

  function submit() {
    setError(null);
    const onError = (e: unknown) => setError((e as AppError)?.message ?? "Something went wrong.");
    if (editing && user) {
      updateUser.mutate(
        { id: user.id, body: { fullName: fullName.trim(), status } },
        {
          onSuccess: () => {
            close();
            toast.success(`${fullName.trim()} was updated.`);
          },
          onError,
        },
      );
    } else {
      if (!email.trim() || !fullName.trim() || roleId === "" || password.length < 8) {
        setError("Email, name, role, and a password of at least 8 characters are required.");
        return;
      }
      const name = fullName.trim();
      createUser.mutate(
        { email: email.trim(), fullName: name, roleId: Number(roleId), password },
        {
          onSuccess: () => {
            close();
            toast.success(`${name} was created.`);
          },
          onError,
        },
      );
    }
  }

  return (
    <Dialog open={open} onClose={close} fullWidth maxWidth="xs">
      <DialogTitle>{editing ? "Edit user" : "New user"}</DialogTitle>
      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          {error && <Alert severity="error">{error}</Alert>}
          {!editing && (
            <TextField
              label="Email"
              type="email"
              size="small"
              fullWidth
              value={email}
              onChange={(e) => setEmail(e.target.value)}
            />
          )}
          <TextField
            label="Full name"
            size="small"
            fullWidth
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
          />
          {!editing && (
            <>
              <TextField
                label="Role"
                select
                size="small"
                fullWidth
                value={roleId}
                onChange={(e) => setRoleId(Number(e.target.value))}
              >
                {roles.map((r) => (
                  <MenuItem key={r.id} value={r.id}>
                    {r.name}
                  </MenuItem>
                ))}
              </TextField>
              <TextField
                label="Temporary password"
                type="password"
                size="small"
                fullWidth
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                helperText="At least 8 characters."
              />
            </>
          )}
          {editing && (
            <TextField
              label="Status"
              select
              size="small"
              fullWidth
              value={status}
              onChange={(e) => setStatus(e.target.value)}
            >
              {STATUSES.map((s) => (
                <MenuItem key={s} value={s}>
                  {s}
                </MenuItem>
              ))}
            </TextField>
          )}
        </Stack>
      </DialogContent>
      <DialogActions>
        <Button onClick={close}>Cancel</Button>
        <Button variant="contained" onClick={submit} disabled={pending}>
          {editing ? "Save" : "Create"}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
