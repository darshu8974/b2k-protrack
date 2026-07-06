import { Box, Card, Chip, Stack, Typography } from "@mui/material";

import { QualityRing } from "../../../components/data/QualityRing";
import type { PreflightDetail } from "../../../types/preflight";
import { PreflightChecklist } from "./PreflightChecklist";

/** Read-only preflight results: the quality ring + issue totals + the six resolved checks. */
export function PreflightSummary({ preflight }: { preflight: PreflightDetail }) {
  return (
    <Card sx={{ p: 2.5 }}>
      <Stack direction={{ xs: "column", md: "row" }} spacing={3} alignItems={{ md: "center" }}>
        <QualityRing score={preflight.overallScore} passed={preflight.passed} />
        <Box sx={{ flex: 1 }}>
          <Stack direction="row" spacing={1} sx={{ mb: 1.5 }} flexWrap="wrap" useFlexGap>
            <Typography variant="subtitle1">Preflight results</Typography>
            {preflight.standard && (
              <Chip size="small" variant="outlined" label={preflight.standard} />
            )}
            <Chip size="small" variant="outlined" label={`${preflight.totalIssues} issue(s)`} />
            {preflight.highSeverity > 0 && (
              <Chip size="small" color="error" label={`${preflight.highSeverity} high`} />
            )}
            {preflight.ranAt && (
              <Typography variant="caption" color="text.secondary" sx={{ alignSelf: "center" }}>
                {new Date(preflight.ranAt).toLocaleString()}
              </Typography>
            )}
          </Stack>
          <PreflightChecklist checks={preflight.checks} />
        </Box>
      </Stack>
    </Card>
  );
}
