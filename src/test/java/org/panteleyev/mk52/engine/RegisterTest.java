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
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class RegisterTest {

    private static List<Arguments> testToStringArguments() {
        return List.of(
                // Ненормализованное число
                arguments(0x40206586025L, "-0.6586025 40"),
                arguments(0x7000000000L, " 00000000."),
                arguments(0x7000000006L, " 00000006."),
                // Нормализованное число
                arguments(0L, " 0."),
                arguments(0x31415926L, " 3.1415926"),
                arguments(0x931415926L, "-3.1415926"),
                arguments(0x911931415926L, "-3.1415926-89"),
                arguments(0x911931415926L, "-3.1415926-89"),
                arguments(0x1045230000L, " 45.23"),
                arguments(0x1945230000L, "-45.23"),
                arguments(0x1060000000L, " 60.")
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
                arguments(0x31415926L, -56, 0x944031415926L)
        );
    }

    @ParameterizedTest
    @MethodSource("testSetExponentArguments")
    public void testSetExponent(long register, int exponent, long expected) {
        assertEquals(expected, Register.setExponent(register, exponent));
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
                arguments(1.2345678, " 00000001."),             // [1]
                arguments(12.345678, " 00000012."),             // [2]
                arguments(123.45678, " 00000123."),             // [3]
                arguments(1234.5678, " 00001234."),             // [4]
                arguments(12345.678, " 00012345."),             // [5]
                arguments(123456.78, " 00123456."),             // [6]
                arguments(1234567.8, " 01234567."),             // [7]
                arguments(12345678., " 12345678."),             // [8]
                arguments(1.2345678e8, " 1.2345678 08"),        // [9]
                arguments(1.2345678e9, " 1.2345678 09"),        // [10]
                arguments(1.2345678e10, " 0.0000001 17"),       // [11]
                arguments(1.2345678e11, " 0.0000012 17"),       // [12]
                // 0.0 ⩽ X < 1.0
                arguments(0, " 00000000."),                     // [13]
                arguments(1.2345678e-1, " 1.2345678-01"),       // [14]
                arguments(1.2345678e-2, " 1.2345678-02"),       // [15]
                arguments(1.2345678e-3, " 1.2345678-03"),       // [16]
                arguments(1.2345678e-4, " 0.1234567-03"),       // [17]
                arguments(1.2345678e-5, " 0.0123456-03"),       // [18]
                arguments(1.2345678e-6, " 0.0012345-03"),       // [19]
                arguments(1.2345678e-7, " 0.0001234-03"),       // [20]
                arguments(1.2345678e-8, " 0.0000123-03"),       // [21]
                arguments(1.2345678e-9, " 0.0000012-03"),       // [22]
                arguments(1.2345678e-10, " 0.0000001-03"),      // [23]
                arguments(1.2345678e-11, " 1.2345678-11"),      // [24]
                arguments(1.2345678e-12, " 1.2345678-12"),      // [25]
                // X < 0.0
                arguments(-123, "-99999123."),                  // [26]
                arguments(-1.23e-2, "-1.23     -02"),           // [27]
                arguments(-1.2345678e-8, "-9.9999123-03")       // [28]
        );
    }

    @ParameterizedTest
    @MethodSource("testConvertForIndirectArguments")
    public void testConvertForIndirect(double doubleValue, String expected) {
        var value = Register.valueOf(doubleValue);
        value = Register.convertForIndirect(value);
        assertEquals(expected, Register.toString(value));
    }

    private static List<Arguments> testDecrementMantissaArguments() {
        return List.of(
                // 1.0 ⩽ X
                arguments(1.2345678, " 00000000."),              // [1]
                arguments(12.345678, " 00000011."),              // [2]
                arguments(123.45678, " 00000122."),              // [3]
                arguments(1234.5678, " 00001233."),              // [4]
                arguments(12345.678, " 00012344."),              // [5]
                arguments(123456.78, " 00123455."),              // [6]
                arguments(1234567.8, " 01234566."),              // [7]
                arguments(12345678., " 12345677."),              // [8]
                arguments(1.2345678e8, " 1.2345677 08"),         // [9]
                arguments(1.2345678e9, " 1.2345677 09"),         // [10]
                arguments(1.2345678e10, " 0.        17"),        // [11]
                arguments(1.2345678e11, " 0.0000011 17"),        // [12]
                // 0.0 ⩽ X < 1.0
                arguments(1.2345678e-1, " 1.2345677-01"),        // [13]
                arguments(1.2345678e-2, " 1.2345677-02"),        // [14]
                arguments(1.2345678e-3, " 1.2345677-03"),        // [15]
                arguments(1.2345678e-4, " 0.1234566-03"),        // [16]
                arguments(1.2345678e-5, " 0.0123455-03"),        // [17]
                arguments(1.2345678e-6, " 0.0012344-03"),        // [18]
                arguments(1.2345678e-7, " 0.0001233-03"),        // [19]
                arguments(1.2345678e-8, " 0.0000122-03"),        // [20]
                arguments(1.2345678e-9, " 0.0000011-03"),        // [21]
                arguments(1.2345678e-10, " 0.       -03"),       // [22]
                arguments(1.2345678e-11, " 1.2345677-11"),       // [23]
                arguments(1.2345678e-12, " 1.2345677-12"),       // [24]
                arguments(0, "-99999999."),                      // [25]
                // X < 0.0
                arguments(-123, "-99999122."),                   // [26]
                arguments(-1.23e-2, "-1.2299999-02"),            // [27]
                arguments(-1.2345678e-8, "-9.9999122-03")        // [28]
        );
    }

    @ParameterizedTest
    @MethodSource("testDecrementMantissaArguments")
    public void testDecrementMantissa(double doubleValue, String expected) {
        var value = Register.valueOf(doubleValue);
        value = Register.convertForIndirect(value);
        value = Register.decrementMantissa(value);
        assertEquals(expected, Register.toString(value));
    }

    private static List<Arguments> testIncrementMantissaArguments() {
        return List.of(
                // 1.0 ⩽ X
                arguments(1.2345678, " 00000002."),             // [1]
                arguments(12.345678, " 00000013."),             // [2]
                arguments(123.45678, " 00000124."),             // [3]
                arguments(1234.5678, " 00001235."),             // [4]
                arguments(12345.678, " 00012346."),             // [5]
                arguments(123456.78, " 00123457."),             // [6]
                arguments(1234567.8, " 01234568."),             // [7]
                arguments(12345678., " 12345679."),             // [8]
                arguments(1.2345678e8, " 1.2345679 08"),        // [9]
                arguments(1.2345678e9, " 1.2345679 09"),        // [10]
                arguments(1.2345678e10, " 0.0000002 17"),       // [11]
                arguments(1.2345678e11, " 0.0000013 17"),       // [12]
                // 0.0 ⩽ X < 1.0
                arguments(1.2345678e-1, " 1.2345679-01"),       // [13]
                arguments(1.2345678e-2, " 1.2345679-02"),       // [14]
                arguments(1.2345678e-3, " 1.2345679-03"),       // [15]
                arguments(1.2345678e-4, " 0.1234568-03"),       // [16]
                arguments(1.2345678e-5, " 0.0123457-03"),       // [17]
                arguments(1.2345678e-6, " 0.0012346-03"),       // [18]
                arguments(1.2345678e-7, " 0.0001235-03"),       // [19]
                arguments(1.2345678e-8, " 0.0000124-03"),       // [20]
                arguments(1.2345678e-9, " 0.0000013-03"),       // [21]
                arguments(1.2345678e-10, " 0.0000002-03"),      // [22]
                arguments(1.2345678e-11, " 1.2345679-11"),      // [23]
                arguments(1.2345678e-12, " 1.2345679-12"),      // [24]
                arguments(99999999, "-00000000."),              // [25]
                // X < 0.0
                arguments(-123, "-99999124."),                  // [26]
                arguments(-1.23e-2, "-1.2300001-02"),           // [27]
                arguments(-1.2345678e-8, "-9.9999124-03")       // [28]
        );
    }

    @ParameterizedTest
    @MethodSource("testIncrementMantissaArguments")
    public void testIncrementMantissa(double doubleValue, String expected) {
        var value = Register.valueOf(doubleValue);
        value = Register.convertForIndirect(value);
        value = Register.incrementMantissa(value);
        assertEquals(expected, Register.toString(value));
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
