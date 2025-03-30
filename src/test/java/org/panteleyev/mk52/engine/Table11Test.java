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
    private static String displayContent = "";
    private static final Engine engine = new Engine(false, (content, _, _) -> displayContent = content);

    @BeforeAll
    public static void beforeAll() {
        engine.init();
        engine.togglePower(true);
    }

    private static List<Arguments> testArguments() {
        return List.of(
                // Программа
                arguments(List.of(F, EE), "           00"),
                arguments(List.of(D4, PUSH, K, GOTO, D3), "  83 0E 04 03"),
                arguments(List.of(F, MINUS, D2, PLUS), "  10 02 21 06"),
                arguments(List.of(K, GOTO, D4, PLUS, D3), "  03 10 84 09"),
                arguments(List.of(MULTIPLICATION, K, GOTO, DOT, MINUS), "  11 8A 12 12"),
                arguments(List.of(RUN_STOP), "  50 11 8A 13"),
                arguments(List.of(F, SIGN, RETURN), " 0.          "),
                // Данные
                arguments(List.of(D5, STORE, D3), " 5.          "),
                arguments(List.of(D7, STORE, D4), " 7.          "),
                arguments(List.of(D1, D2, STORE, DOT), " 12.         "),
                // Выполнение
                arguments(List.of(RUN_STOP), " 18.         "),
                // Контроль регистров
                arguments(List.of(LOAD, D3), " 00000004.   "),
                arguments(List.of(LOAD, D4), " 00000008.   "),
                arguments(List.of(LOAD, DOT), " 00000012.   ")
        );
    }

    @ParameterizedTest
    @MethodSource("testArguments")
    public void test(List<KeyboardButton> buttons, String expected) {
        buttons.forEach(engine::processButton);
        assertEquals(expected, displayContent);
    }
}
