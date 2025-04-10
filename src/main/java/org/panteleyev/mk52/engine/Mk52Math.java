/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.panteleyev.mk52.math.Converter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

import static org.panteleyev.mk52.engine.Constants.MANTISSA_SIZE;
import static org.panteleyev.mk52.engine.Register.isNegative;
import static org.panteleyev.mk52.engine.Register.isZero;
import static org.panteleyev.mk52.engine.Register.toDouble;
import static org.panteleyev.mk52.engine.Register.valueOf;

final class Mk52Math {
    private static final Random RANDOM = new Random(System.currentTimeMillis());
    private static final BigDecimal SIXTY = BigDecimal.valueOf(60);
    private static final BigDecimal D_3600 = BigDecimal.valueOf(3600);

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

    // TODO: переделать
    public static long integer(long x) {
        return Register.valueOf((int) Register.toDouble(x));
    }

    // TODO: переделать
    public static long fractional(long x) {
        return Register.valueOf(Register.toDouble(x) - (int) Register.toDouble(x));
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
        var doubleValue = Math.sin(toRadian(Register.toDouble(x), mode));
        checkResult(doubleValue);
        return Register.valueOf(doubleValue);
    }

    public static long asin(long x, TrigonometricMode mode) {
        var doubleValue = fromRadian(Math.asin(Register.toDouble(x)), mode);
        checkResult(doubleValue);
        return Register.valueOf(doubleValue);
    }

    public static long cos(long x, TrigonometricMode mode) {
        var doubleValue = Math.cos(toRadian(Register.toDouble(x), mode));
        checkResult(doubleValue);
        return Register.valueOf(doubleValue);
    }

    public static long acos(long x, TrigonometricMode mode) {
        var doubleValue = fromRadian(Math.acos(Register.toDouble(x)), mode);
        checkResult(doubleValue);
        return Register.valueOf(doubleValue);
    }

    public static long tan(long x, TrigonometricMode mode) {
        var doubleValue = Math.tan(toRadian(Register.toDouble(x), mode));
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
        var hhMM = Converter.toHoursMinutes(x);
        var signum = Math.signum(hhMM.hours());

        var result = toBigDecimal(hhMM.minutes())
                .divide(SIXTY, MANTISSA_SIZE, RoundingMode.FLOOR)
                .add(toBigDecimal(Math.abs(hhMM.hours())));

        if (signum < 0) {
            result = result.negate();
        }

        return Register.valueOf(downScale(result).doubleValue());
    }

    public static long hoursMinutesSecondsToDegrees(long x) {
        var hhMmSs = Converter.toHoursMinutesSeconds(x);
        var result = toBigDecimal(hhMmSs.hours())
                .add(toBigDecimal(hhMmSs.minutes()).divide(SIXTY, MANTISSA_SIZE, RoundingMode.FLOOR))
                .add(toBigDecimal(hhMmSs.seconds()).divide(D_3600, MANTISSA_SIZE, RoundingMode.FLOOR));

        return Register.valueOf(downScale(result).doubleValue());
    }

    public static long degreesToHoursMinutes(long x) {
        var hours = BigDecimal.valueOf(Register.toDouble(x));
        var fraction = hours.remainder(BigDecimal.ONE);

        var result = fraction.multiply(SIXTY)
                .stripTrailingZeros()
                .movePointLeft(2)
                .add(BigDecimal.valueOf(hours.intValue()));

        return Register.valueOf(downScale(result).doubleValue());
    }

    public static long degreesToHoursMinutesSeconds(long x) {
        var hours = BigDecimal.valueOf(Register.toDouble(x));
        var fraction = hours.remainder(BigDecimal.ONE);

        var minutes = fraction.multiply(SIXTY);

        var minuteFraction = minutes.remainder(BigDecimal.ONE);
        var seconds = minuteFraction.multiply(SIXTY).stripTrailingZeros();
        var secondsFraction = seconds.remainder(BigDecimal.ONE);

        var result = BigDecimal.valueOf(hours.intValue())
                .add(BigDecimal.valueOf(minutes.intValue()).movePointLeft(2))
                .add(BigDecimal.valueOf(seconds.intValue()).movePointLeft(4))
                .add(secondsFraction.movePointLeft(4));
        return Register.valueOf(downScale(result).doubleValue());
    }

    private static double toRadian(double x, TrigonometricMode mode) {
        return switch (mode) {
            case RADIAN -> x;
            case DEGREE -> Math.toRadians(x);
            case GRADIAN -> x * Math.PI / 200;
        };
    }

    private static double fromRadian(double x, TrigonometricMode mode) {
        return switch (mode) {
            case RADIAN -> x;
            case DEGREE -> Math.toDegrees(x);
            case GRADIAN -> 200.0 * x / Math.PI;
        };
    }

    private static BigDecimal toBigDecimal(int x) {
        return BigDecimal.valueOf(x);
    }

    private static BigDecimal toBigDecimal(double x) {
        return BigDecimal.valueOf(x);
    }

    private static BigDecimal downScale(BigDecimal x) {
        var extraPrecision = x.precision() - MANTISSA_SIZE;
        if (extraPrecision > 0) {
            return x.setScale(x.scale() - extraPrecision, RoundingMode.FLOOR);
        } else {
            return x;
        }
    }
}
