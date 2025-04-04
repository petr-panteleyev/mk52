/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.panteleyev.mk52.value.DecimalValue;
import org.panteleyev.mk52.value.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.panteleyev.mk52.engine.Constants.REGISTERS_SIZE;

public class Registers {
    private final Value[] registers = new Value[REGISTERS_SIZE];

    public Registers() {
        reset();
    }

    private Registers(Value[] registers) {
        System.arraycopy(registers, 0, this.registers, 0, REGISTERS_SIZE);
    }

    public void store(int index, Value value) {
        synchronized (registers) {
            registers[Math.abs(index) % REGISTERS_SIZE] = value;
        }
    }

    public Value load(int index) {
        synchronized (registers) {
            return registers[Math.abs(index) % REGISTERS_SIZE];
        }
    }

    public int modifyAndGetRegisterValue(int index) {
        synchronized (registers) {
            var indirectIndex = (int) registers[index].toDecimal().value();
            var sign = indirectIndex < 0 ? -1 : 1;
            if (index <= 3) {
                indirectIndex = sign * (Math.abs(indirectIndex) - 1);
            } else if (index <= 6) {
                indirectIndex++;
            }

            if (indirectIndex > 99999999) {
                indirectIndex = 0;
            } else if (indirectIndex == -1) {
                indirectIndex = -99999999;
            }

            registers[index] = new DecimalValue(indirectIndex, DecimalValue.ValueMode.ADDRESS);
            return indirectIndex;
        }
    }

    public void reset() {
        synchronized (registers) {
            Arrays.fill(registers, DecimalValue.ZERO);
        }
    }

    public Registers copy() {
        synchronized (registers) {
            return new Registers(this.registers);
        }
    }

    public void copyFrom(Registers registers) {
        synchronized (this.registers) {
            System.arraycopy(registers.registers, 0, this.registers, 0, REGISTERS_SIZE);
        }
    }

    public List<String> getSnapshot() {
        synchronized (registers) {
            var snapshot = new ArrayList<String>(REGISTERS_SIZE);
            for (var register : registers) {
                snapshot.add(register.asString());
            }
            return snapshot;
        }
    }

    public void erase(int count) {
        synchronized (registers) {
            Arrays.fill(registers, 0, count, DecimalValue.ZERO);
        }
    }
}
