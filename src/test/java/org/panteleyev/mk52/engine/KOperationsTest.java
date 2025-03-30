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

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.panteleyev.mk52.engine.KeyboardButton.D0;
import static org.panteleyev.mk52.engine.KeyboardButton.D1;
import static org.panteleyev.mk52.engine.KeyboardButton.D2;
import static org.panteleyev.mk52.engine.KeyboardButton.D3;
import static org.panteleyev.mk52.engine.KeyboardButton.D4;
import static org.panteleyev.mk52.engine.KeyboardButton.D5;
import static org.panteleyev.mk52.engine.KeyboardButton.D6;
import static org.panteleyev.mk52.engine.KeyboardButton.D8;
import static org.panteleyev.mk52.engine.KeyboardButton.D9;
import static org.panteleyev.mk52.engine.KeyboardButton.DOT;
import static org.panteleyev.mk52.engine.KeyboardButton.K;
import static org.panteleyev.mk52.engine.KeyboardButton.PLUS;
import static org.panteleyev.mk52.engine.KeyboardButton.PUSH;
import static org.panteleyev.mk52.engine.KeyboardButton.SWAP;

@DisplayName("Операции в режиме K")
public class KOperationsTest extends BaseTest {
    private static StackSnapshot stack = null;
    private static final Engine engine = new Engine(false,
            (_, result, _) -> stack = result == null ? null : result.stack());

    @BeforeEach
    public void beforeEach() {
        engine.togglePower(false);
        engine.togglePower(true);
    }

    private static List<Arguments> testArguments() {
        return List.of(
                argumentSet("K max", List.of(D4, D5, D6, PUSH, D1, D2, D3, K, D9), new StackSnapshot(
                        " 456.        ",
                        " 456.", " 0.", " 0.", " 123."
                )),
                argumentSet("K max", List.of(D1, D2, D3, PUSH, D4, D5, D6, K, D9), new StackSnapshot(
                        " 456.        ",
                        " 123.", " 0.", " 0.", " 456."
                )),
                argumentSet("K max (дефект)", List.of(D1, D2, D3, K, D9), new StackSnapshot(
                        " 0.          ",
                        " 0.", " 0.", " 0.", " 123."
                )),
                argumentSet("K o⃗'", List.of(D6, D0, DOT, D3, D6, K, PLUS), new StackSnapshot(
                        " 60.6        ",
                        " 0.", " 0.", " 0.", " 60.36"
                )),
                argumentSet("K o⃗'", List.of(D6, D0, DOT, D4, K, PLUS), new StackSnapshot(
                        " 60.666666   ",
                        " 0.", " 0.", " 0.", " 60.4"
                )),
                argumentSet("K o⃗'", List.of(D1, D4, D2, DOT, D2, D4, D3, D1, D4, K, PLUS), new StackSnapshot(
                        " 142.40523   ",
                        " 0.", " 0.", " 0.", " 142.24314"
                )),
                argumentSet("K o⃖′", List.of(D6, D0, DOT, D8, K, D6), new StackSnapshot(
                        " 60.48       ",
                        " 0.", " 0.", " 0.", " 60.8"
                )),
                argumentSet("K o⃖′", List.of(D6, D0, DOT, D6, D1, K, D6), new StackSnapshot(
                        " 60.366      ",
                        " 0.", " 0.", " 0.", " 60.61"
                )),
                argumentSet("K o⃖‴", List.of(D2, D0, DOT, D6, D1, D3, D3, D3, D2, K, D3), new StackSnapshot(
                        " 20.364799   ",
                        " 0.", " 0.", " 0.", " 20.613332"
                )),
                argumentSet("K o⃗‴", List.of(D2, D0, DOT, D3, D6, D4, D8, K, SWAP), new StackSnapshot(
                        " 20.613333   ",
                        " 0.", " 0.", " 0.", " 20.3648"
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
