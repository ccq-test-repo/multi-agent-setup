package com.pathiful.calculator.service;

import org.springframework.stereotype.Service;

/**
 * Evaluates arithmetic expressions with operator precedence.
 * Supports +, -, *, /, parentheses, negative numbers, and decimals.
 */
@Service
public class CalculatorService {

    private int pos;
    private String expr;
    private int len;

    public double calculate(String input) {
        if (input == null || input.isBlank()) {
            throw new IllegalArgumentException("Expression must not be empty");
        }
        this.pos = 0;
        this.expr = input.trim();
        this.len = this.expr.length();

        double result = parseExpression();

        if (pos < len) {
            throw new IllegalArgumentException("Invalid expression: unexpected character '" + expr.charAt(pos) + "' at position " + pos);
        }

        return result;
    }

    // '+' and '-' (lowest precedence)
    private double parseExpression() {
        double result = parseTerm();
        while (pos < len) {
            skipWhitespace();
            if (pos >= len) break;
            char op = expr.charAt(pos);
            if (op == '+') {
                pos++;
                result += parseTerm();
            } else if (op == '-') {
                pos++;
                result -= parseTerm();
            } else {
                break;
            }
        }
        return result;
    }

    // '*' and '/' (higher precedence)
    private double parseTerm() {
        double result = parseFactor();
        while (pos < len) {
            skipWhitespace();
            if (pos >= len) break;
            char op = expr.charAt(pos);
            if (op == '*') {
                pos++;
                result *= parseFactor();
            } else if (op == '/') {
                pos++;
                double divisor = parseFactor();
                if (divisor == 0) {
                    throw new ArithmeticException("Division by zero");
                }
                result /= divisor;
            } else {
                break;
            }
        }
        return result;
    }

    // numbers, parentheses, or unary minus
    private double parseFactor() {
        skipWhitespace();

        if (pos >= len) {
            throw new IllegalArgumentException("Unexpected end of expression");
        }

        char c = expr.charAt(pos);

        // handle unary minus
        if (c == '-') {
            pos++;
            skipWhitespace();
            // check if this is a negative number (followed by digit or '.')
            if (pos < len && (Character.isDigit(expr.charAt(pos)) || expr.charAt(pos) == '.')) {
                return -parseNumber();
            }
            // otherwise it's a binary minus — we already consumed it above in parseExpression,
            // but here it means we have a leading unary minus
            // Re-interpret: this is for cases like "-3+2" at the start
            // For unary minus after a binary op like "4+-3", the parseExpression/parseTerm
            // would see '+' then '-' and consume the '-' as binary in parseTerm.
            // Actually, "4+-3": parseExpression gets 4, then sees '+', consumes,
            // calls parseTerm which calls parseFactor. pos points at '-', and since nothing
            // was read yet for the factor, this is the unary case. So we handle it here.
            return -parseFactor();
        }

        if (c == '+' && pos + 1 < len) {
            // unary plus, skip it
            pos++;
            skipWhitespace();
            return parseFactor();
        }

        if (c == '(') {
            pos++;
            double val = parseExpression();
            skipWhitespace();
            if (pos >= len || expr.charAt(pos) != ')') {
                throw new IllegalArgumentException("Missing closing parenthesis");
            }
            pos++;
            return val;
        }

        if (Character.isDigit(c) || c == '.') {
            return parseNumber();
        }

        throw new IllegalArgumentException("Unexpected character '" + c + "' at position " + pos);
    }

    private double parseNumber() {
        skipWhitespace();
        int start = pos;
        boolean hasDot = false;
        while (pos < len) {
            char c = expr.charAt(pos);
            if (Character.isDigit(c)) {
                pos++;
            } else if (c == '.' && !hasDot) {
                hasDot = true;
                pos++;
            } else {
                break;
            }
        }
        if (pos == start) {
            throw new IllegalArgumentException("Expected number at position " + pos);
        }
        return Double.parseDouble(expr.substring(start, pos));
    }

    private void skipWhitespace() {
        while (pos < len && Character.isWhitespace(expr.charAt(pos))) {
            pos++;
        }
    }
}
