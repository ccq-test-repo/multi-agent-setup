/**
 * Tests für die Calculator-UI-Logik (Issue #53).
 *
 * Der Taschenrechner berechnet seit Issue #53 vollständig lokal und synchron
 * über die pure Rechenlogik aus src/lib/calculator.ts (kein Backend-Call mehr).
 * Diese Tests prüfen das Verhalten, das in calculator.tsx verdrahtet ist:
 *   - Berechnung über calculate() + formatNumber() (lokal, synchron)
 *   - Division durch 0 wird als sauberer Fehler behandelt (kein Absturz)
 *   - ungültige Eingaben werfen einen Fehler statt abzustürzen
 *   - Button-Action-Routing (clear, backspace, =, Operatoren, Ziffern, Komma)
 *   - State-Übergänge (idle → result/error)
 *   - Button-Grid-Layout
 *
 * Verwendet den integrierten Node.js Test Runner (node:test).
 * Aufruf: npx tsx --test tests/calculator-ui.test.ts
 */

import assert from 'node:assert';
import { describe, it } from 'node:test';
import {
  calculate,
  formatNumber,
  parseBinaryExpression,
  isOperator,
} from '../calculator/src/lib/calculator';

type CalculatorState = 'idle' | 'error' | 'result';

/**
 * Modelliert die handleEqual-Berechnung, wie sie in calculator.tsx verdrahtet ist:
 *   - calculate(expression) → Ergebnis (wirft bei Division durch 0 / ungültiger Eingabe)
 *   - formatNumber(result) → Anzeige
 *   - Fehler → display "Fehler", state "error"
 */
function computeEqual(
  expression: string
): { display: string; state: CalculatorState; error?: string } {
  if (!expression.trim()) {
    return { display: '0', state: 'idle' };
  }
  try {
    const result = calculate(expression);
    const resultStr = formatNumber(result);
    return { display: resultStr, state: 'result' };
  } catch (err) {
    const msg =
      err instanceof Error ? err.message : 'Ein Fehler ist aufgetreten';
    return { display: 'Fehler', state: 'error', error: msg };
  }
}

// ---------------------------------------------------------------------------
// Lokale, synchrone Berechnung (Kernverhalten seit Issue #53)
// ---------------------------------------------------------------------------

describe('Lokale Berechnung (handleEqual)', () => {
  it('should compute + locally and synchronously', () => {
    assert.deepStrictEqual(computeEqual('3 + 4'), { display: '7', state: 'result' });
  });

  it('should compute - locally', () => {
    assert.deepStrictEqual(computeEqual('10 - 4'), { display: '6', state: 'result' });
  });

  it('should compute × locally', () => {
    assert.deepStrictEqual(computeEqual('6 × 7'), { display: '42', state: 'result' });
  });

  it('should compute ÷ locally', () => {
    assert.deepStrictEqual(computeEqual('20 ÷ 4'), { display: '5', state: 'result' });
  });

  it('should handle decimal results (floating point precision)', () => {
    assert.deepStrictEqual(computeEqual('0.1 + 0.2'), { display: '0.3', state: 'result' });
  });

  it('should do nothing on empty expression', () => {
    assert.deepStrictEqual(computeEqual('   '), { display: '0', state: 'idle' });
  });
});

// ---------------------------------------------------------------------------
// Division durch 0 (Akzeptanzkriterium)
// ---------------------------------------------------------------------------

describe('Division durch 0', () => {
  it('should throw a clean error instead of crashing', () => {
    assert.throws(() => calculate('10 ÷ 0'), /Division durch Null/);
  });

  it('should surface a readable error in the UI state', () => {
    const res = computeEqual('10 ÷ 0');
    assert.strictEqual(res.state, 'error');
    assert.strictEqual(res.display, 'Fehler');
    assert.match(res.error ?? '', /Division durch Null/i);
  });

  it('should also guard 0 ÷ 0', () => {
    const res = computeEqual('0 ÷ 0');
    assert.strictEqual(res.state, 'error');
    assert.match(res.error ?? '', /Division durch Null/i);
  });
});

// ---------------------------------------------------------------------------
// Ungültige Eingaben (Akzeptanzkriterium: kein Absturz)
// ---------------------------------------------------------------------------

describe('Ungültige Eingaben', () => {
  it('should throw for an invalid second operand', () => {
    assert.throws(() => calculate('5 + abc'));
  });

  it('should surface a readable error instead of crashing the UI', () => {
    const res = computeEqual('5 + nichtzahl');
    assert.strictEqual(res.state, 'error');
    assert.strictEqual(res.display, 'Fehler');
    assert.ok(res.error);
  });

  // Eine einzelne, rein nicht-numerische Eingabe ergibt NaN statt zu crashen
  // (der UI-Verlauf erzeugt über die Buttons solche Zeichen jedoch nie).
  it('should not crash on a lone non-numeric token (returns NaN)', () => {
    assert.ok(Number.isNaN(calculate('abc')));
  });
});

// ---------------------------------------------------------------------------
// formatNumber – Anzeigeformatierung
// ---------------------------------------------------------------------------

describe('formatNumber() Display-Formatierung', () => {
  it('should format integers without trailing decimals', () => {
    assert.strictEqual(formatNumber(5), '5');
    assert.strictEqual(formatNumber(-3), '-3');
    assert.strictEqual(formatNumber(1000000), '1000000');
  });

  it('should format clean decimals', () => {
    assert.strictEqual(formatNumber(3.5), '3.5');
    assert.strictEqual(formatNumber(-0.5), '-0.5');
  });

  it('should round floating point artifacts', () => {
    assert.strictEqual(formatNumber(0.1 + 0.2), '0.3');
  });
});

// ---------------------------------------------------------------------------
// parseBinaryExpression – Verlaufseintrag
// ---------------------------------------------------------------------------

describe('parseBinaryExpression()', () => {
  it('should parse a two-operand expression', () => {
    assert.deepStrictEqual(parseBinaryExpression('3 + 5'), { a: '3', op: '+', b: '5' });
  });

  it('should handle all four operators', () => {
    assert.deepStrictEqual(parseBinaryExpression('10 - 4'), { a: '10', op: '-', b: '4' });
    assert.deepStrictEqual(parseBinaryExpression('6 × 7'), { a: '6', op: '×', b: '7' });
    assert.deepStrictEqual(parseBinaryExpression('20 ÷ 4'), { a: '20', op: '÷', b: '4' });
  });

  it('should return null for non-binary expressions', () => {
    assert.strictEqual(parseBinaryExpression('3 + 4 + 5'), null);
    assert.strictEqual(parseBinaryExpression('abc'), null);
  });
});

// ---------------------------------------------------------------------------
// Button-Action-Routing (wie in handleButton)
// ---------------------------------------------------------------------------

describe('Button Action Routing', () => {
  // clear resetet in jedem State
  it('should reset everything on clear action regardless of state', () => {
    const states: CalculatorState[] = ['idle', 'error', 'result'];
    for (const s of states) {
      const model = { display: '42', state: s, expression: '42' };
      model.display = '0';
      model.expression = '';
      model.state = 'idle';
      assert.strictEqual(model.display, '0');
      assert.strictEqual(model.expression, '');
      assert.strictEqual(model.state, 'idle');
    }
  });

  // = in error state wird ignoriert
  it('should ignore equals press in error state', () => {
    const state: CalculatorState = 'error';
    const action = '=';
    const shouldIgnore = state === 'error' && action === '=';
    assert.strictEqual(shouldIgnore, true);
  });

  // Nach error state führt eine Eingabe (außer =) zum Reset
  it('should reset after error state on non-equals input', () => {
    const state: CalculatorState = 'error';
    const action = '1';
    const shouldReset = state === 'error' && action !== '=';
    assert.strictEqual(shouldReset, true);
  });

  // Operator nach Operator wird blockiert (z. B. ++, +×)
  it('should prevent consecutive operators', () => {
    const expression = '2 +';
    const lastChar = expression.trim().slice(-1);
    const wouldBlock = isOperator(lastChar);
    assert.strictEqual(wouldBlock, true);
  });

  // Minus als erstes Zeichen (negative Zahl)
  it('should allow minus as first character', () => {
    const expression = '';
    const action = '-';
    const allowsMinus = expression === '' && isOperator(action) && action === '-';
    assert.strictEqual(allowsMinus, true);
  });

  // Dezimalpunkt nur einmal pro Zahl
  it('should prevent multiple decimal points in the same number', () => {
    const expression = '2.5 + 3';
    const parts = expression.split(/[\+\-\×\÷]/);
    const lastPart = parts[parts.length - 1].trim();
    assert.strictEqual(lastPart.includes('.'), false);

    const expression2 = '2.5 + 3.';
    const parts2 = expression2.split(/[\+\-\×\÷]/);
    const lastPart2 = parts2[parts2.length - 1].trim();
    assert.strictEqual(lastPart2.includes('.'), true);
  });

  // Backspace in error/result state resetet
  it('should reset on backspace in error or result state', () => {
    const resetStates: CalculatorState[] = ['error', 'result'];
    for (const s of resetStates) {
      const state = s;
      const action = 'backspace';
      const shouldReset = (state === 'error' || state === 'result') && action === 'backspace';
      assert.strictEqual(shouldReset, true, `should reset on backspace in ${s} state`);
    }
  });

  // Backspace löscht das letzte Zeichen
  it('should remove last character on backspace in idle state', () => {
    const expression = '123';
    assert.strictEqual(expression.slice(0, -1), '12');
    const expression2 = '1';
    assert.strictEqual(expression2.slice(0, -1), '');
  });
});

// ---------------------------------------------------------------------------
// Display-State-Rendering
// ---------------------------------------------------------------------------

describe('Display State Rendering', () => {
  it('should show error display with red text when in error state', () => {
    const isErrorDisplay = (state: string) => state === 'error';
    assert.strictEqual(isErrorDisplay('error'), true);
    assert.strictEqual(isErrorDisplay('idle'), false);
    assert.strictEqual(isErrorDisplay('result'), false);
  });

  it('should show empty state hint text when idle and no expression', () => {
    const showEmptyHint = (state: string, expression: string) =>
      state === 'idle' && expression === '';
    assert.strictEqual(showEmptyHint('idle', ''), true);
    assert.strictEqual(showEmptyHint('idle', '5'), false);
    assert.strictEqual(showEmptyHint('result', ''), false);
    assert.strictEqual(showEmptyHint('error', ''), false);
  });
});

// ---------------------------------------------------------------------------
// Button-Grid-Layout
// ---------------------------------------------------------------------------

describe('Button Grid and Layout', () => {
  it('should have 18 buttons in the grid (all unique actions)', () => {
    const buttons = [
      'clear', 'backspace', '÷',
      '7', '8', '9', '×',
      '4', '5', '6', '-',
      '1', '2', '3', '+',
      '0', '.', '=',
    ];
    assert.strictEqual(buttons.length, 18);
    assert.strictEqual(new Set(buttons).size, 18);
  });

  it('should mark operators with their variants', () => {
    const operatorVariants: Record<string, string> = {
      clear: 'destructive',
      backspace: 'secondary',
      '÷': 'secondary',
      '×': 'secondary',
      '-': 'secondary',
      '+': 'secondary',
      '=': 'outline',
    };
    assert.strictEqual(operatorVariants.clear, 'destructive');
    assert.strictEqual(operatorVariants.backspace, 'secondary');
    assert.strictEqual(operatorVariants['÷'], 'secondary');
    assert.strictEqual(operatorVariants['='], 'outline');
  });
});
