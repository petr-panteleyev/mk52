/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.panteleyev.mk52.value.Value;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import static org.panteleyev.mk52.engine.Constants.DISPLAY_SIZE;

@SuppressWarnings("SuspiciousNameCombination")
class Stack {
    private Value x = Value.ZERO;
    private Value y = Value.ZERO;
    private Value z = Value.ZERO;
    private Value t = Value.ZERO;
    private Value x1 = Value.ZERO;

    private final NumberBuffer numberBuffer = new NumberBuffer();

    private final AtomicReference<OpCode> lastExecutedOpCode;

    Stack(AtomicReference<OpCode> lastExecutedOpCode) {
        this.lastExecutedOpCode = lastExecutedOpCode;
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

        var tmp = x;
        x = x.normalize();
        return tmp;
    }

    Value xOrBuffer() {
        if (numberBuffer.isInProgress()) {
            return numberBuffer.getCurrentValue();
        } else {
            return x;
        }
    }

    public StackSnapshot getSnapshot() {
        return new StackSnapshot(
                getStringValue(),
                y.stringValue(),
                z.stringValue(),
                t.stringValue(),
                x1.stringValue()
        );
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
        y = x.normalize();
        x = x.normalize();
    }

    void rotate() {
        if (numberBuffer.isInProgress()) {
            x = numberBuffer.getValue();
        }

        var tempX = x.normalize();
        x = y;
        y = z;
        z = t;
        t = tempX;
        x1 = tempX;
    }

    void swap() {
        if (numberBuffer.isInProgress()) {
            x = numberBuffer.getValue();
        }

        var tempX = x.normalize();
        x = y;
        y = tempX;
        x1 = tempX;
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

        x1 = x.normalize();

        var result = operation.apply(x);
        if (result.invalid()) {
            throw new ArithmeticException("Error");
        }

        x = result;
    }

    void binaryOperation(BinaryOperator<Value> operation) {
        if (numberBuffer.isInProgress()) {
            x = numberBuffer.getValue();
        }

        x1 = x.normalize();

        var result = operation.apply(x, y);
        if (result.invalid()) {
            throw new ArithmeticException("Error");
        }

        x = result;
        y = z;
        z = t;
    }

    void binaryKeepYOperation(BinaryOperator<Value> operation) {
        if (numberBuffer.isInProgress()) {
            x = numberBuffer.getValue();
        }

        x1 = x;

        var result = operation.apply(x, y);
        if (result.invalid()) {
            throw new ArithmeticException("Error");
        }

        x = result;
    }

    void addCharacter(char c) {
        if (!numberBuffer.isInProgress() && lastExecutedOpCode.get() != OpCode.PUSH) {
            push();
        }
        numberBuffer.addDigit(c);
    }

    void enterExponent() {
        if (!numberBuffer.isInProgress() && lastExecutedOpCode.get() != OpCode.PUSH) {
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
        var strValue = numberBuffer.isInProgress() ? numberBuffer.getBuffer() : x.stringValue();
        var padLength = DISPLAY_SIZE - strValue.length();
        return strValue + " ".repeat(padLength);
    }
}
