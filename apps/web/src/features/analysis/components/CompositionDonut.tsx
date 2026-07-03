import { Box, Stack, Typography } from "@mui/material";

import type { AnalysisCompositionSegment } from "../../../types/analysis";

const COLORS = ["#0B63CE", "#1F9D57", "#C9821A", "#6D5EF0", "#D64545", "#128F8B", "#8E44AD"];
const RADIUS = 54;
const CIRCUMFERENCE = 2 * Math.PI * RADIUS;

/** Lightweight SVG donut of document composition (no chart dependency). */
export function CompositionDonut({ segments }: { segments: AnalysisCompositionSegment[] }) {
  const data = segments.filter((s) => (s.percentage ?? 0) > 0);
  if (data.length === 0) {
    return (
      <Typography variant="body2" color="text.secondary">
        No composition data.
      </Typography>
    );
  }

  let offset = 0;
  const arcs = data.map((segment, index) => {
    const pct = segment.percentage ?? 0;
    const length = (pct / 100) * CIRCUMFERENCE;
    const arc = {
      color: COLORS[index % COLORS.length],
      dashArray: `${length} ${CIRCUMFERENCE - length}`,
      dashOffset: -offset,
    };
    offset += length;
    return arc;
  });

  return (
    <Stack direction={{ xs: "column", sm: "row" }} spacing={3} alignItems="center">
      <Box sx={{ position: "relative", width: 140, height: 140 }}>
        <svg viewBox="0 0 120 120" width="140" height="140">
          <g transform="rotate(-90 60 60)">
            <circle cx="60" cy="60" r={RADIUS} fill="none" stroke="#ECEFF3" strokeWidth="12" />
            {arcs.map((arc, index) => (
              <circle
                key={index}
                cx="60"
                cy="60"
                r={RADIUS}
                fill="none"
                stroke={arc.color}
                strokeWidth="12"
                strokeDasharray={arc.dashArray}
                strokeDashoffset={arc.dashOffset}
              />
            ))}
          </g>
        </svg>
      </Box>
      <Stack spacing={0.75} sx={{ flex: 1 }}>
        {data.map((segment, index) => (
          <Stack key={segment.segment} direction="row" spacing={1} alignItems="center">
            <Box sx={{ width: 12, height: 12, borderRadius: 0.5, bgcolor: COLORS[index % COLORS.length] }} />
            <Typography variant="body2" sx={{ flex: 1 }}>
              {segment.segment}
            </Typography>
            <Typography variant="body2" sx={{ fontWeight: 600 }}>
              {Math.round((segment.percentage ?? 0) * 10) / 10}%
            </Typography>
          </Stack>
        ))}
      </Stack>
    </Stack>
  );
}
