/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.eeprom;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.panteleyev.mk52.value.DecimalValue;
import org.panteleyev.mk52.value.Value;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class EepromUtilsTest {

    private static List<Arguments> testValueToEepromLineArguments() {
        return List.of(
                arguments(new DecimalValue(3.1415926),
                        new byte[]{0x6, 0x2, 0x9, 0x5, 0x1, 0x4, 0x1, 0x3, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0}),
                arguments(new DecimalValue(-3.1415926e-87),
                        new byte[]{0x6, 0x2, 0x9, 0x5, 0x1, 0x4, 0x1, 0x3, 0x9, 0x3, 0x1, 0x9, 0x0, 0x0}),
                arguments(new DecimalValue(3.1415926e87),
                        new byte[]{0x6, 0x2, 0x9, 0x5, 0x1, 0x4, 0x1, 0x3, 0x0, 0x7, 0x8, 0x0, 0x0, 0x0}),
                arguments(new DecimalValue(3.1415926e-87),
                        new byte[]{0x6, 0x2, 0x9, 0x5, 0x1, 0x4, 0x1, 0x3, 0x0, 0x3, 0x1, 0x9, 0x0, 0x0}),
                arguments(new DecimalValue(123),
                        new byte[]{0x0, 0x0, 0x0, 0x0, 0x0, 0x3, 0x2, 0x1, 0x0, 0x2, 0x0, 0x0, 0x0, 0x0})
        );
    }

    @ParameterizedTest
    @MethodSource("testValueToEepromLineArguments")
    public void testValueToEepromLine(Value value, byte[] expected) {
        var line = value.toByteArray();
        assertArrayEquals(expected, line);
    }

    private static List<Arguments> testValueFromEepromLineArguments() {
        return List.of(
                arguments(new byte[]{0x6, 0x2, 0x9, 0x5, 0x1, 0x4, 0x1, 0x3, 0x0, 0x0, 0x0, 0x0, 0x0, 0x0},
                        new DecimalValue(3.1415926)),
                arguments(new byte[]{0x6, 0x2, 0x9, 0x5, 0x1, 0x4, 0x1, 0x3, 0x9, 0x3, 0x1, 0x9, 0x0, 0x0},
                        new DecimalValue(-3.1415926e-87)),
                arguments(new byte[]{0x6, 0x2, 0x9, 0x5, 0x1, 0x4, 0x1, 0x3, 0x0, 0x7, 0x8, 0x0, 0x0, 0x0},
                        new DecimalValue(3.1415926e87)),
                arguments(new byte[]{0x6, 0x2, 0x9, 0x5, 0x1, 0x4, 0x1, 0x3, 0x0, 0x3, 0x1, 0x9, 0x0, 0x0},
                        new DecimalValue(3.1415926e-87)),
                arguments(new byte[]{0x0, 0x0, 0x0, 0x0, 0x0, 0x3, 0x2, 0x1, 0x0, 0x2, 0x0, 0x0, 0x0, 0x0},
                        new DecimalValue(123))
        );
    }

    @ParameterizedTest
    @MethodSource("testValueFromEepromLineArguments")
    public void testValueFromEepromLine(byte[] line, DecimalValue expected) {
        assertEquals(expected, EepromUtils.valueFromEepromLine(line));
    }

    private static List<Arguments> testWriteEepromLineArguments() {
        return List.of(
                arguments(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0xA, 0xB, 0xC, 0xD}, EepromMode.PROGRAM,
                        new byte[]{2, 3, 4, 5, 6, 7, 8, 9, 0xA, 0xB, 0xC, 0xD, 0, 1}),
                arguments(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0xA, 0xB, 0xC, 0xD}, EepromMode.DATA,
                        new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0xA, 0xB, 0xC, 0xD})
        );
    }

    @ParameterizedTest
    @MethodSource("testWriteEepromLineArguments")
    public void testWriteEepromLine(byte[] line, EepromMode mode, byte[] expected) {
        var eeprom = new byte[14];
        EepromUtils.writeEepromLine(eeprom, 0, line, mode);
        assertArrayEquals(expected, eeprom);
    }

    private static List<Arguments> testReadEepromLineArguments() {
        return List.of(
                arguments(new byte[]{2, 3, 4, 5, 6, 7, 8, 9, 0xA, 0xB, 0xC, 0xD, 0, 1}, EepromMode.PROGRAM,
                        new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0xA, 0xB, 0xC, 0xD}),
                arguments(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0xA, 0xB, 0xC, 0xD}, EepromMode.DATA,
                        new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0xA, 0xB, 0xC, 0xD})
        );
    }

    @ParameterizedTest
    @MethodSource("testReadEepromLineArguments")
    public void testReadEepromLine(byte[] eeprom, EepromMode mode, byte[] expected) {
        var actual = EepromUtils.readEepromLine(eeprom, 0, mode);
        assertArrayEquals(expected, actual);
    }
}
