import { afterEach, describe, expect, it, vi } from "vitest";
import { calculate, health } from "@/lib/api";

const originalFetch = globalThis.fetch;

function mockFetch(impl: (input: RequestInfo | URL, init?: RequestInit) => Promise<Response>) {
  globalThis.fetch = vi.fn(impl) as unknown as typeof fetch;
}

afterEach(() => {
  globalThis.fetch = originalFetch;
  vi.restoreAllMocks();
});

describe("calculate()", () => {
  it("liefert das Ergebnis einer erfolgreichen Rechnung", async () => {
    mockFetch(async () =>
      new Response(JSON.stringify({ result: 14.5 }), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      }),
    );

    const result = await calculate({ left: 12.5, operator: "ADD", right: 2 });
    expect(result).toBe(14.5);
  });

  it("sendet left/operator/right als JSON-Body an /api/calculate", async () => {
    let capturedUrl = "";
    let capturedBody: string | null = null;
    mockFetch(async (url, init) => {
      capturedUrl = String(url);
      capturedBody = String(init?.body ?? "");
      return new Response(JSON.stringify({ result: 42 }), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      });
    });

    await calculate({ left: 7, operator: "MULTIPLY", right: 6 });
    expect(capturedUrl).toContain("/api/calculate");
    expect(capturedBody).toBe(
      JSON.stringify({ left: 7, operator: "MULTIPLY", right: 6 }),
    );
  });

  it("zeigt die menschenlesbare Backend-Message bei HTTP-Fehlern statt des Statuscodes", async () => {
    mockFetch(async () =>
      new Response(JSON.stringify({ error: "DIVISION_BY_ZERO", message: "Division durch null ist nicht definiert." }), {
        status: 400,
        headers: { "Content-Type": "application/json" },
      }),
    );

    await expect(
      calculate({ left: 5, operator: "DIVIDE", right: 0 }),
    ).rejects.toThrow("Division durch null ist nicht definiert.");
  });

  it("liefert einen verständlichen Fallback, wenn der Fehler-Body kein JSON enthält", async () => {
    mockFetch(async () => new Response("nicht erreichbar", { status: 500 }));

    await expect(
      calculate({ left: 1, operator: "ADD", right: 1 }),
    ).rejects.toThrow("Server-Fehler (HTTP 500)");
  });

  it("zeigt Server-Fehler (Kurzname), wenn der Fehler-Body nur error ohne message enthält", async () => {
    mockFetch(async () =>
      new Response(JSON.stringify({ error: "DIVISION_BY_ZERO" }), {
        status: 400,
        headers: { "Content-Type": "application/json" },
      }),
    );

    await expect(
      calculate({ left: 5, operator: "DIVIDE", right: 0 }),
    ).rejects.toThrow("Server-Fehler (DIVISION_BY_ZERO)");
  });

  it("wirft den Fallback, wenn der Fehler-Body weder message noch error enthält", async () => {
    mockFetch(async () =>
      new Response(JSON.stringify({ foo: "bar" }), {
        status: 400,
        headers: { "Content-Type": "application/json" },
      }),
    );

    await expect(
      calculate({ left: 1, operator: "ADD", right: 1 }),
    ).rejects.toThrow("Server-Fehler (HTTP 400)");
  });

  it("reicht ein AbortSignal an fetch weiter", async () => {
    const controller = new AbortController();
    let passedSignal: unknown = null;
    mockFetch(async (_url, init) => {
      passedSignal = init?.signal;
      return new Response(JSON.stringify({ result: 1 }), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      });
    });

    await calculate({ left: 1, operator: "ADD", right: 1 }, controller.signal);
    expect(passedSignal).toBe(controller.signal);
  });
});

describe("health()", () => {
  it("liefert true, wenn das Backend mit status UP antwortet", async () => {
    mockFetch(async () =>
      new Response(JSON.stringify({ status: "UP" }), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      }),
    );
    await expect(health()).resolves.toBe(true);
  });

  it("liefert false bei nicht-2xx-Status", async () => {
    mockFetch(async () => new Response("fehler", { status: 503 }));
    await expect(health()).resolves.toBe(false);
  });

  it("liefert false bei Netzwerkfehler", async () => {
    mockFetch(async () => {
      throw new TypeError("Network error");
    });
    await expect(health()).resolves.toBe(false);
  });

  it("liefert false, wenn das Backend einen anderen Status als UP meldet", async () => {
    mockFetch(async () =>
      new Response(JSON.stringify({ status: "DOWN" }), {
        status: 200,
        headers: { "Content-Type": "application/json" },
      }),
    );
    await expect(health()).resolves.toBe(false);
  });
});
