import { Box, Stack, Typography } from "@mui/material";

import { ConfidenceChip } from "../../../components/ai/ConfidenceChip";
import type { AnalysisTeamSuggestion } from "../../../types/analysis";

/** AI-suggested team roles with match score + rationale. */
export function SuggestedTeam({ team }: { team: AnalysisTeamSuggestion[] }) {
  if (team.length === 0) {
    return (
      <Typography variant="body2" color="text.secondary">
        No team suggestions.
      </Typography>
    );
  }
  return (
    <Stack spacing={1.5}>
      {team.map((member, index) => (
        <Box key={index} sx={{ p: 1.5, border: 1, borderColor: "divider", borderRadius: 1 }}>
          <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 0.5 }}>
            <Typography variant="body2" sx={{ fontWeight: 600, flex: 1 }}>
              {member.role ?? "Suggested role"}
            </Typography>
            <ConfidenceChip value={member.matchScore} label="match" />
          </Stack>
          {member.rationale && (
            <Typography variant="body2" color="text.secondary">
              {member.rationale}
            </Typography>
          )}
        </Box>
      ))}
    </Stack>
  );
}
