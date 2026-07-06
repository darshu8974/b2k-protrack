import DoneAllIcon from "@mui/icons-material/DoneAll";
import {
  Box,
  Button,
  Chip,
  CircularProgress,
  Divider,
  List,
  ListItemButton,
  Popover,
  ToggleButton,
  ToggleButtonGroup,
  Typography,
} from "@mui/material";
import { useState } from "react";
import { useNavigate } from "react-router-dom";

import { NOTIFICATION_TYPE_COLOR, notificationTypeLabel } from "../../lib/labels";
import { formatRelativeTime } from "../../lib/format";
import type { Notification } from "../../types/notification";
import { useMarkAllRead, useMarkRead, useNotifications } from "./hooks";

interface NotificationsPanelProps {
  anchorEl: HTMLElement | null;
  onClose: () => void;
}

/** The notifications feed, shown as a popover under the TopBar bell. Feed + mark-read. */
export function NotificationsPanel({ anchorEl, onClose }: NotificationsPanelProps) {
  const open = Boolean(anchorEl);
  const [unreadOnly, setUnreadOnly] = useState(false);
  const navigate = useNavigate();

  const { data, isLoading } = useNotifications(unreadOnly, open);
  const markRead = useMarkRead();
  const markAllRead = useMarkAllRead();

  const items = data?.content ?? [];
  const hasUnread = items.some((n) => !n.read);

  function handleOpen(notification: Notification) {
    if (!notification.read) {
      markRead.mutate(notification.id);
    }
    if (notification.projectId) {
      navigate(`/projects/${notification.projectId}`);
      onClose();
    }
  }

  return (
    <Popover
      open={open}
      anchorEl={anchorEl}
      onClose={onClose}
      anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
      transformOrigin={{ vertical: "top", horizontal: "right" }}
      slotProps={{ paper: { sx: { width: 380, maxWidth: "90vw" } } }}
    >
      <Box
        sx={{
          px: 2,
          py: 1.5,
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          gap: 1,
        }}
      >
        <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
          Notifications
        </Typography>
        <Button
          size="small"
          startIcon={<DoneAllIcon />}
          disabled={!hasUnread || markAllRead.isPending}
          onClick={() => markAllRead.mutate()}
        >
          Mark all read
        </Button>
      </Box>

      <Box sx={{ px: 2, pb: 1 }}>
        <ToggleButtonGroup
          size="small"
          exclusive
          value={unreadOnly ? "unread" : "all"}
          onChange={(_, value) => {
            if (value !== null) {
              setUnreadOnly(value === "unread");
            }
          }}
        >
          <ToggleButton value="all">All</ToggleButton>
          <ToggleButton value="unread">Unread</ToggleButton>
        </ToggleButtonGroup>
      </Box>
      <Divider />

      {isLoading ? (
        <Box sx={{ display: "flex", justifyContent: "center", py: 4 }}>
          <CircularProgress size={24} />
        </Box>
      ) : items.length === 0 ? (
        <Box sx={{ px: 2, py: 5, textAlign: "center" }}>
          <Typography variant="body2" color="text.secondary">
            {unreadOnly ? "No unread notifications." : "You're all caught up."}
          </Typography>
        </Box>
      ) : (
        <List disablePadding sx={{ maxHeight: 440, overflowY: "auto" }}>
          {items.map((n) => (
            <ListItemButton
              key={n.id}
              onClick={() => handleOpen(n)}
              alignItems="flex-start"
              sx={{
                gap: 0.5,
                flexDirection: "column",
                alignItems: "stretch",
                bgcolor: n.read ? "transparent" : "action.hover",
              }}
            >
              <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                <Chip
                  size="small"
                  label={notificationTypeLabel(n.type)}
                  color={NOTIFICATION_TYPE_COLOR[n.type] ?? "default"}
                  variant="outlined"
                />
                <Box sx={{ flex: 1 }} />
                {!n.read && (
                  <Box
                    sx={{ width: 8, height: 8, borderRadius: "50%", bgcolor: "primary.main" }}
                    aria-label="unread"
                  />
                )}
                <Typography variant="caption" color="text.secondary">
                  {formatRelativeTime(n.createdAt)}
                </Typography>
              </Box>
              <Typography variant="body2" sx={{ fontWeight: n.read ? 400 : 600 }}>
                {n.title}
              </Typography>
              {n.body && (
                <Typography variant="body2" color="text.secondary">
                  {n.body}
                </Typography>
              )}
            </ListItemButton>
          ))}
        </List>
      )}
    </Popover>
  );
}
