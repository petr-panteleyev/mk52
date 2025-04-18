/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

public record IR(long indicator, int dots) {
    public static final IR ZERO = new IR(0xFFFF_0FFF_FFFFL, 0b10000000);
    public static final IR ONE = new IR(0xFFFF_1FFF_FFFFL, 0b10000000);
    public static final IR TWO = new IR(0xFFFF_2FFF_FFFFL, 0b10000000);

    public static final IR EMPTY = new IR(0xFFFF_FFFF_FFFFL);
    public static final IR INITIAL = new IR(0xFFFF_0FFF_FFFFL, 0b10000000);
    public static final IR ERROR = new IR(0xFFFF_EDD0_DFFFL);

    public static final IR PI = new IR(0xFFFF_3_1415926L, 1 << 7);

    public IR(long indicator) {
        this(indicator, 0);
    }

    @Override
    public String toString() {
        return String.format("IR[indicator=%X, dots=%d]", indicator, dots);
    }
}
