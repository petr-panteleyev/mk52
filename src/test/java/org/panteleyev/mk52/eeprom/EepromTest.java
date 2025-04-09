/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.eeprom;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class EepromTest {

    private static List<Arguments> testValueToEepromAddressArguments() {
        return List.of(
                arguments(0x999012000000L, new EepromAddress(2000, 0)),     // TODO: уточнить для дробных
                arguments(0x10000120, new EepromAddress(0, 7)),
                arguments(0x10000150, new EepromAddress(0, 14)),
                arguments(0x10000140, new EepromAddress(0, 14))
        );
    }

    @ParameterizedTest
    @MethodSource("testValueToEepromAddressArguments")
    public void testValueToEepromAddress(long x, EepromAddress expected) {
        assertEquals(expected, Eeprom.valueToEepromAddress(x));
    }
}
