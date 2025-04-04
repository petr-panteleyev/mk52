/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.panteleyev.mk52.value.DecimalValue;
import org.panteleyev.mk52.value.Value;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import static org.panteleyev.mk52.engine.Constants.DISPLAY_SIZE;

@SuppressWarnings("SuspiciousNameCombination")
class Stack {
    private Value x = DecimalValue.ZERO;
    private Value y = DecimalValue.ZERO;
    private Value z = DecimalValue.ZERO;
    private Value t = DecimalValue.ZERO;
    private Value x1 = DecimalValue.ZERO;

    private final NumberBuffer numberBuffer = new NumberBuffer();

    private final AtomicReference<OpCode> lastExecutedOpCode;

    Stack(AtomicReference<OpCode> lastExecutedOpCode) {
        this.lastExecutedOpCode = lastExecutedOpCode;
    }

    NumberBuffer numberBuffer() {
        return numberBuffer;
    }

    void reset() {
        x = DecimalValue.ZERO;
        x1 = DecimalValue.ZERO;
        y = DecimalValue.ZERO;
        z = DecimalValue.ZERO;
        t = DecimalValue.ZERO;
        numberBuffer.reset();
    }

    Value x() {
        if (numberBuffer.isInProgress()) {
            x = numberBuffer.getValue();
        }

        var tmp = x;
        x = x.toNormal();
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
                y.asString(),
                z.asString(),
                t.asString(),
                x1.asString()
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
        y = x.toNormal();
        x = x.toNormal();
    }

    void rotate() {
        if (numberBuffer.isInProgress()) {
            x = numberBuffer.getValue();
        }

        var tempX = x.toNormal();
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

        var tempX = x.toNormal();
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

        x1 = x.toNormal();

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

        x1 = x.toNormal();

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
        var strValue = numberBuffer.isInProgress() ? numberBuffer.getBuffer() : x.asString();
        var padLength = DISPLAY_SIZE - strValue.length();
        return strValue + " ".repeat(padLength);
    }
}
