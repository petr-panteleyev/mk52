/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.eeprom;

import org.panteleyev.mk52.engine.Registers;
import org.panteleyev.mk52.engine.Value;
import org.panteleyev.mk52.program.ProgramMemory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.panteleyev.mk52.engine.Constants.MANTISSA_POSITION;

public class Eeprom {
    private static final Map<EepromAddress, Registers> REG_MEMORY = new ConcurrentHashMap<>();
    private static final Map<EepromAddress, ProgramMemory> PRG_MEMORY = new ConcurrentHashMap<>();

    public static EepromAddress valueToEepromAddress(Value value) {
        var str = value.asString();
        var addr = str.replace(".", "").substring(MANTISSA_POSITION + 1, 4);
        return new EepromAddress(Integer.parseInt(addr), 0);
    }

    public static void erase(Value address, EepromMode mode) {
        var eepromAddress = valueToEepromAddress(address);
        switch (mode) {
            case DATA -> REG_MEMORY.remove(eepromAddress);
            case PROGRAM -> PRG_MEMORY.remove(eepromAddress);
        }
    }

    public static void write(Value address, Registers registers) {
        var eepromAddress = valueToEepromAddress(address);
        REG_MEMORY.put(eepromAddress, registers.copy());
    }

    public static Registers readRegisters(Value address) {
        var stored = REG_MEMORY.get(valueToEepromAddress(address));
        if (stored != null) {
            return stored.copy();
        } else {
            return new Registers();
        }
    }
}
