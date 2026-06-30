import { zodResolver } from "@hookform/resolvers/zod";
import {
  Alert,
  Box,
  Button,
  Card,
  MenuItem,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import { useState } from "react";
import { Controller, useForm } from "react-hook-form";
import { useNavigate } from "react-router-dom";
import { z } from "zod";

import { paths } from "../../../app/router/paths";
import { PRIORITY_LABEL, PUBLICATION_TYPE_LABEL } from "../../../lib/labels";
import type { AppError } from "../../../types/api";
import type { Priority, PublicationType } from "../../../types/project";
import { useImprints } from "../../reference/hooks";
import { useCreateProject } from "../hooks";
import type { CreateProjectBody } from "../api";

const PUBLICATION_TYPES: PublicationType[] = ["STEM_TEXTBOOK", "MONOGRAPH", "JOURNAL", "REFERENCE"];
const PRIORITIES: Priority[] = ["LOW", "MEDIUM", "HIGH"];

const schema = z.object({
  title: z.string().min(3, "Title must be at least 3 characters").max(250),
  isbn: z.string().optional(),
  imprintId: z.string().min(1, "Select an imprint"),
  publicationType: z.enum(["STEM_TEXTBOOK", "MONOGRAPH", "JOURNAL", "REFERENCE"]),
  discipline: z.string().optional(),
  brief: z.string().max(4000).optional(),
  pageExtent: z.string().regex(/^\d*$/, "Numbers only").optional(),
  trimSize: z.string().optional(),
  priority: z.enum(["LOW", "MEDIUM", "HIGH"]),
  dueDate: z.string().optional(),
});

type FormValues = z.infer<typeof schema>;

export function CreateProjectPage() {
  const navigate = useNavigate();
  const { data: imprints } = useImprints();
  const createProject = useCreateProject();
  const [submitError, setSubmitError] = useState<string | null>(null);

  const {
    register,
    control,
    handleSubmit,
    setError,
    formState: { errors, isSubmitting },
  } = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { imprintId: "", publicationType: "STEM_TEXTBOOK", priority: "MEDIUM" },
  });

  const onSubmit = handleSubmit(async (values) => {
    setSubmitError(null);
    const body: CreateProjectBody = {
      title: values.title.trim(),
      imprintId: values.imprintId,
      publicationType: values.publicationType,
      priority: values.priority,
      isbn: values.isbn?.trim() || undefined,
      discipline: values.discipline?.trim() || undefined,
      brief: values.brief?.trim() || undefined,
      pageExtent: values.pageExtent ? Number(values.pageExtent) : undefined,
      trimSize: values.trimSize?.trim() || undefined,
      dueDate: values.dueDate || undefined,
    };
    try {
      const created = await createProject.mutateAsync(body);
      navigate(paths.project(created.id), { replace: true });
    } catch (error) {
      const appError = error as AppError;
      if (appError.fieldErrors?.length) {
        appError.fieldErrors.forEach((fieldError) => {
          setError(fieldError.field as keyof FormValues, { message: fieldError.message });
        });
      }
      setSubmitError(appError.message ?? "Could not create the project.");
    }
  });

  return (
    <Stack spacing={3} sx={{ maxWidth: 760 }}>
      <Box>
        <Typography variant="h4">Create a new project</Typography>
        <Typography color="text.secondary">
          Set up the publication. It starts in the Intake stage.
        </Typography>
      </Box>

      <Card sx={{ p: 3 }}>
        <form onSubmit={onSubmit} noValidate>
          <Stack spacing={2.5}>
            {submitError && <Alert severity="error">{submitError}</Alert>}

            <TextField
              label="Publication title"
              fullWidth
              error={Boolean(errors.title)}
              helperText={errors.title?.message}
              {...register("title")}
            />

            <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
              <TextField
                label="ISBN / identifier (optional)"
                fullWidth
                error={Boolean(errors.isbn)}
                helperText={errors.isbn?.message}
                {...register("isbn")}
              />
              <Controller
                name="imprintId"
                control={control}
                render={({ field }) => (
                  <TextField
                    select
                    label="Imprint"
                    fullWidth
                    error={Boolean(errors.imprintId)}
                    helperText={errors.imprintId?.message}
                    {...field}
                  >
                    <MenuItem value="" disabled>
                      Select an imprint
                    </MenuItem>
                    {(imprints ?? []).map((imprint) => (
                      <MenuItem key={imprint.id} value={imprint.id}>
                        {imprint.name}
                      </MenuItem>
                    ))}
                  </TextField>
                )}
              />
            </Stack>

            <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
              <Controller
                name="publicationType"
                control={control}
                render={({ field }) => (
                  <TextField select label="Publication type" fullWidth {...field}>
                    {PUBLICATION_TYPES.map((t) => (
                      <MenuItem key={t} value={t}>
                        {PUBLICATION_TYPE_LABEL[t]}
                      </MenuItem>
                    ))}
                  </TextField>
                )}
              />
              <TextField label="Discipline (optional)" fullWidth {...register("discipline")} />
            </Stack>

            <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
              <TextField
                label="Page extent (optional)"
                fullWidth
                error={Boolean(errors.pageExtent)}
                helperText={errors.pageExtent?.message}
                {...register("pageExtent")}
              />
              <TextField label="Trim size (optional)" fullWidth {...register("trimSize")} />
            </Stack>

            <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
              <Controller
                name="priority"
                control={control}
                render={({ field }) => (
                  <TextField select label="Priority" fullWidth {...field}>
                    {PRIORITIES.map((p) => (
                      <MenuItem key={p} value={p}>
                        {PRIORITY_LABEL[p]}
                      </MenuItem>
                    ))}
                  </TextField>
                )}
              />
              <TextField
                label="Due date (optional)"
                type="date"
                fullWidth
                InputLabelProps={{ shrink: true }}
                {...register("dueDate")}
              />
            </Stack>

            <TextField
              label="Brief / notes (optional)"
              fullWidth
              multiline
              minRows={3}
              error={Boolean(errors.brief)}
              helperText={errors.brief?.message}
              {...register("brief")}
            />

            <Stack direction="row" spacing={2} justifyContent="flex-end">
              <Button onClick={() => navigate(paths.projects)}>Cancel</Button>
              <Button type="submit" variant="contained" disabled={isSubmitting}>
                {isSubmitting ? "Creating…" : "Create project"}
              </Button>
            </Stack>
          </Stack>
        </form>
      </Card>
    </Stack>
  );
}
