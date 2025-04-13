/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.math;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class Mk52MathTest {
    private static List<Arguments> testIntegerArguments() {
        return List.of(
                arguments(0, 0),
                //
                arguments(0x0012345678L, 0x0010000000L),
                arguments(0x1012345678L, 0x1012000000L),
                arguments(0x2012345678L, 0x2012300000L),
                arguments(0x3012345678L, 0x3012340000L),
                arguments(0x4012345678L, 0x4012345000L),
                arguments(0x5012345678L, 0x5012345600L),
                arguments(0x6012345678L, 0x6012345670L),
                arguments(0x7012345678L, 0x7012345678L),
                //
                arguments(0x0912345678L, 0x0910000000L),
                arguments(0x1912345678L, 0x1912000000L),
                arguments(0x2912345678L, 0x2912300000L),
                arguments(0x3912345678L, 0x3912340000L),
                arguments(0x4912345678L, 0x4912345000L),
                arguments(0x5912345678L, 0x5912345600L),
                arguments(0x6912345678L, 0x6912345670L),
                arguments(0x7912345678L, 0x7912345678L),
                // Отрицательный порядок,
                arguments(0x9990123456789L, 0L)
        );
    }

    @ParameterizedTest
    @MethodSource("testIntegerArguments")
    @DisplayName("К [x]")
    public void testInteger(long x, long expected) {
        assertEquals(expected, Mk52Math.integer(x));
    }

    private static List<Arguments> testFractionalArguments() {
        return List.of(
                arguments(0, 0),
                // Отрицательный порядок,
                arguments(0x9990123456789L, 0x9990123456789L),
                //
                arguments(0x000012345678L, 0x999023456780L),
                arguments(0x001012345678L, 0x999034567800L),
                arguments(0x002012345678L, 0x999045678000L),
                arguments(0x003012345678L, 0x999056780000L),
                arguments(0x004012345678L, 0x999067800000L),
                arguments(0x005012345678L, 0x999078000000L),
                arguments(0x006012345678L, 0x999080000000L),
                arguments(0x007012345678L, 0x000000000000L),
                //
                arguments(0x007912345678L, 0x000000000000L),
                arguments(0x006912345670L, 0x000900000000L),
                arguments(0x005912345600L, 0x000900000000L)
        );
    }

    @ParameterizedTest
    @MethodSource("testFractionalArguments")
    @DisplayName("К {x}")
    public void testFractional(long x, long expected) {
        assertEquals(expected, Mk52Math.fractional(x));
    }
}
