import { Alert, Card, Chip, Stack, Typography } from "@mui/material";
import { useQuery } from "@tanstack/react-query";

import { apiClient } from "../../api/axios";
import { queryKeys } from "../../api/keys";
import { LoadingState } from "../../components/feedback/LoadingState";
import type { AppError } from "../../types/api";

interface HealthStatus {
  status: string;
  service: string;
  version: string;
  timestamp: string;
}

async function fetchHealth(): Promise<HealthStatus> {
  const { data } = await apiClient.get<HealthStatus>("/health");
  return data;
}

/** Sprint-0 landing page: pings the API health endpoint to prove the data path end-to-end. */
export function HealthPage() {
  const { data, isLoading, isError, error } = useQuery<HealthStatus, AppError>({
    queryKey: queryKeys.health,
    queryFn: fetchHealth,
  });

  return (
    <Card sx={{ p: 3, maxWidth: 640 }}>
      <Stack spacing={2}>
        <Typography variant="h5">System health</Typography>

        {isLoading && <LoadingState />}

        {isError && (
          <Alert severity="warning">
            API unreachable ({error.code}): {error.message}
          </Alert>
        )}

        {data && (
          <Stack direction="row" spacing={1} alignItems="center">
            <Chip color="success" label={data.status} />
            <Typography>
              {data.service} · v{data.version}
            </Typography>
          </Stack>
        )}

        <Typography variant="body2" color="text.secondary">
          The walking skeleton is live. Feature screens are built sprint by sprint.
        </Typography>
      </Stack>
    </Card>
  );
}
