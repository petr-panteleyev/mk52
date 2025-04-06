/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.value;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class ValueUtilTest {

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
                arguments(1.2345678e-1, " 1.2345678-01"),       // [13]
                arguments(1.2345678e-2, " 1.2345678-02"),       // [14]
                arguments(1.2345678e-3, " 1.2345678-03"),       // [15]
                arguments(1.2345678e-4, " 0.1234567-03"),       // [16]
                arguments(1.2345678e-5, " 0.0123456-03"),       // [17]
                arguments(1.2345678e-6, " 0.0012345-03"),       // [18]
                arguments(1.2345678e-7, " 0.0001234-03"),       // [19]
                arguments(1.2345678e-8, " 0.0000123-03"),       // [20]
                arguments(1.2345678e-9, " 0.0000012-03"),       // [21]
                arguments(1.2345678e-10, " 0.0000001-03"),      // [22]
                arguments(1.2345678e-11, " 1.2345678-11"),      // [23]
                arguments(1.2345678e-12, " 1.2345678-12"),      // [24]
                // X < 0.0
                arguments(-123, "-99999123."),                  // [25]
                arguments(-1.23e-2, "-1.23     -02"),           // [26]
                arguments(-1.2345678e-8, "-9.9999123-03")       // [27]
        );
    }

    @ParameterizedTest
    @MethodSource("testConvertForIndirectArguments")
    public void testConvertForIndirect(double doubleValue, String expected) {
        var value = new Value(doubleValue);
        var bytes = value.getBytes();
        ValueUtil.convertForIndirect(bytes);
        assertEquals(expected, Value.stringFromBytes(bytes));
    }

    private static List<Arguments> testDecrementMantissaArguments() {
        return List.of(
                // 1.0 ⩽ X
                arguments(1.2345678, false, " 00000000."),              // [1]
                arguments(12.345678, false, " 00000011."),              // [2]
                arguments(123.45678, false, " 00000122."),              // [3]
                arguments(1234.5678, false, " 00001233."),              // [4]
                arguments(12345.678, false, " 00012344."),              // [5]
                arguments(123456.78, false, " 00123455."),              // [6]
                arguments(1234567.8, false, " 01234566."),              // [7]
                arguments(12345678., false, " 12345677."),              // [8]
                arguments(1.2345678e8, false, " 1.2345677 08"),         // [9]
                arguments(1.2345678e9, false, " 1.2345677 09"),         // [10]
                arguments(1.2345678e10, false, " 0.        17"),        // [11]
                arguments(1.2345678e11, false, " 0.0000011 17"),        // [12]
                // 0.0 ⩽ X < 1.0
                arguments(1.2345678e-1, false, " 1.2345677-01"),        // [13]
                arguments(1.2345678e-2, false, " 1.2345677-02"),        // [14]
                arguments(1.2345678e-3, false, " 1.2345677-03"),        // [15]
                arguments(1.2345678e-4, false, " 0.1234566-03"),        // [16]
                arguments(1.2345678e-5, false, " 0.0123455-03"),        // [17]
                arguments(1.2345678e-6, false, " 0.0012344-03"),        // [18]
                arguments(1.2345678e-7, false, " 0.0001233-03"),        // [19]
                arguments(1.2345678e-8, false, " 0.0000122-03"),        // [20]
                arguments(1.2345678e-9, false, " 0.0000011-03"),        // [21]
                arguments(1.2345678e-10, false, " 0.       -03"),       // [22]
                arguments(1.2345678e-11, false, " 1.2345677-11"),       // [23]
                arguments(1.2345678e-12, false, " 1.2345677-12"),       // [24]
                arguments(0, false, "-99999999."),                      // [25]
                // X < 0.0
                arguments(-123, false, "-99999122."),                   // [26]
                arguments(-1.23e-2, false, "-1.2299999-02"),            // [27]
                arguments(-1.2345678e-8, false, "-9.9999122-03")        // [28]
        );
    }

    @ParameterizedTest
    @MethodSource("testDecrementMantissaArguments")
    public void testDecrementMantissa(double doubleValue, boolean checkForOne, String expected) {
        var value = new Value(doubleValue);
        var bytes = value.getBytes();
        ValueUtil.convertForIndirect(bytes);
        ValueUtil.decrementMantissa(bytes);
        assertEquals(expected, Value.stringFromBytes(bytes));
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
        var value = new Value(doubleValue);
        var bytes = value.getBytes();
        ValueUtil.convertForIndirect(bytes);
        ValueUtil.incrementMantissa(bytes);
        assertEquals(expected, Value.stringFromBytes(bytes));
    }
}
