import { Box, Typography, useTheme } from "@mui/material";

import type { ThroughputPoint } from "../../../types/report";

const MONTH_LABELS = [
  "Jan", "Feb", "Mar", "Apr", "May", "Jun",
  "Jul", "Aug", "Sep", "Oct", "Nov", "Dec",
];

function shortMonth(month: string): string {
  // month is YYYY-MM
  const idx = Number(month.slice(5, 7)) - 1;
  return MONTH_LABELS[idx] ?? month;
}

/** Hand-rolled SVG vertical bar chart of titles completed per month (no chart library). */
export function ThroughputChart({ points }: { points: ThroughputPoint[] }) {
  const theme = useTheme();

  if (points.length === 0) {
    return (
      <Typography variant="body2" color="text.secondary">
        No data for this range.
      </Typography>
    );
  }

  const width = 640;
  const height = 220;
  const padding = { top: 20, right: 12, bottom: 28, left: 28 };
  const plotW = width - padding.left - padding.right;
  const plotH = height - padding.top - padding.bottom;
  const max = Math.max(1, ...points.map((p) => p.completed));
  const slot = plotW / points.length;
  const barW = Math.min(48, slot * 0.6);

  return (
    <Box sx={{ width: "100%", overflowX: "auto" }}>
      <svg
        viewBox={`0 0 ${width} ${height}`}
        width="100%"
        role="img"
        aria-label="Titles completed per month"
        style={{ maxWidth: width, display: "block" }}
      >
        {/* baseline */}
        <line
          x1={padding.left}
          y1={padding.top + plotH}
          x2={width - padding.right}
          y2={padding.top + plotH}
          stroke={theme.palette.divider}
        />
        {points.map((p, i) => {
          const barH = (p.completed / max) * plotH;
          const x = padding.left + i * slot + (slot - barW) / 2;
          const y = padding.top + plotH - barH;
          return (
            <g key={p.month}>
              {p.completed > 0 && (
                <text
                  x={x + barW / 2}
                  y={y - 6}
                  textAnchor="middle"
                  fontSize={11}
                  fill={theme.palette.text.secondary}
                >
                  {p.completed}
                </text>
              )}
              <rect
                x={x}
                y={y}
                width={barW}
                height={Math.max(0, barH)}
                rx={3}
                fill={theme.palette.primary.main}
              />
              <text
                x={x + barW / 2}
                y={padding.top + plotH + 18}
                textAnchor="middle"
                fontSize={11}
                fill={theme.palette.text.secondary}
              >
                {shortMonth(p.month)}
              </text>
            </g>
          );
        })}
      </svg>
    </Box>
  );
}
