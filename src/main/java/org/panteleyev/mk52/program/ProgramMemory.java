/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.program;

import java.util.Arrays;

import static org.panteleyev.mk52.engine.Constants.DISPLAY_SIZE;
import static org.panteleyev.mk52.engine.Constants.PROGRAM_MEMORY_SIZE;
import static org.panteleyev.mk52.util.StringUtil.pcToString;

public class ProgramMemory {
    private final int[] memory = new int[PROGRAM_MEMORY_SIZE + 3];

    public ProgramMemory() {
        Arrays.fill(memory, 0);
    }

    public Instruction fetchInstruction(ProgramCounter programCounter) {
        synchronized (memory) {
            var pc = programCounter.getAndIncrement();

            var code = memory[pc.getEffectiveAddress()];
            var opCode = OpCode.findByCode(code);
            if (opCode == OpCode.EMPTY) {
                throw new IllegalStateException("Failed to fetch opcode");
            }

            Address address = null;
            if (opCode.hasAddress()) {
                pc = programCounter.getAndIncrement();
                address = Address.of(memory[pc.getEffectiveAddress()]);
            }
            return new Instruction(opCode, address);
        }
    }

    public String getStringValue(Address pc) {
        synchronized (memory) {
            var buffer = new char[DISPLAY_SIZE];
            Arrays.fill(buffer, ' ');

            var pcString = pcToString(pc);
            buffer[11] = pcString.charAt(0);
            buffer[12] = pcString.charAt(1);

            for (int offset = 2; offset <= 8; offset += 3) {
                pc = pc.decrement();
                showMemoryContent(buffer, offset, pc);
            }

            return new String(buffer);
        }
    }

    private void showMemoryContent(char[] buffer, int offset, Address address) {
        if (address.isDark()) {
            buffer[offset] = ' ';
            buffer[offset + 1] = ' ';
        } else {
            var pcString = codeToString(memory[address.getEffectiveAddress()]);
            buffer[offset] = pcString.charAt(0);
            buffer[offset + 1] = pcString.charAt(1);
        }
    }

    private static String codeToString(int code) {
        return String.format("%02X", code);
    }

    public void storeCode(ProgramCounter pc, int code) {
        synchronized (memory) {
            memory[pc.getAndIncrement().getEffectiveAddress()] = code;
        }
    }

    public void storeCodes(int[] codes) {
        synchronized (memory) {
            System.arraycopy(codes, 0, memory, 0, Math.min(codes.length, memory.length));
        }
    }

    public void erase(int size) {
        synchronized (memory) {
            Arrays.fill(memory, 0, size, 0);
        }
    }

    public int[] getMemoryBytes() {
        synchronized (memory) {
            var copy = new int[PROGRAM_MEMORY_SIZE];
            System.arraycopy(memory, 0, copy, 0, PROGRAM_MEMORY_SIZE);
            return copy;
        }
    }
}
