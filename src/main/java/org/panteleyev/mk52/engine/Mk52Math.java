/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.panteleyev.mk52.math.Converter;
import org.panteleyev.mk52.value.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Random;

import static org.panteleyev.mk52.engine.Constants.MANTISSA_SIZE;
import static org.panteleyev.mk52.engine.Constants.BYTE_0;

final class Mk52Math {
    private static final Random RANDOM = new Random(System.currentTimeMillis());
    private static final BigDecimal SIXTY = BigDecimal.valueOf(60);
    private static final BigDecimal D_3600 = BigDecimal.valueOf(3600);

    public static Value add(Value x, Value y) {
        return new Value(x.doubleValue() + y.doubleValue());
    }

    public static Value subtract(Value x, Value y) {
        return new Value(y.doubleValue() - x.doubleValue());
    }

    public static Value multiply(Value x, Value y) {
        return new Value(x.doubleValue() * y.doubleValue());
    }

    public static Value divide(Value x, Value y) {
        return new Value(y.doubleValue() / x.doubleValue());
    }

    public static Value sqrt(Value x) {
        return new Value(Math.sqrt(x.doubleValue()));
    }

    public static Value sqr(Value x) {
        return new Value(x.doubleValue() * x.doubleValue());
    }

    public static Value oneByX(Value x) {
        return new Value(1.0 / x.doubleValue());
    }

    public static Value lg(Value x) {
        return new Value(Math.log10(x.doubleValue()));
    }

    public static Value ln(Value x) {
        return new Value(Math.log(x.doubleValue()));
    }

    public static Value pow10(Value x) {
        return new Value(Math.pow(10.0, x.doubleValue()));
    }

    public static Value exp(Value x) {
        return new Value(Math.exp(x.doubleValue()));
    }

    public static Value pow(Value x, Value y) {
        if (x.doubleValue() < 0) {
            return new Value(Double.NaN);
        } else {
            return new Value(Math.pow(x.doubleValue(), y.doubleValue()));
        }
    }

    public static Value abs(Value x) {
        return new Value(Math.abs(x.doubleValue()));
    }

    public static Value integer(Value x) {
        return new Value((int) (x.doubleValue()));
    }

    public static Value fractional(Value x) {
        return new Value(x.doubleValue() - (int) x.doubleValue());
    }

    public static Value max(Value x, Value y) {
        if (x.doubleValue() == 0 || y.doubleValue() == 0) {
            // Известный дефект
            return Value.ZERO;
        } else {
            return new Value(Math.max(x.doubleValue(), y.doubleValue()));
        }
    }

    public static Value signum(Value x) {
        return new Value(Math.signum(x.doubleValue()));
    }

    public static Value rand() {
        return new Value(RANDOM.nextDouble());
    }

    // Тригонометрия

    public static Value sin(Value x, TrigonometricMode mode) {
        return new Value(Math.sin(toRadian(x.doubleValue(), mode)));
    }

    public static Value asin(Value x, TrigonometricMode mode) {
        return new Value(fromRadian(Math.asin(x.doubleValue()), mode));
    }

    public static Value cos(Value x, TrigonometricMode mode) {
        return new Value(Math.cos(toRadian(x.doubleValue(), mode)));
    }

    public static Value acos(Value x, TrigonometricMode mode) {
        return new Value(fromRadian(Math.acos(x.doubleValue()), mode));
    }

    public static Value tan(Value x, TrigonometricMode mode) {
        return new Value(Math.tan(toRadian(x.doubleValue(), mode)));
    }

    public static Value atan(Value x, TrigonometricMode mode) {
        return new Value(fromRadian(Math.atan(x.doubleValue()), mode));
    }

    // Логические операции

    private static Value toLogicalValue(byte[] bytes) {
        bytes[7] = 8;
        Arrays.fill(bytes, 8, bytes.length, BYTE_0);
        return new Value(bytes);
    }

    public static Value inversion(Value x) {
        var bytes = x.getBytes();
        for (var i = 0; i < 7; i++) {
            bytes[i] = (byte) (~bytes[i] & 0xF);
        }
        return toLogicalValue(bytes);
    }

    public static Value and(Value x, Value y) {
        var ax = x.getBytes();
        var ay = y.getBytes();
        for (var i = 0; i < 7; i++) {
            ax[i] = (byte) (ax[i] & ay[i]);
        }
        return toLogicalValue(ax);
    }

    public static Value or(Value x, Value y) {
        var ax = x.getBytes();
        var ay = y.getBytes();
        for (var i = 0; i < 7; i++) {
            ax[i] = (byte) (ax[i] | ay[i]);
        }
        return toLogicalValue(ax);
    }

    public static Value xor(Value x, Value y) {
        var ax = x.getBytes();
        var ay = y.getBytes();
        for (var i = 0; i < 7; i++) {
            ax[i] = (byte) (ax[i] ^ ay[i]);
        }
        return toLogicalValue(ax);
    }

    // Угловые операции

    public static Value hoursMinutesToDegrees(Value x) {
        var hhMM = Converter.toHoursMinutes(x);
        var signum = Math.signum(hhMM.hours());

        var result = toBigDecimal(hhMM.minutes())
                .divide(SIXTY, MANTISSA_SIZE, RoundingMode.FLOOR)
                .add(toBigDecimal(Math.abs(hhMM.hours())));

        if (signum < 0) {
            result = result.negate();
        }

        return new Value(downScale(result).doubleValue());
    }

    public static Value hoursMinutesSecondsToDegrees(Value x) {
        var hhMmSs = Converter.toHoursMinutesSeconds(x);
        var result = toBigDecimal(hhMmSs.hours())
                .add(toBigDecimal(hhMmSs.minutes()).divide(SIXTY, MANTISSA_SIZE, RoundingMode.FLOOR))
                .add(toBigDecimal(hhMmSs.seconds()).divide(D_3600, MANTISSA_SIZE, RoundingMode.FLOOR));

        return new Value(downScale(result).doubleValue());
    }

    public static Value degreesToHoursMinutes(Value x) {
        var hours = BigDecimal.valueOf(x.doubleValue());
        var fraction = hours.remainder(BigDecimal.ONE);

        var result = fraction.multiply(SIXTY)
                .stripTrailingZeros()
                .movePointLeft(2)
                .add(BigDecimal.valueOf(hours.intValue()));

        return new Value(downScale(result).doubleValue());
    }

    public static Value degreesToHoursMinutesSeconds(Value x) {
        var hours = BigDecimal.valueOf(x.doubleValue());
        var fraction = hours.remainder(BigDecimal.ONE);

        var minutes = fraction.multiply(SIXTY);

        var minuteFraction = minutes.remainder(BigDecimal.ONE);
        var seconds = minuteFraction.multiply(SIXTY).stripTrailingZeros();
        var secondsFraction = seconds.remainder(BigDecimal.ONE);

        var result = BigDecimal.valueOf(hours.intValue())
                .add(BigDecimal.valueOf(minutes.intValue()).movePointLeft(2))
                .add(BigDecimal.valueOf(seconds.intValue()).movePointLeft(4))
                .add(secondsFraction.movePointLeft(4));
        return new Value(downScale(result).doubleValue());
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
