/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import java.math.BigInteger;
import java.util.Arrays;

class Display {
    private static final BigInteger MAX_UNSCALED = BigInteger.valueOf(99999999);


    // 0 - знак числа
    // 1,2,3,4,5,6,7,8,9 - мантисса с точкой
    // 10 - знак порядка
    // 11, 12 - порядок
    public static final int DISPLAY_SIZE = 13;

    private final char[] display = new char[DISPLAY_SIZE];

    public String asString() {
        return new String(display);
    }

    public void represent(Value value) {
        clear();
        var strValue = value.asString();
        for (int index = 0; index < strValue.length(); index++) {
            display[index] = strValue.charAt(index);
        }
    }

    public void represent(NumberBuffer buffer) {
        clear();

        var mantissa = buffer.getBuffer();
        for (int i = 0; i < mantissa.length(); i++) {
            display[i] = mantissa.charAt(i);
        }
    }

    public void clear() {
        Arrays.fill(display, ' ');
    }
}
