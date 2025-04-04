/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.panteleyev.mk52.math.Converter;
import org.panteleyev.mk52.value.DecimalValue;
import org.panteleyev.mk52.value.LogicalValue;
import org.panteleyev.mk52.value.Value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

import static org.panteleyev.mk52.engine.Constants.MANTISSA_SIZE;

final class Mk52Math {
    private static final Random RANDOM = new Random(System.currentTimeMillis());
    private static final BigDecimal SIXTY = BigDecimal.valueOf(60);
    private static final BigDecimal D_3600 = BigDecimal.valueOf(3600);

    public static Value add(Value x, Value y) {
        return new DecimalValue(x.toDecimal().value() + y.toDecimal().value());
    }

    public static Value subtract(Value x, Value y) {
        return new DecimalValue(y.toDecimal().value() - x.toDecimal().value());
    }

    public static Value multiply(Value x, Value y) {
        return new DecimalValue(x.toDecimal().value() * y.toDecimal().value());
    }

    public static Value divide(Value x, Value y) {
        return new DecimalValue(y.toDecimal().value() / x.toDecimal().value());
    }

    public static Value negate(Value x) {
        return new DecimalValue(-x.toDecimal().value());
    }

    public static Value sqrt(Value x) {
        return new DecimalValue(Math.sqrt(x.toDecimal().value()));
    }

    public static Value sqr(Value x) {
        return new DecimalValue(x.toDecimal().value() * x.toDecimal().value());
    }

    public static Value oneByX(Value x) {
        return new DecimalValue(1.0 / x.toDecimal().value());
    }

    public static Value lg(Value x) {
        return new DecimalValue(Math.log10(x.toDecimal().value()));
    }

    public static Value ln(Value x) {
        return new DecimalValue(Math.log(x.toDecimal().value()));
    }

    public static Value pow10(Value x) {
        return new DecimalValue(Math.pow(10.0, x.toDecimal().value()));
    }

    public static Value exp(Value x) {
        return new DecimalValue(Math.exp(x.toDecimal().value()));
    }

    public static Value pow(Value x, Value y) {
        if (x.toDecimal().value() < 0) {
            return new DecimalValue(Double.NaN);
        } else {
            return new DecimalValue(Math.pow(x.toDecimal().value(), y.toDecimal().value()));
        }
    }

    public static Value abs(Value x) {
        return new DecimalValue(Math.abs(x.toDecimal().value()));
    }

    public static Value integer(Value x) {
        return new DecimalValue((int) (x.toDecimal().value()));
    }

    public static Value fractional(Value x) {
        return new DecimalValue(x.toDecimal().value() - (int) x.toDecimal().value());
    }

    public static Value max(Value x, Value y) {
        if (x.toDecimal().value() == 0 || y.toDecimal().value() == 0) {
            // Известный дефект
            return DecimalValue.ZERO;
        } else {
            return new DecimalValue(Math.max(x.toDecimal().value(), y.toDecimal().value()));
        }
    }

    public static Value signum(Value x) {
        return new DecimalValue(Math.signum(x.toDecimal().value()));
    }

    public static Value rand() {
        return new DecimalValue(RANDOM.nextDouble());
    }

    // Тригонометрия

    public static Value sin(Value x, TrigonometricMode mode) {
        return new DecimalValue(Math.sin(toRadian(x.toDecimal().value(), mode)));
    }

    public static Value asin(Value x, TrigonometricMode mode) {
        return new DecimalValue(fromRadian(Math.asin(x.toDecimal().value()), mode));
    }

    public static Value cos(Value x, TrigonometricMode mode) {
        return new DecimalValue(Math.cos(toRadian(x.toDecimal().value(), mode)));
    }

    public static Value acos(Value x, TrigonometricMode mode) {
        return new DecimalValue(fromRadian(Math.acos(x.toDecimal().value()), mode));
    }

    public static Value tan(Value x, TrigonometricMode mode) {
        return new DecimalValue(Math.tan(toRadian(x.toDecimal().value(), mode)));
    }

    public static Value atan(Value x, TrigonometricMode mode) {
        return new DecimalValue(fromRadian(Math.atan(x.toDecimal().value()), mode));
    }

    // Логические операции

    private static Value toLogicalValue(byte[] bytes) {
        var value = 0;
        for (var i = 6; i >= 0; i--) {
            value = (value << 4) + (bytes[i] & 0xF);
        }
        return new LogicalValue(value);
    }

    public static Value inversion(Value x) {
        var arr = x.toByteArray();
        for (var i = 0; i < 7; i++) {
            arr[i] = (byte) (~arr[i] & 0xF);
        }
        return toLogicalValue(arr);
    }

    public static Value and(Value x, Value y) {
        var ax = x.toByteArray();
        var ay = y.toByteArray();
        for (var i = 0; i < 7; i++) {
            ax[i] = (byte) (ax[i] & ay[i]);
        }
        return toLogicalValue(ax);
    }

    public static Value or(Value x, Value y) {
        var ax = x.toByteArray();
        var ay = y.toByteArray();
        for (var i = 0; i < 7; i++) {
            ax[i] = (byte) (ax[i] | ay[i]);
        }
        return toLogicalValue(ax);
    }

    public static Value xor(Value x, Value y) {
        var ax = x.toByteArray();
        var ay = y.toByteArray();
        for (var i = 0; i < 7; i++) {
            ax[i] = (byte) (ax[i] ^ ay[i]);
        }
        return toLogicalValue(ax);
    }

    // Угловые операции

    public static Value hoursMinutesToDegrees(Value x) {
        var hhMM = Converter.toHoursMinutes(x);

        var result = toBigDecimal(hhMM.minutes())
                .divide(SIXTY, MANTISSA_SIZE, RoundingMode.FLOOR)
                .add(toBigDecimal(hhMM.hours()));

        return new DecimalValue(downScale(result).doubleValue());
    }

    public static Value hoursMinutesSecondsToDegrees(Value x) {
        var hhMmSs = Converter.toHoursMinutesSeconds(x);
        var result = toBigDecimal(hhMmSs.hours())
                .add(toBigDecimal(hhMmSs.minutes()).divide(SIXTY, MANTISSA_SIZE, RoundingMode.FLOOR))
                .add(toBigDecimal(hhMmSs.seconds()).divide(D_3600, MANTISSA_SIZE, RoundingMode.FLOOR));

        return new DecimalValue(downScale(result).doubleValue());
    }

    public static Value degreesToHoursMinutes(Value x) {
        var hours = BigDecimal.valueOf(x.toDecimal().value());
        var fraction = hours.remainder(BigDecimal.ONE);

        var result = fraction.multiply(SIXTY)
                .stripTrailingZeros()
                .movePointLeft(2)
                .add(BigDecimal.valueOf(hours.intValue()));

        return new DecimalValue(downScale(result).doubleValue());
    }

    public static Value degreesToHoursMinutesSeconds(Value x) {
        var hours = BigDecimal.valueOf(x.toDecimal().value());
        var fraction = hours.remainder(BigDecimal.ONE);

        var minutes = fraction.multiply(SIXTY);

        var minuteFraction = minutes.remainder(BigDecimal.ONE);
        var seconds = minuteFraction.multiply(SIXTY).stripTrailingZeros();
        var secondsFraction = seconds.remainder(BigDecimal.ONE);

        var result = BigDecimal.valueOf(hours.intValue())
                .add(BigDecimal.valueOf(minutes.intValue()).movePointLeft(2))
                .add(BigDecimal.valueOf(seconds.intValue()).movePointLeft(4))
                .add(secondsFraction.movePointLeft(4));
        return new DecimalValue(downScale(result).doubleValue());
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
