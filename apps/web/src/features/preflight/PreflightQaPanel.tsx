import RuleFolderIcon from "@mui/icons-material/RuleFolder";
import { Card, Typography } from "@mui/material";

import type { Stage } from "../../types/project";
import { CompletedPanel } from "./components/CompletedPanel";
import { PreflightPanel } from "./components/PreflightPanel";
import { QaSignoffPanel } from "./components/QaSignoffPanel";
import { UploadPdfPanel } from "./components/UploadPdfPanel";

interface PreflightQaPanelProps {
  projectId: string;
  currentStage: Stage;
}

/**
 * The post-InDesign workspace: a stage-adaptive panel that presents the Upload PDF → Preflight →
 * QA Sign-off → Completed flow, mirroring the Sprint-4 AnalysisPanel (one workspace tab, not a
 * separate route). Actions within each sub-panel are role-gated; the backend is the source of truth.
 */
export function PreflightQaPanel({ projectId, currentStage }: PreflightQaPanelProps) {
  switch (currentStage) {
    case "IN_PRODUCTION":
      return <UploadPdfPanel projectId={projectId} />;
    case "PDF_REVIEW":
      return <PreflightPanel projectId={projectId} />;
    case "QA_SIGNOFF":
      return <QaSignoffPanel projectId={projectId} />;
    case "COMPLETED":
      return <CompletedPanel projectId={projectId} />;
    default:
      return (
        <Card sx={{ p: 4, textAlign: "center" }}>
          <RuleFolderIcon color="disabled" sx={{ fontSize: 40, mb: 1 }} />
          <Typography sx={{ mb: 0.5 }}>Preflight &amp; QA not started</Typography>
          <Typography variant="body2" color="text.secondary">
            Once the project reaches production, the designer uploads the production PDF here to run
            AI preflight and QA sign-off.
          </Typography>
        </Card>
      );
  }
}
