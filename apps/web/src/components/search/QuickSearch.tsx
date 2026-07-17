import ArrowForwardIcon from "@mui/icons-material/ArrowForward";
import SearchIcon from "@mui/icons-material/Search";
import { Box, Chip, CircularProgress, Dialog, InputBase, Stack, Typography } from "@mui/material";
import { useState } from "react";
import { useNavigate } from "react-router-dom";

import { paths } from "../../app/router/paths";
import { useDebounce } from "../../hooks/useDebounce";
import { STAGE_LABEL } from "../../lib/labels";
import { useProjects } from "../../features/projects/hooks";

/**
 * Command-style project quick-search. Mounted only while open (so the query is idle otherwise);
 * typing searches projects by title/ISBN and Enter/click jumps to a result. Opened from the top-bar
 * search affordance or ⌘K / Ctrl+K.
 */
export function QuickSearch({ onClose }: { onClose: () => void }) {
  const navigate = useNavigate();
  const [term, setTerm] = useState("");
  const q = useDebounce(term).trim();
  const { data, isFetching } = useProjects({ page: 0, size: 8, q: q || undefined });
  const results = q ? (data?.content ?? []) : [];

  const go = (path: string) => {
    onClose();
    navigate(path);
  };

  return (
    <Dialog
      open
      onClose={onClose}
      fullWidth
      maxWidth="sm"
      slotProps={{ paper: { sx: { position: "fixed", top: 88, m: 0, borderRadius: 3, overflow: "hidden" } } }}
    >
      {/* search input */}
      <Stack
        direction="row"
        spacing={1.5}
        alignItems="center"
        sx={{ px: 2, py: 1.5, borderBottom: 1, borderColor: "divider" }}
      >
        <SearchIcon sx={{ color: "text.secondary" }} />
        <InputBase
          autoFocus
          fullWidth
          placeholder="Search projects by title or ISBN…"
          value={term}
          onChange={(e) => setTerm(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter" && results[0]) {
              go(paths.project(results[0].id));
            }
          }}
          sx={{ fontSize: 15 }}
        />
        {isFetching && <CircularProgress size={16} />}
        <Typography
          variant="caption"
          color="text.secondary"
          sx={{ border: 1, borderColor: "divider", borderRadius: 1, px: 0.75, py: 0.1 }}
        >
          Esc
        </Typography>
      </Stack>

      {/* results */}
      <Box sx={{ maxHeight: 360, overflowY: "auto" }}>
        {!q && (
          <Typography variant="body2" color="text.secondary" sx={{ px: 2, py: 2.5 }}>
            Start typing to search your projects.
          </Typography>
        )}
        {q && results.length === 0 && !isFetching && (
          <Typography variant="body2" color="text.secondary" sx={{ px: 2, py: 2.5 }}>
            No projects match “{q}”.
          </Typography>
        )}
        {results.map((p) => (
          <Box
            key={p.id}
            onClick={() => go(paths.project(p.id))}
            sx={{
              px: 2,
              py: 1.25,
              cursor: "pointer",
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
              gap: 1,
              "&:hover": { bgcolor: "action.hover" },
            }}
          >
            <Box sx={{ minWidth: 0 }}>
              <Typography variant="body2" sx={{ fontWeight: 600 }} noWrap>
                {p.title}
              </Typography>
              {p.isbn && (
                <Typography variant="caption" color="text.secondary">
                  ISBN {p.isbn}
                </Typography>
              )}
            </Box>
            <Chip size="small" label={STAGE_LABEL[p.currentStage]} sx={{ flexShrink: 0 }} />
          </Box>
        ))}
      </Box>

      {/* footer: escape hatch to the full list */}
      {q && (
        <Box
          onClick={() => go(paths.projects)}
          sx={{
            px: 2,
            py: 1.25,
            borderTop: 1,
            borderColor: "divider",
            cursor: "pointer",
            display: "flex",
            alignItems: "center",
            gap: 0.75,
            color: "primary.main",
            "&:hover": { bgcolor: "action.hover" },
          }}
        >
          <Typography variant="body2" sx={{ fontWeight: 600 }}>
            Browse all projects
          </Typography>
          <ArrowForwardIcon sx={{ fontSize: 16 }} />
        </Box>
      )}
    </Dialog>
  );
}
