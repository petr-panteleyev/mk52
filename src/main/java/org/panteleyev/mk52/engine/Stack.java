/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

@SuppressWarnings("SuspiciousNameCombination")
class Stack {
    private Value x = Value.ZERO;
    private Value y = Value.ZERO;
    private Value z = Value.ZERO;
    private Value t = Value.ZERO;
    private Value x1 = Value.ZERO;

    private final NumberBuffer numberBuffer = new NumberBuffer();

    Stack() {
    }

    NumberBuffer numberBuffer() {
        return numberBuffer;
    }

    void reset() {
        x = Value.ZERO;
        x1 = Value.ZERO;
        y = Value.ZERO;
        z = Value.ZERO;
        t = Value.ZERO;
        numberBuffer.reset();
    }

    Value x() {
        if (numberBuffer.isInProgress()) {
            x = numberBuffer.getValue();
        }

        return x;
    }

    void setX(Value x) {
        this.x = x;
    }

    void push() {
        if (numberBuffer.isInProgress()) {
            x = numberBuffer.getValue();
        }

        t = z;
        z = y;
        y = x;
    }

    void rotate() {
        if (numberBuffer.isInProgress()) {
            x = numberBuffer.getValue();
        }

        var tempX = x;
        x = y;
        y = z;
        z = t;
        t = tempX;
    }

    void swap() {
        if (numberBuffer.isInProgress()) {
            x = numberBuffer.getValue();
        }

        var tempX = x;
        x = y;
        y = tempX;
    }

    void restoreX() {
        if (numberBuffer.isInProgress()) {
            x = numberBuffer.getValue();
        }

        t = z;
        z = y;
        y = x;
        x = x1;
    }

    void unaryOperation(UnaryOperator<Value> operation) {
        if (numberBuffer.isInProgress()) {
            x = numberBuffer.getValue();
        }

        x1 = x;
        x = operation.apply(x);
    }

    void binaryOperation(BinaryOperator<Value> operation) {
        if (numberBuffer.isInProgress()) {
            x = numberBuffer.getValue();
        }

        x1 = x;
        x = operation.apply(x, y);
        y = z;
        z = t;
    }

    void addCharacter(char c) {
        if (!numberBuffer.isInProgress()) {
            push();
        }
        numberBuffer.addDigit(c);
    }

    void enterExponent() {
        if (!numberBuffer.isInProgress()) {
            push();
        }
        numberBuffer.enterExponent();
    }

    void addCharacters(char[] chars) {
        for (var ch : chars) {
            addCharacter(ch);
        }
    }

    public String getStringValue() {
        var strValue = numberBuffer.isInProgress()? numberBuffer.getBuffer() : x.asString();
        var padLength = 13 - strValue.length();
        return strValue + " ".repeat(padLength);
    }

    void printStack() {
        System.out.println("x = " + x.asString() + "\ny = " + y.asString());
    }
}
