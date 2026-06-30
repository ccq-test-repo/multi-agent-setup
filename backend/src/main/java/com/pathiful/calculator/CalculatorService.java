package com.pathiful.calculator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service for evaluating arithmetic expressions.
 * Supports +, -, *, / with correct operator precedence.
 */
@Service
public class CalculatorService {

    private static final Logger log = LoggerFactory.getLogger(CalculatorService.class);

    /**
     * Evaluates a mathematical expression string.
     *
     * @param expression a string like "2+3*4"
     * @return the computed result
     * @throws IllegalArgumentException if the expression is invalid or division by zero occurs
     */
    public double evaluate(String expression) {
        if (expression == null || expression.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid expression");
        }

        String cleaned = expression.replaceAll("\\s+", "");
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Invalid expression");
        }

        // Tokenize: split into numbers and operators
        List<String> tokens = tokenize(cleaned);
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("Invalid expression");
        }

        // First pass: handle * and / (higher precedence)
        List<Object> processed = new ArrayList<>();
        processed.add(parseNumber(tokens.get(0)));

        for (int i = 1; i < tokens.size(); i += 2) {
            String op = tokens.get(i);
            double num = parseNumber(tokens.get(i + 1));

            if ("*".equals(op) || "/".equals(op)) {
                double last = (double) processed.remove(processed.size() - 1);
                if ("*".equals(op)) {
                    processed.add(last * num);
                } else {
                    if (num == 0) {
                        throw new IllegalArgumentException("Division by zero");
                    }
                    processed.add(last / num);
                }
            } else {
                processed.add(op);
                processed.add(num);
            }
        }

        // Second pass: handle + and -
        double result = (double) processed.get(0);
        for (int i = 1; i < processed.size(); i += 2) {
            String op = (String) processed.get(i);
            double num = (double) processed.get(i + 1);
            if ("+".equals(op)) {
                result += num;
            } else {
                result -= num;
            }
        }

        log.debug("Calculated: {} = {}", expression, result);
        return result;
    }

    private List<String> tokenize(String expr) {
        List<String> tokens = new ArrayList<>();
        StringBuilder numBuf = new StringBuilder();
        char prev = 0;

        for (int i = 0; i < expr.length(); i++) {
            char ch = expr.charAt(i);

            if (ch == '+' || ch == '-' || ch == '*' || ch == '/') {
                // Handle negative numbers: if first char or after operator
                if (ch == '-' && (i == 0 || isOperator(prev))) {
                    numBuf.append(ch);
                    prev = ch;
                    continue;
                }
                if (numBuf.isEmpty()) {
                    // Unexpected operator
                    throw new IllegalArgumentException("Invalid expression");
                }
                tokens.add(numBuf.toString());
                tokens.add(String.valueOf(ch));
                numBuf.setLength(0);
            } else if (Character.isDigit(ch) || ch == '.') {
                numBuf.append(ch);
            } else {
                throw new IllegalArgumentException("Invalid expression");
            }
            prev = ch;
        }

        if (numBuf.isEmpty()) {
            throw new IllegalArgumentException("Invalid expression");
        }
        tokens.add(numBuf.toString());
        return tokens;
    }

    private boolean isOperator(char ch) {
        return ch == '+' || ch == '-' || ch == '*' || ch == '/';
    }

    private double parseNumber(String s) {
        try {
            return Double.parseDouble(s);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid expression");
        }
    }
}
