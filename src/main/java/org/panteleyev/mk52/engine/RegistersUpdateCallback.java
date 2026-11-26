// Copyright (c) Petr Panteleyev
// SPDX-License-Identifier: MIT
package org.panteleyev.mk52.engine;

import org.panteleyev.mk52.program.StepExecutionResult;

public interface RegistersUpdateCallback {
    void update(StepExecutionResult snapshot);
}
