import { render, screen, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { MemoryRouter, Route, Routes } from "react-router-dom";
import { beforeEach, describe, expect, it, vi } from "vitest";

import type { AuthState } from "./AuthContext";
import { LoginPage } from "./LoginPage";
import { useAuth } from "./useAuth";

vi.mock("./useAuth", () => ({ useAuth: vi.fn() }));

const mockedUseAuth = vi.mocked(useAuth);
const login = vi.fn<AuthState["login"]>();

function setAuth(overrides: Partial<AuthState> = {}) {
  mockedUseAuth.mockReturnValue({
    user: null,
    status: "unauthenticated",
    isAuthenticated: false,
    login,
    logout: vi.fn(),
    setRole: vi.fn(),
    ...overrides,
  } as AuthState);
}

function renderLogin() {
  return render(
    <MemoryRouter initialEntries={["/login"]}>
      <Routes>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/dashboard" element={<div>Dashboard Home</div>} />
      </Routes>
    </MemoryRouter>,
  );
}

describe("<LoginPage>", () => {
  beforeEach(() => {
    login.mockReset();
    setAuth();
  });

  it("blocks submission and shows a validation error for an invalid email", async () => {
    const user = userEvent.setup();
    renderLogin();

    const email = screen.getByLabelText(/work email/i);
    await user.clear(email);
    await user.type(email, "not-an-email");
    await user.click(screen.getByRole("button", { name: /sign in/i }));

    expect(await screen.findByText(/enter a valid work email/i)).toBeInTheDocument();
    expect(login).not.toHaveBeenCalled();
  });

  it("submits the prefilled demo credentials and redirects to the dashboard", async () => {
    login.mockResolvedValue(undefined);
    const user = userEvent.setup();
    renderLogin();

    await user.click(screen.getByRole("button", { name: /sign in/i }));

    await waitFor(() => {
      expect(login).toHaveBeenCalledWith("admin@protrack.io", "password");
    });
    expect(await screen.findByText("Dashboard Home")).toBeInTheDocument();
  });

  it("surfaces the server error message when login fails", async () => {
    login.mockRejectedValue({ status: 401, code: "INVALID_CREDENTIALS", message: "Invalid email or password" });
    const user = userEvent.setup();
    renderLogin();

    await user.click(screen.getByRole("button", { name: /sign in/i }));

    const alert = await screen.findByRole("alert");
    expect(alert).toHaveTextContent("Invalid email or password");
  });

  it("redirects away from the login page when already authenticated", () => {
    setAuth({ isAuthenticated: true });
    renderLogin();
    expect(screen.getByText("Dashboard Home")).toBeInTheDocument();
  });
});
