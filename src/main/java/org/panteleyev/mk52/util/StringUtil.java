/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.util;

import org.panteleyev.mk52.program.Address;

import static org.panteleyev.mk52.engine.Constants.DISPLAY_SIZE;

public final class StringUtil {
    public static String pcToString(int pc) {
        return pc <= 99 ? String.format("%02d", pc) : String.format("A%1d", pc - 100);
    }

    public static String pcToString(Address pc) {
        return String.format("%X%X", pc.high(), pc.low());
    }

    public static String padToDisplay(String s) {
        var padCount = DISPLAY_SIZE + 1 - s.length();
        return padCount > 0 ? s + " ".repeat(DISPLAY_SIZE + 1 - s.length()) : s;
    }

    private StringUtil() {
    }
}
