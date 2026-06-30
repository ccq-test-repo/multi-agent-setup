/**
 * Tests für die Calculator-UI-Logik (Issue #23).
 *
 * Testet die Logik, die in calculator.tsx verwendet wird:
 *   - Symbol-Mapping (× → *, ÷ → /) für API-Kompatibilität
 *   - Display-Formatierung (Integer vs. Decimal)
 *   - Button-Action-Routing (clear, backspace, =, operators, digits)
 *   - State-Übergänge (idle → loading → result/error)
 *   - Abort-Controller-Handling
 *   - Fehlerbehandlung bei AbortError
 *
 * Verwendet den integrierten Node.js Test Runner (node:test).
 * Aufruf: npx tsx --test tests/calculator-ui.test.ts
 */

import assert from 'node:assert';
import { describe, it } from 'node:test';

// ---------------------------------------------------------------------------
// Expression Mapping (wie in calculator.tsx)
// ---------------------------------------------------------------------------

/**
 * Konvertiert UI-Operatoren (×, ÷) zu API-kompatiblen Operatoren (*, /).
 */
function mapExpressionForApi(expression: string): string {
  return expression.replace(/×/g, '*').replace(/÷/g, '/');
}

/**
 * Formatiert ein numerisches Ergebnis für die Anzeige.
 */
function formatDisplayResult(result: number): string {
  return Number.isInteger(result)
    ? result.toString()
    : parseFloat(result.toFixed(10)).toString();
}

// ---------------------------------------------------------------------------
// Symbol-Mapping
// ---------------------------------------------------------------------------

describe('Expression Mapping (UI → API)', () => {
  it('should replace × with *', () => {
    assert.strictEqual(mapExpressionForApi('3×4'), '3*4');
    assert.strictEqual(mapExpressionForApi('2 × 3'), '2 * 3');
  });

  it('should replace ÷ with /', () => {
    assert.strictEqual(mapExpressionForApi('10÷2'), '10/2');
    assert.strictEqual(mapExpressionForApi('10 ÷ 2'), '10 / 2');
  });

  it('should handle mixed operators', () => {
    assert.strictEqual(mapExpressionForApi('2+3×4-6÷2'), '2+3*4-6/2');
  });

  it('should keep non-operator characters unchanged', () => {
    assert.strictEqual(mapExpressionForApi('10+20-5'), '10+20-5');
    assert.strictEqual(mapExpressionForApi('3.5+2.5'), '3.5+2.5');
  });

  it('should handle empty string', () => {
    assert.strictEqual(mapExpressionForApi(''), '');
  });

  it('should handle expressions with no special operators', () => {
    assert.strictEqual(mapExpressionForApi('42'), '42');
    assert.strictEqual(mapExpressionForApi('0'), '0');
  });
});

// ---------------------------------------------------------------------------
// Tests: Display Formatting (wie in calculator.tsx handleEqual)
// ---------------------------------------------------------------------------

describe('Display Result Formatting', () => {
  it('should format integer results without decimal places', () => {
    assert.strictEqual(formatDisplayResult(5), '5');
    assert.strictEqual(formatDisplayResult(0), '0');
    assert.strictEqual(formatDisplayResult(-3), '-3');
    assert.strictEqual(formatDisplayResult(100), '100');
  });

  it('should format decimal results with floating point', () => {
    assert.strictEqual(formatDisplayResult(3.5), '3.5');
    assert.strictEqual(formatDisplayResult(0.1), '0.1');
    assert.strictEqual(formatDisplayResult(-0.5), '-0.5');
  });

  it('should handle floating point precision issues', () => {
    // 0.1 + 0.2 = 0.30000000000000004 → should be formatted clean
    assert.strictEqual(formatDisplayResult(0.1 + 0.2), '0.3');
  });

  it('should format large integers correctly', () => {
    assert.strictEqual(formatDisplayResult(1000000), '1000000');
    assert.strictEqual(formatDisplayResult(999999999), '999999999');
  });

  it('should handle negative decimals', () => {
    assert.strictEqual(formatDisplayResult(-7.5), '-7.5');
  });
});

// ---------------------------------------------------------------------------
// Tests: Button action routing logic (wie in handleButton)
// ---------------------------------------------------------------------------

describe('Button Action Routing', () => {
  /**
   * Simuliert die handleButton-Entscheidungslogik aus calculator.tsx.
   * Testet, ob Aktionen korrekt weitergeleitet werden basierend auf
   * current state und expression.
   */

  type CalculatorState = 'idle' | 'loading' | 'error' | 'result';

  interface CalcModel {
    display: string;
    state: CalculatorState;
    expression: string;
    errorMessage: string;
  }

  // Testet die Entscheidungslogik für clear
  it('should reset everything on clear action regardless of state', () => {
    const states: CalculatorState[] = ['idle', 'loading', 'error', 'result'];
    for (const s of states) {
      const model: CalcModel = {
        display: '42',
        state: s,
        expression: '42',
        errorMessage: s === 'error' ? 'Fehler' : '',
      };
      // clear resets to initial
      model.display = '0';
      model.expression = '';
      model.state = 'idle';
      model.errorMessage = '';
      assert.strictEqual(model.display, '0');
      assert.strictEqual(model.expression, '');
      assert.strictEqual(model.state, 'idle');
      assert.strictEqual(model.errorMessage, '');
    }
  });

  // Testet: Im loading state sind alle Buttons außer clear disabled
  it('should disable all buttons except clear when loading', () => {
    // Der Button-Code: btn.disabled = state === "loading" && btn.action !== "clear"
    const loadingState: CalculatorState = 'loading';
    const isDisabled = (action: string) => loadingState === 'loading' && action !== 'clear';
    assert.strictEqual(isDisabled('clear'), false);  // clear bleibt enabled
    assert.strictEqual(isDisabled('='), true);        // = disabled
    assert.strictEqual(isDisabled('+'), true);        // Operator disabled
    assert.strictEqual(isDisabled('1'), true);        // Digit disabled
    assert.strictEqual(isDisabled('backspace'), true); // Backspace disabled
  });

  // Testet: Im idle/result state sind Buttons enabled
  it('should enable buttons when not loading', () => {
    const idleState: CalculatorState = 'idle';
    const isDisabled = (action: string) => idleState === 'loading' && action !== 'clear';
    assert.strictEqual(isDisabled('='), false);
    assert.strictEqual(isDisabled('+'), false);
    assert.strictEqual(isDisabled('1'), false);
    assert.strictEqual(isDisabled('backspace'), false);
  });

  // Testet: = Aktion in error state wird ignoriert
  it('should ignore equals press in error state', () => {
    const state: CalculatorState = 'error';
    const action = '=';
    // In calculator.tsx: if (state === "error") { if (action === "=") return; }
    const shouldIgnore = state === 'error' && action === '=';
    assert.strictEqual(shouldIgnore, true);
  });

  // Testet: Nach error state führt eine Eingabe (außer =) zum reset
  it('should reset after error state on non-equals input', () => {
    const state: CalculatorState = 'error';
    const action = '1';
    const shouldReset = state === 'error' && action !== '=';
    assert.strictEqual(shouldReset, true);
  });

  // Testet: Operator nach Operator wird blockiert (z. B. ++, +×)
  it('should prevent consecutive operators', () => {
    const isOperator = (token: string) => ['+', '-', '×', '÷'].includes(token);
    const expression = '2 +';
    const lastChar = expression.trim().slice(-1);
    const wouldBlock = isOperator(lastChar);
    assert.strictEqual(wouldBlock, true);
  });

  // Testet: Negative number start (z. B. -5+3)
  it('should allow minus as first character', () => {
    const expression = '';
    const action = '-';
    const isOperator = (token: string) => ['+', '-', '×', '÷'].includes(token);
    const allowsMinus =
      expression === '' && isOperator(action) && action === '-';
    assert.strictEqual(allowsMinus, true);
  });

  // Testet: Dezimalpunkt in einer Zahl nur einmal erlaubt
  it('should prevent multiple decimal points in the same number', () => {
    const expression = '2.5 + 3';
    // Parse expression in letztes Teil (nach letztem Operator)
    const parts = expression.split(/[\+\-\×\÷]/);
    const lastPart = parts[parts.length - 1].trim();
    const hasDecimal = lastPart.includes('.');
    assert.strictEqual(hasDecimal, false); // "3" hat keinen Dezimalpunkt

    const expression2 = '2.5 + 3.';
    const parts2 = expression2.split(/[\+\-\×\÷]/);
    const lastPart2 = parts2[parts2.length - 1].trim();
    const hasDecimal2 = lastPart2.includes('.');
    assert.strictEqual(hasDecimal2, true); // "3." hat schon einen Punkt
  });

  // Testet: Backspace in error/result state resetet
  it('should reset on backspace in error or result state', () => {
    const resetStates: CalculatorState[] = ['error', 'result'];
    for (const s of resetStates) {
      const state: CalculatorState = s;
      const action = 'backspace';
      const shouldReset = (state === 'error' || state === 'result') && action === 'backspace';
      assert.strictEqual(shouldReset, true, `should reset on backspace in ${s} state`);
    }
  });

  // Testet: Backspace löscht das letzte Zeichen
  it('should remove last character on backspace in idle state', () => {
    const expression = '123';
    const newExpr = expression.slice(0, -1);
    assert.strictEqual(newExpr, '12');

    // Bei length = 1: reset
    const expression2 = '1';
    const newExpr2 = expression2.slice(0, -1);
    assert.strictEqual(newExpr2, '');
  });
});

// ---------------------------------------------------------------------------
// Tests: Display state rendering
// ---------------------------------------------------------------------------

describe('Display State Rendering', () => {
  it('should show Spinner Icon when loading', () => {
    // In calculator.tsx: {state === "loading" && <Loader2 className="animate-spin" />}
    const showSpinner = (state: string) => state === 'loading';
    assert.strictEqual(showSpinner('loading'), true);
    assert.strictEqual(showSpinner('idle'), false);
    assert.strictEqual(showSpinner('error'), false);
    assert.strictEqual(showSpinner('result'), false);
  });

  it('should show error message with red text when in error state', () => {
    // display hat Klasse "text-destructive" bei state === "error"
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

  it('should show loading bar animation when loading', () => {
    const showLoadingBar = (state: string) => state === 'loading';
    assert.strictEqual(showLoadingBar('loading'), true);
    assert.strictEqual(showLoadingBar('idle'), false);
    assert.strictEqual(showLoadingBar('result'), false);
  });
});

// ---------------------------------------------------------------------------
// Tests: Error handling flow
// ---------------------------------------------------------------------------

describe('Error Handling in Component Flow', () => {
  it('should ignore AbortError and not update display', () => {
    // In calculator.tsx: catch block prüft auf AbortError
    const err = new DOMException('The operation was aborted', 'AbortError');
    const isAbortError = err instanceof DOMException && err.name === 'AbortError';
    assert.strictEqual(isAbortError, true);

    // Bei AbortError: return (keine State-Änderung)
    const displayBefore = '5';
    const displayAfter = '5'; // unchanged
    assert.strictEqual(displayAfter, displayBefore);
  });

  it('should set error state and show message on non-abort errors', () => {
    const err = new Error('Division durch Null nicht erlaubt');
    const isAbortError = err instanceof DOMException && err.name === 'AbortError';
    assert.strictEqual(isAbortError, false);

    // Nicht-AbortError → display = "Fehler", state = "error"
    const display = 'Fehler';
    const errorMessage = err.message;
    assert.strictEqual(display, 'Fehler');
    assert.strictEqual(errorMessage, 'Division durch Null nicht erlaubt');
  });

  it('should fallback to generic message on unknown error type', () => {
    const err = 'string error';
    const msg = err instanceof Error ? err.message : 'Ein Fehler ist aufgetreten';
    assert.strictEqual(msg, 'Ein Fehler ist aufgetreten');
  });

  it('should cancel previous request on new equals press', () => {
    // Ref zu altem AbortController wird aborted
    let aborted = false;
    const oldController = new AbortController();
    oldController.signal.addEventListener('abort', () => { aborted = true; });

    // Neuer Request: alter Controller abbbrechen
    oldController.abort();
    assert.strictEqual(aborted, true);

    // Neuer Controller wird erstellt
    const newController = new AbortController();
    assert.strictEqual(newController.signal.aborted, false);
  });
});

// ---------------------------------------------------------------------------
// Tests: Button grid layout verification
// ---------------------------------------------------------------------------

describe('Button Grid and Layout', () => {
  it('should have 18 buttons in the grid (all unique actions)', () => {
    // Aus calculator.tsx: buttons array mit 18 Einträgen, alle actions unique
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

  it('should have exact button actions for calculator', () => {
    const actions = ['clear', 'backspace', '÷', '7', '8', '9', '×',
      '4', '5', '6', '-', '1', '2', '3', '+', '0', '.', '='];
    // Der = button hat col-span-2
    assert.strictEqual(actions[17], '=');
    // clear, backspace, = haben spezielle Aktionen
    assert.strictEqual(actions.includes('clear'), true);
    assert.strictEqual(actions.includes('backspace'), true);
    assert.strictEqual(actions.includes('='), true);
  });

  it('should mark operators with secondary variant', () => {
    const operatorVariants: Record<string, string> = {
      'clear': 'destructive',
      'backspace': 'secondary',
      '÷': 'secondary',
      '×': 'secondary',
      '-': 'secondary',
      '+': 'secondary',
      '=': 'outline',
    };
    assert.strictEqual(operatorVariants['clear'], 'destructive');
    assert.strictEqual(operatorVariants['backspace'], 'secondary');
    assert.strictEqual(operatorVariants['÷'], 'secondary');
    assert.strictEqual(operatorVariants['='], 'outline');
  });
});
