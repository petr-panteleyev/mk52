/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.panteleyev.mk52.program.Address;
import org.panteleyev.mk52.value.Value;
import org.panteleyev.mk52.value.ValueUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.panteleyev.mk52.engine.Constants.REGISTERS_SIZE;

public class Registers {
    private final Value[] registers = new Value[REGISTERS_SIZE];

    public Registers() {
        reset();
    }

    public void store(Address address, Value value) {
        synchronized (registers) {
            registers[address.getEffectiveRegister()] = value;
        }
    }

    public Value load(Address address) {
        synchronized (registers) {
            return registers[address.getEffectiveRegister()];
        }
    }

    public Address modifyAndGetAddressValue(Address address) {
        synchronized (registers) {
            var index = address.getEffectiveRegister();
            var bytes = registers[index].getBytes();
            ValueUtil.convertForIndirect(bytes);

            if (index <= 3) {
                ValueUtil.decrementMantissa(bytes);
            } else if (index <= 6) {
                ValueUtil.incrementMantissa(bytes);
            }

            registers[index] = new Value(bytes);
            return Address.of(new byte[]{bytes[0], bytes[1]});
        }
    }

    public int modifyAndGetLoopValue(int index) {
        synchronized (registers) {
            var bytes = registers[index].getBytes();
            ValueUtil.convertForIndirect(bytes);
            if (ValueUtil.getIndirectValue(bytes) == 1) {
                return 0;
            }

            if (index <= 3) {
                ValueUtil.decrementMantissa(bytes);
            } else if (index <= 6) {
                ValueUtil.incrementMantissa(bytes);
            }

            registers[index] = new Value(bytes);
            return ValueUtil.getIndirectValue(bytes);
        }
    }

    public void reset() {
        synchronized (registers) {
            Arrays.fill(registers, Value.ZERO);
        }
    }

    public List<String> getSnapshot() {
        synchronized (registers) {
            var snapshot = new ArrayList<String>(REGISTERS_SIZE);
            for (var register : registers) {
                snapshot.add(register.stringValue());
            }
            return snapshot;
        }
    }

    public void erase(int count) {
        synchronized (registers) {
            Arrays.fill(registers, 0, count, Value.ZERO);
        }
    }
}
