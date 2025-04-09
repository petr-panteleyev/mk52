/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.program;

import static org.panteleyev.mk52.engine.Constants.BYTE_0;
import static org.panteleyev.mk52.engine.Constants.BYTE_9;
import static org.panteleyev.mk52.engine.Constants.BYTE_A;
import static org.panteleyev.mk52.engine.Constants.BYTE_F;
import static org.panteleyev.mk52.engine.Constants.PROGRAM_MEMORY_SIZE;

public record Address(int low, int high) {
    public static final Address ZERO = Address.of(0);

    // Используется для вычисления номера регистра при косвенной адресации
    private final static int[] INDIRECT_INDEX_RULE = new int[]{
            0xA, 0xB, 0xC, 0xD, 0xE, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9
    };

    public static Address of(int low, int high) {
        if ((high & 0xF) == 0xF && (low & 0xF) >= 0xA) {
            return new Address(low, high);
        } else {
            int raw = high * 10 + low;
            return new Address(raw % 10, raw / 10);
        }
    }

    public static Address of(int code) {
        return of(code & 0xF, (code & 0xF0) >> 4);
    }

    public boolean isDark() {
        return high >= 0xC;
    }

    public Address increment() {
        if (high == BYTE_F && low >= BYTE_A) {
            return new Address((byte) (low - 9), BYTE_0);
        }

        if (low + 1 == 10) {
            if (high == BYTE_F) {
                return Address.ZERO;
            } else {
                return new Address(BYTE_0, (byte) (high + 1));
            }
        } else {
            return new Address((byte) (low + 1), high);
        }
    }

    public Address decrement() {
        if (low > 9) {
            return new Address(BYTE_9, high);
        }

        if (low > 0) {
            return new Address((byte) (low - 1), high);
        }

        if (high == 0) {
            return new Address(BYTE_9, BYTE_F);
        }

        return new Address(BYTE_9, (byte) (high - 1));
    }

    public int getEffectiveAddress() {
        var addr = high * 10 + low;
        if (addr < PROGRAM_MEMORY_SIZE) {
            return addr;
        } else if (addr <= 111) {
            return addr - 105;
        } else if (addr <= 165) {
            return addr - 112;
        }
        return -1;
    }

    public int getEffectiveRegister() {
        if (high == 0) {
            return low == 0xF ? 0 : low;
        } else {
            return INDIRECT_INDEX_RULE[low];
        }
    }
}
