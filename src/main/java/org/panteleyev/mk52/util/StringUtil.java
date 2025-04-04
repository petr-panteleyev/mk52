/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.util;

import static org.panteleyev.mk52.engine.Constants.DISPLAY_SIZE;

public final class StringUtil {
    public static String pcToString(int pc) {
        return pc <= 99 ? String.format("%02d", pc) : String.format("A%1d", pc - 100);
    }

    public static String padToDisplay(String s) {
        return s + " ".repeat(DISPLAY_SIZE - s.length());
    }

    public static void stripTrailingZeroes(StringBuilder sb, int delta) {
        for (int index = sb.length() - delta; index > 0; index--) {
            if (sb.charAt(index) == '0') {
                sb.setCharAt(index, ' ');
            } else {
                break;
            }
        }
    }

    private StringUtil() {
    }
}
