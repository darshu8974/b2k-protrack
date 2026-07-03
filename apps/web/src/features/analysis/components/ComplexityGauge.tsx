import { Box, Stack, Typography } from "@mui/material";

interface ComplexityGaugeProps {
  score?: number | null;
  label?: string | null;
}

const ARC_PATH = "M 20 100 A 80 80 0 0 1 180 100";
const ARC_LENGTH = Math.PI * 80;

function colorFor(score: number): string {
  if (score >= 70) return "#D64545";
  if (score >= 40) return "#C9821A";
  return "#1F9D57";
}

/** Lightweight SVG semicircle gauge for the production-complexity score (no chart dependency). */
export function ComplexityGauge({ score, label }: ComplexityGaugeProps) {
  const value = Math.min(100, Math.max(0, score ?? 0));
  const filled = (value / 100) * ARC_LENGTH;
  const color = colorFor(value);

  return (
    <Stack alignItems="center" spacing={0.5}>
      <Box sx={{ position: "relative", width: 200, height: 110 }}>
        <svg viewBox="0 0 200 110" width="200" height="110">
          <path d={ARC_PATH} fill="none" stroke="#ECEFF3" strokeWidth="14" strokeLinecap="round" />
          <path
            d={ARC_PATH}
            fill="none"
            stroke={color}
            strokeWidth="14"
            strokeLinecap="round"
            strokeDasharray={`${filled} ${ARC_LENGTH}`}
          />
          <text x="100" y="92" textAnchor="middle" fontSize="30" fontWeight="700" fill="#1A2027">
            {score == null ? "—" : value}
          </text>
        </svg>
      </Box>
      <Typography variant="body2" color="text.secondary">
        {label ?? "Complexity"}
      </Typography>
    </Stack>
  );
}
