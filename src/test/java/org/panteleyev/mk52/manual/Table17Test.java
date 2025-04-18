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
import static org.panteleyev.mk52.engine.KeyboardButton.D8;
import static org.panteleyev.mk52.engine.KeyboardButton.DOT;
import static org.panteleyev.mk52.engine.KeyboardButton.EE;
import static org.panteleyev.mk52.engine.KeyboardButton.F;
import static org.panteleyev.mk52.engine.KeyboardButton.GOTO;
import static org.panteleyev.mk52.engine.KeyboardButton.LOAD;
import static org.panteleyev.mk52.engine.KeyboardButton.MINUS;
import static org.panteleyev.mk52.engine.KeyboardButton.MULTIPLICATION;
import static org.panteleyev.mk52.engine.KeyboardButton.PUSH;
import static org.panteleyev.mk52.engine.KeyboardButton.RETURN;
import static org.panteleyev.mk52.engine.KeyboardButton.RUN_STOP;
import static org.panteleyev.mk52.engine.KeyboardButton.SIGN;
import static org.panteleyev.mk52.engine.KeyboardButton.STORE;
import static org.panteleyev.mk52.engine.KeyboardButton.SWAP;

@DisplayName("Таблица 17")
public class Table17Test extends BaseTest {
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
                arguments(List.of(PUSH, STORE, D2, D0), new IR(0xF_03_F_00_F_42_F_0EL)),
                arguments(List.of(DOT, D1, D3), new IR(0xF_06_F_03_F_01_F_0AL)),
                arguments(List.of(MULTIPLICATION, D4, DOT), new IR(0xF_09_F_0A_F_04_F_12L)),
                arguments(List.of(D8, MINUS, LOAD, D2), new IR(0xF_12_F_62_F_11_F_08L)),
                arguments(List.of(SWAP, MINUS, RUN_STOP), new IR(0xF_15_F_50_F_11_F_14L)),
                arguments(List.of(GOTO, D0, D1), new IR(0xF_17_F_01_F_51_F_50L)),
                arguments(List.of(F, SIGN, RETURN), new IR(0xFFFF_0_FFF_FFFFL, 1 << 7)),
                // Выполнение
                arguments(List.of(D4, D0, D0, RUN_STOP), new IR(0xFFFF_3528_FFFFL, 1 << 5))
        );
    }

    @ParameterizedTest
    @MethodSource("testArguments")
    public void test(List<KeyboardButton> buttons, IR expected) {
        buttons.forEach(engine::processButton);
        var actual = engine.displayProperty().get();
        assertEquals(expected, engine.displayProperty().get());
    }
}
