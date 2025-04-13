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
import static org.panteleyev.mk52.engine.KeyboardButton.CLEAR_X;
import static org.panteleyev.mk52.engine.KeyboardButton.D1;
import static org.panteleyev.mk52.engine.KeyboardButton.D2;
import static org.panteleyev.mk52.engine.KeyboardButton.D3;
import static org.panteleyev.mk52.engine.KeyboardButton.D4;
import static org.panteleyev.mk52.engine.KeyboardButton.DOT;
import static org.panteleyev.mk52.engine.KeyboardButton.EE;
import static org.panteleyev.mk52.engine.KeyboardButton.F;
import static org.panteleyev.mk52.engine.KeyboardButton.K;
import static org.panteleyev.mk52.engine.KeyboardButton.PLUS;
import static org.panteleyev.mk52.engine.KeyboardButton.PUSH;
import static org.panteleyev.mk52.engine.KeyboardButton.SIGN;

@DisplayName("Логические операции")
public class LogicalOperationsTest extends BaseTest {
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
                // ИНВ
                argumentSet("K ИНВ", List.of(D1, DOT, D1, D2, D3, D4, K, CLEAR_X), new StackSnapshot(
                        " 8.EDCBFFF",
                        " 0.", " 0.", " 0.", " 1.1234", " 8.EDCBFFF"
                )),
                argumentSet("K ИНВ", List.of(F, PLUS, K, CLEAR_X), new StackSnapshot(
                        " 8.EBEA6D9",
                        " 0.", " 0.", " 0.", " 3.1415926", " 8.EBEA6D9"
                )),
                // XOR
                argumentSet("K ⨁", List.of(D1, DOT, D1, D2, D3, PUSH, D1, DOT, D4, D3, D2, K, EE), new StackSnapshot(
                        " 8.511",
                        " 1.123", " 0.", " 0.", " 1.432", " 8.511"
                )),
                // AND
                argumentSet("K ∧", List.of(D1, DOT, D1, D2, D3, PUSH, D1, DOT, D4, D3, D2, K, DOT), new StackSnapshot(
                        " 8.022",
                        " 1.123", " 0.", " 0.", " 1.432", " 8.022"
                )),
                // OR
                argumentSet("K ∨", List.of(D1, DOT, D1, D2, D3, PUSH, D1, DOT, D4, D3, D2, K, SIGN), new StackSnapshot(
                        " 8.533",
                        " 1.123", " 0.", " 0.", " 1.432", " 8.533"
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
