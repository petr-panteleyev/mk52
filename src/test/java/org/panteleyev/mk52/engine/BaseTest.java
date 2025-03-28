/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import java.util.function.Consumer;

public class BaseTest {
    static final String PASS = "PASS";
    static final String DIFF = "DIFF";
    static final String NOT_IMPLEMENTED = "NOT IMPLEMENTED";

    static final Consumer<Engine> NOOP = _ -> {};
    static final Consumer<Engine> POWEROFF = e -> e.togglePower(false);
    static final Consumer<Engine> TR_DEGREE = e -> e.setTrigonometricMode(Engine.TrigonometricMode.DEGREE);
    static final Consumer<Engine> TR_GRADIAN = e -> e.setTrigonometricMode(Engine.TrigonometricMode.GRADIAN);
    static final Consumer<Engine> TR_RADIAN = e -> e.setTrigonometricMode(Engine.TrigonometricMode.RADIAN);
}
