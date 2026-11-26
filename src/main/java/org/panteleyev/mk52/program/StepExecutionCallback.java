// Copyright (c) Petr Panteleyev
// SPDX-License-Identifier: MIT
package org.panteleyev.mk52.program;

public interface StepExecutionCallback {
    void before();
    void after(StepExecutionResult result);
}
