/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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
                arguments(List.of(F, EE), "           00"),
                arguments(List.of(D1, D9, STORE, SIGN), "  4B 09 01 03"),
                arguments(List.of(D9, LOAD, D1, F, MULTIPLICATION), "  22 61 09 06"),
                arguments(List.of(MULTIPLICATION, STORE, D2, LOAD, D1), "  61 42 12 09"),
                arguments(List.of(D2, MULTIPLICATION, F, D1), "  16 12 02 12"),
                arguments(List.of(LOAD, D2, MINUS, K, STEP_RIGHT, SIGN), "  CB 11 62 15"),
                arguments(List.of(LOAD, D1, F, D9, PLUS), "  10 1E 61 18"),
                arguments(List.of(RUN_STOP, LOAD, D1, F, D7), "  1C 61 50 21"),
                arguments(List.of(PLUS, RUN_STOP), "  50 10 1C 23"),
                arguments(List.of(F, SIGN, RETURN), " 0.          "),
                // Данные 1
                arguments(List.of(D1, STORE, D1), " 1.          "),
                arguments(List.of(RUN_STOP), "-5.3536176-02"),
                // Данные 2
                arguments(List.of(D2, STORE, D1), " 2.          "),
                arguments(List.of(RETURN, RUN_STOP), " 19.507447   "),
                // Данные 3
                arguments(List.of(D3, STORE, D1), " 3.          "),
                arguments(List.of(RETURN, RUN_STOP), " 322.56991   ")
        );
    }

    @ParameterizedTest
    @MethodSource("testArguments")
    public void test(List<KeyboardButton> buttons, String expected) {
        buttons.forEach(engine::processButton);
        assertEquals(expected, engine.displayProperty().get());
    }
}
