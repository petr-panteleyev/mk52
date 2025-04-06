/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.value;

import java.util.Arrays;

import static org.panteleyev.mk52.Mk52Application.logger;
import static org.panteleyev.mk52.engine.Constants.MAX_VALUE;
import static org.panteleyev.mk52.engine.Constants.TETRADS_PER_REGISTER;
import static org.panteleyev.mk52.engine.Constants.BYTE_0;
import static org.panteleyev.mk52.value.ValueUtil.checkLength;
import static org.panteleyev.mk52.value.ValueUtil.getExponent;
import static org.panteleyev.mk52.value.ValueUtil.isExpNegative;
import static org.panteleyev.mk52.value.ValueUtil.isNegative;

public final class Value {
    public static final Value ZERO = new Value(
            new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}
    );

    private final byte[] bytes;
    private final double doubleValue;
    private final String stringValue;

    public Value(byte[] bytes) {
        checkLength(bytes);
        this.bytes = new byte[TETRADS_PER_REGISTER];
        System.arraycopy(bytes, 0, this.bytes, 0, TETRADS_PER_REGISTER);
        doubleValue = doubleFromInternal(bytes);
        stringValue = stringFromBytes(bytes);
    }

    public Value(double doubleValue) {
        if (!invalid(doubleValue)) {
            bytes = bytesFromDouble(doubleValue);
            stringValue = stringFromBytes(bytes);
            this.doubleValue = doubleFromInternal(bytes);
        } else {
            bytes = new byte[TETRADS_PER_REGISTER];
            stringValue = "NAN";
            this.doubleValue = doubleValue;
        }
    }

    public double doubleValue() {
        return doubleValue;
    }

    public String stringValue() {
        return stringValue;
    }

    public boolean invalid() {
        return Double.isNaN(doubleValue) || Double.isInfinite(doubleValue) || doubleValue > MAX_VALUE;
    }

    private static boolean invalid(double doubleValue) {
        return Double.isNaN(doubleValue) || Double.isInfinite(doubleValue) || doubleValue > MAX_VALUE;
    }

    public byte[] getBytes() {
        var copy = new byte[TETRADS_PER_REGISTER];
        System.arraycopy(bytes, 0, copy, 0, TETRADS_PER_REGISTER);
        return copy;
    }

    // TODO
    public Value normalize() {
        if (bytes[7] == 0) {
            return new Value(doubleValue);
        } else {
            return this;
        }
    }

    public Value negate() {
        return new Value(ValueUtil.negate(getBytes()));
    }

    static double doubleFromInternal(byte[] bytes) {
        var expSign = bytes[11] & 0xF;
        var sign = bytes[8] & 0xF;

        var exp = bytes[10] * 10 + bytes[9];

        var mantissa = bytes[7] & 0xF;
        for (int i = 6; i >= 0; i--) {
            mantissa = mantissa * 10 + (bytes[i] & 0xF);
        }
        var builder = new StringBuilder();
        builder.append(mantissa);
        if (builder.length() > 8) {
            builder.setLength(8);
        } else if (builder.length() < 8) {
            builder.insert(0, "0".repeat(8 - builder.length()));
        }

        builder.insert(1, '.');

        if (sign != 0) {
            builder.insert(0, '-');
        }

        if (exp != 0) {
            if (expSign != 0) {
                builder.append("e-").append(100 - exp);
            } else {
                builder.append("e").append(exp);
            }
        }

        try {
            return Double.parseDouble(builder.toString());
        } catch (Exception ex) {
            logger().severe("Failed to parse double from: " + builder);
            return Double.NaN;
        }
    }

    static String stringFromBytes(byte[] bytes) {
        var negativeExp = isExpNegative(bytes);
        var negative = isNegative(bytes);

        var exp = getExponent(bytes);

        var mantissaBuilder = new StringBuilder();
        for (int i = 7; i >= 0; i--) {
            mantissaBuilder.append(Integer.toString(bytes[i] & 0xF, 16).toUpperCase());
        }

        if (negativeExp) {
            mantissaBuilder.insert(1, '.');
        } else {
            if (exp <= 7) {
                mantissaBuilder.insert(exp + 1, '.');
                exp = 0;
            } else {
                mantissaBuilder.insert(1, '.');
            }
        }
        if (negative) {
            mantissaBuilder.insert(0, '-');
        } else {
            mantissaBuilder.insert(0, ' ');
        }

        for (int i = mantissaBuilder.length() - 1; i >= 0; i--) {
            if (mantissaBuilder.charAt(i) != '0') {
                break;
            }
            mantissaBuilder.setCharAt(i, ' ');
        }

        var exponentBuilder = new StringBuilder();
        if (exp != 0) {
            exponentBuilder.append(negativeExp ? '-' : ' ');
            exponentBuilder.append(String.format("%02d", Math.abs(exp)));
        }

        return (mantissaBuilder + exponentBuilder.toString()).stripTrailing();
    }

    static byte[] bytesFromDouble(double value) {
        var bytes = new byte[TETRADS_PER_REGISTER];
        Arrays.fill(bytes, BYTE_0);

        var normalized = String.format("% .7e", value)
                .replace("e", "")
                .replace(".", "")
                .replace(",", "");

        var mantissaSign = (byte) (normalized.charAt(0) == '-' ? 9 : 0);
        var expSign = (byte) (normalized.charAt(9) == '-' ? 9 : 0);

        var expValue = Integer.parseInt(normalized.substring(10, 12));
        if (expSign == 9) {
            expValue = 100 - expValue;
        }
        var expStr = String.format("%02d", expValue);

        bytes[11] = expSign;
        bytes[10] = (byte) (expStr.charAt(0) - '0');
        bytes[9] = (byte) (expStr.charAt(1) - '0');
        bytes[8] = mantissaSign;

        for (int i = 7; i >= 0; i--) {
            bytes[i] = (byte) (normalized.charAt(8 - i) - '0');
        }
        return bytes;
    }
}
