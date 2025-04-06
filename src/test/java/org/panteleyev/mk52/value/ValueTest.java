/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.value;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class ValueTest {
    private static List<Arguments> testDoubleFromInternalArguments() {
        return List.of(
                arguments(new byte[]{0, 0, 0, 0, 0, 3, 2, 1, 0, 2, 0, 0, 0, 0}, 123),
                arguments(new byte[]{0, 0, 0, 5, 4, 3, 2, 1, 0, 2, 0, 0}, 123.45),
                arguments(new byte[]{6, 2, 9, 5, 1, 4, 1, 3, 0, 0, 0, 0}, 3.1415926),
                arguments(new byte[]{5, 2, 0, 6, 8, 5, 6, 0, 2, 0, 4, 0}, -6.586025e39),
                arguments(new byte[]{6, 2, 9, 5, 1, 4, 1, 3, 9, 4, 4, 9}, -3.1415926e-56),
                arguments(new byte[]{7, 8, 9, 0xA, 0xB, 0xC, 0xD, 8, 0, 0, 0, 0}, 9.4320987),
                arguments(new byte[]{6, 2, 9, 5, 1, 4, 1, 3, 0, 3, 1, 9, 0, 0}, 3.1415926e-87)
        );
    }

    @ParameterizedTest
    @MethodSource("testDoubleFromInternalArguments")
    public void testDoubleFromInternal(byte[] internal, double expected) {
        assertEquals(expected, Value.doubleFromInternal(internal));
    }

    private static List<Arguments> testStringFromBytesArguments() {
        return List.of(
                // Ненормализованные числа
                arguments(new byte[]{5, 2, 0, 6, 8, 5, 6, 0, 2, 0, 4, 0}, "-0.6586025 40"),
                arguments(new byte[]{6, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0}, " 00000006."),
                // Нормализованные числа
                arguments(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, " 0."),
                arguments(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 9, 0, 0, 0}, "-0."),
                arguments(new byte[]{0, 0, 0, 5, 4, 3, 2, 1, 0, 2, 0, 0}, " 123.45"),
                arguments(new byte[]{0, 0, 0, 5, 4, 3, 2, 1, 9, 2, 0, 0}, "-123.45"),
                arguments(new byte[]{8, 7, 6, 5, 4, 3, 2, 1, 0, 7, 0, 0}, " 12345678."),
                arguments(new byte[]{8, 7, 6, 5, 4, 3, 2, 1, 9, 7, 0, 0}, "-12345678."),
                arguments(new byte[]{6, 2, 9, 5, 1, 4, 1, 3, 9, 4, 4, 9}, "-3.1415926-56"),
                // Шестнадцатеричные цифры в числе
                arguments(new byte[]{7, 8, 9, 0xA, 0xB, 0xC, 0xD, 8, 0, 0, 0, 0}, " 8.DCBA987")
        );
    }

    @ParameterizedTest
    @MethodSource("testStringFromBytesArguments")
    public void testStringFromBytes(byte[] bytes, String expected) {
        assertEquals(expected, Value.stringFromBytes(bytes));
    }

    private static List<Arguments> testBytesFromDoubleArguments() {
        return List.of(
                arguments(3.1415926,
                        new byte[]{0x6, 0x2, 0x9, 0x5, 0x1, 0x4, 0x1, 0x3, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0}),
                arguments(-3.1415926e-87,
                        new byte[]{0x6, 0x2, 0x9, 0x5, 0x1, 0x4, 0x1, 0x3, 0x9, 0x3, 0x1, 0x9, 0x0, 0x0}),
                arguments(3.1415926e87,
                        new byte[]{0x6, 0x2, 0x9, 0x5, 0x1, 0x4, 0x1, 0x3, 0x0, 0x7, 0x8, 0x0, 0x0, 0x0}),
                arguments(3.1415926e-87,
                        new byte[]{0x6, 0x2, 0x9, 0x5, 0x1, 0x4, 0x1, 0x3, 0x0, 0x3, 0x1, 0x9, 0x0, 0x0}),
                arguments(123,
                        new byte[]{0x0, 0x0, 0x0, 0x0, 0x0, 0x3, 0x2, 0x1, 0x0, 0x2, 0x0, 0x0, 0x0, 0x0})
        );
    }

    @ParameterizedTest
    @MethodSource("testBytesFromDoubleArguments")
    public void testBytesFromDouble(double value, byte[] expected) {
        assertArrayEquals(expected, Value.bytesFromDouble(value));
    }

    private static List<Arguments> testAsString() {
        return List.of(
                Arguments.of(Value.ZERO, " 0."),
                Arguments.of(new Value(0), " 0."),
                Arguments.of(new Value(1234567.902345678), " 1234567.9"),
                Arguments.of(new Value(12345678.90234), " 12345679."),
                Arguments.of(new Value(12345678.40234), " 12345678."),
                Arguments.of(new Value(-1.234567890234), "-1.2345679"),
                Arguments.of(new Value(-123), "-123."),
                Arguments.of(new Value(-1234), "-1234."),
                Arguments.of(new Value(-12345), "-12345."),
                Arguments.of(new Value(-123456), "-123456."),
                Arguments.of(new Value(-1234567), "-1234567."),
                Arguments.of(new Value(-12345678), "-12345678."),
                Arguments.of(new Value(-1.2345678), "-1.2345678"),
                Arguments.of(new Value(-12.345678), "-12.345678"),
                Arguments.of(new Value(-123.45678), "-123.45678"),
                Arguments.of(new Value(-1234.5678), "-1234.5678"),
                Arguments.of(new Value(-12345.678), "-12345.678"),
                Arguments.of(new Value(-123456.78), "-123456.78"),
                Arguments.of(new Value(-1234567.8), "-1234567.8"),
                Arguments.of(new Value(-123456789012.01), "-1.2345679 11"),
                Arguments.of(new Value(123456789012.01), " 1.2345679 11"),
                Arguments.of(new Value(-123456789012123.0), "-1.2345679 14"),
                Arguments.of(new Value(123456789012123.0), " 1.2345679 14"),
                Arguments.of(new Value(0.0123), " 1.23     -02"),
                Arguments.of(new Value(0.123), " 1.23     -01"),
                Arguments.of(new Value(0.100000), " 1.       -01")
        );
    }

    @ParameterizedTest
    @MethodSource("testAsString")
    public void testAsString(Value value, String expected) {
        assertEquals(expected, value.stringValue());
    }
}
