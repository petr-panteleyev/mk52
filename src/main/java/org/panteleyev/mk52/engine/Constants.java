/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

public final class Constants {
    public static final int DISPLAY_SIZE = 13;
    public static final String EMPTY_DISPLAY = " ".repeat(DISPLAY_SIZE);

    public static final int MANTISSA_SIZE = 8;

    public static final int MANTISSA_SIGN_POSITION = 0;
    public static final int MANTISSA_POSITION = 1;
    public static final int EXPONENT_SIGN_POSITION = 10;
    public static final int EXPONENT_POSITION = 11;

    public static final int PROGRAM_MEMORY_SIZE = 105;

    private Constants() {
    }
}
