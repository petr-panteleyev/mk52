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
import static org.panteleyev.mk52.engine.KeyboardButton.DOT;
import static org.panteleyev.mk52.engine.KeyboardButton.EE;
import static org.panteleyev.mk52.engine.KeyboardButton.F;
import static org.panteleyev.mk52.engine.KeyboardButton.MULTIPLICATION;
import static org.panteleyev.mk52.engine.KeyboardButton.PLUS;
import static org.panteleyev.mk52.engine.KeyboardButton.PUSH;
import static org.panteleyev.mk52.engine.KeyboardButton.SIGN;
import static org.panteleyev.mk52.engine.KeyboardButton.STORE;
import static org.panteleyev.mk52.engine.KeyboardButton.UP_DOWN;

public class EngineShortTest {
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
                arguments(NOOP, List.of(DIGIT_1, DIGIT_2, DIGIT_3, DIGIT_4), " 1234.       "),
                // [2]
                arguments(NOOP, List.of(DIGIT_5, DIGIT_6, DIGIT_7, DIGIT_8), " 12345678.   "),
                // [3]
                arguments(NOOP, List.of(EE, DIGIT_9, SIGN), " 12345678.-09"),
                // [4]
                arguments(NOOP, List.of(PUSH), " 1.2345678-02"),
                // [5]
                arguments(NOOP, List.of(DIGIT_0, DOT, DIGIT_9), " 0.9         "),
                // [6]
                arguments(NOOP, List.of(MULTIPLICATION), " 1.111111 -02"),
                // [7] // Расхождение
                arguments(DEGREE, List.of(F, DIGIT_7), " 1.9392546-04"),
                // [8]
                arguments(GRADIAN, List.of(F, DIGIT_8), " 1.          "),
                // [9] // Расхождение
                arguments(RADIAN, List.of(F, DIGIT_9), " 1.5574077   "),
                // [10] - Расхождение
                arguments(NOOP, List.of(STORE, DIGIT_1), " 1.5574077   "),
                // [11]
                arguments(NOOP, List.of(F, PLUS), " 3.1415926   "),
                // [12]
                arguments(NOOP, List.of(A_UP), " 3.1415926   "),
                // [13]
                arguments(NOOP, List.of(UP_DOWN), " 3.1415926   "),
                // [14] // Расхождение
                arguments(NOOP, List.of(F, DIGIT_2), " 4.9714987-01"),
                // [15] // Расхождение
                arguments(NOOP, List.of(STORE, DIGIT_2), " 4.9714987-01"),
                // [16]
                arguments(NOOP, List.of(F, PLUS), " 3.1415926   "),
                // [17]
                arguments(NOOP, List.of(UP_DOWN), " 3.1415926   "),
                // [18]
                arguments(NOOP, List.of(), " 3.1415926   "),
                // [19]
                arguments(POWEROFF, List.of(), "             "),
                // [20]
                arguments((Consumer<Engine>) engine -> {
                    engine.togglePower(true);
                    // СЧ Д
                }, List.of(F, PLUS), " 3.1415926   "),
                // [21]
                arguments(NOOP, List.of(A_UP), " 3.1415926   "),
                // [22]
                arguments(NOOP, List.of(UP_DOWN), " 3.1415926   ")
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
