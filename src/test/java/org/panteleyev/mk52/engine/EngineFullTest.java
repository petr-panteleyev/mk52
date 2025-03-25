/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.panteleyev.mk52.engine.KeyboardButton.A_UP;
import static org.panteleyev.mk52.engine.KeyboardButton.CLEAR_X;
import static org.panteleyev.mk52.engine.KeyboardButton.DIGIT_0;
import static org.panteleyev.mk52.engine.KeyboardButton.DIGIT_1;
import static org.panteleyev.mk52.engine.KeyboardButton.DIGIT_2;
import static org.panteleyev.mk52.engine.KeyboardButton.DIGIT_3;
import static org.panteleyev.mk52.engine.KeyboardButton.DIGIT_4;
import static org.panteleyev.mk52.engine.KeyboardButton.DIGIT_5;
import static org.panteleyev.mk52.engine.KeyboardButton.DIGIT_6;
import static org.panteleyev.mk52.engine.KeyboardButton.DIGIT_7;
import static org.panteleyev.mk52.engine.KeyboardButton.DIGIT_8;
import static org.panteleyev.mk52.engine.KeyboardButton.DIGIT_9;
import static org.panteleyev.mk52.engine.KeyboardButton.DIVISION;
import static org.panteleyev.mk52.engine.KeyboardButton.DOT;
import static org.panteleyev.mk52.engine.KeyboardButton.EE;
import static org.panteleyev.mk52.engine.KeyboardButton.F;
import static org.panteleyev.mk52.engine.KeyboardButton.K;
import static org.panteleyev.mk52.engine.KeyboardButton.MINUS;
import static org.panteleyev.mk52.engine.KeyboardButton.PLUS;
import static org.panteleyev.mk52.engine.KeyboardButton.PUSH;
import static org.panteleyev.mk52.engine.KeyboardButton.SIGN;
import static org.panteleyev.mk52.engine.KeyboardButton.STORE;
import static org.panteleyev.mk52.engine.KeyboardButton.SWAP;
import static org.panteleyev.mk52.engine.KeyboardButton.UP_DOWN;

public class EngineFullTest {
    private static final Engine engine = new Engine();
    private static final Consumer<Engine> NOOP = _ -> {};
    private static final Consumer<Engine> POWEROFF = engine -> engine.togglePower(false);
    private static final Consumer<Engine> DEGREE = engine -> engine.setTrigonometricMode(TrigonometricMode.DEGREE);
    private static final Consumer<Engine> GRADIAN = engine -> engine.setTrigonometricMode(TrigonometricMode.GRADIAN);
    private static final Consumer<Engine> RADIAN = engine -> engine.setTrigonometricMode(TrigonometricMode.RADIAN);

    @BeforeAll
    public static void beforeAll() {
        engine.init();
        engine.togglePower(true);
    }

    private static List<Arguments> testArguments() {
        return List.of(
                // [1]
                arguments(NOOP, List.of(DIGIT_6, DIGIT_1), " 61.         "),
                // [2]
                arguments(NOOP, List.of(STORE, CLEAR_X), " 61.         "),
                // [3]
                arguments(NOOP, List.of(DIGIT_3, DIGIT_1, DIGIT_5), " 315.        "),
                // [4]
                arguments(NOOP, List.of(DOT, DIGIT_0, DIGIT_7), " 315.07      "),
                // [5]
                arguments(NOOP, List.of(F, PLUS), " 3.1415926   "),
                // [6]
                arguments(NOOP, List.of(A_UP), " 3.1415926   "),
                // [7]
                arguments(NOOP, List.of(UP_DOWN), " 3.1415926   "),
                // [8]
                arguments(NOOP, List.of(EE, DIGIT_2), " 3.1415926 02"),
                // [9]
                arguments(NOOP, List.of(SWAP), " 315.07      "),
                // [10]
                arguments(NOOP, List.of(MINUS), "-9.1074   -01"),
                // [11]
                arguments(RADIAN, List.of(F, DIGIT_8), " 6.1316135-01"),
                // [12] Расхождение
                arguments(GRADIAN, List.of(F, DIGIT_4), " 42.020491   "),
                // [13]
                arguments(DEGREE, List.of(SIGN), "-42.020491   "),
                // [14]
                arguments(NOOP, List.of(DIGIT_9), " 9.          "),
                // [15]
                arguments(NOOP, List.of(DIVISION), "-4.6689434   "),
                // [16]
                arguments(NOOP, List.of(K, STORE, DIGIT_6), "-4.6689434   "),
                // [17]
                arguments(NOOP, List.of(F, PUSH), " 9.          ")
        );
    }

    @ParameterizedTest
    @MethodSource("testArguments")
    public void test(Consumer<Engine> preOperation, List<KeyboardButton> buttons, String expected) {
        preOperation.accept(engine);
        buttons.forEach(engine::processButton);
        assertEquals(expected, engine.getDisplayString());
    }
}
