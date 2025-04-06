/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public final class Constants {
    public static final double MAX_VALUE = 9.9999999e99;

    public static final int DISPLAY_SIZE = 13;
    public static final String EMPTY_DISPLAY = " ".repeat(DISPLAY_SIZE);
    public static final String INITIAL_DISPLAY = " 0." + " ".repeat(DISPLAY_SIZE - 3);
    public static final String ERROR_DISPLAY = " EDDOD";

    public static final byte BYTE_0 = (byte) 0;
    public static final byte BYTE_1 = (byte) 1;
    public static final byte BYTE_2 = (byte) 2;
    public static final byte BYTE_3 = (byte) 3;
    public static final byte BYTE_4 = (byte) 4;
    public static final byte BYTE_5 = (byte) 5;
    public static final byte BYTE_6 = (byte) 6;
    public static final byte BYTE_7 = (byte) 7;
    public static final byte BYTE_8 = (byte) 8;
    public static final byte BYTE_9 = (byte) 9;
    public static final byte BYTE_A = (byte) 0xA;
    public static final byte BYTE_B = (byte) 0xB;
    public static final byte BYTE_C = (byte) 0xC;
    public static final byte BYTE_D = (byte) 0xD;
    public static final byte BYTE_E = (byte) 0xE;
    public static final byte BYTE_F = (byte) 0xF;
    public static final byte BYTE_F0 = (byte) 0xF0;

    public static final String EMPTY_STRING = "";

    public static final int MANTISSA_SIZE = 8;

    public static final int MANTISSA_SIGN_POSITION = 0;
    public static final int MANTISSA_POSITION = 1;
    public static final int EXPONENT_SIGN_POSITION = 10;
    public static final int EXPONENT_POSITION = 11;

    public static final int PROGRAM_MEMORY_SIZE = 105;
    public static final int CALL_STACK_SIZE = 5;
    public static final int REGISTERS_SIZE = 15;
    // Количество тетрад на представление регистра: 12 + 2
    public static final int TETRADS_PER_REGISTER = 14;

    // Размер в ячейках, одна ячейка - 4 бита
    public static final int EEPROM_SIZE = 1024;

    // Время исполнения команд
    public static final Duration DUR_023 = Duration.of(230, ChronoUnit.MILLIS);
    public static final Duration DUR_028 = Duration.of(280, ChronoUnit.MILLIS);
    public static final Duration DUR_036 = Duration.of(360, ChronoUnit.MILLIS);
    public static final Duration DUR_040 = Duration.of(400, ChronoUnit.MILLIS);
    public static final Duration DUR_045 = Duration.of(450, ChronoUnit.MILLIS);
    public static final Duration DUR_050 = Duration.of(500, ChronoUnit.MILLIS);
    public static final Duration DUR_090 = Duration.of(900, ChronoUnit.MILLIS);
    public static final Duration DUR_150 = Duration.of(1500, ChronoUnit.MILLIS);
    public static final Duration DUR_250 = Duration.of(2500, ChronoUnit.MILLIS);

    public static final Duration TURN_OFF_DISPLAY_DELAY = Duration.of(20, ChronoUnit.MILLIS);
    public static final Duration STORE_CODE_DURATION = DUR_023;

    private Constants() {
    }
}
