/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.program;

import java.time.Duration;
import java.util.Arrays;

import static org.panteleyev.mk52.engine.Constants.BYTE_0;
import static org.panteleyev.mk52.engine.Constants.DUR_023;
import static org.panteleyev.mk52.engine.Constants.DUR_028;
import static org.panteleyev.mk52.engine.Constants.DUR_036;
import static org.panteleyev.mk52.engine.Constants.DUR_040;
import static org.panteleyev.mk52.engine.Constants.DUR_045;
import static org.panteleyev.mk52.engine.Constants.DUR_050;
import static org.panteleyev.mk52.engine.Constants.DUR_090;
import static org.panteleyev.mk52.engine.Constants.DUR_150;
import static org.panteleyev.mk52.engine.Constants.DUR_250;

public enum OpCode {
    // Коды эмулятора
    EMPTY(-1, Duration.ZERO),
    TO_PROGRAMMING_MODE(-2, Duration.ZERO),
    TO_EXECUTION_MODE(-3, Duration.ZERO),
    STEP_LEFT(-4, DUR_023),
    STEP_RIGHT(-5, DUR_023),
    // Недокументированные команды
    K_1(0x55, DUR_023),
    K_2(0x56, DUR_023),
    // Цифры
    ZERO(0x00, DUR_028), ONE(0x01, DUR_028), TWO(0x02, DUR_028), THREE(0x03, DUR_028), FOUR(0x04, DUR_028),
    FIVE(0x05, DUR_028), SIX(0x06, DUR_028), SEVEN(0x07, DUR_028), EIGHT(0x08, DUR_028), NINE(0x09, DUR_028),
    // Арифметика
    ADD(0x10, DUR_023),
    SUBTRACT(0x11, DUR_023),
    MULTIPLY(0x12, DUR_023),
    DIVIDE(0x13, DUR_023),
    // Стек
    SWAP(0x14, DUR_023), PUSH(0x0E, DUR_040), RESTORE_X(0x0F, DUR_040), ROTATE(0x25, DUR_023),
    //
    DOT(0x0A, DUR_028),
    SIGN(0x0B, DUR_028),
    ENTER_EXPONENT(0x0C, DUR_023),
    CLEAR_X(0x0D, DUR_028),
    STOP_RUN(0x50, DUR_023),
    GOTO(0x51, DUR_036, 2),
    RETURN(0x52, DUR_036),
    GOSUB(0x53, DUR_036, 2),
    POWER_OF_TEN(0x15, DUR_150),
    LG(0x17, DUR_150),
    LN(0x18, DUR_150),
    EXP(0x16, DUR_150),
    ASIN(0x19, DUR_150),
    ACOS(0x1A, DUR_150),
    ATAN(0x1B, DUR_150),
    SIN(0x1C, DUR_150),
    COS(0x1D, DUR_150),
    TAN(0x1E, DUR_150),
    PI(0x20, DUR_023),
    SQRT(0x21, DUR_040),
    SQR(0x22, DUR_023),
    ONE_BY_X(0x23, DUR_023),
    POWER_OF_X(0x24, DUR_250),
    // Условный переход
    X_LT_0(0x5C, DUR_040, 2), X_EQ_0(0x5E, DUR_040, 2), X_GE_0(0x59, DUR_040, 2), X_NE_0(0x57, DUR_040, 2),
    // Цикл
    L0(0x5D, DUR_040, 2), L1(0x5B, DUR_040, 2), L2(0x58, DUR_040, 2), L3(0x5A, DUR_040, 2),
    // Сохранение в регистр
    STORE_R0(0x40, DUR_023, true), STORE_R1(0x41, DUR_023, true), STORE_R2(0x42, DUR_023, true),
    STORE_R3(0x43, DUR_023, true), STORE_R4(0x44, DUR_023, true), STORE_R5(0x45, DUR_023, true),
    STORE_R6(0x46, DUR_023, true), STORE_R7(0x47, DUR_023, true), STORE_R8(0x48, DUR_023, true),
    STORE_R9(0x49, DUR_023, true), STORE_RA(0x4A, DUR_023, true), STORE_RB(0x4B, DUR_023, true),
    STORE_RC(0x4C, DUR_023, true), STORE_RD(0x4D, DUR_023, true), STORE_RE(0x4E, DUR_023, true),
    // Косвенное сохранение в регистр
    IND_STORE_R0(0xB0, DUR_036, true), IND_STORE_R1(0xB1, DUR_036, true), IND_STORE_R2(0xB2, DUR_036, true),
    IND_STORE_R3(0xB3, DUR_036, true), IND_STORE_R4(0xB4, DUR_036, true), IND_STORE_R5(0xB5, DUR_036, true),
    IND_STORE_R6(0xB6, DUR_036, true), IND_STORE_R7(0xB7, DUR_036, true), IND_STORE_R8(0xB8, DUR_036, true),
    IND_STORE_R9(0xB9, DUR_036, true), IND_STORE_RA(0xBA, DUR_036, true), IND_STORE_RB(0xBB, DUR_036, true),
    IND_STORE_RC(0xBC, DUR_036, true), IND_STORE_RD(0xBD, DUR_036, true), IND_STORE_RE(0xBE, DUR_036, true),
    // Загрузка из регистра
    LOAD_R0(0x60, DUR_040, true), LOAD_R1(0x61, DUR_040, true), LOAD_R2(0x62, DUR_040, true),
    LOAD_R3(0x63, DUR_040, true), LOAD_R4(0x64, DUR_040, true), LOAD_R5(0x65, DUR_040, true),
    LOAD_R6(0x66, DUR_040, true), LOAD_R7(0x67, DUR_040, true), LOAD_R8(0x68, DUR_040, true),
    LOAD_R9(0x69, DUR_040, true), LOAD_RA(0x6A, DUR_040, true), LOAD_RB(0x6B, DUR_040, true),
    LOAD_RC(0x6C, DUR_040, true), LOAD_RD(0x6D, DUR_040, true), LOAD_RE(0x6E, DUR_040, true),
    // Косвенная загрузка из регистра
    IND_LOAD_R0(0xD0, DUR_050, true), IND_LOAD_R1(0xD1, DUR_050, true), IND_LOAD_R2(0xD2, DUR_050, true),
    IND_LOAD_R3(0xD3, DUR_050, true), IND_LOAD_R4(0xD4, DUR_050, true), IND_LOAD_R5(0xD5, DUR_050, true),
    IND_LOAD_R6(0xD6, DUR_050, true), IND_LOAD_R7(0xD7, DUR_050, true), IND_LOAD_R8(0xD8, DUR_050, true),
    IND_LOAD_R9(0xD9, DUR_050, true), IND_LOAD_RA(0xDA, DUR_050, true), IND_LOAD_RB(0xDB, DUR_050, true),
    IND_LOAD_RC(0xDC, DUR_050, true), IND_LOAD_RD(0xDD, DUR_050, true), IND_LOAD_RE(0xDE, DUR_050, true),
    // Пустая операция
    NOOP(0x54, DUR_023),
    // Переход по адресу в регистре
    GOTO_R0(0x80, DUR_028, true), GOTO_R1(0x81, DUR_028, true), GOTO_R2(0x82, DUR_028, true),
    GOTO_R3(0x83, DUR_028, true), GOTO_R4(0x84, DUR_028, true), GOTO_R5(0x85, DUR_028, true),
    GOTO_R6(0x86, DUR_028, true), GOTO_R7(0x87, DUR_028, true), GOTO_R8(0x88, DUR_028, true),
    GOTO_R9(0x89, DUR_028, true), GOTO_RA(0x8A, DUR_028, true), GOTO_RB(0x8B, DUR_028, true),
    GOTO_RC(0x8C, DUR_028, true), GOTO_RD(0x8D, DUR_028, true), GOTO_RE(0x8E, DUR_028, true),
    // Вызов подпрограммы в адресу в регистре
    GOSUB_R0(0xA0, DUR_028, true), GOSUB_R1(0xA1, DUR_028, true), GOSUB_R2(0xA2, DUR_028, true),
    GOSUB_R3(0xA3, DUR_028, true), GOSUB_R4(0xA4, DUR_028, true), GOSUB_R5(0xA5, DUR_028, true),
    GOSUB_R6(0xA6, DUR_028, true), GOSUB_R7(0xA7, DUR_028, true), GOSUB_R8(0xA8, DUR_028, true),
    GOSUB_R9(0xA9, DUR_028, true), GOSUB_RA(0xAA, DUR_028, true), GOSUB_RB(0xAB, DUR_028, true),
    GOSUB_RC(0xAC, DUR_028, true), GOSUB_RD(0xAD, DUR_028, true), GOSUB_RE(0xAE, DUR_028, true),
    // Косвенный переход по условию x = 0
    GOTO_EQ_0_R0(0xE0, DUR_050, true), GOTO_EQ_0_R1(0xE1, DUR_050, true), GOTO_EQ_0_R2(0xE2, DUR_050, true),
    GOTO_EQ_0_R3(0xE3, DUR_050, true), GOTO_EQ_0_R4(0xE4, DUR_050, true), GOTO_EQ_0_R5(0xE5, DUR_050, true),
    GOTO_EQ_0_R6(0xE6, DUR_050, true), GOTO_EQ_0_R7(0xE7, DUR_050, true), GOTO_EQ_0_R8(0xE8, DUR_050, true),
    GOTO_EQ_0_R9(0xE9, DUR_050, true), GOTO_EQ_0_RA(0xEA, DUR_050, true), GOTO_EQ_0_RB(0xEB, DUR_050, true),
    GOTO_EQ_0_RC(0xEC, DUR_050, true), GOTO_EQ_0_RD(0xED, DUR_050, true), GOTO_EQ_0_RE(0xEE, DUR_050, true),
    // Косвенный переход по условию x < 0
    GOTO_LT_0_R0(0xC0, DUR_050, true), GOTO_LT_0_R1(0xC1, DUR_050, true), GOTO_LT_0_R2(0xC2, DUR_050, true),
    GOTO_LT_0_R3(0xC3, DUR_050, true), GOTO_LT_0_R4(0xC4, DUR_050, true), GOTO_LT_0_R5(0xC5, DUR_050, true),
    GOTO_LT_0_R6(0xC6, DUR_050, true), GOTO_LT_0_R7(0xC7, DUR_050, true), GOTO_LT_0_R8(0xC8, DUR_050, true),
    GOTO_LT_0_R9(0xC9, DUR_050, true), GOTO_LT_0_RA(0xCA, DUR_050, true), GOTO_LT_0_RB(0xCB, DUR_050, true),
    GOTO_LT_0_RC(0xCC, DUR_050, true), GOTO_LT_0_RD(0xCD, DUR_050, true), GOTO_LT_0_RE(0xCE, DUR_050, true),
    // Косвенный переход по условию x >= 0
    GOTO_GE_0_R0(0x90, DUR_050, true), GOTO_GE_0_R1(0x91, DUR_050, true), GOTO_GE_0_R2(0x92, DUR_050, true),
    GOTO_GE_0_R3(0x93, DUR_050, true), GOTO_GE_0_R4(0x94, DUR_050, true), GOTO_GE_0_R5(0x95, DUR_050, true),
    GOTO_GE_0_R6(0x96, DUR_050, true), GOTO_GE_0_R7(0x97, DUR_050, true), GOTO_GE_0_R8(0x98, DUR_050, true),
    GOTO_GE_0_R9(0x99, DUR_050, true), GOTO_GE_0_RA(0x9A, DUR_050, true), GOTO_GE_0_RB(0x9B, DUR_050, true),
    GOTO_GE_0_RC(0x9C, DUR_050, true), GOTO_GE_0_RD(0x9D, DUR_050, true), GOTO_GE_0_RE(0x9E, DUR_050, true),
    // Косвенный переход по условию x != 0
    GOTO_NE_0_R0(0x70, DUR_050, true), GOTO_NE_0_R1(0x71, DUR_050, true), GOTO_NE_0_R2(0x72, DUR_050, true),
    GOTO_NE_0_R3(0x73, DUR_050, true), GOTO_NE_0_R4(0x74, DUR_050, true), GOTO_NE_0_R5(0x75, DUR_050, true),
    GOTO_NE_0_R6(0x76, DUR_050, true), GOTO_NE_0_R7(0x77, DUR_050, true), GOTO_NE_0_R8(0x78, DUR_050, true),
    GOTO_NE_0_R9(0x79, DUR_050, true), GOTO_NE_0_RA(0x7A, DUR_050, true), GOTO_NE_0_RB(0x7B, DUR_050, true),
    GOTO_NE_0_RC(0x7C, DUR_050, true), GOTO_NE_0_RD(0x7D, DUR_050, true), GOTO_NE_0_RE(0x7E, DUR_050, true),
    //
    INTEGER(0x34, DUR_028),
    FRACTIONAL(0x35, DUR_028),
    MAX(0x36, DUR_023),
    ABS(0x31, DUR_023),
    SIGNUM(0x32, DUR_023),
    RANDOM(0x3B, DUR_045),
    // Угловые значения
    DEG_TO_HH_MM(0x33, DUR_023),
    HH_MM_TO_DEG(0x26, DUR_045),
    HH_MM_SS_TO_DEG(0x2A, DUR_090),
    DEG_TO_HH_MM_SS(0x30, DUR_036),
    // Логические операции
    AND(0x37, DUR_023),
    OR(0x38, DUR_023),
    XOR(0x39, DUR_023),
    INVERSION(0x3A, DUR_023);

    private final int code;
    private final Duration duration;
    private final boolean register;
    private final int size;

    OpCode(int code, Duration duration, int size, boolean register) {
        this.code = code;
        this.duration = duration;
        this.register = register;
        this.size = size;
    }

    OpCode(int code, Duration duration, int size) {
        this(code, duration, size, false);
    }

    OpCode(int code, Duration duration, boolean register) {
        this(code, duration, 1, register);
    }

    OpCode(int code, Duration duration) {
        this(code, duration, 1);
    }

    public int code() {
        return code;
    }

    public Duration duration() {
        return duration;
    }

    public boolean isRegister() {
        return register;
    }

    public boolean hasAddress() {
        return size == 2;
    }

    public boolean inRange(OpCode first, OpCode last) {
        return this.ordinal() >= first.ordinal() && this.ordinal() <= last.ordinal();
    }

    public static OpCode findByCode(int code) {
        return Arrays.stream(OpCode.values())
                .filter(e -> e.code == code)
                .findAny()
                .orElse(OpCode.EMPTY);
    }

    public static boolean isDigit(OpCode opCode) {
        return opCode != null && opCode.inRange(OpCode.ZERO, OpCode.NINE);
    }
}
