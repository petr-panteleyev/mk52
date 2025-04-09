/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.math;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class ConverterTest {
    private static List<Arguments> testToHoursMinutesArguments() {
        return List.of(
                arguments(0x2014224314L, new HoursMinutes(142, 24.314)),
                arguments(0x1060360000L, new HoursMinutes(60, 36)),
                arguments(0x1060400000L, new HoursMinutes(60, 40))
        );
    }

    @ParameterizedTest
    @MethodSource("testToHoursMinutesArguments")
    public void testToHoursMinutes(long x, HoursMinutes expected) {
        assertEquals(expected, Converter.toHoursMinutes(x));
    }

    private static List<Arguments> testToHoursMinutesSecondsArguments() {
        return List.of(
                arguments(0x2014224314L, new HoursMinutesSeconds(142, 24, 31.4)),
                arguments(0x1060360000L, new HoursMinutesSeconds(60, 36, 0)),
                arguments(0x1060400000L, new HoursMinutesSeconds(60, 40, 0)),
                arguments(0x1060401000L, new HoursMinutesSeconds(60, 40, 10))
        );
    }

    @ParameterizedTest
    @MethodSource("testToHoursMinutesSecondsArguments")
    public void testToHoursMinutesSeconds(long x, HoursMinutesSeconds expected) {
        assertEquals(expected, Converter.toHoursMinutesSeconds(x));
    }
}
