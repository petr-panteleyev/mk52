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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.panteleyev.mk52.engine.KeyboardButton.D1;
import static org.panteleyev.mk52.engine.KeyboardButton.D2;
import static org.panteleyev.mk52.engine.KeyboardButton.D3;
import static org.panteleyev.mk52.engine.KeyboardButton.D4;
import static org.panteleyev.mk52.engine.KeyboardButton.D5;
import static org.panteleyev.mk52.engine.KeyboardButton.D7;
import static org.panteleyev.mk52.engine.KeyboardButton.DOT;
import static org.panteleyev.mk52.engine.KeyboardButton.EE;
import static org.panteleyev.mk52.engine.KeyboardButton.F;
import static org.panteleyev.mk52.engine.KeyboardButton.GOTO;
import static org.panteleyev.mk52.engine.KeyboardButton.K;
import static org.panteleyev.mk52.engine.KeyboardButton.LOAD;
import static org.panteleyev.mk52.engine.KeyboardButton.MINUS;
import static org.panteleyev.mk52.engine.KeyboardButton.MULTIPLICATION;
import static org.panteleyev.mk52.engine.KeyboardButton.PLUS;
import static org.panteleyev.mk52.engine.KeyboardButton.PUSH;
import static org.panteleyev.mk52.engine.KeyboardButton.RETURN;
import static org.panteleyev.mk52.engine.KeyboardButton.RUN_STOP;
import static org.panteleyev.mk52.engine.KeyboardButton.SIGN;
import static org.panteleyev.mk52.engine.KeyboardButton.STORE;

@DisplayName("Таблица 11")
public class Table11Test extends BaseTest {
    private static final Engine engine = new Engine(false, _ -> {});

    @BeforeAll
    public static void beforeAll() {
        engine.init();
        engine.togglePower(true);
    }

    private static List<Arguments> testArguments() {
        return List.of(
                // Программа
                arguments(List.of(F, EE), new IR(0xF00F_FFFF_FFFFL)),
                arguments(List.of(D4, PUSH, K, GOTO, D3), new IR(0xF03F_83_F_0E_F_04L)),
                arguments(List.of(F, MINUS, D2, PLUS), new IR(0xF06F_10_F_02_F_21L)),
                arguments(List.of(K, GOTO, D4, PLUS, D3), new IR(0xF09F_03_F_10_F_84L)),
                arguments(List.of(MULTIPLICATION, K, GOTO, DOT, MINUS), new IR(0xF12F_11_F_8A_F_12L)),
                arguments(List.of(RUN_STOP), new IR(0xF13F_50_F_11_F_8AL)),
                arguments(List.of(F, SIGN, RETURN), new IR(0xFFFF_0FFF_FFFFL, 1 << 7)),
                // Данные
                arguments(List.of(D5, STORE, D3), new IR(0xFFFF_5FFF_FFFFL, 1 << 7)),
                arguments(List.of(D7, STORE, D4), new IR(0xFFFF_7FFF_FFFFL, 1 << 7)),
                arguments(List.of(D1, D2, STORE, DOT), new IR(0xFFFF_12FF_FFFFL, 1 << 6)),
                // Выполнение
                arguments(List.of(RUN_STOP), new IR(0xFFFF_18FF_FFFFL, 1 << 6)),
                // Контроль регистров
                arguments(List.of(LOAD, D3), new IR(0xFFFF_0000_0004L, 1)),
                arguments(List.of(LOAD, D4), new IR(0xFFFF_0000_0008L, 1)),
                arguments(List.of(LOAD, DOT), new IR(0xFFFF_0000_0012L, 1))
        );
    }

    @ParameterizedTest
    @MethodSource("testArguments")
    public void test(List<KeyboardButton> buttons, IR expected) {
        buttons.forEach(engine::processButton);
        assertEquals(expected, engine.displayProperty().get());
    }
}
