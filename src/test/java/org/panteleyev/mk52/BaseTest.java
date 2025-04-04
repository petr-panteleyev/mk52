/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52;

import org.panteleyev.mk52.engine.Engine;
import org.panteleyev.mk52.engine.TrigonometricMode;

import java.util.function.Consumer;

public class BaseTest {
    public static final String PASS = "PASS";
    public static final String DIFF = "DIFF";

    public static final Consumer<Engine> NOOP = _ -> {};
    public static final Consumer<Engine> POWEROFF = e -> e.togglePower(false);
    public static final Consumer<Engine> TR_DEGREE = e -> e.setTrigonometricMode(TrigonometricMode.DEGREE);
    public static final Consumer<Engine> TR_GRADIAN = e -> e.setTrigonometricMode(TrigonometricMode.GRADIAN);
    public static final Consumer<Engine> TR_RADIAN = e -> e.setTrigonometricMode(TrigonometricMode.RADIAN);
}
