import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { queryKeys } from "../../api/keys";
import {
  assignRole,
  bulkUpdate,
  createUser,
  deactivateUser,
  listRoles,
  listUsers,
  revokeRole,
  updateUser,
  type CreateUserBody,
  type UpdateUserBody,
  type UserListParams,
} from "./api";

export function useAdminUsers(params: UserListParams) {
  return useQuery({
    queryKey: queryKeys.adminUsers(params),
    queryFn: () => listUsers(params),
    placeholderData: keepPreviousData,
  });
}

export function useRoles() {
  return useQuery({ queryKey: queryKeys.roles, queryFn: listRoles });
}

/** Invalidate every admin-users page after a mutation so the directory refreshes. */
function useInvalidateUsers() {
  const queryClient = useQueryClient();
  return () => queryClient.invalidateQueries({ queryKey: ["admin", "users"] });
}

export function useCreateUser() {
  const invalidate = useInvalidateUsers();
  return useMutation({
    mutationFn: (body: CreateUserBody) => createUser(body),
    onSuccess: invalidate,
  });
}

export function useUpdateUser() {
  const invalidate = useInvalidateUsers();
  return useMutation({
    mutationFn: ({ id, body }: { id: string; body: UpdateUserBody }) => updateUser(id, body),
    onSuccess: invalidate,
  });
}

export function useDeactivateUser() {
  const invalidate = useInvalidateUsers();
  return useMutation({
    mutationFn: (id: string) => deactivateUser(id),
    onSuccess: invalidate,
  });
}

export function useAssignRole() {
  const invalidate = useInvalidateUsers();
  return useMutation({
    mutationFn: ({ id, roleId }: { id: string; roleId: number }) => assignRole(id, roleId),
    onSuccess: invalidate,
  });
}

export function useRevokeRole() {
  const invalidate = useInvalidateUsers();
  return useMutation({
    mutationFn: ({ id, roleId }: { id: string; roleId: number }) => revokeRole(id, roleId),
    onSuccess: invalidate,
  });
}

export function useBulkUpdate() {
  const invalidate = useInvalidateUsers();
  return useMutation({
    mutationFn: ({ action, userIds }: { action: "ACTIVATE" | "DEACTIVATE"; userIds: string[] }) =>
      bulkUpdate(action, userIds),
    onSuccess: invalidate,
  });
}
