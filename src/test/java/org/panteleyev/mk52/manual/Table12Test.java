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
import static org.panteleyev.mk52.engine.KeyboardButton.D1;
import static org.panteleyev.mk52.engine.KeyboardButton.D2;
import static org.panteleyev.mk52.engine.KeyboardButton.D3;
import static org.panteleyev.mk52.engine.KeyboardButton.D7;
import static org.panteleyev.mk52.engine.KeyboardButton.D9;
import static org.panteleyev.mk52.engine.KeyboardButton.EE;
import static org.panteleyev.mk52.engine.KeyboardButton.F;
import static org.panteleyev.mk52.engine.KeyboardButton.K;
import static org.panteleyev.mk52.engine.KeyboardButton.LOAD;
import static org.panteleyev.mk52.engine.KeyboardButton.MINUS;
import static org.panteleyev.mk52.engine.KeyboardButton.MULTIPLICATION;
import static org.panteleyev.mk52.engine.KeyboardButton.PLUS;
import static org.panteleyev.mk52.engine.KeyboardButton.RETURN;
import static org.panteleyev.mk52.engine.KeyboardButton.RUN_STOP;
import static org.panteleyev.mk52.engine.KeyboardButton.SIGN;
import static org.panteleyev.mk52.engine.KeyboardButton.STEP_RIGHT;
import static org.panteleyev.mk52.engine.KeyboardButton.STORE;

@DisplayName("Таблица 12")
public class Table12Test extends BaseTest {
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
                arguments(List.of(D1, D9, STORE, SIGN), new IR(0xF03F_4B_F_09_F_01L)),
                arguments(List.of(D9, LOAD, D1, F, MULTIPLICATION), new IR(0xF06F_22_F_61_F_09L)),
                arguments(List.of(MULTIPLICATION, STORE, D2, LOAD, D1), new IR(0xF09F_61_F_42_F_12L)),
                arguments(List.of(D2, MULTIPLICATION, F, D1), new IR(0xF12F_16_F_12_F_02L)),
                arguments(List.of(LOAD, D2, MINUS, K, STEP_RIGHT, SIGN), new IR(0xF15F_CB_F_11_F_62L)),
                arguments(List.of(LOAD, D1, F, D9, PLUS), new IR(0xF18F_10_F_1E_F_61L)),
                arguments(List.of(RUN_STOP, LOAD, D1, F, D7), new IR(0xF21F_1C_F_61_F_50L)),
                arguments(List.of(PLUS, RUN_STOP), new IR(0xF23F_50_F_10_F_1CL)),
                arguments(List.of(F, SIGN, RETURN), new IR(0xFFFF_0FFF_FFFFL, 1 << 7)),
                // Данные 1
                arguments(List.of(D1, STORE, D1), new IR(0xFFFF_1FFF_FFFFL, 1 << 7)),
                arguments(List.of(RUN_STOP), new IR(0xA02A_5353_62FFL, 1 << 7)), // -5.35365  -02
                // Данные 2
                arguments(List.of(D2, STORE, D1), new IR(0xFFFF_2FFF_FFFFL, 1 << 7)),
                arguments(List.of(RETURN, RUN_STOP), new IR(0xFFFF_1950_7447L, 1 << 6)), // 19.507444
                // Данные 3
                arguments(List.of(D3, STORE, D1), new IR(0xFFFF_3FFF_FFFFL, 1 << 7)),
                arguments(List.of(RETURN, RUN_STOP), new IR(0xFFFF_3225_6991L, 1 << 5)) // 322.56986
        );
    }

    @ParameterizedTest
    @MethodSource("testArguments")
    public void test(List<KeyboardButton> buttons, IR expected) {
        buttons.forEach(engine::processButton);
        assertEquals(expected, engine.displayProperty().get());
    }
}
