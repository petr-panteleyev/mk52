/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.math;

import org.panteleyev.mk52.value.Value;

public final class Converter {

    public static HoursMinutes toHoursMinutes(Value x) {
        var str = x.asString();
        var dot = str.indexOf(".");
        var hours = Integer.parseInt(str.substring(1, dot));

        var minutesBuilder = new StringBuilder("0");
        var minutesFractionBuilder = new StringBuilder();

        for (int i = dot + 1; i < str.length(); i++) {
            var ch = str.charAt(i);
            if (!Character.isDigit(ch)) {
                break;
            }
            if (minutesBuilder.length() < 3) {
                minutesBuilder.append(ch);
            } else {
                minutesFractionBuilder.append(ch);
            }
        }

        if (minutesBuilder.length() < 3) {
            minutesBuilder.append("0".repeat(3 - minutesBuilder.length()));
        }

        var minutes = Double.parseDouble(minutesBuilder + "." + minutesFractionBuilder);
        return new HoursMinutes(hours, minutes);
    }

    public static HoursMinutesSeconds toHoursMinutesSeconds(Value x) {
        var str = x.asString();
        var dot = str.indexOf(".");
        var hours = Integer.parseInt(str.substring(1, dot));

        var minutesBuilder = new StringBuilder("0");
        var secondsBuilder = new StringBuilder("0");
        var secondsFractionBuilder = new StringBuilder();

        for (int i = dot + 1; i < str.length(); i++) {
            var ch = str.charAt(i);
            if (!Character.isDigit(ch)) {
                break;
            }
            if (minutesBuilder.length() < 3) {
                minutesBuilder.append(ch);
            } else if (secondsBuilder.length() < 3) {
                secondsBuilder.append(ch);
            } else {
                secondsFractionBuilder.append(ch);
            }
        }

        if (minutesBuilder.length() < 3) {
            minutesBuilder.append("0".repeat(3 - minutesBuilder.length()));
        }
        if (secondsBuilder.length() < 3) {
            secondsBuilder.append("0".repeat(3 - secondsBuilder.length()));
        }

        var minutes = Integer.parseInt(minutesBuilder.toString());
        var seconds = Double.parseDouble(secondsBuilder + "." + secondsFractionBuilder);
        return new HoursMinutesSeconds(hours, minutes, seconds);
    }
}
