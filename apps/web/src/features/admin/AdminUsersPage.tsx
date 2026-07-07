import AddIcon from "@mui/icons-material/Add";
import BlockIcon from "@mui/icons-material/Block";
import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import PersonAddAlt1Icon from "@mui/icons-material/PersonAddAlt1";
import {
  Alert,
  Avatar,
  Box,
  Button,
  Card,
  Checkbox,
  Chip,
  IconButton,
  Menu,
  MenuItem,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TablePagination,
  TableRow,
  TextField,
  Tooltip,
  Typography,
} from "@mui/material";
import { useState } from "react";

import { ErrorState } from "../../components/feedback/ErrorState";
import { LoadingState } from "../../components/feedback/LoadingState";
import { useDebounce } from "../../hooks/useDebounce";
import type { AppError } from "../../types/api";
import { useAuth } from "../auth/useAuth";
import type { AdminUser } from "./api";
import {
  useAdminUsers,
  useAssignRole,
  useBulkUpdate,
  useDeactivateUser,
  useRevokeRole,
  useRoles,
} from "./hooks";
import { UserFormDialog } from "./UserFormDialog";

const STATUS_COLOR: Record<string, "success" | "warning" | "default"> = {
  ACTIVE: "success",
  SUSPENDED: "warning",
  INACTIVE: "default",
};
const STATUS_FILTERS = ["ACTIVE", "INACTIVE", "SUSPENDED"];

/** Administrator user directory: create, edit, deactivate, assign/revoke roles, bulk status. */
export function AdminUsersPage() {
  const { user: me } = useAuth();
  const [page, setPage] = useState(0);
  const [size, setSize] = useState(20);
  const [search, setSearch] = useState("");
  const [roleFilter, setRoleFilter] = useState("");
  const [statusFilter, setStatusFilter] = useState("");
  const q = useDebounce(search);

  const [selected, setSelected] = useState<string[]>([]);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [editUser, setEditUser] = useState<AdminUser | null>(null);
  const [roleMenu, setRoleMenu] = useState<{ anchor: HTMLElement; user: AdminUser } | null>(null);
  const [banner, setBanner] = useState<string | null>(null);

  const { data: roles = [] } = useRoles();
  const { data, isLoading, isError } = useAdminUsers({
    page,
    size,
    role: roleFilter || undefined,
    status: statusFilter || undefined,
    q: q || undefined,
  });

  const assignRole = useAssignRole();
  const revokeRole = useRevokeRole();
  const deactivate = useDeactivateUser();
  const bulk = useBulkUpdate();

  const users = data?.content ?? [];
  const onError = (e: unknown) => setBanner((e as AppError)?.message ?? "Action failed.");

  function toggle(id: string) {
    setSelected((s) => (s.includes(id) ? s.filter((x) => x !== id) : [...s, id]));
  }

  function runBulk(action: "ACTIVATE" | "DEACTIVATE") {
    setBanner(null);
    bulk.mutate(
      { action, userIds: selected },
      {
        onSuccess: (res) => {
          setSelected([]);
          if (res.skipped > 0) {
            setBanner(`${res.updated} updated, ${res.skipped} skipped (e.g. your own account).`);
          }
        },
        onError,
      },
    );
  }

  function openCreate() {
    setEditUser(null);
    setDialogOpen(true);
  }
  function openEdit(u: AdminUser) {
    setEditUser(u);
    setDialogOpen(true);
  }

  return (
    <Stack spacing={2}>
      <Stack direction="row" justifyContent="space-between" alignItems="center" flexWrap="wrap" useFlexGap>
        <Typography variant="h4">Users &amp; roles</Typography>
        <Button variant="contained" startIcon={<PersonAddAlt1Icon />} onClick={openCreate}>
          New user
        </Button>
      </Stack>

      {banner && (
        <Alert severity="info" onClose={() => setBanner(null)}>
          {banner}
        </Alert>
      )}

      {/* Filters */}
      <Card sx={{ p: 2 }}>
        <Stack direction="row" spacing={2} flexWrap="wrap" useFlexGap>
          <TextField
            size="small"
            label="Search name or email"
            value={search}
            onChange={(e) => {
              setSearch(e.target.value);
              setPage(0);
            }}
            sx={{ minWidth: 220 }}
          />
          <TextField
            size="small"
            select
            label="Role"
            value={roleFilter}
            onChange={(e) => {
              setRoleFilter(e.target.value);
              setPage(0);
            }}
            sx={{ minWidth: 160 }}
          >
            <MenuItem value="">All roles</MenuItem>
            {roles.map((r) => (
              <MenuItem key={r.id} value={r.code}>
                {r.name}
              </MenuItem>
            ))}
          </TextField>
          <TextField
            size="small"
            select
            label="Status"
            value={statusFilter}
            onChange={(e) => {
              setStatusFilter(e.target.value);
              setPage(0);
            }}
            sx={{ minWidth: 160 }}
          >
            <MenuItem value="">All statuses</MenuItem>
            {STATUS_FILTERS.map((s) => (
              <MenuItem key={s} value={s}>
                {s}
              </MenuItem>
            ))}
          </TextField>
        </Stack>
      </Card>

      {/* Bulk bar */}
      {selected.length > 0 && (
        <Card sx={{ p: 1.5 }}>
          <Stack direction="row" spacing={1.5} alignItems="center">
            <Typography variant="body2">{selected.length} selected</Typography>
            <Button size="small" onClick={() => runBulk("ACTIVATE")} disabled={bulk.isPending}>
              Activate
            </Button>
            <Button
              size="small"
              color="warning"
              onClick={() => runBulk("DEACTIVATE")}
              disabled={bulk.isPending}
            >
              Deactivate
            </Button>
            <Button size="small" onClick={() => setSelected([])}>
              Clear
            </Button>
          </Stack>
        </Card>
      )}

      <Card>
        {isError ? (
          <ErrorState message="Unable to load users." />
        ) : isLoading ? (
          <LoadingState />
        ) : (
          <>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell padding="checkbox" />
                  <TableCell>Name</TableCell>
                  <TableCell>Roles</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Last login</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {users.map((u) => {
                  const isSelf = u.id === me?.id;
                  const roleObjs = roles.filter((r) => u.roles.includes(r.code));
                  const addable = roles.filter((r) => !u.roles.includes(r.code));
                  return (
                    <TableRow key={u.id} hover>
                      <TableCell padding="checkbox">
                        <Checkbox checked={selected.includes(u.id)} onChange={() => toggle(u.id)} />
                      </TableCell>
                      <TableCell>
                        <Stack direction="row" spacing={1.5} alignItems="center">
                          <Avatar
                            sx={{
                              width: 32,
                              height: 32,
                              fontSize: 13,
                              bgcolor: u.avatarColor ?? undefined,
                            }}
                          >
                            {u.avatarInitials ?? "?"}
                          </Avatar>
                          <Box>
                            <Typography variant="body2" sx={{ fontWeight: 600 }}>
                              {u.fullName}
                            </Typography>
                            <Typography variant="caption" color="text.secondary">
                              {u.email}
                            </Typography>
                          </Box>
                        </Stack>
                      </TableCell>
                      <TableCell>
                        <Stack direction="row" spacing={0.5} alignItems="center" flexWrap="wrap" useFlexGap>
                          {roleObjs.map((r) => (
                            <Chip
                              key={r.id}
                              label={r.code}
                              size="small"
                              onDelete={
                                u.roles.length > 1
                                  ? () => {
                                      setBanner(null);
                                      revokeRole.mutate({ id: u.id, roleId: r.id }, { onError });
                                    }
                                  : undefined
                              }
                            />
                          ))}
                          {addable.length > 0 && (
                            <Tooltip title="Add role">
                              <IconButton
                                size="small"
                                onClick={(e) => setRoleMenu({ anchor: e.currentTarget, user: u })}
                              >
                                <AddIcon fontSize="small" />
                              </IconButton>
                            </Tooltip>
                          )}
                        </Stack>
                      </TableCell>
                      <TableCell>
                        <Chip
                          size="small"
                          color={STATUS_COLOR[u.status] ?? "default"}
                          label={u.status}
                        />
                      </TableCell>
                      <TableCell>
                        <Typography variant="caption" color="text.secondary">
                          {u.lastLoginAt ? new Date(u.lastLoginAt).toLocaleDateString() : "Never"}
                        </Typography>
                      </TableCell>
                      <TableCell align="right">
                        <Tooltip title="Edit">
                          <IconButton size="small" onClick={() => openEdit(u)}>
                            <EditOutlinedIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title={isSelf ? "You cannot deactivate yourself" : "Deactivate"}>
                          <span>
                            <IconButton
                              size="small"
                              disabled={isSelf || u.status === "INACTIVE"}
                              onClick={() => {
                                setBanner(null);
                                deactivate.mutate(u.id, { onError });
                              }}
                            >
                              <BlockIcon fontSize="small" />
                            </IconButton>
                          </span>
                        </Tooltip>
                      </TableCell>
                    </TableRow>
                  );
                })}
                {users.length === 0 && (
                  <TableRow>
                    <TableCell colSpan={6}>
                      <Typography
                        variant="body2"
                        color="text.secondary"
                        sx={{ py: 2, textAlign: "center" }}
                      >
                        No users match these filters.
                      </Typography>
                    </TableCell>
                  </TableRow>
                )}
              </TableBody>
            </Table>
            <TablePagination
              component="div"
              count={data?.totalElements ?? 0}
              page={data?.page ?? 0}
              rowsPerPage={data?.size ?? size}
              rowsPerPageOptions={[20, 50, 100]}
              onPageChange={(_, next) => setPage(next)}
              onRowsPerPageChange={(e) => {
                setSize(parseInt(e.target.value, 10));
                setPage(0);
              }}
            />
          </>
        )}
      </Card>

      {/* Add-role menu */}
      <Menu open={!!roleMenu} anchorEl={roleMenu?.anchor} onClose={() => setRoleMenu(null)}>
        {roleMenu &&
          roles
            .filter((r) => !roleMenu.user.roles.includes(r.code))
            .map((r) => (
              <MenuItem
                key={r.id}
                onClick={() => {
                  setBanner(null);
                  assignRole.mutate({ id: roleMenu.user.id, roleId: r.id }, { onError });
                  setRoleMenu(null);
                }}
              >
                {r.name}
              </MenuItem>
            ))}
      </Menu>

      <UserFormDialog
        open={dialogOpen}
        onClose={() => setDialogOpen(false)}
        roles={roles}
        user={editUser}
      />
    </Stack>
  );
}
