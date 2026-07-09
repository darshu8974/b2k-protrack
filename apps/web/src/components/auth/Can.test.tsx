import { render, screen } from "@testing-library/react";
import { describe, expect, it, vi } from "vitest";

import type { AuthState } from "../../features/auth/AuthContext";
import { useAuth } from "../../features/auth/useAuth";
import type { UserSummary } from "../../types/domain";
import { Can } from "./Can";

vi.mock("../../features/auth/useAuth", () => ({
  useAuth: vi.fn(),
}));

const mockedUseAuth = vi.mocked(useAuth);

function asUser(user: UserSummary | null) {
  mockedUseAuth.mockReturnValue({ user } as AuthState);
}

const pm: UserSummary = {
  id: "1",
  fullName: "Priya Anand",
  email: "priya@protrack.io",
  roles: ["PM"],
  permissions: ["project.create"],
  avatarInitials: "PA",
};

describe("<Can>", () => {
  it("renders children when the user holds one of the allowed roles", () => {
    asUser(pm);
    render(
      <Can roles={["PM", "ADMIN"]}>
        <span>allowed</span>
      </Can>,
    );
    expect(screen.getByText("allowed")).toBeInTheDocument();
  });

  it("renders the fallback when the role does not match", () => {
    asUser(pm);
    render(
      <Can roles={["ADMIN"]} fallback={<span>denied</span>}>
        <span>allowed</span>
      </Can>,
    );
    expect(screen.queryByText("allowed")).not.toBeInTheDocument();
    expect(screen.getByText("denied")).toBeInTheDocument();
  });

  it("requires both role and permission when both are provided", () => {
    asUser(pm);
    render(
      <Can roles={["PM"]} permission="project.delete" fallback={<span>denied</span>}>
        <span>allowed</span>
      </Can>,
    );
    expect(screen.getByText("denied")).toBeInTheDocument();
  });

  it("passes a permission-only gate when the permission is held", () => {
    asUser(pm);
    render(
      <Can permission="project.create">
        <span>allowed</span>
      </Can>,
    );
    expect(screen.getByText("allowed")).toBeInTheDocument();
  });

  it("renders the fallback when there is no authenticated user", () => {
    asUser(null);
    render(
      <Can roles={["PM"]} fallback={<span>denied</span>}>
        <span>allowed</span>
      </Can>,
    );
    expect(screen.getByText("denied")).toBeInTheDocument();
  });
});
