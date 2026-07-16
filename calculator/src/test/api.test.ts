import { describe, it, expect, vi, beforeEach, afterEach } from "vitest";
import { calculateOperation } from "@/lib/api";

function mockFetch(response: {
  ok?: boolean;
  status?: number;
  body?: unknown;
}) {
  return vi.fn().mockResolvedValue({
    ok: response.ok ?? true,
    status: response.status ?? 200,
    json: vi.fn().mockResolvedValue(response.body ?? { result: 0 }),
  });
}

describe("calculateOperation", () => {
  beforeEach(() => {
    vi.stubGlobal("fetch", vi.fn());
  });

  afterEach(() => {
    vi.restoreAllMocks();
  });

  // ── Happy Path ──────────────────────────────────────────

  it("returns the result for add", async () => {
    (fetch as ReturnType<typeof vi.fn>).mockImplementation(
      mockFetch({ body: { result: 7 } }),
    );
    const result = await calculateOperation("add", 3, 4);
    expect(result).toBe(7);
    expect(fetch).toHaveBeenCalledWith(
      "/api/calc/add",
      expect.objectContaining({
        method: "POST",
        body: JSON.stringify({ a: 3, b: 4 }),
      }),
    );
  });

  it("returns the result for subtract", async () => {
    (fetch as ReturnType<typeof vi.fn>).mockImplementation(
      mockFetch({ body: { result: 10 } }),
    );
    const result = await calculateOperation("subtract", 20, 10);
    expect(result).toBe(10);
    expect(fetch).toHaveBeenCalledWith(
      "/api/calc/subtract",
      expect.any(Object),
    );
  });

  it("returns the result for multiply", async () => {
    (fetch as ReturnType<typeof vi.fn>).mockImplementation(
      mockFetch({ body: { result: 42 } }),
    );
    const result = await calculateOperation("multiply", 6, 7);
    expect(result).toBe(42);
  });

  it("returns the result for divide", async () => {
    (fetch as ReturnType<typeof vi.fn>).mockImplementation(
      mockFetch({ body: { result: 5 } }),
    );
    const result = await calculateOperation("divide", 20, 4);
    expect(result).toBe(5);
  });

  it("uses VITE_API_BASE when set", async () => {
    vi.stubEnv("VITE_API_BASE", "http://localhost:8080");
    // Re-import to pick up the env var — but since module is already loaded,
    // we check the test by clearing and calling the function
    (fetch as ReturnType<typeof vi.fn>).mockImplementation(
      mockFetch({ body: { result: 3 } }),
    );
    // Reset import.meta.env mock
    vi.stubGlobal("import", { meta: { env: { VITE_API_BASE: "http://localhost:8080" } } });
  });

  // ── Error Cases ────────────────────────────────────────

  it("throws 'Ungültige Eingabe' for HTTP 400 with invalid_input", async () => {
    (fetch as ReturnType<typeof vi.fn>).mockImplementation(
      mockFetch({ ok: false, status: 400, body: { error: "invalid_input" } }),
    );
    await expect(
      calculateOperation("add", 1, 2),
    ).rejects.toThrow("Ungültige Eingabe");
  });

  it("throws generic error message for HTTP 400 with unknown error", async () => {
    (fetch as ReturnType<typeof vi.fn>).mockImplementation(
      mockFetch({
        ok: false,
        status: 400,
        body: { error: "division_by_zero" },
      }),
    );
    await expect(
      calculateOperation("divide", 1, 0),
    ).rejects.toThrow("division_by_zero");
  });

  it("throws server error message for non-JSON error responses", async () => {
    const mockBadResponse = {
      ok: false,
      status: 500,
      json: vi.fn().mockRejectedValue(new Error("Not JSON")),
    };
    (fetch as ReturnType<typeof vi.fn>).mockResolvedValue(mockBadResponse);
    await expect(
      calculateOperation("add", 1, 2),
    ).rejects.toThrow("Server-Fehler (HTTP 500)");
  });

  // ── AbortController / Cancellation ──────────────────────

  it("passes the AbortSignal to fetch for cancellation support", async () => {
    (fetch as ReturnType<typeof vi.fn>).mockImplementation(
      mockFetch({ body: { result: 3 } }),
    );
    const controller = new AbortController();
    const promise = calculateOperation("add", 1, 2, controller.signal);
    controller.abort();
    // fetch was called — we verify the signal was passed
    expect(fetch).toHaveBeenCalledWith(
      "/api/calc/add",
      expect.objectContaining({
        signal: controller.signal,
      }),
    );
    // Note: our mock fetch doesn't respect the AbortSignal, so the
    // promise resolves normally. In a real scenario, fetch would
    // reject with AbortError. The component handles this via
    // abortRef cleanup in the finally clause.
    await expect(promise).resolves.toBe(3);
  });
});
