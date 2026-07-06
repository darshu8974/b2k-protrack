import { Box, Stack, Typography } from "@mui/material";

interface QualityRingProps {
  /** Preflight quality score 0–100 (null renders a dash). */
  score?: number | null;
  passed?: boolean | null;
  label?: string;
  size?: number;
}

const STROKE = 12;

function colorFor(score: number): string {
  if (score >= 75) return "#1F9D57"; // green
  if (score >= 50) return "#C9821A"; // amber
  return "#D64545"; // red
}

/** Lightweight SVG progress ring for the preflight quality score (no chart dependency). */
export function QualityRing({ score, passed, label = "Quality", size = 140 }: QualityRingProps) {
  const value = Math.min(100, Math.max(0, score ?? 0));
  const radius = (size - STROKE) / 2;
  const circumference = 2 * Math.PI * radius;
  const filled = (value / 100) * circumference;
  const color = colorFor(value);
  const center = size / 2;

  return (
    <Stack alignItems="center" spacing={0.5}>
      <Box sx={{ position: "relative", width: size, height: size }}>
        <svg viewBox={`0 0 ${size} ${size}`} width={size} height={size}>
          <circle
            cx={center}
            cy={center}
            r={radius}
            fill="none"
            stroke="#ECEFF3"
            strokeWidth={STROKE}
          />
          <circle
            cx={center}
            cy={center}
            r={radius}
            fill="none"
            stroke={color}
            strokeWidth={STROKE}
            strokeLinecap="round"
            strokeDasharray={`${filled} ${circumference}`}
            transform={`rotate(-90 ${center} ${center})`}
          />
          <text
            x={center}
            y={center - 2}
            textAnchor="middle"
            dominantBaseline="middle"
            fontSize={size * 0.26}
            fontWeight="700"
            fill="#1A2027"
          >
            {score == null ? "—" : value}
          </text>
          <text
            x={center}
            y={center + size * 0.18}
            textAnchor="middle"
            fontSize={size * 0.1}
            fill="#5A6472"
          >
            / 100
          </text>
        </svg>
      </Box>
      <Typography variant="body2" color="text.secondary">
        {label}
      </Typography>
      {passed != null && (
        <Typography
          variant="caption"
          sx={{ fontWeight: 700, color: passed ? "success.main" : "error.main" }}
        >
          {passed ? "PASSED" : "REVIEW NEEDED"}
        </Typography>
      )}
    </Stack>
  );
}
