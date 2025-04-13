/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.math;

import org.panteleyev.mk52.engine.Register;
import org.panteleyev.mk52.engine.TrigonometricMode;

import java.util.Random;

import static org.panteleyev.mk52.engine.Register.isNegative;
import static org.panteleyev.mk52.engine.Register.isZero;
import static org.panteleyev.mk52.engine.Register.toDouble;
import static org.panteleyev.mk52.engine.Register.valueOf;

public final class Mk52Math {
    private static final Random RANDOM = new Random(System.currentTimeMillis());

    public static final long SIXTY = 0x1060000000L;
    private static final long DEGREES_TO_RADIANS = 0x998017453292L;
    private static final long RADIANS_TO_DEGREES = 0x001057295779L;

    private static void checkResult(double x) {
        if (Double.isNaN(x) || Double.isInfinite(x)) {
            throw new ArithmeticException();
        }
    }

    public static long add(long x, long y) {
        var doubleValue = toDouble(x) + toDouble(y);
        checkResult(doubleValue);
        return valueOf(doubleValue);
    }

    public static long subtract(long x, long y) {
        var doubleValue = toDouble(y) - toDouble(x);
        checkResult(doubleValue);
        return valueOf(doubleValue);
    }

    public static long multiply(long x, long y) {
        var doubleValue = toDouble(y) * toDouble(x);
        checkResult(doubleValue);
        return valueOf(doubleValue);
    }

    public static long divide(long x, long y) {
        var doubleValue = toDouble(y) / toDouble(x);
        checkResult(doubleValue);
        return valueOf(doubleValue);
    }

    public static long sqrt(long x) {
        if (Register.isNegative(x)) {
            throw new ArithmeticException();
        }
        var doubleValue = Math.sqrt(toDouble(x));
        checkResult(doubleValue);
        return valueOf(doubleValue);
    }

    public static long sqr(long x) {
        var doubleValue = toDouble(x) * toDouble(x);
        checkResult(doubleValue);
        return valueOf(doubleValue);
    }

    public static long oneByX(long x) {
        if (isZero(x)) {
            throw new ArithmeticException();
        }

        return valueOf(1.0 / toDouble(x));
    }

    public static long lg(long x) {
        var doubleValue = Math.log10(toDouble(x));
        checkResult(doubleValue);
        return valueOf(doubleValue);
    }

    public static long ln(long x) {
        var doubleValue = Math.log(toDouble(x));
        checkResult(doubleValue);
        return valueOf(doubleValue);
    }

    public static long pow10(long x) {
        var doubleValue = Math.pow(10.0, toDouble(x));
        checkResult(doubleValue);
        return valueOf(doubleValue);
    }

    public static long exp(long x) {
        var doubleValue = Math.exp(toDouble(x));
        checkResult(doubleValue);
        return valueOf(doubleValue);
    }

    public static long pow(long x, long y) {
        if (isNegative(x)) {
            throw new ArithmeticException();
        }
        var doubleValue = Math.pow(toDouble(x), toDouble(y));
        return valueOf(doubleValue);
    }

    public static long abs(long x) {
        return Register.abs(x);
    }

    public static long integer(long x) {
        if (x == 0) {
            return 0;
        }

        var exp = Register.getExponent(x);
        if (exp < 0) {
            return 0;
        } else if (exp >= 7) {
            return x;
        } else {
            for (int i = 0; i < 7 - exp; i++) {
                x = Register.clearMantissaDigit(x, i);
            }
            return x;
        }
    }

    public static long fractional(long x) {
        if (x == 0) {
            return 0;
        }

        var exp = Register.getExponent(x);
        if (exp < 0) {
            return x;
        } else if (exp >= 7) {
            return 0;
        } else {
            return Register.shiftLeft(x, exp + 1);
        }
    }

    public static long max(long x, long y) {
        if (Register.isZero(x) || Register.isZero(y)) {
            // Известный дефект
            return 0;
        } else {
            return Register.valueOf(Math.max(Register.toDouble(x), Register.toDouble(y)));
        }
    }

    public static long signum(long x) {
        if (Register.isZero(x)) {
            return 0;
        } else if (Register.isNegative(x)) {
            return Register.MINUS_ONE;
        } else {
            return Register.ONE;
        }
    }

    public static long rand() {
        return Register.valueOf(RANDOM.nextDouble());
    }

    // Тригонометрия

    public static long sin(long x, TrigonometricMode mode) {
        var doubleValue = Math.sin(Register.toDouble(toRadian(x, mode)));
        checkResult(doubleValue);
        return Register.valueOf(doubleValue);
    }

    public static long asin(long x, TrigonometricMode mode) {
        var doubleValue = fromRadian(Math.asin(Register.toDouble(x)), mode);
        checkResult(doubleValue);
        return Register.valueOf(doubleValue);
    }

    public static long cos(long x, TrigonometricMode mode) {
        var doubleValue = Math.cos(Register.toDouble(toRadian(x, mode)));
        checkResult(doubleValue);
        return Register.valueOf(doubleValue);
    }

    public static long acos(long x, TrigonometricMode mode) {
        var doubleValue = fromRadian(Math.acos(Register.toDouble(x)), mode);
        checkResult(doubleValue);
        return Register.valueOf(doubleValue);
    }

    public static long tan(long x, TrigonometricMode mode) {
        var doubleValue = Math.tan(Register.toDouble(toRadian(x, mode)));
        checkResult(doubleValue);
        return Register.valueOf(doubleValue);
    }

    public static long atan(long x, TrigonometricMode mode) {
        var doubleValue = fromRadian(Math.atan(Register.toDouble(x)), mode);
        checkResult(doubleValue);
        return Register.valueOf(doubleValue);
    }

    // Логические операции

    public static long inversion(long x) {
        return Register.toLogical(~x);
    }

    public static long and(long x, long y) {
        return Register.toLogical(x & y);
    }

    public static long or(long x, long y) {
        return Register.toLogical(x | y);
    }

    public static long xor(long x, long y) {
        return Register.toLogical(x ^ y);
    }

    // Угловые операции

    public static long hoursMinutesToDegrees(long x) {
        return add(integer(x), Register.modifyExponent(divide(SIXTY, fractional(x)), 2));
    }

    public static long hoursMinutesSecondsToDegrees(long x) {
        var frac100 = Register.modifyExponent(fractional(x), 2);
        return add(integer(x), divide(SIXTY,
                add(integer(frac100), divide(0x60000000, Register.modifyExponent(fractional(frac100), 1)))));
    }

    public static long degreesToHoursMinutes(long x) {
        var xInt = integer(x);
        var conv = multiply(fractional(x), 0x999060000000L);
        if (Register.isZero(xInt)) {
            return conv;
        } else {
            return add(xInt, conv);
        }
    }

    public static long degreesToHoursMinutesSeconds(long x) {
        var minutes = multiply(fractional(x), SIXTY);
        var seconds = multiply(fractional(minutes), SIXTY);

        return add(add(add(integer(x), Register.modifyExponent(integer(minutes), -2)),
                Register.modifyExponent(integer(seconds), -4)), Register.modifyExponent(fractional(seconds), -4));
    }

    private static long toRadian(long x, TrigonometricMode mode) {
        return switch (mode) {
            case RADIAN -> x;
            case DEGREE -> multiply(x, DEGREES_TO_RADIANS);
            case GRADIAN -> multiply(x, 0x998015707963L);
        };
    }

    private static double fromRadian(double x, TrigonometricMode mode) {
        return switch (mode) {
            case RADIAN -> x;
            case DEGREE -> Math.toDegrees(x);
            case GRADIAN -> 200.0 * x / Math.PI;
        };
    }
}
