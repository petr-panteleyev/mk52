/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.panteleyev.mk52.BaseTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.panteleyev.mk52.engine.KeyboardButton.CLEAR_X;
import static org.panteleyev.mk52.engine.KeyboardButton.D1;
import static org.panteleyev.mk52.engine.KeyboardButton.D2;
import static org.panteleyev.mk52.engine.KeyboardButton.D3;
import static org.panteleyev.mk52.engine.KeyboardButton.D4;
import static org.panteleyev.mk52.engine.KeyboardButton.D5;
import static org.panteleyev.mk52.engine.KeyboardButton.D6;
import static org.panteleyev.mk52.engine.KeyboardButton.D7;
import static org.panteleyev.mk52.engine.KeyboardButton.D8;
import static org.panteleyev.mk52.engine.KeyboardButton.D9;
import static org.panteleyev.mk52.engine.KeyboardButton.EE;
import static org.panteleyev.mk52.engine.KeyboardButton.F;
import static org.panteleyev.mk52.engine.KeyboardButton.MULTIPLICATION;
import static org.panteleyev.mk52.engine.KeyboardButton.PLUS;
import static org.panteleyev.mk52.engine.KeyboardButton.PUSH;
import static org.panteleyev.mk52.engine.KeyboardButton.RETURN;
import static org.panteleyev.mk52.engine.KeyboardButton.RUN_STOP;
import static org.panteleyev.mk52.engine.KeyboardButton.SIGN;
import static org.panteleyev.mk52.engine.KeyboardButton.SWAP;

@DisplayName("Операции со стеком")
public class StackTest extends BaseTest {
    private static StackSnapshot stack = null;
    private static final Engine engine = new Engine(false,
            (result, _) -> stack = result == null ? null : result.stack());

    @BeforeEach
    public void beforeEach() {
        engine.togglePower(false);
        engine.togglePower(true);
    }

    private static List<Arguments> testArguments() {
        return List.of(
                arguments(List.of(D1, PUSH, D2, PLUS), new StackSnapshot(
                        " 3.",
                        " 0.", " 0.", " 0.", " 2.", " 3."
                )),
                arguments(List.of(F, EE, D1, PUSH, D2, PLUS, RUN_STOP, F, SIGN, RETURN, RUN_STOP), new StackSnapshot(
                        " 3.",
                        " 0.", " 0.", " 0.", " 2.", " 3."
                )),
                arguments(List.of(D1, D2, D3, CLEAR_X, D4, D5, D6), new StackSnapshot(
                        " 456.",
                        " 0.", " 0.", " 0.", " 0.", " 456."
                )),
                arguments(List.of(D1, D2, D3, PUSH, D4, D5, D6), new StackSnapshot(
                        " 456.",
                        " 123.", " 0.", " 0.", " 0.", " 456."
                )),
                arguments(List.of(D1, D2, D3, PUSH, D4, D5, D6, CLEAR_X, D7, D8, D9), new StackSnapshot(
                        " 789.",
                        " 123.", " 0.", " 0.", " 0.", " 789."
                )),
                arguments(List.of(D1, D2, D3, SWAP, D4, D5, D6), new StackSnapshot(
                        " 456.",
                        " 0.", " 123.", " 0.", " 123.", " 456."
                )),
                arguments(List.of(D1, D2, D3, F, PLUS), new StackSnapshot(
                        " 3.1415926",
                        " 123.", " 0.", " 0.", " 123.", " 3.1415926"
                )),
                arguments(List.of(D1, PUSH, D2, PUSH, D3, PUSH, D4, F, MULTIPLICATION, F, PUSH), new StackSnapshot(
                        " 4.",
                        " 16.", " 3.", " 2.", " 4.", " 4."
                ))
        );
    }

    @ParameterizedTest
    @MethodSource("testArguments")
    public void test(List<KeyboardButton> buttons, StackSnapshot expected) {
        buttons.forEach(engine::processButton);
        assertEquals(expected, stack);
    }
}
