/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.program;

import org.panteleyev.mk52.engine.OpCode;

import static java.util.Objects.requireNonNull;

public record Instruction(OpCode opCode, Address address) {
    public Instruction {
        requireNonNull(opCode);
    }

    public Instruction(OpCode opCode) {
        this(opCode, null);
    }

    public int size() {
        return address == null ? 1 : 2;
    }
}
