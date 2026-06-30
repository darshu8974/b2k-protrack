import { createTheme } from "@mui/material/styles";

import { components } from "./components";
import { tokens } from "./palette";
import { typography } from "./typography";

/** The Protrack MUI theme, assembled from the approved design tokens. Light mode now; dark-ready. */
export const theme = createTheme({
  palette: {
    mode: "light",
    primary: { main: tokens.primary, light: tokens.primaryTint },
    secondary: { main: tokens.ai, light: tokens.aiTint },
    success: { main: tokens.success },
    warning: { main: tokens.warning },
    error: { main: tokens.danger },
    background: { default: tokens.canvas, paper: tokens.surface },
    divider: tokens.border,
    text: { primary: tokens.ink },
  },
  shape: { borderRadius: 12 },
  typography,
  components,
});
