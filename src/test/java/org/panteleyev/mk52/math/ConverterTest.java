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
}
