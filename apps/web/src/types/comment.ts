/** A threaded project comment with its author resolved for display. */
export interface Comment {
  id: string;
  projectId: string;
  parentId?: string | null;
  authorId: string;
  authorName?: string | null;
  authorInitials?: string | null;
  authorColor?: string | null;
  contextType: string;
  contextId?: string | null;
  body: string;
  edited: boolean;
  createdAt: string;
  updatedAt: string;
}
