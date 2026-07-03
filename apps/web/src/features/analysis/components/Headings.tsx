import { Box, Stack, Typography } from "@mui/material";

import type { AnalysisHeadingCount } from "../../../types/analysis";

/** Heading structure breakdown (H1/H2/H3 counts) as simple proportional bars. */
export function Headings({ headings }: { headings: AnalysisHeadingCount[] }) {
  if (headings.length === 0) {
    return (
      <Typography variant="body2" color="text.secondary">
        No headings detected.
      </Typography>
    );
  }
  const max = Math.max(1, ...headings.map((h) => h.count));
  return (
    <Stack spacing={1}>
      {headings.map((heading) => (
        <Stack key={heading.level} direction="row" spacing={1.5} alignItems="center">
          <Typography variant="body2" sx={{ width: 32, fontWeight: 600 }}>
            {heading.level}
          </Typography>
          <Box sx={{ flex: 1, bgcolor: "action.hover", borderRadius: 1, height: 10 }}>
            <Box
              sx={{
                width: `${(heading.count / max) * 100}%`,
                bgcolor: "primary.main",
                borderRadius: 1,
                height: 10,
              }}
            />
          </Box>
          <Typography variant="body2" sx={{ width: 32, textAlign: "right" }}>
            {heading.count}
          </Typography>
        </Stack>
      ))}
    </Stack>
  );
}
