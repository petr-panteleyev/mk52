/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.math;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.panteleyev.mk52.engine.Value;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class ConverterTest {

    private static List<Arguments> testToLogicalOperandArguments() {
        return List.of(
                arguments(new Value(12345), new LogicalOperand(0x2345, 4)),
                arguments(new Value(1.2345), new LogicalOperand(0x2345, 4)),
                arguments(new Value(0.012345), new LogicalOperand(0x2345, 4)),
                arguments(new Value(0.123), new LogicalOperand(0x23, 2))
        );
    }

    @ParameterizedTest
    @MethodSource("testToLogicalOperandArguments")
    public void testToLogicalOperand(Value x, LogicalOperand expected) {
        assertEquals(expected, Converter.toLogicalOperand(x));
    }

    private static List<Arguments> testToHoursMinutesArguments() {
        return List.of(
                arguments(new Value(142.24314), new HoursMinutes(142, 24.314)),
                arguments(new Value(60.36), new HoursMinutes(60, 36)),
                arguments(new Value(60.4), new HoursMinutes(60, 40))
        );
    }

    @ParameterizedTest
    @MethodSource("testToHoursMinutesArguments")
    public void testToHoursMinutes(Value x, HoursMinutes expected) {
        assertEquals(expected, Converter.toHoursMinutes(x));
    }

    private static List<Arguments> testToHoursMinutesSecondsArguments() {
        return List.of(
                arguments(new Value(142.24314), new HoursMinutesSeconds(142, 24, 31.4)),
                arguments(new Value(60.36), new HoursMinutesSeconds(60, 36, 0)),
                arguments(new Value(60.4), new HoursMinutesSeconds(60, 40, 0)),
                arguments(new Value(60.401), new HoursMinutesSeconds(60, 40, 10)),
                arguments(new Value(60.4010), new HoursMinutesSeconds(60, 40, 10))
        );
    }

    @ParameterizedTest
    @MethodSource("testToHoursMinutesSecondsArguments")
    public void testToHoursMinutesSeconds(Value x, HoursMinutesSeconds expected) {
        assertEquals(expected, Converter.toHoursMinutesSeconds(x));
    }
}
