/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class RegisterTest {

    private static List<Arguments> testToStringArguments() {
        return List.of(
                // Ненормализованное число
                arguments(0x40206586025L, "-0.6586025  40"),
                arguments(0x7000000000L, " 00000000."),
                arguments(0x7000000006L, " 00000006."),
                // Нормализованное число
                arguments(0L, " 0."),
                arguments(0x31415926L, " 3.1415926"),
                arguments(0x931415926L, "-3.1415926"),
                arguments(0x911931415926L, "-3.1415926 -89"),
                arguments(0x911931415926L, "-3.1415926 -89"),
                arguments(0x1045230000L, " 45.23"),
                arguments(0x1945230000L, "-45.23"),
                arguments(0x1060000000L, " 60."),
                // Переполнение порядка
                arguments(0x123010000000L, " 1.        123")
        );
    }

    @ParameterizedTest
    @MethodSource("testToStringArguments")
    public void testToString(long register, String expected) {
        assertEquals(expected, Register.toString(register));
    }

    private static List<Arguments> testToDoubleArguments() {
        return List.of(
                // Ненормализованное число
                arguments(0x40206586025L, -6.586025e39),
                arguments(0x7000000006L, 6),
                // Нормализованное число
                arguments(0L, 0),
                arguments(0x31415926L, 3.1415926),
                arguments(0x931415926L, -3.1415926),
                arguments(0x911931415926L, -3.1415926e-89),
                arguments(0x911931415926L, -3.1415926e-89),
                arguments(0x1045230000L, 45.23),
                arguments(0x1945230000L, -45.23)
        );
    }

    @ParameterizedTest
    @MethodSource("testToDoubleArguments")
    public void testToDouble(long register, double expected) {
        assertEquals(expected, Register.toDouble(register));
    }

    private static List<Arguments> testNegateArguments() {
        return List.of(
                arguments(0x931415926L, 0x31415926L),
                arguments(0x31415926L, 0x931415926L),
                arguments(0x911931415926L, 0x911031415926L),
                arguments(0x911031415926L, 0x911931415926L)
        );
    }

    @ParameterizedTest
    @MethodSource("testNegateArguments")
    public void testNegate(long register, long expected) {
        assertEquals(expected, Register.negate(register));
    }

    private static List<Arguments> testSetExponentArguments() {
        return List.of(
                arguments(0x31415926L, 56, 0x56031415926L),
                arguments(0x31415926L, -56, 0x944031415926L),
                arguments(0x10000000L, 123, 0x123010000000L),
                arguments(0x10000000L, -123, 0)
        );
    }

    @ParameterizedTest
    @MethodSource("testSetExponentArguments")
    public void testSetExponent(long register, int exponent, long expected) {
        assertEquals(expected, Register.setExponent(register, exponent));
    }

    private static List<Arguments> testGetExponentArguments() {
        return List.of(
                arguments(0x56031415926L, 56),
                arguments(0x944031415926L, -56),
                arguments(0x123010000000L, 123)
        );
    }

    @ParameterizedTest
    @MethodSource("testGetExponentArguments")
    public void testGetExponent(long register, int expected) {
        assertEquals(expected, Register.getExponent(register));
    }

    private static List<Arguments> testOfDoubleArguments() {
        return List.of(
                arguments(3.1415926, 0x31415926L),
                arguments(-3.1415926e-87, 0x913931415926L),
                arguments(3.1415926e87, 0x87031415926L),
                arguments(3.1415926e-87, 0x913031415926L),
                arguments(123, 0x2012300000L)
        );
    }

    @ParameterizedTest
    @MethodSource("testOfDoubleArguments")
    public void testOfDouble(double x, long expected) {
        assertEquals(expected, Register.valueOf(x));
    }

    private static List<Arguments> testConvertForIndirectArguments() {
        return List.of(
                // 1.0 ⩽ X
                argumentSet("01", 1.2345678, 0x007000000001L),
                argumentSet("02", 12.345678, 0x007000000012L),
                argumentSet("03", 123.45678, 0x007000000123L),
                argumentSet("04", 1234.5678, 0x007000001234L),
                argumentSet("05", 12345.678, 0x007000012345L),
                argumentSet("06", 123456.78, 0x007000123456L),
                argumentSet("07", 1234567.8, 0x007001234567L),
                argumentSet("08", 12345678., 0x007012345678L),
                argumentSet("09", 1.2345678e8, 0x008012345678L),
                argumentSet("10", 1.2345678e9, 0x009012345678L),
                argumentSet("11", 1.2345678e10, 0x017000000001L),
                argumentSet("12", 1.2345678e11, 0x017000000012L),
                // 0.0 ⩽ X < 1.0
                argumentSet("13", 0, 0x007000000000L),
                argumentSet("14", 1.2345678e-1, 0x999012345678L),
                argumentSet("15", 1.2345678e-2, 0x998012345678L),
                argumentSet("16", 1.2345678e-3, 0x997012345678L),
                argumentSet("17", 1.2345678e-4, 0x997001234567L),
                argumentSet("18", 1.2345678e-5, 0x997000123456L),
                argumentSet("19", 1.2345678e-6, 0x997000012345L),
                argumentSet("20", 1.2345678e-7, 0x997000001234L),
                argumentSet("21", 1.2345678e-8, 0x997000000123L),
                argumentSet("22", 1.2345678e-9, 0x997000000012L),
                argumentSet("23", 1.2345678e-10, 0x997000000001L),
                argumentSet("24", 1.2345678e-11, 0x989012345678L),
                argumentSet("25", 1.2345678e-12, 0x988012345678L),
                // X < 0.0
                argumentSet("26", -123, 0x007999999123L),
                argumentSet("27", -1.23e-2, 0x998912300000L),
                argumentSet("28", -1.2345678e-8, 0x997999999123L)
        );
    }

    @ParameterizedTest
    @MethodSource("testConvertForIndirectArguments")
    public void testConvertForIndirect(double doubleValue, long expected) {
        var value = Register.valueOf(doubleValue);
        value = Register.convertForIndirect(value);
        assertEquals(expected, value);
    }

    private static List<Arguments> testDecrementMantissaArguments() {
        return List.of(
                // 1.0 ⩽ X
                argumentSet("01", 1.2345678, 0x007000000000L),
                argumentSet("02", 12.345678, 0x007000000011L),
                argumentSet("03", 123.45678, 0x007000000122L),
                argumentSet("04", 1234.5678, 0x007000001233L),
                argumentSet("05", 12345.678, 0x007000012344L),
                argumentSet("06", 123456.78, 0x007000123455L),
                argumentSet("07", 1234567.8, 0x007001234566L),
                argumentSet("08", 12345678., 0x007012345677L),
                argumentSet("09", 1.2345678e8, 0x008012345677L),
                argumentSet("10", 1.2345678e9, 0x009012345677L),
                argumentSet("11", 1.2345678e10, 0x017000000000L),
                argumentSet("12", 1.2345678e11, 0x017000000011L),
                // 0.0 ⩽ X < 1.0
                argumentSet("13", 1.2345678e-1, 0x999012345677L),
                argumentSet("14", 1.2345678e-2, 0x998012345677L),
                argumentSet("15", 1.2345678e-3, 0x997012345677L),
                argumentSet("16", 1.2345678e-4, 0x997001234566L),
                argumentSet("17", 1.2345678e-5, 0x997000123455L),
                argumentSet("18", 1.2345678e-6, 0x997000012344L),
                argumentSet("19", 1.2345678e-7, 0x997000001233L),
                argumentSet("20", 1.2345678e-8, 0x997000000122L),
                argumentSet("21", 1.2345678e-9, 0x997000000011L),
                argumentSet("22", 1.2345678e-10, 0x997000000000L),
                argumentSet("23", 1.2345678e-11, 0x989012345677L),
                argumentSet("24", 1.2345678e-12, 0x988012345677L),
                argumentSet("25", 0, 0x007999999999L),
                // X < 0.0
                argumentSet("26", -123, 0x007999999122L),
                argumentSet("27", -1.23e-2, 0x998912299999L),
                argumentSet("28", -1.2345678e-8, 0x997999999122L)
        );
    }

    @ParameterizedTest
    @MethodSource("testDecrementMantissaArguments")
    public void testDecrementMantissa(double doubleValue, long expected) {
        var value = Register.valueOf(doubleValue);
        value = Register.convertForIndirect(value);
        value = Register.decrementMantissa(value);
        assertEquals(expected, value);
    }

    private static List<Arguments> testIncrementMantissaArguments() {
        return List.of(
                // 1.0 ⩽ X
                argumentSet("01", 1.2345678, 0x007000000002L),
                argumentSet("02", 12.345678, 0x007000000013L),
                argumentSet("03", 123.45678, 0x007000000124L),
                argumentSet("04", 1234.5678, 0x007000001235L),
                argumentSet("05", 12345.678, 0x007000012346L),
                argumentSet("06", 123456.78, 0x007000123457L),
                argumentSet("07", 1234567.8, 0x007001234568L),
                argumentSet("08", 12345678., 0x007012345679L),
                argumentSet("09", 1.2345678e8, 0x008012345679L),
                argumentSet("10", 1.2345678e9, 0x009012345679L),
                argumentSet("11", 1.2345678e10, 0x017000000002L),
                argumentSet("12", 1.2345678e11, 0x017000000013L),
                // 0.0 ⩽ X < 1.0
                argumentSet("13", 1.2345678e-1, 0x999012345679L),
                argumentSet("14", 1.2345678e-2, 0x998012345679L),
                argumentSet("15", 1.2345678e-3, 0x997012345679L),
                argumentSet("16", 1.2345678e-4, 0x997001234568L),
                argumentSet("17", 1.2345678e-5, 0x997000123457L),
                argumentSet("18", 1.2345678e-6, 0x997000012346L),
                argumentSet("19", 1.2345678e-7, 0x997000001235L),
                argumentSet("20", 1.2345678e-8, 0x997000000124L),
                argumentSet("21", 1.2345678e-9, 0x997000000013L),
                argumentSet("22", 1.2345678e-10, 0x997000000002L),
                argumentSet("23", 1.2345678e-11, 0x989012345679L),
                argumentSet("24", 1.2345678e-12, 0x988012345679L),
                argumentSet("25", 99999999, 0x007100000000L),
                // X < 0.0
                argumentSet("26", -123, 0x007999999124L),
                argumentSet("27", -1.23e-2, 0x998912300001L),
                argumentSet("28", -1.2345678e-8, 0x997999999124L)
        );
    }

    @ParameterizedTest
    @MethodSource("testIncrementMantissaArguments")
    public void testIncrementMantissa(double doubleValue, long expected) {
        var value = Register.valueOf(doubleValue);
        value = Register.convertForIndirect(value);
        value = Register.incrementMantissa(value);
        assertEquals(expected, value);
    }

    private static List<Arguments> testXToIndicatorArguments() {
        return List.of(
                arguments(0x007012345678L, new IR(0xFFFF12345678L, 1)),
                arguments(0x000031415926L, new IR(0xFFFF31415926L, 0b10000000)),
                arguments(0x000931415926L, new IR(0xFFFA31415926L, 0b10000000)),
                arguments(0x000010012300L, new IR(0xFFFF100123FFL, 0b10000000)),
                //
                arguments(0x999031830989L, new IR(0xA01F31830989L, 0b10000000)),
                arguments(0x030012345678L, new IR(0xF30F12345678L, 0b10000000)),
                // Адрес
                arguments(0x007000000001L, new IR(0xFFFF00000001L, 1)),
                // Ненормализованное число
                arguments(0x40206586025L, new IR(0xF40A06586025L, 1 << 7))
        );
    }

    @ParameterizedTest
    @MethodSource("testXToIndicatorArguments")
    public void testXToIndicator(long x, IR expected) {
        assertEquals(expected, Register.xToIndicator(x));
    }
}
