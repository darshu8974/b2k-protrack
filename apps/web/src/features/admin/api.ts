import { apiClient } from "../../api/axios";
import type { Page } from "../../types/api";

export interface AdminUser {
  id: string;
  email: string;
  fullName: string;
  avatarInitials?: string | null;
  avatarColor?: string | null;
  status: string;
  roles: string[];
  lastLoginAt?: string | null;
}

export interface Role {
  id: number;
  code: string;
  name: string;
  description?: string | null;
}

export interface UserListParams {
  page: number;
  size: number;
  role?: string;
  status?: string;
  q?: string;
}

export interface CreateUserBody {
  email: string;
  fullName: string;
  roleId: number;
  password: string;
  avatarColor?: string;
}

export interface UpdateUserBody {
  fullName?: string;
  avatarColor?: string;
  status?: string;
}

export interface BulkResult {
  updated: number;
  skipped: number;
}

/** GET /api/v1/admin/users — paginated, filterable administrator user directory. */
export async function listUsers(params: UserListParams): Promise<Page<AdminUser>> {
  const { data } = await apiClient.get<Page<AdminUser>>("/admin/users", {
    params: {
      page: params.page,
      size: params.size,
      role: params.role || undefined,
      status: params.status || undefined,
      q: params.q || undefined,
    },
  });
  return data;
}

export async function createUser(body: CreateUserBody): Promise<AdminUser> {
  const { data } = await apiClient.post<AdminUser>("/admin/users", body);
  return data;
}

export async function updateUser(id: string, body: UpdateUserBody): Promise<AdminUser> {
  const { data } = await apiClient.patch<AdminUser>(`/admin/users/${id}`, body);
  return data;
}

export async function deactivateUser(id: string): Promise<void> {
  await apiClient.delete(`/admin/users/${id}`);
}

/** Permanently delete a user (hard delete). 409 if the user is still referenced by activity. */
export async function deleteUser(id: string): Promise<void> {
  await apiClient.delete(`/admin/users/${id}/permanent`);
}

export async function assignRole(id: string, roleId: number): Promise<AdminUser> {
  const { data } = await apiClient.post<AdminUser>(`/admin/users/${id}/roles`, { roleId });
  return data;
}

export async function revokeRole(id: string, roleId: number): Promise<void> {
  await apiClient.delete(`/admin/users/${id}/roles/${roleId}`);
}

export async function bulkUpdate(
  action: "ACTIVATE" | "DEACTIVATE",
  userIds: string[],
): Promise<BulkResult> {
  const { data } = await apiClient.post<BulkResult>("/admin/users:bulk", { action, userIds });
  return data;
}

export async function listRoles(): Promise<Role[]> {
  const { data } = await apiClient.get<Role[]>("/admin/roles");
  return data;
}
