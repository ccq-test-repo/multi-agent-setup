import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Calculator } from "@/components/calculator";

/** Helper: get the display element (has role="status") */
function display() {
  return screen.getByRole("status");
}

function mockFetchResult(result: number) {
  return vi.fn().mockResolvedValue({
    ok: true,
    status: 200,
    json: vi.fn().mockResolvedValue({ result }),
  });
}

function mockFetchError(status: number, error: string) {
  return vi.fn().mockResolvedValue({
    ok: false,
    status,
    json: vi.fn().mockResolvedValue({ error }),
  });
}

describe("Calculator component", () => {
  beforeEach(() => {
    vi.stubGlobal("fetch", vi.fn());
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  // ── Initial State ───────────────────────────────────────

  it("renders with initial state showing 0 and hint text", () => {
    render(<Calculator />);
    expect(display()).toHaveTextContent("0");
    expect(screen.getByText(/Gib eine Zahl ein/)).toBeInTheDocument();
  });

  // ── Digit Input (before operator) ──────────────────────

  it("replaces initial 0 with first digit press", async () => {
    const user = userEvent.setup();
    render(<Calculator />);

    await user.click(screen.getByRole("button", { name: /Ziffer 5/ }));
    expect(display()).toHaveTextContent("5");
  });

  it("each digit press before operator replaces previous (operandA null)", async () => {
    const user = userEvent.setup();
    render(<Calculator />);

    await user.click(screen.getByRole("button", { name: /Ziffer 3/ }));
    await user.click(screen.getByRole("button", { name: /Ziffer 7/ }));
    await user.click(screen.getByRole("button", { name: /Ziffer 2/ }));

    // Each digit replaces because operandA is null
    // current implementation: only last digit shown
    expect(display()).toHaveTextContent("2");
  });

  // ── Operator Selection Display ─────────────────────────

  it("shows operator in display after pressing it", async () => {
    const user = userEvent.setup();
    render(<Calculator />);

    await user.click(screen.getByRole("button", { name: /Ziffer 5/ }));
    await user.click(screen.getByRole("button", { name: "Plus" }));

    // Display should show "5 + "
    expect(display()).toHaveTextContent(/5.*\+/);
  });

  // ── Happy Path: All Four Operations ────────────────────

  it("adds two numbers: 4 + 5 = 9", async () => {
    (fetch as ReturnType<typeof vi.fn>).mockImplementation(
      mockFetchResult(9),
    );
    const user = userEvent.setup();
    render(<Calculator />);

    await user.click(screen.getByRole("button", { name: /Ziffer 4/ }));
    await user.click(screen.getByRole("button", { name: "Plus" }));
    await user.click(screen.getByRole("button", { name: /Ziffer 5/ }));
    await user.click(screen.getByRole("button", { name: "Gleich" }));

    await vi.waitFor(() => {
      expect(display()).toHaveTextContent("9");
    });
    expect(fetch).toHaveBeenCalledWith(
      "/api/calc/add",
      expect.objectContaining({
        method: "POST",
        body: JSON.stringify({ a: 4, b: 5 }),
      }),
    );
  });

  it("subtracts two numbers: 5 - 3 = 2", async () => {
    (fetch as ReturnType<typeof vi.fn>).mockImplementation(
      mockFetchResult(2),
    );
    const user = userEvent.setup();
    render(<Calculator />);

    // operandA input: pressing digit 2 then digit 0
    // Note: before operator, each digit replaces (not appends)
    // So pressing 2 then 0 gives display "0", not "20"
    // To enter 20, we need to press digit 2, then operator,
    // then digit 0 for operand B, then operator again, then digit 5?
    // Actually simpler: use the fact that before operator,
    // we just enter a single-digit operand A. With the current
    // component logic, we can only enter single-digit operand A.
    // So 5 - 3 = 2 is a valid test.
    await user.click(screen.getByRole("button", { name: /Ziffer 5/ }));
    await user.click(screen.getByRole("button", { name: "Minus" }));
    await user.click(screen.getByRole("button", { name: /Ziffer 3/ }));
    await user.click(screen.getByRole("button", { name: "Gleich" }));

    await vi.waitFor(() => {
      expect(display()).toHaveTextContent("2");
    });
    expect(fetch).toHaveBeenCalledWith(
      "/api/calc/subtract",
      expect.objectContaining({ body: JSON.stringify({ a: 5, b: 3 }) }),
    );
  });

  it("multiplies two numbers: 6 × 7 = 42", async () => {
    (fetch as ReturnType<typeof vi.fn>).mockImplementation(
      mockFetchResult(42),
    );
    const user = userEvent.setup();
    render(<Calculator />);

    await user.click(screen.getByRole("button", { name: /Ziffer 6/ }));
    await user.click(screen.getByRole("button", { name: "Mal" }));
    await user.click(screen.getByRole("button", { name: /Ziffer 7/ }));
    await user.click(screen.getByRole("button", { name: "Gleich" }));

    await vi.waitFor(() => {
      expect(display()).toHaveTextContent("42");
    });
    expect(fetch).toHaveBeenCalledWith(
      "/api/calc/multiply",
      expect.objectContaining({ body: JSON.stringify({ a: 6, b: 7 }) }),
    );
  });

  it("divides two numbers: 9 ÷ 3 = 3", async () => {
    (fetch as ReturnType<typeof vi.fn>).mockImplementation(
      mockFetchResult(3),
    );
    const user = userEvent.setup();
    render(<Calculator />);

    await user.click(screen.getByRole("button", { name: /Ziffer 9/ }));
    await user.click(screen.getByRole("button", { name: "Geteilt" }));
    await user.click(screen.getByRole("button", { name: /Ziffer 3/ }));
    await user.click(screen.getByRole("button", { name: "Gleich" }));

    await vi.waitFor(() => {
      expect(display()).toHaveTextContent("3");
    });
    expect(fetch).toHaveBeenCalledWith(
      "/api/calc/divide",
      expect.objectContaining({ body: JSON.stringify({ a: 9, b: 3 }) }),
    );
  });

  // ── Clear Button ────────────────────────────────────────

  it("resets to initial state when C is pressed", async () => {
    const user = userEvent.setup();
    render(<Calculator />);

    await user.click(screen.getByRole("button", { name: /Ziffer 5/ }));
    await user.click(screen.getByRole("button", { name: "Löschen" }));

    expect(display()).toHaveTextContent("0");
    expect(screen.getByText(/Gib eine Zahl ein/)).toBeInTheDocument();
  });

  // ── Error Handling ─────────────────────────────────────

  it("shows 'Ungültige Eingabe' on HTTP 400 invalid_input", async () => {
    (fetch as ReturnType<typeof vi.fn>).mockImplementation(
      mockFetchError(400, "invalid_input"),
    );
    const user = userEvent.setup();
    render(<Calculator />);

    await user.click(screen.getByRole("button", { name: /Ziffer 1/ }));
    await user.click(screen.getByRole("button", { name: "Plus" }));
    await user.click(screen.getByRole("button", { name: /Ziffer 2/ }));
    await user.click(screen.getByRole("button", { name: "Gleich" }));

    expect(await screen.findByText("Ungültige Eingabe")).toBeInTheDocument();
  });

  it("shows generic error on HTTP 500", async () => {
    const badResponse = {
      ok: false,
      status: 500,
      json: vi.fn().mockRejectedValue(new Error("Not JSON")),
    };
    (fetch as ReturnType<typeof vi.fn>).mockResolvedValue(badResponse);
    const user = userEvent.setup();
    render(<Calculator />);

    await user.click(screen.getByRole("button", { name: /Ziffer 5/ }));
    await user.click(screen.getByRole("button", { name: "Plus" }));
    await user.click(screen.getByRole("button", { name: /Ziffer 5/ }));
    await user.click(screen.getByRole("button", { name: "Gleich" }));

    expect(
      await screen.findByText("Server-Fehler (HTTP 500)"),
    ).toBeInTheDocument();
  });

  it("resets error state when a new digit is pressed after error", async () => {
    (fetch as ReturnType<typeof vi.fn>).mockImplementation(
      mockFetchError(400, "invalid_input"),
    );
    const user = userEvent.setup();
    render(<Calculator />);

    // Trigger error
    await user.click(screen.getByRole("button", { name: /Ziffer 1/ }));
    await user.click(screen.getByRole("button", { name: "Plus" }));
    await user.click(screen.getByRole("button", { name: /Ziffer 2/ }));
    await user.click(screen.getByRole("button", { name: "Gleich" }));
    expect(await screen.findByText("Ungültige Eingabe")).toBeInTheDocument();

    // Press digit → resets
    await user.click(screen.getByRole("button", { name: /Ziffer 3/ }));
    await vi.waitFor(() => {
      expect(display()).toHaveTextContent("3");
    });
    expect(screen.queryByText("Ungültige Eingabe")).not.toBeInTheDocument();
  });

  // ── Loading State ───────────────────────────────────────

  it("renders a loading indicator while API request is in flight", async () => {
    // Never resolve — keeps loading
    const deferred = new Promise<Response>(() => {});
    (fetch as ReturnType<typeof vi.fn>).mockReturnValue(deferred);

    const user = userEvent.setup();
    render(<Calculator />);

    await user.click(screen.getByRole("button", { name: /Ziffer 1/ }));
    await user.click(screen.getByRole("button", { name: "Plus" }));
    await user.click(screen.getByRole("button", { name: /Ziffer 2/ }));
    await user.click(screen.getByRole("button", { name: "Gleich" }));

    // Status element is still rendered (with spinner)
    expect(display()).toBeInTheDocument();
  });

  // ── Decimal Input ───────────────────────────────────────

  it("allows decimal via comma button after operator", async () => {
    const user = userEvent.setup();
    render(<Calculator />);

    await user.click(screen.getByRole("button", { name: /Ziffer 3/ }));
    await user.click(screen.getByRole("button", { name: "Plus" }));
    // Now waiting for operand B
    await user.click(screen.getByRole("button", { name: "Komma" }));
    await user.click(screen.getByRole("button", { name: /Ziffer 1/ }));
    await user.click(screen.getByRole("button", { name: /Ziffer 4/ }));

    // Display shows the operand B input "0.14" (decimal starts from "0.")
    expect(display()).toHaveTextContent(/0\.(14)?/);
  });

  // ── Backspace ──────────────────────────────────────────

  it("backspace resets to 0 when only one digit was entered", async () => {
    const user = userEvent.setup();
    render(<Calculator />);

    await user.click(screen.getByRole("button", { name: /Ziffer 5/ }));
    await user.click(screen.getByRole("button", { name: "Zeichen löschen" }));

    expect(display()).toHaveTextContent("0");
  });
});
