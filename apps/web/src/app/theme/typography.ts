import type { ThemeOptions } from "@mui/material/styles";

/** Inter as the single UI typeface, per the design system. */
export const typography: ThemeOptions["typography"] = {
  fontFamily: "Inter, system-ui, Arial, sans-serif",
  h4: { fontWeight: 800 },
  h5: { fontWeight: 700 },
  h6: { fontWeight: 700 },
  subtitle1: { fontWeight: 600 },
  subtitle2: { fontWeight: 600 },
  button: { textTransform: "none", fontWeight: 600 },
};
