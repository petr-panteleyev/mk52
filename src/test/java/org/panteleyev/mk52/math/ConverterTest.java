/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.math;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.panteleyev.mk52.value.DecimalValue;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class ConverterTest {
    private static List<Arguments> testToHoursMinutesArguments() {
        return List.of(
                arguments(new DecimalValue(142.24314), new HoursMinutes(142, 24.314)),
                arguments(new DecimalValue(60.36), new HoursMinutes(60, 36)),
                arguments(new DecimalValue(60.4), new HoursMinutes(60, 40))
        );
    }

    @ParameterizedTest
    @MethodSource("testToHoursMinutesArguments")
    public void testToHoursMinutes(DecimalValue x, HoursMinutes expected) {
        assertEquals(expected, Converter.toHoursMinutes(x));
    }

    private static List<Arguments> testToHoursMinutesSecondsArguments() {
        return List.of(
                arguments(new DecimalValue(142.24314), new HoursMinutesSeconds(142, 24, 31.4)),
                arguments(new DecimalValue(60.36), new HoursMinutesSeconds(60, 36, 0)),
                arguments(new DecimalValue(60.4), new HoursMinutesSeconds(60, 40, 0)),
                arguments(new DecimalValue(60.401), new HoursMinutesSeconds(60, 40, 10)),
                arguments(new DecimalValue(60.4010), new HoursMinutesSeconds(60, 40, 10))
        );
    }

    @ParameterizedTest
    @MethodSource("testToHoursMinutesSecondsArguments")
    public void testToHoursMinutesSeconds(DecimalValue x, HoursMinutesSeconds expected) {
        assertEquals(expected, Converter.toHoursMinutesSeconds(x));
    }
}
