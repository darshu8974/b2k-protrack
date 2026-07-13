import type { ThemeOptions } from "@mui/material/styles";

import { tokens } from "./palette";

/** Component overrides: hairline cards with a whisper of elevation, rounded controls. */
export const components: ThemeOptions["components"] = {
  MuiCard: {
    defaultProps: { elevation: 0 },
    styleOverrides: {
      root: {
        border: `1px solid ${tokens.border}`,
        borderRadius: 12,
        // A whisper of depth so cards lift off the canvas like the design (still near-flat).
        boxShadow: "0 1px 2px rgba(16, 26, 31, 0.04)",
      },
    },
  },
  MuiButton: {
    defaultProps: { disableElevation: true },
    styleOverrides: {
      root: { borderRadius: 10, paddingInline: 16 },
      sizeLarge: { paddingBlock: 10 },
    },
  },
  MuiToggleButton: {
    styleOverrides: {
      root: { textTransform: "none", fontWeight: 600 },
    },
  },
  MuiOutlinedInput: {
    styleOverrides: {
      root: { borderRadius: 10 },
    },
  },
  MuiChip: {
    styleOverrides: {
      root: { fontWeight: 600 },
    },
  },
  MuiAppBar: {
    defaultProps: { elevation: 0 },
  },
};
