// Copyright (c) Petr Panteleyev
// SPDX-License-Identifier: MIT
package org.panteleyev.mk52.program;

import static java.util.Objects.requireNonNull;

public record Instruction(OpCode opCode, Address address) {
    public Instruction {
        requireNonNull(opCode);
    }

    public Instruction(OpCode opCode) {
        this(opCode, null);
    }
}
