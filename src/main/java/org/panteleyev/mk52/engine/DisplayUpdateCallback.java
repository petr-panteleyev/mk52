/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.panteleyev.mk52.program.StepExecutionResult;

public interface DisplayUpdateCallback {
    default void clearDisplay() {
    }

    void updateDisplay(String content, StepExecutionResult snapshot, boolean running);
}
