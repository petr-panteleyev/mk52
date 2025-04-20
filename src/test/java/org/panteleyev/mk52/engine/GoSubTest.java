/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Вызовы подпрограмм и возвраты")
public class GoSubTest {
    private static final int[] PROGRAM = new int[]{
            0x52,       // 00: В/0
            0x53,       // 01: ПП
            0x05,       // 02: 05
            0x01,       // 03: 1
            0x50,       // 04: С/П
            0x53,       // 05: ПП
            0x08,       // 06: 08
            0x52,       // 07: В/0
            0x53,       // 08: ПП
            0x11,       // 09: 11
            0x52,       // 10: В/0
            0x53,       // 11: ПП
            0x14,       // 12: 14
            0x52,       // 13: В/0
            0x53,       // 14: ПП
            0x17,       // 15: 17
            0x52,       // 16: В/0
            0x52,       // 17: В/0
            0x00, 0x00, 0x00, 0x00, 0x00,
            0x20,       // 23: F π
            0x50        // 24: С/П
    };

    @Test
    public void test() {
        var engine = new Engine(false, _ -> {});
        engine.togglePower(true);
        engine.loadMemoryBytes(PROGRAM);

        // Первый запуск
        engine.processButton(KeyboardButton.RUN_STOP);
        assertEquals(new IR(0xFFFF1FFFFFFFL, 1 << 7), engine.displayProperty().get());
        assertEquals(5, engine.getProgramCounter().getEffectiveAddress());

        // Второй запуск
        engine.processButton(KeyboardButton.RETURN);
        engine.processButton(KeyboardButton.RUN_STOP);
        assertEquals(new IR(0xFFFF31415926L, 1 << 7), engine.displayProperty().get());
        assertEquals(25, engine.getProgramCounter().getEffectiveAddress());
    }
}
