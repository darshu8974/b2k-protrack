import CheckIcon from "@mui/icons-material/Check";
import { Box, Typography } from "@mui/material";

import type { Stage, WorkflowStage } from "../../types/project";

/**
 * Horizontal pipeline as a connected node stepper: completed stages are filled green with a
 * check, the current stage is a filled blue node, upcoming stages are hollow. Connector lines
 * fill in behind completed steps — mirroring the workspace design.
 */
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
    <Box sx={{ display: "flex", alignItems: "flex-start", width: "100%" }}>
      {ordered.map((stage, index) => {
        const state =
          index < currentIndex ? "done" : index === currentIndex ? "active" : "upcoming";
        const nodeBg =
          state === "done" ? "success.main" : state === "active" ? "primary.main" : "transparent";
        const nodeBorder =
          state === "done" ? "success.main" : state === "active" ? "primary.main" : "divider";
        const nodeText = state === "upcoming" ? "text.disabled" : "common.white";
        const labelColor = state === "upcoming" ? "text.disabled" : "text.primary";
        // Connector into this node is "reached" once the previous step is complete.
        const connectorReached = index <= currentIndex;

        return (
          <Box
            key={stage.code}
            sx={{
              flex: 1,
              minWidth: 64,
              display: "flex",
              flexDirection: "column",
              alignItems: "center",
              position: "relative",
            }}
          >
            {index > 0 && (
              <Box
                sx={{
                  position: "absolute",
                  top: 15,
                  right: "50%",
                  left: "-50%",
                  height: 2,
                  bgcolor: connectorReached ? "success.main" : "divider",
                }}
              />
            )}
            <Box
              sx={{
                width: 32,
                height: 32,
                borderRadius: "50%",
                bgcolor: nodeBg,
                border: 2,
                borderColor: nodeBorder,
                color: nodeText,
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                zIndex: 1,
                fontSize: 13,
                fontWeight: 700,
              }}
            >
              {state === "done" ? <CheckIcon sx={{ fontSize: 18 }} /> : index + 1}
            </Box>
            <Typography
              variant="caption"
              sx={{
                mt: 0.75,
                fontWeight: state === "active" ? 700 : 500,
                color: labelColor,
                textAlign: "center",
                lineHeight: 1.2,
              }}
            >
              {stage.name}
            </Typography>
          </Box>
        );
      })}
    </Box>
  );
}
