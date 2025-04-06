/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.eeprom;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class EepromUtilsTest {

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
