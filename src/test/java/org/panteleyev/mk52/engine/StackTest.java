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
                        0x3000_0000L, 0, 0, 0, 0x0000_2000_0000L, new IR(0xFFFF_3FFF_FFFFL, 1 << 7)
                )),
                arguments(List.of(F, EE, D1, PUSH, D2, PLUS, RUN_STOP, F, SIGN, RETURN, RUN_STOP), new StackSnapshot(
                        0x3000_0000L, 0, 0, 0, 0x0000_2000_0000L, new IR(0xFFFF_3FFF_FFFFL, 1 << 7)
                )),
                arguments(List.of(D1, D2, D3, CLEAR_X, D4, D5, D6), new StackSnapshot(
                        0x0020_4560_0000L, 0, 0, 0, 0, new IR(0xFFFF_456F_FFFFL, 1 << 5)
                )),
                arguments(List.of(D1, D2, D3, PUSH, D4, D5, D6), new StackSnapshot(
                        0x0020_4560_0000L, 0x0020_1230_0000L, 0, 0, 0, new IR(0xFFFF_456F_FFFFL, 1 << 5)
                )),
                arguments(List.of(D1, D2, D3, PUSH, D4, D5, D6, CLEAR_X, D7, D8, D9), new StackSnapshot(
                        0x0020_7890_0000L, 0x0020_1230_0000L, 0, 0, 0,
                        new IR(0xFFFF_789F_FFFFL, 1 << 5)
                )),
                arguments(List.of(D1, D2, D3, SWAP, D4, D5, D6), new StackSnapshot(
                        0x0020_4560_0000L, 0, 0x0020_1230_0000L, 0, 0x0020_1230_0000L,
                        new IR(0xFFFF_456F_FFFFL, 1 << 5)
                )),
                arguments(List.of(D1, D2, D3, F, PLUS), new StackSnapshot(
                        Register.PI, 0x00201230_0000L, 0, 0, 0x00201230_0000L, IR.PI
                )),
                arguments(List.of(D1, PUSH, D2, PUSH, D3, PUSH, D4, F, MULTIPLICATION, F, PUSH), new StackSnapshot(
                        0x4000_0000L, 0x0101600_0000L, 0x3000_0000L, 0x2000_0000L, 0x4000_0000L,
                        new IR(0xFFFF_4FFF_FFFFL, 1 << 7)
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
