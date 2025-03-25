/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

enum ValueMode {
    NORMAL,
    ADDRESS,
}

public record Value(double value, ValueMode mode) {
    public static final Value ZERO = new Value();
    public static final Value PI = new Value(3.1415926);
    public static final Value E = new Value(2.7182818);

    private static final double MAX_NATURAL = 99999999;
    private static final int MANTISSA_LIMIT = 8;

    private static final double MIN_VALUE = Double.parseDouble("1e-99");
    private static final double MAX_VALUE = Double.parseDouble("9.9999999e99");

    public Value() {
        this(0.0, ValueMode.NORMAL);
    }

    public Value(double value) {
        this(value, ValueMode.NORMAL);
    }

    public String asString() {
        if (Double.isInfinite(value) || Double.isNaN(value)) {
            return "ERROR";
        }

        var absValue = Math.abs(value);
        if (absValue > MAX_VALUE) {
            return "ERROR";
        }

        if (mode == ValueMode.ADDRESS) {
            return String.format("% 09d.", (int) value);
        } else {
            if (this == ZERO || value == 0) {
                return " 0.";
            }

            if (absValue >= 1 && absValue <= MAX_NATURAL) {
                var precision = MANTISSA_LIMIT - String.valueOf((int) absValue).length();

                var strValue = format(value, "% ." + precision + "f");
                if (strValue.indexOf(".") == -1) {
                    strValue.append('.');
                }

                stripTrailingZeroes(strValue, 1);
                return strValue.toString().stripTrailing();
            } else {
                var strValue = format(value, "% .7e");
                stripTrailingZeroes(strValue, 4);
                removeZeroExponent(strValue);
                return strValue.toString();
            }
        }
    }

    private StringBuilder format(double value, String formatString) {
        return new StringBuilder(String.format(formatString, value)
                .replace(",", ".")
                .replace("e", "")
                .replace("+", " "));
    }

    private void stripTrailingZeroes(StringBuilder sb, int delta) {
        for (int index = sb.length() - delta; index > 0; index--) {
            if (sb.charAt(index) == '0') {
                sb.setCharAt(index, ' ');
            } else {
                break;
            }
        }
    }

    private void removeZeroExponent(StringBuilder sb) {
        if (sb.toString().endsWith("00")) {
            sb.setLength(sb.length() - 3);
        }
    }
}
