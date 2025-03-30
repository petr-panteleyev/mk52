/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.program;

public interface StepExecutionCallback {
    void before();
    void after(StepExecutionResult result);
}
