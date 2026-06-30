/**
 * Tests für Taschenrechner-Logik (Issue #21).
 *
 * Testet die puren Rechenfunktionen aus src/lib/calculator.ts:
 *   - isOperator()
 *   - applyOperation()
 *   - calculate()
 *   - formatNumber()
 *
 * Verwendet den integrierten Node.js Test Runner (node:test) + tsx für TypeScript.
 * Aufruf: npx tsx --test tests/calculator.test.ts
 *
 * HINWEIS: Im Gegensatz zur vorherigen Version importieren diese Tests
 * die echten Funktionen aus dem Modul statt sie zu duplizieren.
 * Änderungen in calculator.tsx → calculator.ts → Tests bleiben synchron.
 */

import assert from 'node:assert';
import { describe, it } from 'node:test';
import {
  calculate,
  formatNumber,
  applyOperation,
  isOperator,
} from '../calculator/src/lib/calculator';

// ---------------------------------------------------------------------------
// Tests: isOperator
// ---------------------------------------------------------------------------

describe('isOperator()', () => {
  it('should recognize +, -, ×, ÷ as operators', () => {
    assert.strictEqual(isOperator('+'), true);
    assert.strictEqual(isOperator('-'), true);
    assert.strictEqual(isOperator('\u00D7'), true);
    assert.strictEqual(isOperator('\u00F7'), true);
  });

  it('should reject non-operator characters', () => {
    assert.strictEqual(isOperator('*'), false);
    assert.strictEqual(isOperator('/'), false);
    assert.strictEqual(isOperator('='), false);
    assert.strictEqual(isOperator('.'), false);
    assert.strictEqual(isOperator('0'), false);
    assert.strictEqual(isOperator(''), false);
  });
});

// ---------------------------------------------------------------------------
// Tests: applyOperation
// ---------------------------------------------------------------------------

describe('applyOperation()', () => {
  it('should add two numbers', () => {
    assert.strictEqual(applyOperation(2, '+', 3), 5);
    assert.strictEqual(applyOperation(0, '+', 0), 0);
    assert.strictEqual(applyOperation(-1, '+', 1), 0);
    assert.strictEqual(applyOperation(1.5, '+', 2.3), 3.8);
  });

  it('should subtract two numbers', () => {
    assert.strictEqual(applyOperation(10, '-', 4), 6);
    assert.strictEqual(applyOperation(5, '-', 10), -5);
    assert.strictEqual(applyOperation(1.5, '-', 0.5), 1.0);
  });

  it('should multiply two numbers', () => {
    assert.strictEqual(applyOperation(3, '\u00D7', 4), 12);
    assert.strictEqual(applyOperation(-2, '\u00D7', 5), -10);
    assert.strictEqual(applyOperation(0, '\u00D7', 100), 0);
    assert.strictEqual(applyOperation(1.5, '\u00D7', 2), 3);
  });

  it('should divide two numbers', () => {
    assert.strictEqual(applyOperation(10, '\u00F7', 2), 5);
    assert.strictEqual(applyOperation(7, '\u00F7', 2), 3.5);
    assert.strictEqual(applyOperation(0, '\u00F7', 5), 0);
    assert.strictEqual(applyOperation(-6, '\u00F7', 3), -2);
  });

  it('should throw on division by zero', () => {
    assert.throws(() => applyOperation(5, '\u00F7', 0), /Division durch Null/);
    assert.throws(() => applyOperation(0, '\u00F7', 0), /Division durch Null/);
  });
});

// ---------------------------------------------------------------------------
// Tests: formatNumber
// ---------------------------------------------------------------------------

describe('formatNumber()', () => {
  it('should format integers without decimal places', () => {
    assert.strictEqual(formatNumber(5), '5');
    assert.strictEqual(formatNumber(0), '0');
    assert.strictEqual(formatNumber(-3), '-3');
    assert.strictEqual(formatNumber(100), '100');
  });

  it('should format decimals with appropriate precision', () => {
    assert.strictEqual(formatNumber(3.5), '3.5');
    assert.strictEqual(formatNumber(0.1), '0.1');
    assert.strictEqual(formatNumber(0.1 + 0.2), '0.3');
  });

  it('should strip unnecessary trailing zeros from decimals', () => {
    assert.strictEqual(formatNumber(2.0), '2');
    assert.strictEqual(formatNumber(1.5), '1.5');
  });
});

// ---------------------------------------------------------------------------
// Tests: calculate (Hauptlogik)
// ---------------------------------------------------------------------------

describe('calculate() – Grundrechenarten', () => {
  it('should add two numbers', () => {
    assert.strictEqual(calculate('2+3'), 5);
    assert.strictEqual(calculate('10+20'), 30);
    assert.strictEqual(calculate('0+0'), 0);
  });

  it('should subtract two numbers', () => {
    assert.strictEqual(calculate('10-4'), 6);
    assert.strictEqual(calculate('4-10'), -6);
    assert.strictEqual(calculate('0-5'), -5);
  });

  it('should multiply two numbers', () => {
    assert.strictEqual(calculate('3\u00D74'), 12);
    assert.strictEqual(calculate('0\u00D75'), 0);
    assert.strictEqual(calculate('-2\u00D73'), -6);
  });

  it('should divide two numbers', () => {
    assert.strictEqual(calculate('10\u00F72'), 5);
    assert.strictEqual(calculate('7\u00F72'), 3.5);
    assert.strictEqual(calculate('0\u00F75'), 0);
  });

  it('should throw on division by zero', () => {
    assert.throws(() => calculate('5\u00F70'), /Division durch Null/);
  });
});

describe('calculate() – Operator-Präzedenz', () => {
  it('should handle × before +', () => {
    assert.strictEqual(calculate('2+3\u00D74'), 14);
  });

  it('should handle ÷ before -', () => {
    assert.strictEqual(calculate('10-6\u00F72'), 7);
  });

  it('should handle multiple operators with precedence', () => {
    assert.strictEqual(calculate('2+3\u00D74-6\u00F72'), 11);
  });

  it('should handle consecutive × and ÷', () => {
    assert.strictEqual(calculate('10\u00F72\u00D73'), 15);
    assert.strictEqual(calculate('10\u00D72\u00F75'), 4);
  });

  it('should evaluate left-to-right for same precedence', () => {
    assert.strictEqual(calculate('10-2-3'), 5);
    assert.strictEqual(calculate('10+2+3'), 15);
    assert.strictEqual(calculate('10+2-3'), 9);
    assert.strictEqual(calculate('10-2+3'), 11);
  });
});

describe('calculate() – Edge Cases', () => {
  it('should return 0 for empty input', () => {
    assert.strictEqual(calculate(''), 0);
  });

  it('should handle single number', () => {
    assert.strictEqual(calculate('42'), 42);
    assert.strictEqual(calculate('0'), 0);
    assert.strictEqual(calculate('-5'), -5);
  });

  it('should handle decimal numbers', () => {
    assert.strictEqual(calculate('1.5+2.5'), 4);
    assert.ok(Math.abs(calculate('0.1+0.2') - 0.3) < 1e-10, 'should be close to 0.3');
    assert.strictEqual(calculate('3.5\u00D72'), 7);
  });

  it('should handle negative numbers at start', () => {
    assert.strictEqual(calculate('-5+3'), -2);
    assert.strictEqual(calculate('-10\u00F72'), -5);
  });

  it('should throw for invalid input in second operand', () => {
    assert.throws(() => calculate('5+a'), /Ungültige Eingabe/);
    assert.ok(isNaN(calculate('abc')));
    assert.ok(isNaN(calculate('a+5')));
  });
});

describe('calculate() – Gemischte Ausdrücke', () => {
  it('should handle chain: 2+3+4', () => {
    assert.strictEqual(calculate('2+3+4'), 9);
  });

  it('should handle chain: 20-5-3-2', () => {
    assert.strictEqual(calculate('20-5-3-2'), 10);
  });

  it('should handle chain with mixed operators', () => {
    assert.strictEqual(calculate('2\u00D73+4\u00F72'), 8);
    assert.strictEqual(calculate('10\u00F72-3\u00D72'), -1);
  });

  it('should handle result with floating point precision', () => {
    const result = calculate('10\u00F73');
    assert.ok(Math.abs(result - 3.3333333333) < 0.0001);
  });
});

// ---------------------------------------------------------------------------
// Tests: Display-Format-Logik (end-to-end output check)
// ---------------------------------------------------------------------------

describe('calculate() + formatNumber() – Display-Integration', () => {
  it('should format integer result as integer', () => {
    const result = calculate('4\u00D75');
    assert.strictEqual(formatNumber(result), '20');
  });

  it('should format clean decimal result', () => {
    const result = calculate('10\u00F74');
    assert.strictEqual(formatNumber(result), '2.5');
  });

  it('should format zero correctly', () => {
    const result = calculate('0+0');
    assert.strictEqual(formatNumber(result), '0');
  });

  it('should format negative result', () => {
    const result = calculate('3-10');
    assert.strictEqual(formatNumber(result), '-7');
  });
});
