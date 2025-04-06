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
import static org.panteleyev.mk52.engine.Constants.BYTE_0;
import static org.panteleyev.mk52.engine.Constants.BYTE_1;
import static org.panteleyev.mk52.engine.Constants.BYTE_2;
import static org.panteleyev.mk52.engine.Constants.BYTE_3;
import static org.panteleyev.mk52.engine.Constants.BYTE_4;
import static org.panteleyev.mk52.engine.Constants.BYTE_5;
import static org.panteleyev.mk52.engine.Constants.BYTE_6;
import static org.panteleyev.mk52.engine.Constants.BYTE_7;
import static org.panteleyev.mk52.engine.Constants.BYTE_8;
import static org.panteleyev.mk52.engine.Constants.BYTE_9;
import static org.panteleyev.mk52.engine.Constants.BYTE_A;
import static org.panteleyev.mk52.engine.Constants.BYTE_B;
import static org.panteleyev.mk52.engine.Constants.BYTE_C;
import static org.panteleyev.mk52.engine.Constants.BYTE_D;
import static org.panteleyev.mk52.engine.Constants.BYTE_E;
import static org.panteleyev.mk52.engine.Constants.BYTE_F;

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
                arguments(new byte[]{1, 0}, new Address(BYTE_1, BYTE_0)),
                arguments(new byte[]{0xF, 9}, new Address(BYTE_5, BYTE_A)),
                arguments(new byte[]{5, 0xA}, new Address(BYTE_5, BYTE_A)),
                arguments(new byte[]{0xF, 0xF}, new Address(BYTE_F, BYTE_F))
        );
    }

    @ParameterizedTest
    @MethodSource("testOfBytesArguments")
    @DisplayName("Создание адреса из байтов")
    public void testOfBytes(byte[] bytes, Address expected) {
        assertEquals(expected, Address.of(bytes));
    }

    private static List<Arguments> testOfCodeArguments() {
        return List.of(
                arguments(0x12, new Address(BYTE_2, BYTE_1)),
                arguments(0x9F, new Address(BYTE_5, BYTE_A)),
                arguments(0xAC, new Address(BYTE_2, BYTE_B))
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
                arguments(new Address(BYTE_0, BYTE_0), new Address(BYTE_1, BYTE_0)),
                arguments(new Address(BYTE_8, BYTE_9), new Address(BYTE_9, BYTE_9)),
                arguments(new Address(BYTE_9, BYTE_9), new Address(BYTE_0, BYTE_A)),
                arguments(new Address(BYTE_9, BYTE_A), new Address(BYTE_0, BYTE_B)),
                arguments(new Address(BYTE_9, BYTE_F), new Address(BYTE_0, BYTE_0)),
                arguments(new Address(BYTE_A, BYTE_F), new Address(BYTE_1, BYTE_0)),
                arguments(new Address(BYTE_B, BYTE_F), new Address(BYTE_2, BYTE_0)),
                arguments(new Address(BYTE_C, BYTE_F), new Address(BYTE_3, BYTE_0)),
                arguments(new Address(BYTE_D, BYTE_F), new Address(BYTE_4, BYTE_0)),
                arguments(new Address(BYTE_E, BYTE_F), new Address(BYTE_5, BYTE_0)),
                arguments(new Address(BYTE_F, BYTE_F), new Address(BYTE_6, BYTE_0))
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
                arguments(new Address(BYTE_0, BYTE_0), 0),
                arguments(new Address(BYTE_1, BYTE_0), 1),
                arguments(new Address(BYTE_9, BYTE_9), 99),
                arguments(new Address(BYTE_4, BYTE_A), 104),
                arguments(new Address(BYTE_5, BYTE_A), 0),
                arguments(new Address(BYTE_1, BYTE_B), 6),
                arguments(new Address(BYTE_2, BYTE_B), 0),
                arguments(new Address(BYTE_3, BYTE_B), 1),
                arguments(new Address(BYTE_9, BYTE_E), 37),
                arguments(new Address(BYTE_9, BYTE_F), 47),
                arguments(new Address(BYTE_F, BYTE_F), 53)

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
                arguments(new Address(BYTE_0, BYTE_0), 0),
                arguments(new Address(BYTE_1, BYTE_0), 1),
                arguments(new Address(BYTE_2, BYTE_0), 2),
                arguments(new Address(BYTE_3, BYTE_0), 3),
                arguments(new Address(BYTE_4, BYTE_0), 4),
                arguments(new Address(BYTE_5, BYTE_0), 5),
                arguments(new Address(BYTE_6, BYTE_0), 6),
                arguments(new Address(BYTE_7, BYTE_0), 7),
                arguments(new Address(BYTE_8, BYTE_0), 8),
                arguments(new Address(BYTE_9, BYTE_0), 9),
                arguments(new Address(BYTE_A, BYTE_0), 10),
                arguments(new Address(BYTE_B, BYTE_0), 11),
                arguments(new Address(BYTE_C, BYTE_0), 12),
                arguments(new Address(BYTE_D, BYTE_0), 13),
                arguments(new Address(BYTE_E, BYTE_0), 14),
                arguments(new Address(BYTE_F, BYTE_0), 0),
                // Старшая цифра не 0
                arguments(new Address(BYTE_0, BYTE_1), 10),
                arguments(new Address(BYTE_1, BYTE_1), 11),
                arguments(new Address(BYTE_2, BYTE_2), 12),
                arguments(new Address(BYTE_3, BYTE_3), 13),
                arguments(new Address(BYTE_4, BYTE_4), 14),
                arguments(new Address(BYTE_5, BYTE_5), 0),
                arguments(new Address(BYTE_6, BYTE_6), 0),
                arguments(new Address(BYTE_7, BYTE_7), 1),
                arguments(new Address(BYTE_8, BYTE_8), 2),
                arguments(new Address(BYTE_9, BYTE_9), 3),
                arguments(new Address(BYTE_A, BYTE_A), 4),
                arguments(new Address(BYTE_B, BYTE_B), 5),
                arguments(new Address(BYTE_C, BYTE_C), 6),
                arguments(new Address(BYTE_D, BYTE_D), 7),
                arguments(new Address(BYTE_E, BYTE_E), 8),
                arguments(new Address(BYTE_F, BYTE_F), 9)
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
        var address = new Address(BYTE_9, BYTE_F);

        for (int i = ADDRESS_SPACE.length - 1; i >= 0; i--) {
            assertEquals(ADDRESS_SPACE[i], address.getEffectiveAddress());
            address = address.decrement();
        }

        assertEquals(47, address.getEffectiveAddress());
    }
}
