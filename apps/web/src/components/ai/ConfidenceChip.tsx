import { Chip } from "@mui/material";

interface ConfidenceChipProps {
  value?: number | null;
  label?: string;
}

/** A small chip showing a 0–100 confidence/match score, colored by band. */
export function ConfidenceChip({ value, label }: ConfidenceChipProps) {
  if (value == null) {
    return <Chip size="small" variant="outlined" label="—" />;
  }
  const color = value >= 80 ? "success" : value >= 50 ? "warning" : "default";
  return (
    <Chip
      size="small"
      color={color}
      variant={color === "default" ? "outlined" : "filled"}
      label={label ? `${label} ${value}%` : `${value}%`}
    />
  );
}
