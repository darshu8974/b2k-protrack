import type { ThemeOptions } from "@mui/material/styles";

import { tokens } from "./palette";

/** Component overrides: hairline cards, flat surfaces, rounded controls. */
export const components: ThemeOptions["components"] = {
  MuiCard: {
    defaultProps: { elevation: 0 },
    styleOverrides: {
      root: {
        border: `1px solid ${tokens.border}`,
        borderRadius: 12,
      },
    },
  },
  MuiButton: {
    defaultProps: { disableElevation: true },
  },
  MuiAppBar: {
    defaultProps: { elevation: 0 },
  },
};
