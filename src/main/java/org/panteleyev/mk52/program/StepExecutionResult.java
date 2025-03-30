/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.program;

import org.panteleyev.mk52.engine.StackSnapshot;

import java.util.Deque;
import java.util.List;

public record StepExecutionResult(
        String display,
        int programCounter,
        StackSnapshot stack,
        List<String> registers,
        Deque<Integer> callStack
) {
}
