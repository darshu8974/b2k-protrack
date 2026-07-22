import DeleteOutlineIcon from "@mui/icons-material/DeleteOutline";
import EditOutlinedIcon from "@mui/icons-material/EditOutlined";
import ReplyIcon from "@mui/icons-material/Reply";
import {
  Avatar,
  Box,
  Button,
  Card,
  CircularProgress,
  Divider,
  IconButton,
  Stack,
  TextField,
  Tooltip,
  Typography,
} from "@mui/material";
import { useState } from "react";

import { EmptyState } from "../../components/feedback/EmptyState";
import { ErrorState } from "../../components/feedback/ErrorState";
import { useToast } from "../../components/feedback/ToastProvider";
import { formatRelativeTime } from "../../lib/format";
import type { AppError } from "../../types/api";
import type { Comment } from "../../types/comment";
import { useAuth } from "../auth/useAuth";
import { useAddComment, useComments, useDeleteComment, useEditComment } from "./hooks";

/** The workspace Comments tab: a composer plus a two-level threaded discussion (roots + replies). */
export function CommentsTab({ projectId }: { projectId: string }) {
  const { user } = useAuth();
  const toast = useToast();
  const { data, isLoading, isError } = useComments(projectId);
  const addComment = useAddComment(projectId);
  const editComment = useEditComment(projectId);
  const deleteComment = useDeleteComment(projectId);
  const onError = (e: unknown) => toast.error((e as AppError)?.message ?? "Something went wrong.");

  const [newBody, setNewBody] = useState("");
  const [replyTo, setReplyTo] = useState<string | null>(null);
  const [replyBody, setReplyBody] = useState("");
  const [editingId, setEditingId] = useState<string | null>(null);
  const [editBody, setEditBody] = useState("");

  const comments = data?.content ?? [];
  const roots = comments.filter((c) => !c.parentId);
  const repliesByParent = comments.reduce<Record<string, Comment[]>>((acc, c) => {
    if (c.parentId) {
      (acc[c.parentId] ??= []).push(c);
    }
    return acc;
  }, {});

  const isAdmin = (user?.roles ?? []).includes("ADMIN");
  const canEdit = (c: Comment) => c.authorId === user?.id;
  const canDelete = (c: Comment) => canEdit(c) || isAdmin;

  function submitRoot() {
    const body = newBody.trim();
    if (!body) return;
    addComment.mutate({ body }, { onSuccess: () => setNewBody(""), onError });
  }

  function submitReply(parentId: string) {
    const body = replyBody.trim();
    if (!body) return;
    addComment.mutate(
      { body, parentId },
      {
        onSuccess: () => {
          setReplyBody("");
          setReplyTo(null);
        },
        onError,
      },
    );
  }

  function submitEdit(id: string) {
    const body = editBody.trim();
    if (!body) return;
    editComment.mutate({ id, body }, { onSuccess: () => setEditingId(null), onError });
  }

  function renderComment(c: Comment, isReply = false) {
    const editing = editingId === c.id;
    return (
      <Box key={c.id} sx={{ pl: isReply ? 5 : 0 }}>
        <Stack direction="row" spacing={1.5} alignItems="flex-start">
          <Avatar sx={{ width: 32, height: 32, fontSize: 13, bgcolor: c.authorColor ?? undefined }}>
            {c.authorInitials ?? "?"}
          </Avatar>
          <Box sx={{ flex: 1 }}>
            <Stack direction="row" spacing={1} alignItems="baseline">
              <Typography variant="body2" sx={{ fontWeight: 600 }}>
                {c.authorName ?? "Unknown"}
              </Typography>
              <Typography variant="caption" color="text.secondary">
                {formatRelativeTime(c.createdAt)}
                {c.edited ? " · edited" : ""}
              </Typography>
            </Stack>

            {editing ? (
              <Stack spacing={1} sx={{ mt: 0.5 }}>
                <TextField
                  size="small"
                  multiline
                  fullWidth
                  value={editBody}
                  onChange={(e) => setEditBody(e.target.value)}
                />
                <Stack direction="row" spacing={1}>
                  <Button size="small" variant="contained" onClick={() => submitEdit(c.id)}>
                    Save
                  </Button>
                  <Button size="small" onClick={() => setEditingId(null)}>
                    Cancel
                  </Button>
                </Stack>
              </Stack>
            ) : (
              <Typography variant="body2" sx={{ whiteSpace: "pre-wrap" }}>
                {c.body}
              </Typography>
            )}

            {!editing && (
              <Stack direction="row" spacing={0.5} sx={{ mt: 0.25 }}>
                {!isReply && (
                  <Button
                    size="small"
                    startIcon={<ReplyIcon fontSize="small" />}
                    onClick={() => {
                      setReplyTo(replyTo === c.id ? null : c.id);
                      setReplyBody("");
                    }}
                  >
                    Reply
                  </Button>
                )}
                {canEdit(c) && (
                  <Tooltip title="Edit">
                    <IconButton
                      size="small"
                      aria-label="Edit comment"
                      onClick={() => {
                        setEditingId(c.id);
                        setEditBody(c.body);
                      }}
                    >
                      <EditOutlinedIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
                )}
                {canDelete(c) && (
                  <Tooltip title="Delete">
                    <IconButton
                      size="small"
                      aria-label="Delete comment"
                      onClick={() => {
                        if (window.confirm("Delete this comment?")) {
                          deleteComment.mutate(c.id, { onError });
                        }
                      }}
                    >
                      <DeleteOutlineIcon fontSize="small" />
                    </IconButton>
                  </Tooltip>
                )}
              </Stack>
            )}

            {replyTo === c.id && (
              <Stack direction="row" spacing={1} sx={{ mt: 1 }} alignItems="flex-start">
                <TextField
                  size="small"
                  fullWidth
                  multiline
                  placeholder="Write a reply…"
                  value={replyBody}
                  onChange={(e) => setReplyBody(e.target.value)}
                />
                <Button
                  variant="contained"
                  size="small"
                  disabled={!replyBody.trim() || addComment.isPending}
                  onClick={() => submitReply(c.id)}
                >
                  Reply
                </Button>
              </Stack>
            )}
          </Box>
        </Stack>

        {(repliesByParent[c.id] ?? []).map((reply) => (
          <Box key={reply.id} sx={{ mt: 2 }}>
            {renderComment(reply, true)}
          </Box>
        ))}
      </Box>
    );
  }

  if (isError) {
    return <ErrorState message="Unable to load comments." />;
  }

  return (
    <Card sx={{ p: 2.5 }}>
      <Typography variant="subtitle1" sx={{ mb: 1.5 }}>
        Comments
      </Typography>

      {/* Composer */}
      <Stack direction="row" spacing={1} alignItems="flex-start" sx={{ mb: 2 }}>
        <TextField
          fullWidth
          size="small"
          multiline
          minRows={1}
          placeholder="Add a comment…"
          value={newBody}
          onChange={(e) => setNewBody(e.target.value)}
        />
        <Button
          variant="contained"
          disabled={!newBody.trim() || addComment.isPending}
          onClick={submitRoot}
        >
          Comment
        </Button>
      </Stack>
      <Divider sx={{ mb: 2 }} />

      {isLoading ? (
        <Box sx={{ display: "flex", justifyContent: "center", py: 4 }}>
          <CircularProgress size={24} aria-label="Loading comments" />
        </Box>
      ) : roots.length === 0 ? (
        <EmptyState message="No comments yet. Start the discussion." />
      ) : (
        <Stack spacing={2.5} divider={<Divider flexItem />}>
          {roots.map((c) => renderComment(c))}
        </Stack>
      )}
    </Card>
  );
}
