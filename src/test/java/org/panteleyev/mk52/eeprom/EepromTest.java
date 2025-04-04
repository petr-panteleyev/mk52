/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.eeprom;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.panteleyev.mk52.value.DecimalValue;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class EepromTest {

    private static List<Arguments> testValueToEepromAddressArguments() {
        return List.of(
                arguments(new DecimalValue(0.12), new EepromAddress(0, 0)),
                arguments(new DecimalValue(1000012), new EepromAddress(0, 7)),
                arguments(new DecimalValue(1000015), new EepromAddress(0, 14)),
                arguments(new DecimalValue(1000014), new EepromAddress(0, 14))
        );
    }

    @ParameterizedTest
    @MethodSource("testValueToEepromAddressArguments")
    public void testValueToEepromAddress(DecimalValue value, EepromAddress expected) {
        assertEquals(expected, Eeprom.valueToEepromAddress(value));
    }
}
