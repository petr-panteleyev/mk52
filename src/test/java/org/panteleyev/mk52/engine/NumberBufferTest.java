/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NumberBufferTest {
    private final NumberBuffer buffer = new NumberBuffer();

    private static List<Arguments> testGetValueArguments() {
        return List.of(
                Arguments.of(
                        new char[]{'1', '2', '3', '4', '5'}, 12345d
                ),
                Arguments.of(
                        new char[]{'1', '2', '3', '.', '4', '5'}, 123.45d
                ),
                Arguments.of(
                        new char[]{'-', '1', '2', '3', '.', '4', '5'}, -123.45d
                ),
                Arguments.of(
                        new char[]{'0', '0', '1', '2', '3', '.', '4', '-', '5'}, -123.45d
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testGetValueArguments")
    public void testGetValue(char[] chars, double expected) {
        for (var ch : chars) {
            buffer.addDigit(ch);
        }

        var actual = buffer.getValue();
        assertEquals(expected, actual.doubleValue());
    }

    public static List<Arguments> testGetBufferArguments() {
        return List.of(
                Arguments.of(
                        new char[]{'E'},
                        " 1.        00"
                ),
                Arguments.of(
                        new char[]{'1', '2', '3', '4', '5', '6', '7', '8', '-', 'E', '4', '5', '-'},
                        "-12345678.-45"
                ),
                Arguments.of(
                        new char[]{'1', '.', '2', '3', '4', '5', '6', '7', '8', '-', 'E', '4', '5', '-'},
                        "-1.2345678-45"
                ),
                Arguments.of(
                        new char[]{'1', '.'},
                        " 1.          "
                ),
                Arguments.of(
                        new char[]{'1', '.', '2'},
                        " 1.2         "
                ),
                Arguments.of(
                        new char[]{'1', '.', '2', '3'},
                        " 1.23        "
                ),
                Arguments.of(
                        new char[]{'1', '.', '2', '3', '4'},
                        " 1.234       "
                ),
                Arguments.of(
                        new char[]{'1', '.', '2', '3', '4', '5'},
                        " 1.2345      "
                ),
                Arguments.of(
                        new char[]{'1', '.', '2', '3', '4', '5', '6'},
                        " 1.23456     "
                ),
                Arguments.of(
                        new char[]{'1', '.', '2', '3', '4', '5', '6', '7'},
                        " 1.234567    "
                ),
                Arguments.of(
                        new char[]{'1', '.', '2', '3', '4', '5', '6', '7', '8'},
                        " 1.2345678   "
                ),
                Arguments.of(
                        new char[]{'1', '.', '2', '3', '4', '.', '5', '-', '6', '7', '-', '8'},
                        " 1.2345678   "
                )
        );
    }

    @ParameterizedTest
    @MethodSource("testGetBufferArguments")
    public void testGetBuffer(char[] chars, String expected) {
        buffer.reset();
        for (var ch : chars) {
            if (ch == 'E') {
                buffer.enterExponent();
            } else {
                buffer.addDigit(ch);
            }
        }

        assertEquals(expected, buffer.getBuffer());
    }
}
