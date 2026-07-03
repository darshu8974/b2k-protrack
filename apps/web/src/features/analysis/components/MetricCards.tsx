import { Box, Card, Stack, Typography } from "@mui/material";

import { ConfidenceChip } from "../../../components/ai/ConfidenceChip";
import { metricLabel } from "../../../lib/labels";
import type { AnalysisMetric } from "../../../types/analysis";

/** The metric cards (pages/figures/equations/…) — counts come from the deterministic parser. */
export function MetricCards({ metrics }: { metrics: AnalysisMetric[] }) {
  if (metrics.length === 0) {
    return null;
  }
  return (
    <Box
      sx={{
        display: "grid",
        gap: 1.5,
        gridTemplateColumns: { xs: "repeat(2, 1fr)", sm: "repeat(3, 1fr)", md: "repeat(6, 1fr)" },
      }}
    >
      {metrics.map((metric) => (
        <Card key={metric.key} sx={{ p: 2 }}>
          <Stack spacing={0.5}>
            <Typography variant="h5" sx={{ fontWeight: 700 }}>
              {metric.value ?? "—"}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              {metricLabel(metric.key)}
            </Typography>
            <Box>
              <ConfidenceChip value={metric.confidence} />
            </Box>
          </Stack>
        </Card>
      ))}
    </Box>
  );
}
