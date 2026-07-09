import AutoAwesomeIcon from "@mui/icons-material/AutoAwesome";
import SendIcon from "@mui/icons-material/Send";
import {
  Alert,
  Avatar,
  Box,
  Card,
  Chip,
  CircularProgress,
  IconButton,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import { useEffect, useRef, useState } from "react";

import { EmptyState } from "../../components/feedback/EmptyState";
import { ErrorState } from "../../components/feedback/ErrorState";
import { formatRelativeTime } from "../../lib/format";
import type { AssistantMessage } from "../../types/assistant";
import { useAskAssistant, useAssistantThread } from "./hooks";

const SUGGESTED_PROMPTS = [
  "What stage is this project in and what happens next?",
  "Summarize the main production risks for this project.",
  "What should the team focus on before QA sign-off?",
  "Explain the preflight checks in plain language.",
];

/**
 * The scoped AI Assistant tab: a project-bounded chat. Questions and answers are persisted per
 * (project, user); the backend calls the AI service (Claude, or the deterministic mock in dev).
 */
export function AssistantPanel({ projectId }: { projectId: string }) {
  const { data, isLoading, isError } = useAssistantThread(projectId);
  const ask = useAskAssistant(projectId);
  const [draft, setDraft] = useState("");
  const scrollRef = useRef<HTMLDivElement>(null);

  const messages = data?.messages ?? [];
  const pendingQuestion = ask.isPending ? ask.variables : null;

  useEffect(() => {
    scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: "smooth" });
  }, [messages.length, ask.isPending]);

  function send(text: string) {
    const content = text.trim();
    if (!content || ask.isPending) return;
    setDraft("");
    ask.mutate(content);
  }

  if (isError) {
    return <ErrorState message="Unable to load the assistant." />;
  }

  const showSuggestions = !isLoading && messages.length === 0 && !pendingQuestion;

  return (
    <Card sx={{ p: 2.5, display: "flex", flexDirection: "column", height: "70vh" }}>
      <Stack direction="row" spacing={1} alignItems="center" sx={{ mb: 0.5 }}>
        <AutoAwesomeIcon fontSize="small" color="primary" />
        <Typography variant="subtitle1">AI Assistant</Typography>
      </Stack>
      <Typography variant="caption" color="text.secondary" sx={{ mb: 1.5 }}>
        Ask scoped questions about this project. Answers are advisory — a human always decides.
      </Typography>

      {/* Transcript */}
      <Box ref={scrollRef} sx={{ flex: 1, overflowY: "auto", pr: 1 }}>
        {isLoading ? (
          <Box sx={{ display: "flex", justifyContent: "center", py: 4 }}>
            <CircularProgress size={24} aria-label="Loading conversation" />
          </Box>
        ) : messages.length === 0 && !pendingQuestion ? (
          <EmptyState message="No questions yet. Ask the assistant anything about this project." />
        ) : (
          <Stack spacing={2}>
            {messages.map((m) => (
              <MessageBubble key={m.id} message={m} />
            ))}
            {pendingQuestion && (
              <MessageBubble
                message={{
                  id: "pending",
                  role: "USER",
                  content: pendingQuestion,
                  createdAt: new Date().toISOString(),
                }}
              />
            )}
            {ask.isPending && <ThinkingBubble />}
          </Stack>
        )}
      </Box>

      {ask.isError && (
        <Alert severity="error" sx={{ mt: 1 }}>
          The assistant could not answer that. Please try again.
        </Alert>
      )}

      {showSuggestions && (
        <Stack direction="row" spacing={1} flexWrap="wrap" useFlexGap sx={{ mt: 1.5 }}>
          {SUGGESTED_PROMPTS.map((prompt) => (
            <Chip
              key={prompt}
              label={prompt}
              variant="outlined"
              size="small"
              onClick={() => send(prompt)}
              clickable
            />
          ))}
        </Stack>
      )}

      {/* Composer */}
      <Stack direction="row" spacing={1} alignItems="flex-end" sx={{ mt: 1.5 }}>
        <TextField
          fullWidth
          size="small"
          multiline
          maxRows={4}
          placeholder="Ask a question…"
          value={draft}
          onChange={(e) => setDraft(e.target.value)}
          onKeyDown={(e) => {
            if (e.key === "Enter" && !e.shiftKey) {
              e.preventDefault();
              send(draft);
            }
          }}
        />
        <IconButton
          color="primary"
          disabled={!draft.trim() || ask.isPending}
          onClick={() => send(draft)}
          aria-label="Send"
        >
          <SendIcon />
        </IconButton>
      </Stack>
    </Card>
  );
}

function MessageBubble({ message }: { message: AssistantMessage }) {
  const isUser = message.role === "USER";
  return (
    <Stack
      direction="row"
      spacing={1.5}
      justifyContent={isUser ? "flex-end" : "flex-start"}
      alignItems="flex-start"
    >
      {!isUser && (
        <Avatar sx={{ width: 30, height: 30, bgcolor: "primary.main" }}>
          <AutoAwesomeIcon sx={{ fontSize: 16 }} />
        </Avatar>
      )}
      <Box sx={{ maxWidth: "78%" }}>
        <Box
          sx={{
            px: 1.5,
            py: 1,
            borderRadius: 2,
            bgcolor: isUser ? "primary.main" : "action.hover",
            color: isUser ? "primary.contrastText" : "text.primary",
          }}
        >
          <Typography variant="body2" sx={{ whiteSpace: "pre-wrap" }}>
            {message.content}
          </Typography>
        </Box>
        {message.citations && message.citations.length > 0 && (
          <Stack direction="row" spacing={0.5} flexWrap="wrap" useFlexGap sx={{ mt: 0.5 }}>
            {message.citations.map((c) => (
              <Chip key={c} label={c} size="small" variant="outlined" />
            ))}
          </Stack>
        )}
        <Typography
          variant="caption"
          color="text.secondary"
          sx={{ display: "block", mt: 0.25, textAlign: isUser ? "right" : "left" }}
        >
          {formatRelativeTime(message.createdAt)}
        </Typography>
      </Box>
    </Stack>
  );
}

function ThinkingBubble() {
  return (
    <Stack direction="row" spacing={1.5} alignItems="center">
      <Avatar sx={{ width: 30, height: 30, bgcolor: "primary.main" }}>
        <AutoAwesomeIcon sx={{ fontSize: 16 }} />
      </Avatar>
      <Box sx={{ px: 1.5, py: 1, borderRadius: 2, bgcolor: "action.hover" }}>
        <Stack direction="row" spacing={1} alignItems="center">
          <CircularProgress size={14} />
          <Typography variant="body2" color="text.secondary">
            Thinking…
          </Typography>
        </Stack>
      </Box>
    </Stack>
  );
}
