import {
  Card,
  Chip,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
} from "@mui/material";
import { useQuery } from "@tanstack/react-query";

import { ErrorState } from "../../components/feedback/ErrorState";
import { LoadingState } from "../../components/feedback/LoadingState";
import type { AppError } from "../../types/api";
import { listUsers, type AdminUser } from "./api";

/** Administrator-only user directory (RBAC demonstration; not a full admin feature yet). */
export function AdminUsersPage() {
  const { data, isLoading, isError, error } = useQuery<AdminUser[], AppError>({
    queryKey: ["admin", "users"],
    queryFn: listUsers,
  });

  if (isLoading) {
    return <LoadingState />;
  }
  if (isError) {
    return <ErrorState message={`${error.code}: ${error.message}`} />;
  }

  return (
    <Stack spacing={2}>
      <Typography variant="h4">Users &amp; roles</Typography>
      <Card>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Name</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Roles</TableCell>
              <TableCell>Status</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {data?.map((user) => (
              <TableRow key={user.id}>
                <TableCell>{user.fullName}</TableCell>
                <TableCell>{user.email}</TableCell>
                <TableCell>
                  {user.roles.map((role) => (
                    <Chip key={role} label={role} size="small" sx={{ mr: 0.5 }} />
                  ))}
                </TableCell>
                <TableCell>{user.status}</TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Card>
    </Stack>
  );
}
