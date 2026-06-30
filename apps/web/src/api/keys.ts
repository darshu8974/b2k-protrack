/**
 * Centralized query-key factory. Feature keys are added here as features land, keeping
 * cache invalidation consistent across the app.
 */
export const queryKeys = {
  health: ["health"] as const,
};
