/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.program;

public final class Operations {
    public static final int STORE_BASE = 0x40;
    public static final int LOAD_BASE = 0x60;

    public static final int INDIRECT_STORE_BASE = 0xB0;
    public static final int INDIRECT_LOAD_BASE = 0xD0;

    public static final int INDIRECT_GOTO_BASE = 0x80;
    public static final int INDIRECT_GOSUB_BASE = 0xA0;

    public static final int GOTO_NE_0_BASE = 0x70;
    public static final int GOTO_GE_0_BASE = 0x90;
    public static final int GOTO_LT_0_BASE = 0xC0;
    public static final int GOTO_EQ_0_BASE = 0xE0;

    private Operations() {
    }
}
