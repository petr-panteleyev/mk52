/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.program;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@DisplayName("Адрес")
public class AddressTest {
    private static final int[] ADDRESS_SPACE = new int[]{
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
            10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
            20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
            30, 31, 32, 33, 34, 35, 36, 37, 38, 39,
            40, 41, 42, 43, 44, 45, 46, 47, 48, 49,
            50, 51, 52, 53, 54, 55, 56, 57, 58, 59,
            60, 61, 62, 63, 64, 65, 66, 67, 68, 69,
            70, 71, 72, 73, 74, 75, 76, 77, 78, 79,
            80, 81, 82, 83, 84, 85, 86, 87, 88, 89,
            90, 91, 92, 93, 94, 95, 96, 97, 98, 99,
            100, 101, 102, 103, 104,
            // Короткая побочная ветвь
            0, 1, 2, 3, 4, 5, 6,
            // Длинная побочная ветвь
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9,
            10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
            20, 21, 22, 23, 24, 25, 26, 27, 28, 29,
            30, 31, 32, 33, 34, 35, 36, 37, 38, 39,
            40, 41, 42, 43, 44, 45, 46, 47
    };


    private static List<Arguments> testOfBytesArguments() {
        return List.of(
                arguments(1, 0, new Address(1, 0)),
                arguments(0xF, 9, new Address(5, 0xA)),
                arguments(5, 0xA, new Address(5, 0xA)),
                arguments(0xF, 0xF, new Address(0xF, 0xF))
        );
    }

    @ParameterizedTest
    @MethodSource("testOfBytesArguments")
    @DisplayName("Создание адреса из байтов")
    public void testOfBytes(int low, int high, Address expected) {
        assertEquals(expected, Address.of(low, high));
    }

    private static List<Arguments> testOfCodeArguments() {
        return List.of(
                arguments(0x12, new Address(2, 1)),
                arguments(0x9F, new Address(5, 0xA)),
                arguments(0xAC, new Address(2, 0xB))
        );
    }

    @ParameterizedTest
    @MethodSource("testOfCodeArguments")
    @DisplayName("Создание адреса из кода")
    public void testOfCode(int code, Address expected) {
        assertEquals(expected, Address.of(code));
    }

    private static List<Arguments> testIncrementArguments() {
        return List.of(
                arguments(new Address(0, 0), new Address(1, 0)),
                arguments(new Address(8, 9), new Address(9, 9)),
                arguments(new Address(9, 9), new Address(0, 0xA)),
                arguments(new Address(9, 0xA), new Address(0, 0xB)),
                arguments(new Address(9, 0xF), new Address(0, 0)),
                arguments(new Address(0xA, 0xF), new Address(1, 0)),
                arguments(new Address(0xB, 0xF), new Address(2, 0)),
                arguments(new Address(0xC, 0xF), new Address(3, 0)),
                arguments(new Address(0xD, 0xF), new Address(4, 0)),
                arguments(new Address(0xE, 0xF), new Address(5, 0)),
                arguments(new Address(0xF, 0xF), new Address(6, 0))
        );
    }

    @ParameterizedTest
    @MethodSource("testIncrementArguments")
    @DisplayName("Адрес + 1")
    public void testIncrement(Address addr, Address expected) {
        assertEquals(expected, addr.increment());
    }

    private static List<Arguments> testGetEffectiveAddressArguments() {
        return List.of(
                arguments(new Address(0, 0), 0),
                arguments(new Address(1, 0), 1),
                arguments(new Address(9, 9), 99),
                arguments(new Address(4, 0xA), 104),
                arguments(new Address(5, 0xA), 0),
                arguments(new Address(1, 0xB), 6),
                arguments(new Address(2, 0xB), 0),
                arguments(new Address(3, 0xB), 1),
                arguments(new Address(9, 0xE), 37),
                arguments(new Address(9, 0xF), 47),
                arguments(new Address(0xF, 0xF), 53)

        );
    }

    @ParameterizedTest
    @MethodSource("testGetEffectiveAddressArguments")
    @DisplayName("Эффективный адрес")
    public void testGetEffectiveAddress(Address addr, int expected) {
        assertEquals(expected, addr.getEffectiveAddress());
    }

    private static List<Arguments> testGetEffectiveRegisterArguments() {
        return List.of(
                // Старшая цифра 0
                arguments(new Address(0, 0), 0),
                arguments(new Address(1, 0), 1),
                arguments(new Address(2, 0), 2),
                arguments(new Address(3, 0), 3),
                arguments(new Address(4, 0), 4),
                arguments(new Address(5, 0), 5),
                arguments(new Address(6, 0), 6),
                arguments(new Address(7, 0), 7),
                arguments(new Address(8, 0), 8),
                arguments(new Address(9, 0), 9),
                arguments(new Address(0xA, 0), 10),
                arguments(new Address(0xB, 0), 11),
                arguments(new Address(0xC, 0), 12),
                arguments(new Address(0xD, 0), 13),
                arguments(new Address(0xE, 0), 14),
                arguments(new Address(0xF, 0), 0),
                // Старшая цифра не 0
                arguments(new Address(0, 1), 10),
                arguments(new Address(1, 1), 11),
                arguments(new Address(2, 2), 12),
                arguments(new Address(3, 3), 13),
                arguments(new Address(4, 4), 14),
                arguments(new Address(5, 5), 0),
                arguments(new Address(6, 6), 0),
                arguments(new Address(7, 7), 1),
                arguments(new Address(8, 8), 2),
                arguments(new Address(9, 9), 3),
                arguments(new Address(0xA, 0xA), 4),
                arguments(new Address(0xB, 0xB), 5),
                arguments(new Address(0xC, 0xC), 6),
                arguments(new Address(0xD, 0xD), 7),
                arguments(new Address(0xE, 0xE), 8),
                arguments(new Address(0xF, 0xF), 9)
        );
    }

    @ParameterizedTest
    @MethodSource("testGetEffectiveRegisterArguments")
    @DisplayName("Эффективный номер регистра")
    public void testGetEffectiveRegister(Address addr, int expected) {
        assertEquals(expected, addr.getEffectiveRegister());
    }

    @Test
    public void testBranchesForwards() {
        var address = Address.ZERO;

        for (int effective : ADDRESS_SPACE) {
            assertEquals(effective, address.getEffectiveAddress());
            address = address.increment();
        }

        assertEquals(0, address.getEffectiveAddress());
    }

    @Test
    public void testBranchesBackwards() {
        var address = new Address(9, 0xF);

        for (int i = ADDRESS_SPACE.length - 1; i >= 0; i--) {
            assertEquals(ADDRESS_SPACE[i], address.getEffectiveAddress());
            address = address.decrement();
        }

        assertEquals(47, address.getEffectiveAddress());
    }
}
