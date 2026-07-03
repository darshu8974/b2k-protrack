import { Box, Chip, Stack, Typography } from "@mui/material";

import { SEVERITY_COLOR, SEVERITY_LABEL } from "../../../lib/labels";
import type { AnalysisRisk } from "../../../types/analysis";

/** Flagged production risks as cards, colored by severity. */
export function Risks({ risks }: { risks: AnalysisRisk[] }) {
  if (risks.length === 0) {
    return (
      <Typography variant="body2" color="text.secondary">
        No risks flagged.
      </Typography>
    );
  }
  return (
    <Stack spacing={1.5}>
      {risks.map((risk, index) => (
        <Box key={index} sx={{ p: 1.5, border: 1, borderColor: "divider", borderRadius: 1 }}>
          <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 0.5 }}>
            <Chip
              size="small"
              color={SEVERITY_COLOR[risk.severity] ?? "default"}
              label={SEVERITY_LABEL[risk.severity] ?? risk.severity}
            />
            <Typography variant="body2" sx={{ fontWeight: 600 }}>
              {risk.title}
            </Typography>
          </Stack>
          {risk.description && (
            <Typography variant="body2" color="text.secondary">
              {risk.description}
            </Typography>
          )}
        </Box>
      ))}
    </Stack>
  );
}
