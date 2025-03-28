/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.panteleyev.mk52.math.Converter;

import java.util.Random;

final class Mk52Math {
    private static final Random RANDOM = new Random(System.currentTimeMillis());

    public static Value add(Value x, Value y) {
        return new Value(x.value() + y.value());
    }

    public static Value subtract(Value x, Value y) {
        return new Value(y.value() - x.value());
    }

    public static Value multiply(Value x, Value y) {
        return new Value(x.value() * y.value());
    }

    public static Value divide(Value x, Value y) {
        return new Value(y.value() / x.value());
    }

    public static Value negate(Value x) {
        return new Value(-x.value());
    }

    public static Value sqrt(Value x) {
        return new Value(Math.sqrt(x.value()));
    }

    public static Value sqr(Value x) {
        return new Value(x.value() * x.value());
    }

    public static Value oneByX(Value x) {
        return new Value(1.0 / x.value());
    }

    public static Value lg(Value x) {
        return new Value(Math.log10(x.value()));
    }

    public static Value ln(Value x) {
        return new Value(Math.log(x.value()));
    }

    public static Value pow10(Value x) {
        return new Value(Math.pow(10, x.value()));
    }

    public static Value exp(Value x) {
        return new Value(Math.exp(x.value()));
    }

    public static Value pow(Value x, Value y) {
        return new Value(Math.pow(x.value(), y.value()));
    }

    public static Value abs(Value x) {
        return new Value(Math.abs(x.value()));
    }

    public static Value integer(Value x) {
        return new Value(Math.ceil(x.value()));
    }

    public static Value fractional(Value x) {
        return new Value(x.value() - (int) x.value());
    }

    public static Value max(Value x, Value y) {
        return new Value(Math.max(x.value(), y.value()));
    }

    public static Value signum(Value x) {
        return new Value(Math.signum(x.value()));
    }

    public static Value rand() {
        return new Value(RANDOM.nextDouble());
    }

    // Тригонометрия

    public static Value sin(Value x, Engine.TrigonometricMode mode) {
        return new Value(Math.sin(toRadian(x.value(), mode)));
    }

    public static Value asin(Value x, Engine.TrigonometricMode mode) {
        return new Value(fromRadian(Math.asin(x.value()), mode));
    }

    public static Value cos(Value x, Engine.TrigonometricMode mode) {
        return new Value(Math.cos(toRadian(x.value(), mode)));
    }

    public static Value acos(Value x, Engine.TrigonometricMode mode) {
        return new Value(fromRadian(Math.acos(x.value()), mode));
    }

    public static Value tan(Value x, Engine.TrigonometricMode mode) {
        return new Value(Math.tan(toRadian(x.value(), mode)));
    }

    public static Value atan(Value x, Engine.TrigonometricMode mode) {
        return new Value(fromRadian(Math.atan(x.value()), mode));
    }

    // Логические операции

    public static Value inversion(Value x) {
        var operand = Converter.toLogicalOperand(x);
        var mask = 0xF;
        for (int i = 0; i < operand.length() - 1; i++) {
            mask = mask << 4 | 0xFF;
        }

        var result = ~operand.value() & mask;
        return new Value(result, Value.ValueMode.LOGICAL, operand.length());
    }

    public static Value and(Value x, Value y) {
        var lX = Converter.toLogicalOperand(x);
        var lY = Converter.toLogicalOperand(y);
        return new Value (lX.value() & lY.value(), Value.ValueMode.LOGICAL, Math.max(lX.length(), lY.length()));
    }

    public static Value or(Value x, Value y) {
        var lX = Converter.toLogicalOperand(x);
        var lY = Converter.toLogicalOperand(y);
        return new Value (lX.value() | lY.value(), Value.ValueMode.LOGICAL, Math.max(lX.length(), lY.length()));
    }

    public static Value xor(Value x, Value y) {
        var lX = Converter.toLogicalOperand(x);
        var lY = Converter.toLogicalOperand(y);
        return new Value (lX.value() ^ lY.value(), Value.ValueMode.LOGICAL, Math.max(lX.length(), lY.length()));
    }

    private static double toRadian(double x, Engine.TrigonometricMode mode) {
        return switch (mode) {
            case RADIAN -> x;
            case DEGREE -> Math.toRadians(x);
            case GRADIAN -> x * Math.PI / 200;
        };
    }

    private static double fromRadian(double x, Engine.TrigonometricMode mode) {
        return switch (mode) {
            case RADIAN -> x;
            case DEGREE -> Math.toDegrees(x);
            case GRADIAN -> 200.0 * x / Math.PI;
        };
    }
}
