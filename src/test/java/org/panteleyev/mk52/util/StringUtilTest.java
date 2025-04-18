/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class StringUtilTest {
    private static List<Arguments> testPadToDisplayArguments() {
        return List.of(
                arguments("", "             "),
                arguments("1234567890123", "1234567890123"),
                arguments("12345678901234", "12345678901234")
        );
    }

    @ParameterizedTest
    @MethodSource("testPadToDisplayArguments")
    public void testPadToDisplay(String str, String expected) {
        assertEquals(expected, StringUtil.padToDisplay(str));
    }
}
