/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.math;

import org.panteleyev.mk52.engine.Value;

import static org.panteleyev.mk52.engine.Constants.EXPONENT_SIGN_POSITION;
import static org.panteleyev.mk52.engine.Constants.MANTISSA_POSITION;

public final class Converter {
    public static LogicalOperand toLogicalOperand(Value x) {
        var str = x.asString();
        var upperIndex = Math.min(str.length(), EXPONENT_SIGN_POSITION);
        // Берем мантиссу без знака и убираем точку и пробелы
        var mantissa = str.substring(MANTISSA_POSITION + 1, upperIndex)
                .replace(".", "")
                .replace(" ", "");
        // Интерпретируем как hex
        return new LogicalOperand(Integer.parseInt(mantissa, 16), mantissa.length());
    }
}
