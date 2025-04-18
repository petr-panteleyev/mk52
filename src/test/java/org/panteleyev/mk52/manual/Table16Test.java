/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.manual;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.panteleyev.mk52.BaseTest;
import org.panteleyev.mk52.engine.Engine;
import org.panteleyev.mk52.engine.IR;
import org.panteleyev.mk52.engine.KeyboardButton;
import org.panteleyev.mk52.engine.TrigonometricMode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.panteleyev.mk52.engine.KeyboardButton.D0;
import static org.panteleyev.mk52.engine.KeyboardButton.D1;
import static org.panteleyev.mk52.engine.KeyboardButton.D2;
import static org.panteleyev.mk52.engine.KeyboardButton.D3;
import static org.panteleyev.mk52.engine.KeyboardButton.D4;
import static org.panteleyev.mk52.engine.KeyboardButton.D5;
import static org.panteleyev.mk52.engine.KeyboardButton.D9;
import static org.panteleyev.mk52.engine.KeyboardButton.DIVISION;
import static org.panteleyev.mk52.engine.KeyboardButton.EE;
import static org.panteleyev.mk52.engine.KeyboardButton.F;
import static org.panteleyev.mk52.engine.KeyboardButton.LOAD;
import static org.panteleyev.mk52.engine.KeyboardButton.MULTIPLICATION;
import static org.panteleyev.mk52.engine.KeyboardButton.PLUS;
import static org.panteleyev.mk52.engine.KeyboardButton.RETURN;
import static org.panteleyev.mk52.engine.KeyboardButton.RUN_STOP;
import static org.panteleyev.mk52.engine.KeyboardButton.SIGN;
import static org.panteleyev.mk52.engine.KeyboardButton.STORE;

@DisplayName("Таблица 16")
public class Table16Test extends BaseTest {
    private static final Engine engine = new Engine(false, (_, _) -> {});

    @BeforeAll
    public static void beforeAll() {
        engine.init();
        engine.togglePower(true);
        engine.setTrigonometricMode(TrigonometricMode.RADIAN);
    }

    private static List<Arguments> testArguments() {
        return List.of(
                // Программа
                arguments(List.of(F, EE), new IR(0xF00F_FFFF_FFFFL)),
                arguments(List.of(STORE, D0, LOAD, D0, D3), new IR(0xF_03_F_03_F_60_F_40L)),
                arguments(List.of(DIVISION, F, D9, D2), new IR(0xF_06_F_02_F_1E_F_13L)),
                arguments(List.of(MULTIPLICATION, D4, PLUS), new IR(0xF_09_F_10_F_04_F_12L)),
                arguments(List.of(LOAD, D5, PLUS, STORE, D5), new IR(0xF_12_F_45_F_10_F_65L)),
                arguments(List.of(F, LOAD, D0, D1, RUN_STOP), new IR(0xF_15_F_50_F_01_F_5DL)),
                arguments(List.of(F, SIGN, RETURN), new IR(0xFFFF_0_FFF_FFFFL, 1 << 7)),
                // Выполнение
                arguments(List.of(D4, RUN_STOP), new IR(0xFFFF_2964_4465L, 1 << 6)) // 29.644467
        );
    }

    @ParameterizedTest
    @MethodSource("testArguments")
    public void test(List<KeyboardButton> buttons, IR expected) {
        buttons.forEach(engine::processButton);
        assertEquals(expected, engine.displayProperty().get());
    }
}
