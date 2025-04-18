/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Программа взята <a href="https://xvadim.github.io/xbasoft/pmk/undocument_features.html#div_indirect_addr_reg">здесь</a>.
 */
@DisplayName("Фокус к косвенной адресацией регистра")
public class IndirectRegisterTrickTest {
    private static final int[] PROGRAM = new int[]{
            0x0D,       // 00: Cx
            0x40,       // 01: x→П 0
            0x3B,       // 02: К СЧ
            0x3A,       // 03: К ИНВ
            0xB0,       // 04: К х→П 0
            0x60,       // 05: П→x 0
            0x59,       // 06: F x≥0
            0x02,       // 07: 02
            0x50        // 08: С/П
    };

    @Test
    public void test() {
        var engine = new Engine(false, (_, _) -> {});
        engine.togglePower(true);
        engine.loadMemoryBytes(PROGRAM);

        engine.processButton(KeyboardButton.RUN_STOP);
        var ri1 = engine.displayProperty().get();
        assertEquals(0x8000_0000L, ri1.indicator() & 0x0000_F000_0000L);
        assertEquals(1 << 7, ri1.dots());
        assertEquals(9, engine.getProgramCounter().getEffectiveAddress());

        // Регистры 0-3 заполнены результатами логических операций
        List.of(KeyboardButton.D0, KeyboardButton.D1, KeyboardButton.D2, KeyboardButton.D3).forEach(button -> {
            List.of(KeyboardButton.LOAD, button).forEach(engine::processButton);
            var ri2 = engine.displayProperty().get();
            assertEquals(0x8000_0000L, ri2.indicator() & 0x0000_F000_0000L);
            assertEquals(1 << 7, ri2.dots());
        });
        // Регистры 4-e заполнены нулями
        List.of(KeyboardButton.D4, KeyboardButton.D5, KeyboardButton.D6, KeyboardButton.D7, KeyboardButton.D8,
                KeyboardButton.D9, KeyboardButton.DOT, KeyboardButton.SIGN, KeyboardButton.EE, KeyboardButton.CLEAR_X,
                KeyboardButton.PUSH).forEach(button -> {
            List.of(KeyboardButton.LOAD, button).forEach(engine::processButton);
            assertEquals(IR.INITIAL, engine.displayProperty().get());
        });
    }
}
