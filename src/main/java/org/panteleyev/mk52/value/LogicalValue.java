/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.value;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.panteleyev.mk52.engine.Constants.TETRADS_PER_REGISTER;
import static org.panteleyev.mk52.engine.Constants.ZERO_BYTE;

public record LogicalValue(int value) implements Value {
    @Override
    public String asString() {
        var builder = new StringBuilder(" 8." + String.format("%07X", value));
        stripTrailingZeroes(builder);
        return builder.toString();
    }

    @Override
    public Value toNormal() {
        return this;
    }

    @Override
    public DecimalValue toDecimal() {
        var accum = BigDecimal.valueOf(8);
        var temp = value;
        for (int i = 7; i >= 1; i--) {
            var add = BigDecimal.valueOf(temp & 0xF).movePointLeft(i);
            accum = accum.add(add);
            temp = temp >> 4;
        }
        return new DecimalValue(accum.doubleValue());
    }

    @Override
    public boolean invalid() {
        return false;
    }

    @Override
    public byte[] toByteArray() {
        var bytes = new byte[TETRADS_PER_REGISTER];
        Arrays.fill(bytes, ZERO_BYTE);

        var temp = value;
        for (int i = 0; i < 7; i++) {
            bytes[i] = (byte) (temp & 0xF);
            temp = temp >> 4;
        }
        bytes[7] = 8;
        return bytes;
    }

    private static void stripTrailingZeroes(StringBuilder sb) {
        while (sb.charAt(sb.length() - 1) == '0') {
            sb.deleteCharAt(sb.length() - 1);
        }
    }
}
