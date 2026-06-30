import { Card, Stack, Typography } from "@mui/material";

export function KpiCard({ label, value }: { label: string; value: number | string }) {
  return (
    <Card sx={{ p: 2.5, minWidth: 160, flex: 1 }}>
      <Stack spacing={0.5}>
        <Typography variant="h4" sx={{ fontWeight: 800 }}>
          {value}
        </Typography>
        <Typography variant="body2" color="text.secondary">
          {label}
        </Typography>
      </Stack>
    </Card>
  );
}
