/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.panteleyev.mk52.program.Address;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.panteleyev.mk52.engine.KeyboardButton.D0;
import static org.panteleyev.mk52.engine.KeyboardButton.D1;
import static org.panteleyev.mk52.engine.KeyboardButton.D6;
import static org.panteleyev.mk52.engine.KeyboardButton.EE;
import static org.panteleyev.mk52.engine.KeyboardButton.F;
import static org.panteleyev.mk52.engine.KeyboardButton.LOAD;
import static org.panteleyev.mk52.engine.KeyboardButton.MULTIPLICATION;
import static org.panteleyev.mk52.engine.KeyboardButton.PLUS;
import static org.panteleyev.mk52.engine.KeyboardButton.RETURN;
import static org.panteleyev.mk52.engine.KeyboardButton.RUN_STOP;
import static org.panteleyev.mk52.engine.KeyboardButton.SIGN;
import static org.panteleyev.mk52.engine.KeyboardButton.STORE;

@DisplayName("Еггогология")
public class ErrorTest {
    private static final Engine engine = new Engine(false, (_) -> {});

    @BeforeEach
    public void beforeEach() {
        engine.init();
        engine.togglePower(true);
    }

    @Test
    @DisplayName("Ярус 1, режим калькулятора: sqr(1e61)")
    public void manualErrorLevel1() {
        List.of(D6, D1, F, D0, F, MULTIPLICATION).forEach(engine::processButton);
        assertEquals(IR.ERROR, engine.x2().get());
        assertEquals(0x122010000000L, engine.stack().xValue());
        assertEquals(0x061010000000L, engine.stack().x1Value());
        assertEquals(new Address(0, 0), engine.getProgramCounter());
    }

    @Test
    @DisplayName("Ярус 2, режим калькулятора: sqr(sqr(1e61))")
    public void manualErrorLevel2() {
        List.of(D6, D1, F, D0, F, MULTIPLICATION, F, MULTIPLICATION).forEach(engine::processButton);
        assertEquals(IR.ERROR_2, engine.x2().get());
        assertEquals(0x244010000000L, engine.stack().xValue());
        assertEquals(0x122010000000L, engine.stack().x1Value());
        assertEquals(new Address(4, 2), engine.getProgramCounter());
    }

    @Test
    @DisplayName("Ярус 1, автоматический режим: sqr(1e61)")
    public void automaticErrorLevel2() {
        List.of(
                F, EE, D6, D1, F, D0, F, MULTIPLICATION, STORE, D0, F, PLUS, RUN_STOP, LOAD, D0,
                F, SIGN, RETURN, RUN_STOP
        ).forEach(engine::processButton);

        assertEquals(new IR(0xFFFF31415926L, 1 << 7), engine.displayProperty().get());
        assertEquals(0x000031415926L, engine.stack().xValue());
        assertEquals(0x122010000000L, engine.stack().x1Value());
        assertEquals(0x122010000000L, engine.stack().yValue());
        assertEquals(0, engine.stack().zValue());
        assertEquals(0, engine.stack().tValue());
        assertEquals(0x122010000000L, engine.registers().load(new Address(0, 0)));
        assertEquals(new Address(7, 0), engine.getProgramCounter());

        List.of(RUN_STOP).forEach(engine::processButton);

        assertEquals(IR.ERROR, engine.displayProperty().get());
        assertEquals(0x122010000000L, engine.stack().xValue());
        assertEquals(0x122010000000L, engine.stack().x1Value());
        assertEquals(0x000031415926L, engine.stack().yValue());
        assertEquals(0x122010000000L, engine.stack().zValue());
        assertEquals(0, engine.stack().tValue());
        assertEquals(0x122010000000L, engine.registers().load(new Address(0, 0)));
        assertEquals(new Address(8, 0), engine.getProgramCounter());
    }
}
