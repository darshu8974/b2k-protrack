import {
  Avatar,
  Box,
  Card,
  Chip,
  Divider,
  Stack,
  Tab,
  Tabs,
  Typography,
} from "@mui/material";
import { useState } from "react";
import { useParams } from "react-router-dom";

import { ErrorState } from "../../../components/feedback/ErrorState";
import { LoadingState } from "../../../components/feedback/LoadingState";
import { StagePipeline } from "../../../components/data/StagePipeline";
import {
  auditEventLabel,
  PRIORITY_COLOR,
  PRIORITY_LABEL,
  PUBLICATION_TYPE_LABEL,
  STAGE_LABEL,
  STATUS_COLOR,
  STATUS_LABEL,
} from "../../../lib/labels";
import { useAuth } from "../../auth/useAuth";
import { useProjectActivity } from "../../audit/hooks";
import { AnalysisPanel } from "../../analysis/AnalysisPanel";
import { DocumentsPanel } from "../../manuscripts/components/DocumentsPanel";
import { PackagePanel } from "../../package/components/PackagePanel";
import { useWorkflowStages } from "../../reference/hooks";
import { useProject, useProjectTimeline } from "../hooks";

function MetaRow({ label, value }: { label: string; value?: string | number | null }) {
  return (
    <Stack direction="row" justifyContent="space-between" sx={{ py: 0.5 }}>
      <Typography variant="body2" color="text.secondary">
        {label}
      </Typography>
      <Typography variant="body2" sx={{ fontWeight: 500, textAlign: "right" }}>
        {value ?? "—"}
      </Typography>
    </Stack>
  );
}

export function ProjectDetailsPage() {
  const { id = "" } = useParams();
  const { user } = useAuth();
  const [tab, setTab] = useState("overview");

  const { data: project, isLoading, isError } = useProject(id);
  const { data: timeline } = useProjectTimeline(id);
  const { data: activity } = useProjectActivity(id);
  const { data: stages } = useWorkflowStages();

  // The production-package tab mirrors the backend RBAC (Designer/PM/Admin — QA excluded).
  const canSeePackage = (user?.roles ?? []).some((role) =>
    ["DESIGNER", "PM", "ADMIN"].includes(role),
  );
  // If the active tab becomes unavailable (e.g. role change), fall back to a valid one.
  const activeTab = tab === "package" && !canSeePackage ? "overview" : tab;

  if (isLoading) {
    return <LoadingState />;
  }
  if (isError || !project) {
    return <ErrorState message="Project not found." />;
  }

  return (
    <Stack spacing={3}>
      <Box>
        <Typography variant="h4">{project.title}</Typography>
        <Stack direction="row" spacing={1} sx={{ mt: 1 }} flexWrap="wrap" useFlexGap>
          <Chip size="small" color="primary" label={STAGE_LABEL[project.currentStage]} />
          <Chip size="small" color={STATUS_COLOR[project.status]} label={STATUS_LABEL[project.status]} />
          <Chip
            size="small"
            variant="outlined"
            color={PRIORITY_COLOR[project.priority]}
            label={`${PRIORITY_LABEL[project.priority]} priority`}
          />
          {project.imprint && <Chip size="small" variant="outlined" label={project.imprint.name} />}
        </Stack>
      </Box>

      <Box sx={{ borderBottom: 1, borderColor: "divider" }}>
        <Tabs value={activeTab} onChange={(_, value) => setTab(value)}>
          <Tab label="Overview" value="overview" />
          <Tab label="Files" value="files" />
          <Tab label="AI Analysis" value="analysis" />
          {canSeePackage && <Tab label="Package" value="package" />}
        </Tabs>
      </Box>

      {activeTab === "overview" && (
        <Stack spacing={3}>
          {/* Stage pipeline */}
          {stages && (
            <Card sx={{ p: 2.5 }}>
              <Typography variant="subtitle1" sx={{ mb: 1.5 }}>
                Workflow
              </Typography>
              <StagePipeline stages={stages} currentStage={project.currentStage} />
            </Card>
          )}

          <Stack direction={{ xs: "column", md: "row" }} spacing={2} alignItems="flex-start">
            {/* Overview */}
            <Card sx={{ p: 2.5, flex: 1 }}>
              <Typography variant="subtitle1" sx={{ mb: 1 }}>
                Overview
              </Typography>
              <MetaRow label="Type" value={PUBLICATION_TYPE_LABEL[project.publicationType]} />
              <MetaRow label="Discipline" value={project.discipline} />
              <MetaRow label="ISBN" value={project.isbn} />
              <MetaRow label="Imprint" value={project.imprint?.name} />
              <MetaRow label="Extent" value={project.pageExtent ? `${project.pageExtent} pp` : null} />
              <MetaRow label="Trim size" value={project.trimSize} />
              <MetaRow label="Owner" value={project.owner?.fullName} />
              <MetaRow label="Created" value={project.createdDate} />
              <MetaRow label="Due" value={project.dueDate} />
              {project.brief && (
                <>
                  <Divider sx={{ my: 1.5 }} />
                  <Typography variant="body2" color="text.secondary" sx={{ mb: 0.5 }}>
                    Brief
                  </Typography>
                  <Typography variant="body2">{project.brief}</Typography>
                </>
              )}
            </Card>

            {/* Team */}
            <Card sx={{ p: 2.5, flex: 1 }}>
              <Typography variant="subtitle1" sx={{ mb: 1.5 }}>
                Team
              </Typography>
              <Stack spacing={1.5}>
                {project.members.map((member) => (
                  <Stack key={member.userId} direction="row" spacing={1.5} alignItems="center">
                    <Avatar sx={{ width: 32, height: 32, fontSize: 13 }}>
                      {member.avatarInitials ?? "?"}
                    </Avatar>
                    <Box sx={{ flex: 1 }}>
                      <Typography variant="body2" sx={{ fontWeight: 600 }}>
                        {member.fullName ?? member.userId}
                      </Typography>
                      <Typography variant="caption" color="text.secondary">
                        {member.roleInProject ?? "Member"}
                      </Typography>
                    </Box>
                    {member.owner && <Chip size="small" label="Owner" />}
                    {member.matchScore != null && (
                      <Chip size="small" variant="outlined" label={`${member.matchScore}% match`} />
                    )}
                  </Stack>
                ))}
              </Stack>
            </Card>
          </Stack>

          {/* Timeline */}
          <Card sx={{ p: 2.5 }}>
            <Typography variant="subtitle1" sx={{ mb: 1.5 }}>
              Workflow timeline
            </Typography>
            {timeline && timeline.length > 0 ? (
              <Stack spacing={1}>
                {timeline.map((entry, index) => (
                  <Stack key={index} direction="row" spacing={1} alignItems="baseline">
                    <Typography variant="body2" sx={{ minWidth: 280 }}>
                      {entry.fromStage ? `${STAGE_LABEL[entry.fromStage as never] ?? entry.fromStage} → ` : ""}
                      <strong>{STAGE_LABEL[entry.toStage as never] ?? entry.toStage}</strong>
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {entry.triggeredByName ?? "—"} ({entry.triggeredRole}) ·{" "}
                      {new Date(entry.occurredAt).toLocaleString()}
                      {entry.note ? ` · ${entry.note}` : ""}
                    </Typography>
                  </Stack>
                ))}
              </Stack>
            ) : (
              <Typography variant="body2" color="text.secondary">
                No transitions yet.
              </Typography>
            )}
          </Card>

          {/* Activity (audit trail) */}
          <Card sx={{ p: 2.5 }}>
            <Typography variant="subtitle1" sx={{ mb: 1.5 }}>
              Activity
            </Typography>
            {activity && activity.length > 0 ? (
              <Stack spacing={1}>
                {activity.map((entry) => (
                  <Stack key={entry.id} direction="row" spacing={1} alignItems="baseline">
                    <Chip size="small" variant="outlined" label={auditEventLabel(entry.eventType)} />
                    <Typography variant="body2" sx={{ flex: 1 }}>
                      {entry.summary}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {entry.actorName ?? "—"} · {new Date(entry.createdAt).toLocaleString()}
                    </Typography>
                  </Stack>
                ))}
              </Stack>
            ) : (
              <Typography variant="body2" color="text.secondary">
                No activity yet.
              </Typography>
            )}
          </Card>
        </Stack>
      )}

      {activeTab === "files" && <DocumentsPanel projectId={project.id} />}

      {activeTab === "analysis" && (
        <AnalysisPanel projectId={project.id} currentStage={project.currentStage} />
      )}

      {activeTab === "package" && canSeePackage && <PackagePanel projectId={project.id} />}
    </Stack>
  );
}
