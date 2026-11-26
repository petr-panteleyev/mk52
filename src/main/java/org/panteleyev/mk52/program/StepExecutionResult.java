// Copyright (c) Petr Panteleyev
// SPDX-License-Identifier: MIT
package org.panteleyev.mk52.program;

import org.panteleyev.mk52.engine.CallStackSnapshot;
import org.panteleyev.mk52.engine.IR;
import org.panteleyev.mk52.engine.StackSnapshot;

import java.util.List;

public record StepExecutionResult(
        IR display,
        Address programCounter,
        StackSnapshot stack,
        long[] registers,
        CallStackSnapshot callStack
) {
}
