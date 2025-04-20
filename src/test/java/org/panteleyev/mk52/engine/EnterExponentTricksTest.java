/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.panteleyev.mk52.engine.KeyboardButton.D0;
import static org.panteleyev.mk52.engine.KeyboardButton.DIVISION;
import static org.panteleyev.mk52.engine.KeyboardButton.EE;
import static org.panteleyev.mk52.engine.KeyboardButton.F;
import static org.panteleyev.mk52.engine.KeyboardButton.K;
import static org.panteleyev.mk52.engine.KeyboardButton.PUSH;
import static org.panteleyev.mk52.engine.KeyboardButton.RETURN;
import static org.panteleyev.mk52.engine.KeyboardButton.RUN_STOP;
import static org.panteleyev.mk52.engine.KeyboardButton.SIGN;

@DisplayName("Фокусы с ВП")
public class EnterExponentTricksTest {
    private static final Engine engine = new Engine(false, (_) -> {});

    @BeforeEach
    public void beforeEach() {
        engine.init();
        engine.togglePower(true);
    }

    @Test
    @DisplayName("K НОП, 0, 0, 0, ВП, С/П")
    public void test1() {
        List.of(F, EE, K, D0, D0, D0, D0, EE, RUN_STOP, F, SIGN, RETURN, RUN_STOP).forEach(engine::processButton);
        assertEquals(new IR(0xFFFF100FFFFFL, 1 << 5), engine.x2().get());
        assertEquals(0x002010000000L, engine.stack().xValue());
    }

    @Test
    @DisplayName("K 1/x, ВП, В↑")
    public void test2() {
        List.of(F, DIVISION, EE).forEach(engine::processButton);
        assertEquals(new IR(0xF00FDDD0DFFFL, 1 << 7), engine.x2().get());
        assertEquals(0x0000F0000000L, engine.stack().xValue());
        assertEquals(0, engine.stack().yValue());

        List.of(PUSH).forEach(engine::processButton);
        assertEquals(new IR(0xFFFFFFFFFFFFL, 1 << 7), engine.x2().get());
        assertEquals(0x0000F0000000L, engine.stack().xValue());
        assertEquals(0x0000F0000000L, engine.stack().yValue());
    }
}
