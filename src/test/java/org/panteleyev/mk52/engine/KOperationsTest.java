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
import static org.panteleyev.mk52.engine.KeyboardButton.D0;
import static org.panteleyev.mk52.engine.KeyboardButton.D1;
import static org.panteleyev.mk52.engine.KeyboardButton.D2;
import static org.panteleyev.mk52.engine.KeyboardButton.D3;
import static org.panteleyev.mk52.engine.KeyboardButton.D4;
import static org.panteleyev.mk52.engine.KeyboardButton.D5;
import static org.panteleyev.mk52.engine.KeyboardButton.D6;
import static org.panteleyev.mk52.engine.KeyboardButton.D7;
import static org.panteleyev.mk52.engine.KeyboardButton.D8;
import static org.panteleyev.mk52.engine.KeyboardButton.D9;
import static org.panteleyev.mk52.engine.KeyboardButton.DOT;
import static org.panteleyev.mk52.engine.KeyboardButton.K;
import static org.panteleyev.mk52.engine.KeyboardButton.PLUS;
import static org.panteleyev.mk52.engine.KeyboardButton.PUSH;
import static org.panteleyev.mk52.engine.KeyboardButton.SIGN;
import static org.panteleyev.mk52.engine.KeyboardButton.SWAP;

@DisplayName("Операции в режиме K")
public class KOperationsTest extends BaseTest {
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
                argumentSet("[1] K max", List.of(D4, D5, D6, PUSH, D1, D2, D3, K, D9), new StackSnapshot(
                        0x0020_4560_0000L, 0x0020_4560_0000L, 0, 0, 0x0020_1230_0000L,
                        new IR(0xFFFF_456F_FFFFL, 1 << 5)
                )),
                argumentSet("[2] K max", List.of(D1, D2, D3, PUSH, D4, D5, D6, K, D9), new StackSnapshot(
                        0x0020_4560_0000L, 0x0020_1230_0000L, 0, 0, 0x0020_4560_0000L,
                        new IR(0xFFFF_456F_FFFFL, 1 << 5)
                )),
                argumentSet("[3] K max (дефект)", List.of(D1, D2, D3, K, D9), new StackSnapshot(
                        0, 0, 0, 0, 0x0020_1230_0000L, IR.ZERO
                )),
                argumentSet("[4] K o⃗'", List.of(D6, D0, DOT, D3, D6, K, PLUS), new StackSnapshot(
                        0x0010_6060_0000L, 0, 0, 0, 0x0010_6036_0000L,
                        new IR(0xFFFF_606F_FFFFL, 1 << 6)
                )),
                argumentSet("[5] K o⃗'", List.of(D6, D0, DOT, D3, D6, SIGN, K, PLUS), new StackSnapshot(
                        0x0019_6060_0000L, 0, 0, 0, 0x0019_6036_0000L,
                        new IR(0xFFFA_606F_FFFFL, 1 << 6)
                )),
                argumentSet("[6] K o⃗'", List.of(D6, D0, K, PLUS), new StackSnapshot(
                        0x0010_6000_0000L, 0, 0, 0, 0x0010_6000_0000L,
                        new IR(0xFFFF_60FF_FFFFL, 1 << 6)
                )),
                // 60.666666-01
                argumentSet("[7] K o⃗'", List.of(D6, D0, DOT, D4, K, PLUS), new StackSnapshot(
                        0x0010_60_666667L, 0, 0, 0, 0x0010_60_400000L,
                        new IR(0xFFFF_60_666667L, 1 << 6)
                )),
                // 6.6666666-01
                argumentSet("[8] K o⃗'", List.of(D0, DOT, D4, K, PLUS), new StackSnapshot(
                        0x9990_6666_6667L, 0, 0, 0, 0x9990_4000_0000L,
                        new IR(0xA01F_6_6666667L, 1 << 7)
                )),
                argumentSet("[9] K o⃗'", List.of(D1, D4, D2, DOT, D2, D4, D3, D1, D4, K, PLUS), new StackSnapshot(
                        0x020_142_40523L, 0, 0, 0, 0x020_142_24314L,
                        new IR(0xFFFF_142_40523L, 1 << 5)
                )),
                // 4.0523332-01
                argumentSet("[10] K o⃗'", List.of(D0, DOT, D2, D4, D3, D1, D4, K, PLUS), new StackSnapshot(
                        0x9990_4_0523333L, 0, 0, 0, 0x9990_2_4314000L,
                        new IR(0xA01F_4_0523333L, 1 << 7)
                )),
                argumentSet("[11] K o⃖′", List.of(D6, D0, DOT, D8, K, D6), new StackSnapshot(
                        0x0010_60_480000L, 0, 0, 0, 0x0010_60_800000L,
                        new IR(0xFFFF_60_48FFFFL, 1 << 6)
                )),
                argumentSet("[12] K o⃖′", List.of(D6, D0, DOT, D6, D1, K, D6), new StackSnapshot(
                        0x0010_60_366000L, 0, 0, 0, 0x0010_60_610000L,
                        new IR(0xFFFF_60_366FFFL, 1 << 6)
                )),
                // 20.364799
                argumentSet("[13] K o⃖‴", List.of(D2, D0, DOT, D6, D1, D3, D3, D3, D2, K, D3), new StackSnapshot(
                        0x010_20_364800L, 0, 0, 0, 0x010_20_613332L,
                        new IR(0xFFFF_20_3648FFL, 1 << 6)
                )),
                argumentSet("[14] K o⃗‴", List.of(D2, D0, DOT, D3, D6, D4, D8, K, SWAP), new StackSnapshot(
                        0x0010_20_613333L, 0, 0, 0, 0x010_20_364800L,
                        new IR(0xFFFF_20_613333L, 1 << 6)
                )),
                argumentSet("[15] K [12.345]", List.of(D1, D2, DOT, D3, D4, D5, K, D7), new StackSnapshot(
                        0x0010_1200_0000L, 0, 0, 0, 0x0010_12_345000L,
                        new IR(0xFFFF_12_FFFFFFL, 1 << 6)
                )),
                argumentSet("[16] K [-12.345]", List.of(D1, D2, DOT, D3, D4, D5, SIGN, K, D7), new StackSnapshot(
                        0x0019_12_000000L, 0, 0, 0, 0x0019_12_345000L,
                        new IR(0xFFFA_12_FFFFFFL, 1 << 6)
                )),
                argumentSet("[17] K [0.234]", List.of(D0, DOT, D2, D3, D4, K, D7), new StackSnapshot(
                        0, 0, 0, 0, 0x9990_2_3400000L, IR.ZERO
                )),
                argumentSet("[18] K [-0.234]", List.of(D0, DOT, D2, D3, D4, SIGN, K, D7), new StackSnapshot(
                        0, 0, 0, 0, 0x9999_2_3400000L, IR.ZERO
                )),
                argumentSet("[19] K {12.345}", List.of(D1, D2, DOT, D3, D4, D5, K, D8), new StackSnapshot(
                        0x9990_3_4500000L, 0, 0, 0, 0x0010_12_345000L,
                        new IR(0xA01F_3_45FFFFFL, 1 << 7)
                )),
                argumentSet("[20] K {-12.345}", List.of(D1, D2, DOT, D3, D4, D5, SIGN, K, D8), new StackSnapshot(
                        0x9999_3450_0000L, 0, 0, 0, 0x19_1234_5000L,
                        new IR(0xA01A_3_45FFFFFL, 1 << 7)
                )),
                argumentSet("[21] K {0.234}", List.of(D0, DOT, D2, D3, D4, K, D8), new StackSnapshot(
                        0x9990_2340_0000L, 0, 0, 0, 0x9990_2340_0000L,
                        new IR(0xA01F_2_34FFFFFL, 1 << 7)
                )),
                argumentSet("[22] K {-0.234}", List.of(D0, DOT, D2, D3, D4, SIGN, K, D8), new StackSnapshot(
                        0x9999_2340_0000L, 0, 0, 0, 0x9999_2340_0000L,
                        new IR(0xA01A_2_34FFFFFL, 1 << 7)
                )),
                argumentSet("[23] K |1.234|", List.of(D1, DOT, D2, D3, D4, K, D4), new StackSnapshot(
                        0x1234_0000L, 0, 0, 0, 0x1234_0000L,
                        new IR(0xFFFF_1234_FFFFL, 1 << 7)
                )),
                argumentSet("[24] K |-1.234|", List.of(D1, DOT, D2, D3, D4, SIGN, K, D4), new StackSnapshot(
                        0x1234_0000L, 0, 0, 0, 0x0009_1234_0000L,
                        new IR(0xFFFF_1234_FFFFL, 1 << 7)
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
