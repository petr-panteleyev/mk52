/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import java.util.Arrays;

class Registers {
    private static final int COUNT = 15;

    private final Value[] registers = new Value[COUNT];

    public void store(int index, Value value) {
        if (index < 0 || index > COUNT) {
            return;
        }
        registers[index] = value;
    }

    public Value load(int index) {
        return registers[index];
    }

    public int modifyAndGetIndirectIndex(int index) {
        var indirectIndex = (int) registers[index].value();
        if (index <= 3) {
            indirectIndex--;
        } else if (index <= 6) {
            indirectIndex++;
        }
        registers[index] = new Value(indirectIndex, ValueMode.ADDRESS);
        return indirectIndex;
    }

    public void reset() {
        Arrays.fill(registers, Value.ZERO);
    }
}
