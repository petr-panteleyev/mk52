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
import static org.panteleyev.mk52.engine.KeyboardButton.D4;
import static org.panteleyev.mk52.engine.KeyboardButton.D7;
import static org.panteleyev.mk52.engine.KeyboardButton.D9;
import static org.panteleyev.mk52.engine.KeyboardButton.DIVISION;
import static org.panteleyev.mk52.engine.KeyboardButton.DOT;
import static org.panteleyev.mk52.engine.KeyboardButton.EE;
import static org.panteleyev.mk52.engine.KeyboardButton.F;
import static org.panteleyev.mk52.engine.KeyboardButton.GOSUB;
import static org.panteleyev.mk52.engine.KeyboardButton.K;
import static org.panteleyev.mk52.engine.KeyboardButton.LOAD;
import static org.panteleyev.mk52.engine.KeyboardButton.MINUS;
import static org.panteleyev.mk52.engine.KeyboardButton.MULTIPLICATION;
import static org.panteleyev.mk52.engine.KeyboardButton.PLUS;
import static org.panteleyev.mk52.engine.KeyboardButton.RETURN;
import static org.panteleyev.mk52.engine.KeyboardButton.RUN_STOP;
import static org.panteleyev.mk52.engine.KeyboardButton.SIGN;
import static org.panteleyev.mk52.engine.KeyboardButton.STORE;
import static org.panteleyev.mk52.engine.KeyboardButton.SWAP;

@DisplayName("Таблица 13")
public class Table13Test extends BaseTest {
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
                arguments(List.of(D1, D9, STORE, D7), new IR(0xF03F_47_F_09_F_01L)),
                arguments(List.of(K, GOSUB, D7, PLUS, LOAD, DOT), new IR(0xF06F_6A_F_10_F_A7L)),
                arguments(List.of(DIVISION, D2, DIVISION), new IR(0xF09F_13_F_02_F_13L)),
                arguments(List.of(STORE, D1, K, GOSUB, D7, SWAP), new IR(0xF12F_14_F_A7_F_41L)),
                arguments(List.of(MINUS, LOAD, DOT, DIVISION), new IR(0xF15F_13_F_6A_F_11L)),
                arguments(List.of(D2, DIVISION, STORE, D2), new IR(0xF18F42_F_13_F_02L)),
                arguments(List.of(RUN_STOP, LOAD, DOT, LOAD, EE), new IR(0xF21F_6C_F_6A_F_50L)),
                arguments(List.of(MULTIPLICATION, D4, MULTIPLICATION), new IR(0xF24F_12_F_04_F_12L)),
                arguments(List.of(LOAD, SIGN, F, MULTIPLICATION, SWAP), new IR(0xF27F_14_F_22_F_6BL)),
                arguments(List.of(MINUS, F, MINUS, LOAD, SIGN), new IR(0xF30F_6B_F_21_F_11L)),
                arguments(List.of(SIGN, RETURN), new IR(0xF32F_52_F_0B_F_6BL)),
                arguments(List.of(F, SIGN, RETURN), new IR(0xFFFF_0FFF_FFFFL, 1 << 7)),
                // Данные
                arguments(List.of(D3, STORE, DOT), new IR(0xFFFF_3FFF_FFFFL, 1 << 7)),
                arguments(List.of(D2, STORE, SIGN), new IR(0xFFFF_2FFF_FFFFL, 1 << 7)),
                arguments(List.of(D1, SIGN, STORE, EE), new IR(0xFFFA_1FFF_FFFFL, 1 << 7)),
                // Выполнение
                arguments(List.of(RUN_STOP), new IR(0xFFFA_1FFF_FFFFL, 1 << 7)),
                // Контроль регистров
                arguments(List.of(LOAD, D1), new IR(0xA01F_3333_3334L, 1 << 7)) // 3.3333333-01
        );
    }

    @ParameterizedTest
    @MethodSource("testArguments")
    public void test(List<KeyboardButton> buttons, IR expected) {
        buttons.forEach(engine::processButton);
        assertEquals(expected, engine.displayProperty().get());
    }
}
