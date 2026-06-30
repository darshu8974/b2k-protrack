import { Box, Stack, Typography } from "@mui/material";

import type { Stage, WorkflowStage } from "../../types/project";

/** Horizontal pipeline: stages before the current one are done, the current is active. */
export function StagePipeline({
  stages,
  currentStage,
}: {
  stages: WorkflowStage[];
  currentStage: Stage;
}) {
  const ordered = [...stages].sort((a, b) => a.sequence - b.sequence);
  const currentIndex = ordered.findIndex((s) => s.code === currentStage);

  return (
    <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap>
      {ordered.map((stage, index) => {
        const state =
          index < currentIndex ? "done" : index === currentIndex ? "active" : "upcoming";
        const color =
          state === "active" ? "primary.main" : state === "done" ? "success.main" : "divider";
        const textColor = state === "upcoming" ? "text.disabled" : "text.primary";
        return (
          <Box
            key={stage.code}
            sx={{
              px: 1.5,
              py: 0.75,
              borderRadius: 999,
              border: 2,
              borderColor: color,
              bgcolor: state === "active" ? "primary.light" : "transparent",
            }}
          >
            <Typography variant="caption" sx={{ fontWeight: 600, color: textColor }}>
              {index + 1}. {stage.name}
            </Typography>
          </Box>
        );
      })}
    </Stack>
  );
}
