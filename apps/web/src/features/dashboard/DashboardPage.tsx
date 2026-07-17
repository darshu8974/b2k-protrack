import ArrowForwardIcon from "@mui/icons-material/ArrowForward";
import BrushOutlinedIcon from "@mui/icons-material/BrushOutlined";
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutline";
import FactCheckOutlinedIcon from "@mui/icons-material/FactCheckOutlined";
import FolderOpenOutlinedIcon from "@mui/icons-material/FolderOpenOutlined";
import LibraryBooksOutlinedIcon from "@mui/icons-material/LibraryBooksOutlined";
import { Box, Button, Card, Stack, Typography } from "@mui/material";
import type { ReactNode } from "react";
import { useNavigate } from "react-router-dom";

import { paths } from "../../app/router/paths";
import { tokens } from "../../app/theme/palette";
import { Can } from "../../components/auth/Can";
import { ProjectsTable } from "../../components/data/ProjectsTable";
import { EmptyState } from "../../components/feedback/EmptyState";
import { ErrorState } from "../../components/feedback/ErrorState";
import { LoadingState } from "../../components/feedback/LoadingState";
import { STAGE_LABEL, STATUS_LABEL } from "../../lib/labels";
import type { AppError } from "../../types/api";
import type { ProjectStatus, Stage } from "../../types/project";
import { useAuth } from "../auth/useAuth";
import { useDashboard } from "./hooks";

/** A cohesive hue per pipeline stage / status for the dashboard charts (SVG needs hex, not MUI names). */
const STAGE_HEX: Record<Stage, string> = {
  INTAKE: "#94A3B8",
  AI_ANALYSIS: "#6D5EF0",
  DESIGN_PREP: "#0EA5E9",
  IN_PRODUCTION: "#F59E0B",
  PDF_REVIEW: "#0B63CE",
  QC_REVIEW: "#0891B2",
  QA_SIGNOFF: "#8B5CF6",
  COMPLETED: "#1F9D57",
};

const STATUS_HEX: Record<ProjectStatus, string> = {
  ACTIVE: "#0B63CE",
  ON_HOLD: "#C9821A",
  COMPLETED: "#1F9D57",
  ARCHIVED: "#94A3B8",
};

function greeting(): string {
  const h = new Date().getHours();
  if (h < 12) return "Good morning";
  if (h < 18) return "Good afternoon";
  return "Good evening";
}

/** Thin SVG progress ring used in the hero (completion rate). */
function Ring({ pct, label, sublabel }: { pct: number; label: string; sublabel: string }) {
  const size = 108;
  const stroke = 10;
  const r = (size - stroke) / 2;
  const c = 2 * Math.PI * r;
  const dash = (Math.min(100, Math.max(0, pct)) / 100) * c;
  return (
    <Box sx={{ position: "relative", width: size, height: size, flexShrink: 0 }}>
      <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`}>
        <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke="rgba(255,255,255,0.25)" strokeWidth={stroke} />
        <circle
          cx={size / 2}
          cy={size / 2}
          r={r}
          fill="none"
          stroke="#FFFFFF"
          strokeWidth={stroke}
          strokeLinecap="round"
          strokeDasharray={`${dash} ${c - dash}`}
          strokeDashoffset={c / 4}
          transform={`rotate(-90 ${size / 2} ${size / 2})`}
        />
      </svg>
      <Box
        sx={{
          position: "absolute",
          inset: 0,
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
          color: "common.white",
        }}
      >
        <Typography sx={{ fontWeight: 800, fontSize: 24, lineHeight: 1 }}>{label}</Typography>
        <Typography sx={{ fontSize: 10.5, opacity: 0.85, letterSpacing: 0.4 }}>{sublabel}</Typography>
      </Box>
    </Box>
  );
}

/** Rich KPI card: tinted icon chip, big figure, and a share-of-total mini bar. */
function StatCard({
  label,
  value,
  icon,
  color,
  tint,
  share,
}: {
  label: string;
  value: number;
  icon: ReactNode;
  color: string;
  tint: string;
  share: number;
}) {
  return (
    <Card sx={{ p: 2.5, flex: 1, minWidth: 190, position: "relative", overflow: "hidden" }}>
      {/* accent hairline */}
      <Box sx={{ position: "absolute", top: 0, left: 0, right: 0, height: 3, bgcolor: color }} />
      <Stack direction="row" alignItems="center" justifyContent="space-between">
        <Box>
          <Typography sx={{ fontWeight: 800, fontSize: 30, lineHeight: 1.05 }}>{value}</Typography>
          <Typography variant="body2" color="text.secondary" sx={{ mt: 0.25 }}>
            {label}
          </Typography>
        </Box>
        <Box
          sx={{
            width: 44,
            height: 44,
            borderRadius: 2.5,
            bgcolor: tint,
            color,
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            "& .MuiSvgIcon-root": { fontSize: 24 },
          }}
        >
          {icon}
        </Box>
      </Stack>
      <Box sx={{ mt: 1.75, height: 6, borderRadius: 3, bgcolor: "action.hover", overflow: "hidden" }}>
        <Box
          sx={{
            width: `${Math.round(Math.min(1, Math.max(0, share)) * 100)}%`,
            height: "100%",
            borderRadius: 3,
            bgcolor: color,
            transition: "width .5s ease",
          }}
        />
      </Box>
    </Card>
  );
}

/** SVG donut chart for status distribution, with a total in the centre. */
function StatusDonut({ data, total }: { data: { label: string; value: number; color: string }[]; total: number }) {
  const size = 168;
  const stroke = 20;
  const r = (size - stroke) / 2;
  const c = 2 * Math.PI * r;
  let offset = 0;

  return (
    <Box sx={{ position: "relative", width: size, height: size, mx: "auto" }}>
      <svg width={size} height={size} viewBox={`0 0 ${size} ${size}`}>
        <circle cx={size / 2} cy={size / 2} r={r} fill="none" stroke={tokens.border} strokeWidth={stroke} />
        {total > 0 &&
          data
            .filter((d) => d.value > 0)
            .map((d) => {
              const frac = d.value / total;
              const dash = frac * c;
              const el = (
                <circle
                  key={d.label}
                  cx={size / 2}
                  cy={size / 2}
                  r={r}
                  fill="none"
                  stroke={d.color}
                  strokeWidth={stroke}
                  strokeDasharray={`${dash} ${c - dash}`}
                  strokeDashoffset={-offset}
                  transform={`rotate(-90 ${size / 2} ${size / 2})`}
                />
              );
              offset += dash;
              return el;
            })}
      </svg>
      <Box
        sx={{
          position: "absolute",
          inset: 0,
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
        }}
      >
        <Typography sx={{ fontWeight: 800, fontSize: 26, lineHeight: 1 }}>{total}</Typography>
        <Typography variant="caption" color="text.secondary">
          projects
        </Typography>
      </Box>
    </Box>
  );
}

function LegendDot({ color }: { color: string }) {
  return <Box sx={{ width: 9, height: 9, borderRadius: "50%", bgcolor: color, flexShrink: 0 }} />;
}

function SectionCard({
  title,
  action,
  children,
}: {
  title: string;
  action?: ReactNode;
  children: ReactNode;
}) {
  return (
    <Card sx={{ p: 0 }}>
      <Stack
        direction="row"
        alignItems="center"
        justifyContent="space-between"
        sx={{ px: 2.5, py: 1.75 }}
      >
        <Typography variant="subtitle1" sx={{ fontWeight: 700 }}>
          {title}
        </Typography>
        {action}
      </Stack>
      {children}
    </Card>
  );
}

export function DashboardPage() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const { data, isLoading, isError, error } = useDashboard();
  const firstName = user?.fullName?.split(" ")[0] ?? "there";

  if (isLoading) {
    return <LoadingState />;
  }
  if (isError || !data) {
    return <ErrorState message={(error as unknown as AppError | null)?.message} />;
  }

  const { kpis } = data;
  const total = kpis.totalProjects || 0;
  const completedTotal = data.statusCounts.find((s) => s.status === "COMPLETED")?.count ?? 0;
  const completionPct = total > 0 ? Math.round((completedTotal / total) * 100) : 0;

  const stageSegments = data.stageCounts
    .map((s) => ({ stage: s.stage, count: s.count, color: STAGE_HEX[s.stage] }))
    .filter((s) => s.count > 0);
  const stageTotal = data.stageCounts.reduce((sum, s) => sum + s.count, 0);

  const statusData = data.statusCounts.map((s) => ({
    label: STATUS_LABEL[s.status],
    value: s.count,
    color: STATUS_HEX[s.status],
  }));
  const statusTotal = data.statusCounts.reduce((sum, s) => sum + s.count, 0);

  return (
    <Stack spacing={3}>
      {/* ── Hero ─────────────────────────────────────────────────────────── */}
      <Card
        sx={{
          p: { xs: 2.5, md: 3.5 },
          color: "common.white",
          background: `linear-gradient(120deg, ${tokens.primary} 0%, #2C5BD6 45%, ${tokens.ai} 100%)`,
          position: "relative",
          overflow: "hidden",
        }}
      >
        {/* decorative glow */}
        <Box
          sx={{
            position: "absolute",
            right: -60,
            top: -80,
            width: 260,
            height: 260,
            borderRadius: "50%",
            background: "radial-gradient(circle, rgba(255,255,255,0.18) 0%, rgba(255,255,255,0) 70%)",
          }}
        />
        <Stack
          direction={{ xs: "column", sm: "row" }}
          alignItems={{ sm: "center" }}
          justifyContent="space-between"
          spacing={3}
          sx={{ position: "relative" }}
        >
          <Box>
            <Typography sx={{ opacity: 0.85, fontSize: 13, letterSpacing: 0.3 }}>
              {new Date().toLocaleDateString(undefined, {
                weekday: "long",
                month: "long",
                day: "numeric",
              })}
            </Typography>
            <Typography variant="h4" sx={{ fontWeight: 800, mt: 0.5 }}>
              {greeting()}, {firstName}
            </Typography>
            <Typography sx={{ opacity: 0.9, mt: 0.75, maxWidth: 460 }}>
              {total > 0
                ? `You're tracking ${total} title${total === 1 ? "" : "s"} — ${kpis.activeProjects} active, ${kpis.inProduction} in production, ${kpis.awaitingQa} awaiting QA.`
                : "No titles yet. Create your first project to kick off the publishing pipeline."}
            </Typography>
            <Can roles={["PROJECT_MANAGER", "ADMIN"]}>
              <Button
                variant="contained"
                endIcon={<ArrowForwardIcon />}
                onClick={() => navigate(paths.projectNew)}
                sx={{
                  mt: 2,
                  bgcolor: "common.white",
                  color: "primary.main",
                  fontWeight: 700,
                  "&:hover": { bgcolor: "rgba(255,255,255,0.9)" },
                }}
              >
                New project
              </Button>
            </Can>
          </Box>
          <Ring pct={completionPct} label={`${completionPct}%`} sublabel="COMPLETED" />
        </Stack>
      </Card>

      {/* ── KPI row ──────────────────────────────────────────────────────── */}
      <Stack direction="row" spacing={2} flexWrap="wrap" useFlexGap>
        <StatCard
          label="Active projects"
          value={kpis.activeProjects}
          icon={<FolderOpenOutlinedIcon />}
          color={tokens.primary}
          tint={tokens.primaryTint}
          share={total ? kpis.activeProjects / total : 0}
        />
        <StatCard
          label="In production"
          value={kpis.inProduction}
          icon={<BrushOutlinedIcon />}
          color={tokens.ai}
          tint={tokens.aiTint}
          share={total ? kpis.inProduction / total : 0}
        />
        <StatCard
          label="Awaiting QA"
          value={kpis.awaitingQa}
          icon={<FactCheckOutlinedIcon />}
          color={tokens.warning}
          tint="#FBF1E3"
          share={total ? kpis.awaitingQa / total : 0}
        />
        <StatCard
          label="Completed this month"
          value={kpis.completedThisMonth}
          icon={<CheckCircleOutlineIcon />}
          color={tokens.success}
          tint="#E7F5EC"
          share={total ? kpis.completedThisMonth / total : 0}
        />
      </Stack>

      {/* ── Pipeline + Status ────────────────────────────────────────────── */}
      <Stack direction={{ xs: "column", md: "row" }} spacing={2} alignItems="stretch">
        {/* Pipeline funnel */}
        <Card sx={{ p: 2.5, flex: 2, minWidth: 0 }}>
          <Typography variant="subtitle1" sx={{ fontWeight: 700, mb: 2 }}>
            Production pipeline
          </Typography>

          {stageTotal > 0 ? (
            <>
              {/* segmented bar */}
              <Box
                sx={{
                  display: "flex",
                  height: 14,
                  borderRadius: 999,
                  overflow: "hidden",
                  bgcolor: "action.hover",
                }}
              >
                {stageSegments.map((s) => (
                  <Box
                    key={s.stage}
                    title={`${STAGE_LABEL[s.stage]}: ${s.count}`}
                    sx={{
                      flexGrow: s.count,
                      flexBasis: 0,
                      minWidth: 4,
                      bgcolor: s.color,
                    }}
                  />
                ))}
              </Box>

              {/* legend grid */}
              <Box
                sx={{
                  mt: 2.5,
                  display: "grid",
                  gridTemplateColumns: { xs: "1fr 1fr", sm: "1fr 1fr 1fr 1fr" },
                  gap: 1.5,
                }}
              >
                {data.stageCounts.map((s) => (
                  <Stack key={s.stage} direction="row" spacing={1} alignItems="center">
                    <LegendDot color={STAGE_HEX[s.stage]} />
                    <Box sx={{ minWidth: 0 }}>
                      <Typography variant="body2" sx={{ fontWeight: 700, lineHeight: 1.1 }}>
                        {s.count}
                      </Typography>
                      <Typography variant="caption" color="text.secondary" noWrap sx={{ display: "block" }}>
                        {STAGE_LABEL[s.stage]}
                      </Typography>
                    </Box>
                  </Stack>
                ))}
              </Box>
            </>
          ) : (
            <Box sx={{ py: 4, textAlign: "center", color: "text.secondary" }}>
              <Typography variant="body2">No titles are in the pipeline yet.</Typography>
            </Box>
          )}
        </Card>

        {/* Status donut */}
        <Card sx={{ p: 2.5, flex: 1, minWidth: 240 }}>
          <Typography variant="subtitle1" sx={{ fontWeight: 700, mb: 2 }}>
            By status
          </Typography>
          <StatusDonut data={statusData} total={statusTotal} />
          <Stack spacing={1} sx={{ mt: 2.5 }}>
            {statusData.map((s) => (
              <Stack key={s.label} direction="row" alignItems="center" justifyContent="space-between">
                <Stack direction="row" spacing={1} alignItems="center">
                  <LegendDot color={s.color} />
                  <Typography variant="body2">{s.label}</Typography>
                </Stack>
                <Typography variant="body2" sx={{ fontWeight: 700 }}>
                  {s.value}
                </Typography>
              </Stack>
            ))}
          </Stack>
        </Card>
      </Stack>

      {/* ── Recent + My projects ─────────────────────────────────────────── */}
      <SectionCard
        title="Recent projects"
        action={
          <Button size="small" endIcon={<ArrowForwardIcon />} onClick={() => navigate(paths.projects)}>
            View all
          </Button>
        }
      >
        <ProjectsTable
          projects={data.recentProjects}
          emptyState={
            <EmptyState
              icon={<LibraryBooksOutlinedIcon />}
              title="No projects yet"
              message="Create your first title to kick off the publishing pipeline."
              action={
                <Can roles={["PROJECT_MANAGER", "ADMIN"]}>
                  <Button variant="contained" onClick={() => navigate(paths.projectNew)}>
                    New project
                  </Button>
                </Can>
              }
            />
          }
        />
      </SectionCard>

      <SectionCard title="My projects">
        <ProjectsTable
          projects={data.myProjects}
          emptyState={
            <EmptyState
              icon={<FolderOpenOutlinedIcon />}
              title="Nothing assigned to you"
              message="Projects you own or are a member of will show up here."
            />
          }
        />
      </SectionCard>
    </Stack>
  );
}
