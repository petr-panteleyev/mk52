/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.panteleyev.mk52.program.OpCode;
import org.panteleyev.mk52.value.Value;
import org.panteleyev.mk52.value.ValueUtil;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import static org.panteleyev.mk52.engine.Constants.BYTE_0;
import static org.panteleyev.mk52.engine.Constants.EXPONENT_POSITION;
import static org.panteleyev.mk52.engine.Constants.EXPONENT_SIGN_POSITION;
import static org.panteleyev.mk52.engine.Constants.MANTISSA_POSITION;
import static org.panteleyev.mk52.engine.Constants.MANTISSA_SIZE;
import static org.panteleyev.mk52.engine.Constants.TETRADS_PER_REGISTER;
import static org.panteleyev.mk52.util.StringUtil.padToDisplay;

@SuppressWarnings("SuspiciousNameCombination")
class Stack {
    private Value x = Value.ZERO;
    private Value y = Value.ZERO;
    private Value z = Value.ZERO;
    private Value t = Value.ZERO;
    private Value x1 = Value.ZERO;
    private String display = x.stringValue();

    private final AtomicReference<OpCode> lastExecutedOpCode;

    private final byte[] xBuffer = new byte[TETRADS_PER_REGISTER];
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
        x = Value.ZERO;
        x1 = Value.ZERO;
        y = Value.ZERO;
        z = Value.ZERO;
        t = Value.ZERO;
        display = x.stringValue();
        //
        enteringExponent = false;
    }

    synchronized Value x() {
        var tmp = x;
        x = x.normalize();
        display = x.stringValue();
        return tmp;
    }

    synchronized String display() {
        return display;
    }

    synchronized Value xOrBuffer() {
        return x;
    }

    synchronized public StackSnapshot getSnapshot() {
        return new StackSnapshot(
                x.stringValue(),
                y.stringValue(),
                z.stringValue(),
                t.stringValue(),
                x1.stringValue(),
                display
        );
    }

    synchronized void setX(Value x) {
        enteringExponent = false;

        this.x = x;
        display = x.stringValue();
    }

    synchronized public void clearX() {
        enteringExponent = false;

        x = Value.ZERO;
        display = x.stringValue();
    }

    synchronized public void pi() {
        enteringExponent = false;

        push();
        x1 = x;
        x = Value.PI;
        display = x.stringValue();
    }

    synchronized void push() {
        enteringExponent = false;

        t = z;
        z = y;
        y = x.normalize();
        x = x.normalize();
        display = x.stringValue();
    }

    synchronized void rotate() {
        enteringExponent = false;

        var tempX = x.normalize();
        x = y;
        y = z;
        z = t;
        t = tempX;
        x1 = tempX;
        display = x.stringValue();
    }

    synchronized void swap() {
        enteringExponent = false;

        var tempX = x.normalize();
        x = y;
        y = tempX;
        x1 = tempX;
        display = x.stringValue();
    }

    synchronized void restoreX() {
        enteringExponent = false;

        t = z;
        z = y;
        y = x;
        x = x1;
        display = x.stringValue();
    }

    synchronized void unaryOperation(UnaryOperator<Value> operation) {
        enteringExponent = false;

        x1 = x.normalize();

        var result = operation.apply(x);
        if (result.invalid()) {
            throw new ArithmeticException("Error");
        }

        x = result;
        display = x.stringValue();
    }

    synchronized void binaryOperation(BinaryOperator<Value> operation) {
        enteringExponent = false;

        x1 = x.normalize();

        var result = operation.apply(x, y);
        if (result.invalid()) {
            throw new ArithmeticException("Error");
        }

        x = result;
        y = z;
        z = t;
        display = x.stringValue();
    }

    synchronized void binaryKeepYOperation(BinaryOperator<Value> operation) {
        enteringExponent = false;

        x1 = x;

        var result = operation.apply(x, y);
        if (result.invalid()) {
            throw new ArithmeticException("Error");
        }

        x = result;
        display = x.stringValue();
    }

    synchronized void negate() {
        if (!enteringExponent) {
            x = x.negate().normalize();
            display = x.stringValue();
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
            Arrays.fill(xBuffer, BYTE_0);
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
            ValueUtil.setExponent(xBuffer, exponentSign * enteredExponent + xBufferExponent);
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
            xBuffer[xBufferPosition] = (byte) (c - '0');
            mantissaBuffer[mantissaIndex++] = c;
            if (!hasDot) {
                xBufferExponent = 7 - xBufferPosition;
                ValueUtil.setExponent(xBuffer, xBufferExponent);
                mantissaBuffer[mantissaIndex] = '.';
            }
            xBufferPosition--;
        }
        if (enteringExponent) {
            x = new Value(xBuffer).normalize();
            display = new String(exponentDisplayBuffer).stripTrailing();
        } else {
            x = new Value(xBuffer).normalize();
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
        System.arraycopy(x.getBytes(), 0, xBuffer, 0, TETRADS_PER_REGISTER);

        // Если X == 0., то ставим в 1.
        var allZero = true;
        for (int i = 0; i < MANTISSA_SIZE; i++) {
            if (xBuffer[i] != 0) {
                allZero = false;
                break;
            }
        }
        if (allZero) {
            xBuffer[7] = 1;
            x = new Value(xBuffer);
            exponentDisplayBuffer[MANTISSA_POSITION] = '1';
        }

        xBufferExponent = ValueUtil.getExponent(xBuffer);

        enteredExponent = 0;
        display = new String(exponentDisplayBuffer);
    }
}
