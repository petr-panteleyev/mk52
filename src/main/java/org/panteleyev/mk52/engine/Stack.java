/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.panteleyev.mk52.program.OpCode;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import static org.panteleyev.mk52.engine.Constants.EXPONENT_POSITION;
import static org.panteleyev.mk52.engine.Constants.EXPONENT_SIGN_POSITION;
import static org.panteleyev.mk52.engine.Constants.MANTISSA_POSITION;
import static org.panteleyev.mk52.engine.Constants.MANTISSA_SIZE;
import static org.panteleyev.mk52.util.StringUtil.padToDisplay;

class Stack {
    private final AtomicLong x = new AtomicLong(0);
    private final AtomicLong y = new AtomicLong(0);
    private final AtomicLong z = new AtomicLong(0);
    private final AtomicLong t = new AtomicLong(0);
    private final AtomicLong x1 = new AtomicLong(0);
    private String display = Register.toString(x);

    private final AtomicReference<OpCode> lastExecutedOpCode;

    private long xBuffer = 0;
    private final char[] mantissaBuffer = new char[MANTISSA_SIZE + 1];
    private int mantissaIndex = 0;
    private char[] exponentDisplayBuffer = null;
    private int xBufferPosition = 7;
    private int xBufferExponent = 0;
    private boolean hasDot = false;
    private boolean enteringExponent = false;
    private int enteredExponent = 0;
    private int exponentSign = 1;

    Stack(AtomicReference<OpCode> lastExecutedOpCode) {
        this.lastExecutedOpCode = lastExecutedOpCode;
    }

    synchronized void reset() {
        x.set(0);
        x1.set(0);
        y.set(0);
        z.set(0);
        t.set(0);
        display = Register.toString(x);
        //
        enteringExponent = false;
    }

    synchronized long x() {
        var tmp = x.get();
        x.set(Register.normalize(x.get()));
        display = Register.toString(x);
        return tmp;
    }

    synchronized String display() {
        return display;
    }

    synchronized long xOrBuffer() {
        return x.get();
    }

    synchronized public StackSnapshot getSnapshot() {
        return new StackSnapshot(
                Register.toString(x),
                Register.toString(y),
                Register.toString(z),
                Register.toString(t),
                Register.toString(x1),
                display
        );
    }

    synchronized void setX(long x) {
        enteringExponent = false;
        this.x.set(x);
        display = Register.toString(x);
    }

    synchronized public void clearX() {
        enteringExponent = false;

        x.set(0);
        display = Register.toString(x);
    }

    synchronized public void pi() {
        enteringExponent = false;

        push();
        x1.set(x.get());
        x.set(Register.PI);
        display = Register.toString(x);
    }

    synchronized void push() {
        enteringExponent = false;

        t.set(z.get());
        z.set(y.get());
        y.set(Register.normalize(x.get()));
        x.set(Register.normalize(x.get()));
        display = Register.toString(x);
    }

    synchronized void rotate() {
        enteringExponent = false;

        var tempX = Register.normalize(x.get());
        x.set(y.get());
        y.set(z.get());
        z.set(t.get());
        t.set(tempX);
        x1.set(tempX);
        display = Register.toString(x);
    }

    synchronized void swap() {
        enteringExponent = false;

        var tempX = Register.normalize(x.get());
        x.set(y.get());
        y.set(tempX);
        x1.set(tempX);
        display = Register.toString(x);
    }

    synchronized void restoreX() {
        enteringExponent = false;

        t.set(z.get());
        z.set(y.get());
        y.set(x.get());
        x.set(x1.get());
        display = Register.toString(x);
    }

    synchronized void unaryOperation(UnaryOperator<Long> operation) {
        enteringExponent = false;

        x1.set(Register.normalize(x.get()));

        var result = operation.apply(x.get());
        x.set(Register.normalize(result));
        display = Register.toString(result);
    }

    synchronized void binaryOperation(BinaryOperator<Long> operation) {
        enteringExponent = false;

        x1.set(Register.normalize(x.get()));

        var result = operation.apply(x.get(), y.get());

        x.set(Register.normalize(result));
        y.set(z.get());
        z.set(t.get());
        display = Register.toString(result);
    }

    synchronized void binaryKeepYOperation(BinaryOperator<Long> operation) {
        enteringExponent = false;

        x1.set(x.get());

        var result = operation.apply(x.get(), y.get());

        x.set(Register.normalize(result));
        display = Register.toString(result);
    }

    synchronized void negate() {
        if (!enteringExponent) {
            x.set(Register.normalize(Register.negate(x.get())));
            display = Register.toString(x);
        } else {
            addCharacter('-');
        }
    }

    synchronized void addCharacter(char c) {
        var lastOpCode = lastExecutedOpCode.get();
        if (!OpCode.isDigit(lastOpCode) && lastOpCode != OpCode.DOT && !enteringExponent) {
            if (lastOpCode != OpCode.PUSH && lastOpCode != OpCode.CLEAR_X) {
                push();
            }
            xBuffer = 0;
            xBufferPosition = 7;
            xBufferExponent = 0;
            hasDot = false;
            mantissaIndex = 0;
            Arrays.fill(mantissaBuffer, ' ');
        }

        if (enteringExponent) {
            if (c == '.') {
                throw new ArithmeticException();
            }
            if (c == '-') {
                exponentSign = -exponentSign;
            } else {
                enteredExponent = (enteredExponent * 10 + c - '0') % 100;
                if (Math.abs(exponentSign * enteredExponent + xBufferExponent) > 99) {
                    throw new ArithmeticException();
                }
            }
            xBuffer = Register.setExponent(xBuffer, exponentSign * enteredExponent + xBufferExponent);
            updateExponentDisplay();
        } else {
            if (c == '.') {
                if (xBufferPosition == 7 || hasDot) {
                    return;
                }
                hasDot = true;
                mantissaBuffer[mantissaIndex++] = c;
                return;
            }

            if (xBufferPosition < 0) {
                return;
            }
            xBuffer = Register.setMantissaDigit(xBuffer, xBufferPosition, c);
            mantissaBuffer[mantissaIndex++] = c;
            if (!hasDot) {
                xBufferExponent = 7 - xBufferPosition;
                xBuffer = Register.setExponent(xBuffer, xBufferExponent);
                mantissaBuffer[mantissaIndex] = '.';
            }
            xBufferPosition--;
        }
        if (enteringExponent) {
            x.set(Register.normalize(xBuffer));
            display = new String(exponentDisplayBuffer).stripTrailing();
        } else {
            x.set(Register.normalize(xBuffer));
            display = " " + new String(mantissaBuffer).stripTrailing();
        }
    }

    private void updateExponentDisplay() {
        if (exponentSign == -1) {
            exponentDisplayBuffer[EXPONENT_SIGN_POSITION] = '-';
        } else {
            exponentDisplayBuffer[EXPONENT_SIGN_POSITION] = ' ';
        }
        exponentDisplayBuffer[EXPONENT_POSITION] = (char) (enteredExponent / 10 + '0');
        exponentDisplayBuffer[EXPONENT_POSITION + 1] = (char) (enteredExponent % 10 + '0');
    }

    synchronized void enterExponent() {
        enteringExponent = true;
        exponentSign = 1;

        // Запоминаем текущий дисплей
        exponentDisplayBuffer = padToDisplay(display).toCharArray();

        // Ставим экспоненту +00
        exponentDisplayBuffer[EXPONENT_SIGN_POSITION] = ' ';
        exponentDisplayBuffer[EXPONENT_POSITION] = '0';
        exponentDisplayBuffer[EXPONENT_POSITION + 1] = '0';

        // Запоминаем текущие байты регистра X
        xBuffer = x.get();

        // Если X == 0., то ставим в 1.
        if (Register.isZero(xBuffer)) {
            xBuffer = Register.ONE;
            x.set(xBuffer);
            exponentDisplayBuffer[MANTISSA_POSITION] = '1';
        }

        xBufferExponent = Register.getExponent(xBuffer);

        enteredExponent = 0;
        display = new String(exponentDisplayBuffer);
    }
}
