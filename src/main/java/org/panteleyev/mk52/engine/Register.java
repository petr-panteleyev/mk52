/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;

import static org.panteleyev.mk52.engine.Constants.DISPLAY_SIZE;

public final class Register {
    public static final long ONE = 0x10000000;
    public static final long MINUS_ONE = 0x910000000L;
    public static final long PI = 0x31415926;

    private static final long REGISTER_MASK = 0xFFFF_FFFF_FFFFL;
    // Мантисса
    private static final long MANTISSA_MASK = 0xFFFF_FFFFL;
    private static final long MANTISSA_CLEAR_MASK = ~MANTISSA_MASK;

    private static final long MANTISSA_SIGN_MASK = 0xF_0000_0000L;
    private static final long MANTISSA_SIGN_SHIFT = 32;
    private static final long MANTISSA_NEGATIVE_BITS = 0x9_0000_0000L;
    private static final long MANTISSA_HI_DIGIT_MASK = 0xF000_0000L;
    private static final int MANTISSA_HI_DIGIT_SHIFT = 28;
    // Мантисса логического результата
    private static final long MANTISSA_LOGICAL_MASK = 0xFFF_FFFFL;
    private static final long MANTISSA_LOGICAL_BITS = 0x8000_0000L;

    // Экспонента
    private static final long EXPONENT_MASK = 0xFF0_0000_0000L;
    private static final long EXPONENT_AND_SIGN_MASK = 0xFFF0_0000_0000L;
    private static final long EXPONENT_LO_MASK = 0x0F0_0000_0000L;
    private static final int EXPONENT_LO_SHIFT = 36;
    private static final long EXPONENT_HI_MASK = 0xF00_0000_0000L;
    private static final int EXPONENT_HI_SHIFT = 40;
    private static final long EXPONENT_SIGN_MASK = 0xF000_0000_0000L;
    private static final long EXPONENT_NEGATIVE_BITS = 0x9000_0000_0000L;

    private static final int TETRAD_MASK = 0xF;
    private static final int TETRAD_1_MASK = TETRAD_MASK << 4;

    public static boolean isNegative(long register) {
        return (register & MANTISSA_SIGN_MASK) != 0;
    }

    public static boolean isExpNegative(long register) {
        return (register & EXPONENT_SIGN_MASK) != 0;
    }

    public static boolean isZero(long register) {
        return (register & MANTISSA_MASK) == 0;
    }

    public static int getExponent(long register) {
        var exp = (int) (((register & EXPONENT_HI_MASK) >> EXPONENT_HI_SHIFT) * 10
                + ((register & EXPONENT_LO_MASK) >> EXPONENT_LO_SHIFT));
        return isExpNegative(register) ? exp - 100 : exp;
    }

    public static long setExponent(long register, int exponent) {
        var expSign = 0L;
        if (exponent < 0) {
            exponent = 100 + exponent;
            expSign = EXPONENT_NEGATIVE_BITS;
        }

        long exponentLo = (long) ((exponent % 10) & TETRAD_MASK) << EXPONENT_LO_SHIFT;
        long exponentHi = (long) ((exponent / 10) & TETRAD_MASK) << EXPONENT_HI_SHIFT;

        return ((register & ~EXPONENT_AND_SIGN_MASK) & REGISTER_MASK)
                | expSign
                | exponentLo
                | exponentHi;
    }

    public static long modifyExponent(long x, int delta) {
        var exponent = Register.getExponent(x);
        return Register.setExponent(x, exponent + delta);
    }

    public static long setMantissaDigit(long x, int index, char digit) {
        return setMantissaDigit(x, index, digit - '0');
    }

    public static long setMantissaDigit(long x, int index, int digit) {
        if (index < 0 || index > 7) {
            throw new IllegalArgumentException();
        }

        long clearMask = 0xFL << (index * 4);
        long digitMask = ((digit) & 0xFL) << (index * 4);
        return (x & ~clearMask) | digitMask;
    }

    public static long clearMantissaDigit(long x, int index) {
        if (index < 0 || index > 7) {
            throw new IllegalArgumentException();
        }

        return (x & ~(0xFL << (index * 4))) & REGISTER_MASK;
    }

    public static long shiftLeft(long x, int count) {
        if (count <= 0) {
            return x;
        }

        var exp = getExponent(x);
        long mantissa = ((x & MANTISSA_MASK) << 4 * count) & MANTISSA_MASK;
        x = x & MANTISSA_CLEAR_MASK | mantissa;
        var newExp = isZero(x) ? 0 : exp - count;
        return setExponent(x, newExp);
    }

    public static long normalize(long register) {
        if (isZero(register)) {
            return 0;
        }

        if ((register & MANTISSA_HI_DIGIT_MASK) != 0) {
            return register;
        }

        long mantissa = register & MANTISSA_MASK;
        var exponent = getExponent(register);
        while ((mantissa & MANTISSA_HI_DIGIT_MASK) == 0) {
            mantissa = mantissa << 4;
            exponent--;
        }
        mantissa = mantissa & MANTISSA_MASK;
        register = setExponent(register, exponent);
        return register & MANTISSA_CLEAR_MASK | mantissa;
    }

    public static long negate(long register) {
        if ((register & MANTISSA_SIGN_MASK) == 0) {
            return ((register & ~MANTISSA_SIGN_MASK) | MANTISSA_NEGATIVE_BITS) & REGISTER_MASK;
        } else {
            return (register & ~MANTISSA_SIGN_MASK) & REGISTER_MASK;
        }
    }

    public static long abs(long register) {
        return register & ~MANTISSA_SIGN_MASK;
    }

    public static String toString(AtomicLong register) {
        return toString(register.get());
    }

    public static long toLogical(long register) {
        return register & MANTISSA_LOGICAL_MASK | MANTISSA_LOGICAL_BITS;
    }

    public static String toString(long register) {
        var charBuffer = new char[DISPLAY_SIZE - 1];
        Arrays.fill(charBuffer, ' ');

        var negative = isNegative(register);
        if (negative) {
            charBuffer[0] = '-';
        }

        int exponent = getExponent(register);

        var mantissaBits = register & MANTISSA_MASK;
        for (int i = 7; i >= 0; i--) {
            var digit = mantissaBits & TETRAD_MASK;
            charBuffer[i + 1] = Long.toString(digit & 0xF, 16).toUpperCase().charAt(0);
            mantissaBits >>= 4;
        }
        if (charBuffer[1] == ' ') {
            charBuffer[1] = '0';
        }

        var dotPosition = 2;
        if (exponent >= 0 && exponent <= 7) {
            dotPosition += exponent;
            exponent = 0;
        }

        // Убираем концевые нули
        for (int i = 8; i >= 0; i--) {
            if (i == dotPosition - 1) {
                break;
            }
            if (charBuffer[i] == '0') {
                charBuffer[i] = ' ';
            } else {
                break;
            }
        }

        if (exponent != 0) {
            var expStr = String.format("%02d", Math.abs(exponent));
            if (exponent < 0) {
                charBuffer[9] = '-';
            }
            charBuffer[10] = expStr.charAt(0);
            charBuffer[11] = expStr.charAt(1);
        }

        var result = new StringBuilder().append(charBuffer);
        result.insert(dotPosition, '.');
        return result.toString().stripTrailing();
    }

    public static double toDouble(long x) {
        var mantissa = calculateAbsoluteMantissa(x);
        if (isNegative(x)) {
            mantissa = -mantissa;
        }

        var exp = getExponent(x);
        var str = String.format("%dE%d", mantissa, exp - 7);
        return Double.parseDouble(str);
    }

    public static long valueOf(double x) {
//        var format = new DecimalFormat("0.0000000E00");
//        format.setMaximumIntegerDigits(1);
//        format.setRoundingMode(RoundingMode.HALF_DOWN);
//        format.setPositivePrefix("+");
//        var normalized = format.format(x)
//                .replace("e", "")
//                .replace("E", "")
//                .replace(".", "")
//                .replace(",", "");
//        var bbb = new StringBuilder(normalized);
//        if (bbb.charAt(9) != '-') {
//            bbb.insert(9, "+");
//            normalized = bbb.toString();
//        }

        var normalized = String.format("% .7e", x)
                .replace("e", "")
                .replace(".", "")
                .replace(",", "");

        var expValue = Integer.parseInt(normalized.substring(9, 12));

        var result = 0L;
        result = setExponent(result, expValue);
        if (x < 0) {
            result = result | MANTISSA_NEGATIVE_BITS;
        }

        var shift = 0;
        for (int i = 8; i >= 1; i--) {
            result = result | ((long) (normalized.charAt(i) - '0') << shift);
            shift += 4;
        }

        return result;
    }

    public static long convertForIndirect(long x) {
        var exp = getExponent(x);

        long fill = (long) (isNegative(x) ? 0x9 : 0x0) << MANTISSA_HI_DIGIT_SHIFT;
        long mantissaBits = x & MANTISSA_MASK;

        if (!isExpNegative(x)) {
            var zeroes = 7 - exp % 10;
            if (zeroes > 0) {
                for (int i = 0; i < zeroes; i++) {
                    mantissaBits = (mantissaBits >> 4) | fill;
                }
                x = setExponent(x, exp + zeroes);
            }
        } else {
            var zeroes = (10 + Math.abs(exp) % 10 - 3) % 10;
            if (zeroes < 8 && zeroes > 0) {
                for (int i = 0; i < zeroes; i++) {
                    mantissaBits = (mantissaBits >> 4) | fill;
                }
                x = setExponent(x, exp + zeroes);
            }
        }
        x = x & MANTISSA_CLEAR_MASK | mantissaBits;
        return x;
    }

    public static int getIndirectValue(long x) {
        return (int) ((x & TETRAD_MASK) + ((x & TETRAD_1_MASK) >> 4) * 10);
    }

    public static long decrementMantissa(long x) {
        for (int i = 0; i <= 7; i++) {
            var shift = 4 * i;
            long mask = (long) TETRAD_MASK << shift;

            var tetrad = (x & mask) >> shift;
            if (tetrad > 0) {
                if (tetrad > 9) {
                    x = (x & ~mask) | (9L << shift);
                } else {
                    x = (x & ~mask) | ((tetrad - 1) << shift);
                }
                return x;
            } else {
                x = (x & ~mask) | (9L << shift);
            }
        }
        return negate(x);
    }

    public static long incrementMantissa(long x) {
        var mantissa = calculateAbsoluteMantissa(x);

        if (mantissa == 99999999) {
            x = x & MANTISSA_CLEAR_MASK;

            if (isNegative(x)) {
                x = x & ~MANTISSA_SIGN_MASK;
            } else {
                x = x & ~MANTISSA_SIGN_MASK | (1L << MANTISSA_SIGN_SHIFT);
            }
        } else {
            mantissa++;
            for (int i = 0; i <= 7; i++) {
                x = setMantissaDigit(x, i, (int) (mantissa % 10));
                mantissa = mantissa / 10;
            }
        }

        return x;
    }

    private static long calculateAbsoluteMantissa(long x) {
        long mantissaBits = x & MANTISSA_MASK;
        long mantissa = 0L;
        long multiplier = 1;
        for (int i = 0; i <= 7; i++) {
            var digit = mantissaBits & TETRAD_MASK;
            mantissa = mantissa + digit * multiplier;
            multiplier *= 10;
            mantissaBits >>= 4;
        }
        return mantissa;
    }
}
