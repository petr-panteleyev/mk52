/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.value;

import java.util.Arrays;

import static org.panteleyev.mk52.engine.Constants.TETRADS_PER_REGISTER;
import static org.panteleyev.mk52.engine.Constants.ZERO_BYTE;
import static org.panteleyev.mk52.util.StringUtil.stripTrailingZeroes;

public record DecimalValue(double value, ValueMode mode) implements Value {
    public enum ValueMode {
        NORMAL,
        ADDRESS
    }

    public static final DecimalValue ZERO = new DecimalValue();
    public static final DecimalValue PI = new DecimalValue(3.1415926);

    private static final double MAX_NATURAL = 99999999;
    private static final int MANTISSA_LIMIT = 8;
    private static final String ERROR_MSG = "EDDOD";

    private static final double MIN_VALUE = Double.parseDouble("1e-99");
    private static final double MAX_VALUE = Double.parseDouble("9.9999999e99");

    public DecimalValue() {
        this(0.0, ValueMode.NORMAL);
    }

    public DecimalValue(double value) {
        this(value, ValueMode.NORMAL);
    }

    public String asString() {
        if (invalid()) {
            return ERROR_MSG;
        }

        var absValue = Math.abs(value);
        if (absValue > MAX_VALUE) {
            return ERROR_MSG;
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

    @Override
    public byte[] toByteArray() {
        var line = new byte[TETRADS_PER_REGISTER];
        Arrays.fill(line, ZERO_BYTE);

        var normalized = String.format("% .7e", value())
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

        line[11] = expSign;
        line[10] = (byte) (expStr.charAt(0) - '0');
        line[9] = (byte) (expStr.charAt(1) - '0');
        line[8] = mantissaSign;

        for (int i = 7; i >= 0; i--) {
            line[i] = (byte) (normalized.charAt(8 - i) - '0');
        }
        return line;
    }

    private StringBuilder format(double value, String formatString) {
        return new StringBuilder(String.format(formatString, value)
                .replace(",", ".")
                .replace("e", "")
                .replace("+", " "));
    }

    public Value toNormal() {
        if (mode == ValueMode.ADDRESS) {
            return new DecimalValue(value, ValueMode.NORMAL);
        } else {
            return this;
        }
    }

    @Override
    public DecimalValue toDecimal() {
        return this;
    }

    private void removeZeroExponent(StringBuilder sb) {
        if (sb.toString().endsWith("00")) {
            sb.setLength(sb.length() - 3);
        }
    }


    public boolean invalid() {
        return Double.isNaN(value) || Double.isInfinite(value);
    }
}
