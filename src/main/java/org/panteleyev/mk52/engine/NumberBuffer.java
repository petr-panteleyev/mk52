/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import java.util.Arrays;

import static org.panteleyev.mk52.engine.Constants.DISPLAY_SIZE;
import static org.panteleyev.mk52.engine.Constants.MANTISSA_SIZE;

class NumberBuffer {
    private boolean hasDot = false;

    private final char[] buffer = new char[DISPLAY_SIZE];
    private static final int MANTISSA_SIGN_INDEX = 0;
    private static final int MANTISSA_INDEX = MANTISSA_SIGN_INDEX + 1;
    private static final int EXPONENT_SIGN_INDEX = 10;
    private static final int EXPONENT_INDEX = EXPONENT_SIGN_INDEX + 1;

    private int mantissaIndex = MANTISSA_INDEX;

    private boolean enteringMantissa = false;
    private boolean enteringExponent = false;

    void reset() {
        clear();

        enteringMantissa = false;
        enteringExponent = false;
    }

    void clear() {
        Arrays.fill(buffer, ' ');
        mantissaIndex = MANTISSA_INDEX;
        hasDot = false;
    }

    boolean isInProgress() {
        return enteringMantissa || enteringExponent;
    }

    String getMantissaDigits() {
        return new String(buffer, 0, MANTISSA_SIZE + 2);
    }

    String getExponentDigits() {
        return new String(buffer, EXPONENT_SIGN_INDEX, 3);
    }

    String getBuffer() {
        return new String(buffer);
    }

    void addDigit(char ch) {
        if (!isInProgress()) {
            enteringMantissa = true;
            enteringExponent = false;
            clear();
        }

        if (enteringMantissa) {
            if (ch == '-') {
                changeSign(MANTISSA_SIGN_INDEX);
            } else if (ch == '.') {
                if (!hasDot) {
                    buffer[mantissaIndex++] = ch;
                    hasDot = true;
                }
            } else {
                if (mantissaIndex <= MANTISSA_SIZE + (hasDot ? 1 : 0)) {
                    buffer[mantissaIndex++] = ch;
                    if (!hasDot) {
                        buffer[mantissaIndex] = '.';
                    }
                }
            }
        }

        if (enteringExponent) {
            if (ch == '-') {
                changeSign(EXPONENT_SIGN_INDEX);
            } else if (Character.isDigit(ch)) {
                buffer[EXPONENT_INDEX] = buffer[EXPONENT_INDEX + 1];
                buffer[EXPONENT_INDEX + 1] = ch;
            }
        }
    }

    private void changeSign(int index) {
        buffer[index] = buffer[index] == ' ' ? '-' : ' ';
    }

    void enterExponent() {
        enteringMantissa = false;
        enteringExponent = true;

        if (mantissaIndex == MANTISSA_INDEX) {
            buffer[mantissaIndex++] = '1';
            buffer[mantissaIndex++] = '.';
        }

        buffer[EXPONENT_INDEX] = '0';
        buffer[EXPONENT_INDEX + 1] = '0';
    }

    Value getValue() {
        enteringMantissa = false;
        enteringExponent = false;
        return getCurrentValue();
    }

    Value getCurrentValue() {
        var valueString = new StringBuilder(getMantissaDigits().trim());
        var exponent = getExponentDigits();
        if (!exponent.isBlank()) {
            valueString.append("E").append(exponent.trim());
        }

        var value = Double.parseDouble(valueString.toString());

        return new Value(value);
    }
}
