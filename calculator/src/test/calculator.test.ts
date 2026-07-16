import { describe, it, expect } from "vitest";
import { isOperator, opToApiOperation, formatNumber } from "@/lib/calculator";

describe("isOperator", () => {
  it("returns true for valid operators", () => {
    expect(isOperator("+")).toBe(true);
    expect(isOperator("-")).toBe(true);
    expect(isOperator("×")).toBe(true);
    expect(isOperator("÷")).toBe(true);
  });

  it("returns false for non-operator tokens", () => {
    expect(isOperator("=")).toBe(false);
    expect(isOperator("C")).toBe(false);
    expect(isOperator("0")).toBe(false);
    expect(isOperator(".")).toBe(false);
    expect(isOperator("")).toBe(false);
  });
});

describe("opToApiOperation", () => {
  it("maps display operators to API operation names", () => {
    expect(opToApiOperation("+")).toBe("add");
    expect(opToApiOperation("-")).toBe("subtract");
    expect(opToApiOperation("×")).toBe("multiply");
    expect(opToApiOperation("÷")).toBe("divide");
  });
});

describe("formatNumber", () => {
  it("formats integers without decimal places", () => {
    expect(formatNumber(42)).toBe("42");
    expect(formatNumber(0)).toBe("0");
    expect(formatNumber(-7)).toBe("-7");
  });

  it("formats numbers with decimals up to 10 places", () => {
    expect(formatNumber(3.14)).toBe("3.14");
    expect(formatNumber(0.1 + 0.2)).toBe("0.3");
  });

  it("handles large integers below 1e15", () => {
    expect(formatNumber(999_999_999_999_999)).toBe("999999999999999");
  });

  it("does not add trailing zeros", () => {
    expect(formatNumber(5.0)).toBe("5");
    expect(formatNumber(1.5)).toBe("1.5");
  });
});
