/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.math;

import org.panteleyev.mk52.engine.Register;

import java.text.DecimalFormat;

public final class Converter {
    private static final DecimalFormat FORMAT = new DecimalFormat("#0.00000000");

    public static HoursMinutes toHoursMinutes(long x) {
        var str = FORMAT.format(Register.toDouble(x)).replace(",", ".");

        var dot = str.indexOf(".");
        var hours = Integer.parseInt(str.substring(0, dot));

        var minuteStr = str.substring(dot + 1, dot + 3);
        var fractionStr = str.substring(dot + 3);
        var minutes = Double.parseDouble(minuteStr + "." + fractionStr);
        return new HoursMinutes(hours, minutes);
    }

    public static HoursMinutesSeconds toHoursMinutesSeconds(long x) {
        var str = FORMAT.format(Register.toDouble(x)).replace(",", ".");

        var dot = str.indexOf(".");
        var hours = Integer.parseInt(str.substring(0, dot));
        var minutes = Integer.parseInt(str.substring(dot + 1, dot + 3));

        var secondsStr = str.substring(dot + 3, dot + 5);
        var secondsFractionStr = str.substring(dot + 5);

        var seconds = Double.parseDouble(secondsStr + "." + secondsFractionStr);
        return new HoursMinutesSeconds(hours, minutes, seconds);
    }
}
