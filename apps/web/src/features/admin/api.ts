import { apiClient } from "../../api/axios";

export interface AdminUser {
  id: string;
  email: string;
  fullName: string;
  status: string;
  roles: string[];
}

/** GET /api/v1/admin/users — administrator-only user directory. */
export async function listUsers(): Promise<AdminUser[]> {
  const { data } = await apiClient.get<AdminUser[]>("/admin/users");
  return data;
}
