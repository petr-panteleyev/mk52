/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.panteleyev.mk52.value.DecimalValue;
import org.panteleyev.mk52.value.LogicalValue;
import org.panteleyev.mk52.value.Value;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.panteleyev.mk52.value.DecimalValue.ValueMode.ADDRESS;

@SuppressWarnings("ALL")
public class ValueTest {

    private static List<Arguments> testAsString() {
        return List.of(
                Arguments.of(new DecimalValue(Double.NaN), "EDDOD"),
                Arguments.of(new DecimalValue(1.0 / 0.0), "EDDOD"),
                Arguments.of(DecimalValue.ZERO, " 0."),
                Arguments.of(new DecimalValue(0), " 0."),
                Arguments.of(DecimalValue.PI, " 3.1415926"),
                Arguments.of(new DecimalValue(1234567.902345678), " 1234567.9"),
                Arguments.of(new DecimalValue(12345678.90234), " 12345679."),
                Arguments.of(new DecimalValue(12345678.40234), " 12345678."),
                Arguments.of(new DecimalValue(-1.234567890234), "-1.2345679"),
                Arguments.of(new DecimalValue(-123), "-123."),
                Arguments.of(new DecimalValue(-1234), "-1234."),
                Arguments.of(new DecimalValue(-12345), "-12345."),
                Arguments.of(new DecimalValue(-123456), "-123456."),
                Arguments.of(new DecimalValue(-1234567), "-1234567."),
                Arguments.of(new DecimalValue(-12345678), "-12345678."),
                Arguments.of(new DecimalValue(-1.2345678), "-1.2345678"),
                Arguments.of(new DecimalValue(-12.345678), "-12.345678"),
                Arguments.of(new DecimalValue(-123.45678), "-123.45678"),
                Arguments.of(new DecimalValue(-1234.5678), "-1234.5678"),
                Arguments.of(new DecimalValue(-12345.678), "-12345.678"),
                Arguments.of(new DecimalValue(-123456.78), "-123456.78"),
                Arguments.of(new DecimalValue(-1234567.8), "-1234567.8"),
                Arguments.of(new DecimalValue(-123456789012.01), "-1.2345679 11"),
                Arguments.of(new DecimalValue(123456789012.01), " 1.2345679 11"),
                Arguments.of(new DecimalValue(-123456789012123.0), "-1.2345679 14"),
                Arguments.of(new DecimalValue(123456789012123.0), " 1.2345679 14"),
                Arguments.of(new DecimalValue(0.0123), " 1.23     -02"),
                Arguments.of(new DecimalValue(0.123), " 1.23     -01"),
                Arguments.of(new DecimalValue(0.100000), " 1.       -01"),
                Arguments.of(new DecimalValue(0, ADDRESS), " 00000000."),
                Arguments.of(new DecimalValue(1, ADDRESS), " 00000001."),
                Arguments.of(new DecimalValue(12345678, ADDRESS), " 12345678."),
                Arguments.of(new DecimalValue(-12345678, ADDRESS), "-12345678."),
                arguments(new LogicalValue(123456), " 8.001E24")
        );
    }

    @ParameterizedTest
    @MethodSource("testAsString")
    public void testAsString(Value value, String expected) {
        assertEquals(expected, value.asString());
    }
}
