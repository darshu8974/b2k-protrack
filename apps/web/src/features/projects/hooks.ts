import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

import { queryKeys } from "../../api/keys";
import {
  createProject,
  getProject,
  getProjectTimeline,
  listProjects,
  type CreateProjectBody,
  type ProjectListParams,
} from "./api";

export function useProjects(params: ProjectListParams) {
  return useQuery({
    queryKey: queryKeys.projects(params),
    queryFn: () => listProjects(params),
    placeholderData: keepPreviousData,
  });
}

export function useProject(id: string) {
  return useQuery({ queryKey: queryKeys.project(id), queryFn: () => getProject(id), enabled: !!id });
}

export function useProjectTimeline(id: string) {
  return useQuery({
    queryKey: queryKeys.projectTimeline(id),
    queryFn: () => getProjectTimeline(id),
    enabled: !!id,
  });
}

export function useCreateProject() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (body: CreateProjectBody) => createProject(body),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["projects"] });
      queryClient.invalidateQueries({ queryKey: queryKeys.dashboard });
    },
  });
}
