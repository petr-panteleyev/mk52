/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.program;

import org.panteleyev.mk52.engine.IR;
import org.panteleyev.mk52.engine.Register;

import java.util.Arrays;

import static org.panteleyev.mk52.engine.Constants.PROGRAM_MEMORY_SIZE;

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
                throw new ArithmeticException("Failed to fetch opcode");
            }

            Address address = null;
            if (opCode.hasAddress()) {
                pc = programCounter.getAndIncrement();
                address = Address.of(memory[pc.getEffectiveAddress()]);
            }
            return new Instruction(opCode, address);
        }
    }

    public IR getIndicator(Address pc) {
        synchronized (memory) {
            long ir = IR.EMPTY.indicator();

            ir = Register.setTetrad(ir, 9, pc.low());
            ir = Register.setTetrad(ir, 10, pc.high());

            for (int tetrad = 6; tetrad >= 0; tetrad -= 3) {
                pc = pc.decrement();
                if (pc.isDark()) {
                    continue;
                }
                var code = memory[pc.getEffectiveAddress()];
                ir = Register.setTetrad(ir, tetrad, code & 0xF);
                ir = Register.setTetrad(ir, tetrad + 1, (code & 0xF0) >> 4);
            }

            return new IR(ir, 0);
        }
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
