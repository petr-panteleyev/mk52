/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import java.util.Arrays;

public class Registers {
    private static final int COUNT = 15;

    private final Value[] registers;

    public Registers() {
        this.registers = new Value[COUNT];
        reset();
    }

    private Registers(Value[] registers) {
        this.registers = registers;
    }

    public void store(int index, Value value) {
        if (index < 0 || index > COUNT) {
            return;
        }
        registers[index] = value;
    }

    public Value load(int index) {
        return registers[index];
    }

    public int modifyAndGetRegisterValue(int index) {
        var indirectIndex = (int) registers[index].value();
        if (index <= 3) {
            indirectIndex--;
        } else if (index <= 6) {
            indirectIndex++;
        }
        registers[index] = new Value(indirectIndex, Value.ValueMode.ADDRESS, 0);
        return indirectIndex;
    }

    public void reset() {
        Arrays.fill(registers, Value.ZERO);
    }

    public Registers copy() {
        var regCopy = new Value[COUNT];
        System.arraycopy(registers, 0, regCopy, 0, COUNT);
        return new Registers(regCopy);
    }

    public void copyFrom(Registers registers) {
        System.arraycopy(registers.registers, 0, this.registers, 0, COUNT);
    }
}
