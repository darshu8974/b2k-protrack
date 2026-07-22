import { Box, Card, Skeleton, Stack } from "@mui/material";

/** Row-shaped skeleton for a data table, matching the header + N body rows it will replace. */
export function TableSkeleton({ rows = 6, columns = 5 }: { rows?: number; columns?: number }) {
  return (
    <Box sx={{ p: 2 }}>
      <Stack spacing={1.5}>
        {Array.from({ length: rows }).map((_, row) => (
          <Stack key={row} direction="row" spacing={2} alignItems="center">
            {Array.from({ length: columns }).map((_, col) => (
              <Skeleton
                key={col}
                variant="text"
                height={20}
                sx={{ flex: col === 0 ? 2 : 1 }}
              />
            ))}
          </Stack>
        ))}
      </Stack>
    </Box>
  );
}

/** Matches the shape of a Card-based content block while its data loads. */
export function CardSkeleton({ height = 160 }: { height?: number }) {
  return (
    <Card sx={{ p: 2.5 }}>
      <Skeleton variant="text" width="40%" height={28} sx={{ mb: 1.5 }} />
      <Skeleton variant="rounded" height={height} />
    </Card>
  );
}

/** Placeholder for the dashboard hero + KPI row + pipeline/status cards, shown on first load. */
export function DashboardSkeleton() {
  return (
    <Stack spacing={3}>
      <Skeleton variant="rounded" height={148} />
      <Stack direction="row" spacing={2} flexWrap="wrap" useFlexGap>
        {Array.from({ length: 4 }).map((_, i) => (
          <Card key={i} sx={{ p: 2.5, flex: 1, minWidth: 190 }}>
            <Skeleton variant="text" width="60%" height={36} />
            <Skeleton variant="text" width="80%" />
          </Card>
        ))}
      </Stack>
      <Stack direction={{ xs: "column", md: "row" }} spacing={2}>
        <Box sx={{ flex: 2, minWidth: 0 }}>
          <CardSkeleton height={90} />
        </Box>
        <Box sx={{ flex: 1, minWidth: 240 }}>
          <CardSkeleton height={168} />
        </Box>
      </Stack>
      <CardSkeleton height={120} />
    </Stack>
  );
}
