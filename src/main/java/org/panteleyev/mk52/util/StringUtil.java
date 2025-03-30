/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.util;

public final class StringUtil {
    public static String pcToString(int pc) {
        return pc <= 99 ? String.format("%02d", pc) : String.format("A%1d", pc - 100);
    }

    private StringUtil() {
    }
}
