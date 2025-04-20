/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.panteleyev.mk52.program.Address;

import java.util.Arrays;

import static org.panteleyev.mk52.engine.Constants.REGISTERS_SIZE;

public class Registers {
    private final long[] registers = new long[REGISTERS_SIZE];

    public Registers() {
        reset();
    }

    public void store(Address address, long value) {
        synchronized (registers) {
            registers[address.getEffectiveRegister()] = value;
        }
    }

    public long load(Address address) {
        synchronized (registers) {
            return registers[address.getEffectiveRegister()];
        }
    }

    public Address modifyAndGetAddressValue(Address address) {
        synchronized (registers) {
            var index = address.getEffectiveRegister();

            var x = Register.convertForIndirect(registers[index]);

            if (index <= 3) {
                x = Register.decrementMantissa(x);
            } else if (index <= 6) {
                x = Register.incrementMantissa(x);
            }

            registers[index] = x;
            return Address.of((int) (x & 0xFF));
        }
    }

    public int modifyAndGetLoopValue(int index) {
        synchronized (registers) {
            var x = Register.convertForIndirect(registers[index]);
            x = Register.convertForIndirect(x);
            if (Register.getIndirectValue(x) == 1) {
                return 0;
            }

            if (index <= 3) {
                x = Register.decrementMantissa(x);
            } else if (index <= 6) {
                x = Register.incrementMantissa(x);
            }

            registers[index] = x;
            return Register.getIndirectValue(x);
        }
    }

    public void reset() {
        synchronized (registers) {
            Arrays.fill(registers, 0);
        }
    }

    public long[] getSnapshot() {
        synchronized (registers) {
            return Arrays.copyOf(registers, registers.length);
        }
    }

    public void erase(int count) {
        synchronized (registers) {
            Arrays.fill(registers, 0, count, 0);
        }
    }
}
