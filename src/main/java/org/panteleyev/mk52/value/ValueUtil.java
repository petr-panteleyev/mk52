/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.value;

import java.util.Arrays;

import static org.panteleyev.mk52.engine.Constants.BYTE_0;
import static org.panteleyev.mk52.engine.Constants.BYTE_9;
import static org.panteleyev.mk52.engine.Constants.TETRADS_PER_REGISTER;

public final class ValueUtil {
    public static final byte[] MANTISSA_ONE = new byte[]{1, 0, 0, 0, 0, 0, 0, 0};


    public static boolean isNegative(byte[] bytes) {
        return (bytes[8] & 0xF) != 0;
    }

    public static boolean isExpNegative(byte[] bytes) {
        return (bytes[11] & 0xF) != 0;
    }

    public static int getExponent(byte[] bytes) {
        var exp = bytes[10] * 10 + bytes[9];
        return isExpNegative(bytes) ? -(100 - exp) : exp;
    }

    public static byte[] negate(byte[] bytes) {
        bytes[8] = bytes[8] == 0 ? BYTE_9 : BYTE_0;
        return bytes;
    }

    public static void setExponent(byte[] bytes, int exponent) {
        bytes[11] = exponent < 0 ? BYTE_9 : BYTE_0;
        if (exponent < 0) {
            exponent = 100 - Math.abs(exponent);
        }

        bytes[9] = (byte) (exponent % 10);
        bytes[10] = (byte) (exponent / 10);
    }

    public static void convertForIndirect(byte[] bytes) {
        var negative = isNegative(bytes);
        var expNegative = isExpNegative(bytes);
        var exp = getExponent(bytes);

        var fill = negative ? BYTE_9 : BYTE_0;

        if (!expNegative) {
            var zeroes = 7 - exp % 10;
            if (zeroes > 0) {
                System.arraycopy(bytes, zeroes, bytes, 0, 8 - zeroes);
                Arrays.fill(bytes, 8 - zeroes, 8, fill);
                setExponent(bytes, exp + zeroes);
            }
        } else {
            var zeroes = (10 + Math.abs(exp) % 10 - 3) % 10;
            if (zeroes < 8 && zeroes > 0) {
                System.arraycopy(bytes, zeroes, bytes, 0, 8 - zeroes);
                Arrays.fill(bytes, 8 - zeroes, 8, fill);
                setExponent(bytes, exp + zeroes);
            }
        }
    }

    public static void decrementMantissa(byte[] bytes) {
        for (int i = 0; i <= 7; i++) {
            if (bytes[i] > 0) {
                if (bytes[i] > 9) {
                    bytes[i] = 9;
                } else {
                    bytes[i] = (byte) (bytes[i] - 1);
                }
                return;
            } else {
                bytes[i] = 9;
            }
        }
        negate(bytes);
    }

    public static void incrementMantissa(byte[] bytes) {
        int mantissa = bytes[7] & 0xF;
        for (int i = 6; i >= 0; i--) {
            mantissa = mantissa * 10 + bytes[i];
        }
        if (mantissa == 99999999) {
            Arrays.fill(bytes, 0, 8, BYTE_0);
            if (isNegative(bytes)) {
                bytes[8] = 0;
            } else {
                bytes[8] = 1;
            }
        } else {
            mantissa++;
            for (int i = 0; i <= 7; i++) {
                bytes[i] = (byte) (mantissa % 10);
                mantissa = mantissa / 10;
            }
        }
    }

    public static int getIndirectValue(byte[] bytes) {
        return (bytes[1] & 0xF) * 10 + (bytes[0] & 0xF);
    }

    public static void checkLength(byte[] bytes) {
        if (bytes.length != TETRADS_PER_REGISTER) {
            throw new IllegalArgumentException("Byte value must be of length " + TETRADS_PER_REGISTER);
        }
    }
}
