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
                arguments(List.of(F, EE), "           00"),
                arguments(List.of(D1, D9, STORE, D7), "  47 09 01 03"),
                arguments(List.of(K, GOSUB, D7, PLUS, LOAD, DOT), "  6A 10 A7 06"),
                arguments(List.of(DIVISION, D2, DIVISION), "  13 02 13 09"),
                arguments(List.of(STORE, D1, K, GOSUB, D7, SWAP), "  14 A7 41 12"),
                arguments(List.of(MINUS, LOAD, DOT, DIVISION), "  13 6A 11 15"),
                arguments(List.of(D2, DIVISION, STORE, D2), "  42 13 02 18"),
                arguments(List.of(RUN_STOP, LOAD, DOT, LOAD, EE), "  6C 6A 50 21"),
                arguments(List.of(MULTIPLICATION, D4, MULTIPLICATION), "  12 04 12 24"),
                arguments(List.of(LOAD, SIGN, F, MULTIPLICATION, SWAP), "  14 22 6B 27"),
                arguments(List.of(MINUS, F, MINUS, LOAD, SIGN), "  6B 21 11 30"),
                arguments(List.of(SIGN, RETURN), "  52 0B 6B 32"),
                arguments(List.of(F, SIGN, RETURN), " 0.          "),
                // Данные
                arguments(List.of(D3, STORE, DOT), " 3.          "),
                arguments(List.of(D2, STORE, SIGN), " 2.          "),
                arguments(List.of(D1, SIGN, STORE, EE), "-1.          "),
                // Выполнение
                arguments(List.of(RUN_STOP), "-1.          "),
                // Контроль регистров
                arguments(List.of(LOAD, D1), " 3.3333333-01")
        );
    }

    @ParameterizedTest
    @MethodSource("testArguments")
    public void test(List<KeyboardButton> buttons, String expected) {
        buttons.forEach(engine::processButton);
        assertEquals(expected, engine.displayProperty().get());
    }
}
