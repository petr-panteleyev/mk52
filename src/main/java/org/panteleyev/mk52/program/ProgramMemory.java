/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.program;

import org.panteleyev.mk52.engine.OpCode;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.panteleyev.mk52.engine.Constants.DISPLAY_SIZE;
import static org.panteleyev.mk52.engine.Constants.PROGRAM_MEMORY_SIZE;
import static org.panteleyev.mk52.util.StringUtil.pcToString;

public class ProgramMemory {
    private final int[] memory = new int[PROGRAM_MEMORY_SIZE + 3];

    public ProgramMemory() {
        Arrays.fill(memory, 0);
    }

    public Instruction fetchInstruction(AtomicInteger programCounter) {
        synchronized (memory) {
            var pc = programCounter.get();
            if (pc < 0 || pc >= PROGRAM_MEMORY_SIZE) {
                throw new IllegalArgumentException("PC out of range");
            }
            programCounter.getAndIncrement();

            var code = memory[pc];
            var opCode = OpCode.findByCode(code);
            if (opCode == OpCode.EMPTY) {
                throw new IllegalStateException("Failed to fetch opcode");
            }

            Integer address = null;
            if (opCode.hasAddress()) {
                pc = programCounter.getAndIncrement();
                if (pc >= PROGRAM_MEMORY_SIZE) {
                    throw new IllegalArgumentException("PC out of range");
                }
                address = memory[pc];
            }
            return new Instruction(opCode, address);
        }
    }

    public String getStringValue(int pc) {
        synchronized (memory) {
            var buffer = new char[DISPLAY_SIZE];
            Arrays.fill(buffer, ' ');

            var pcString = pcToString(pc);
            buffer[11] = pcString.charAt(0);
            buffer[12] = pcString.charAt(1);

            var pc1 = pc - 1;
            if (pc1 >= 0) {
                pcString = codeToString(memory[pc - 1]);
                buffer[2] = pcString.charAt(0);
                buffer[3] = pcString.charAt(1);
            }

            var pc2 = pc - 2;
            if (pc2 >= 0) {
                pcString = codeToString(memory[pc2]);
                buffer[5] = pcString.charAt(0);
                buffer[6] = pcString.charAt(1);
            }

            var pc3 = pc - 3;
            if (pc3 >= 0) {
                pcString = codeToString(memory[pc3]);
                buffer[8] = pcString.charAt(0);
                buffer[9] = pcString.charAt(1);
            }

            return new String(buffer);
        }
    }

    private static String codeToString(int code) {
        return String.format("%02X", code);
    }

    public void storeCode(AtomicInteger pc, int code) {
        synchronized (memory) {
            if (pc.get() < 0 || pc.get() >= PROGRAM_MEMORY_SIZE) {
                throw new IllegalArgumentException("PC out of range");
            }
            memory[pc.getAndIncrement()] = code;
        }
    }
}
