/**
 * Tests für hello.js
 *
 * Verwendet den integrierten Node.js Test Runner (node:test).
 * Aufruf: node --test tests/
 */

const assert = require('node:assert');
const { describe, it, mock, beforeEach, afterEach } = require('node:test');

// ---------------------------------------------------------------------------
// hello.js – Console-Ausgabe
// ---------------------------------------------------------------------------

describe('hello.js', () => {
    let logCalls;

    beforeEach(() => {
        logCalls = [];
        mock.method(console, 'log', (...args) => {
            logCalls.push(args.join(' '));
        });
    });

    afterEach(() => {
        mock.reset();
    });

    it('should print "Hello, World!" when required directly', () => {
        // Clean require cache
        delete require.cache[require.resolve('../hello.js')];
        require('../hello.js');
        assert.strictEqual(logCalls.length, 1, 'sollte genau einmal loggen');
        assert.strictEqual(logCalls[0], 'Hello, World!');
    });

    it('should be idempotent when required multiple times', () => {
        delete require.cache[require.resolve('../hello.js')];
        require('../hello.js');
        require('../hello.js'); // zweites require sollte Module-Cache nutzen
        assert.strictEqual(logCalls.length, 1, 'require-Cache verhindert erneute Ausführung');
    });
});
