/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import java.util.Arrays;

public enum OpCode {
    // Коды эмулятора
    EMPTY(-1),
    TO_PROGRAMMING_MODE(-2),
    TO_EXECUTION_MODE(-2),
    // Недокументированные команды
    K_1(0x55),
    K_2(0x56),
    // Цифры
    ZERO(0x00), ONE(0x01), TWO(0x02), THREE(0x03), FOUR(0x04),
    FIVE(0x05), SIX(0x06), SEVEN(0x07), EIGHT(0x08), NINE(0x09),
    // Арифметика
    ADD(0x10), SUBTRACT(0x11), MULTIPLY(0x12), DIVIDE(0x13),
    // Стек
    SWAP(0x14), PUSH(0x0E), RESTORE_X(0x0F), ROTATE(0x25),
    //
    DOT(0x0A),
    SIGN(0x0B),
    ENTER_EXPONENT(0x0C),
    CLEAR_X(0x0D),
    STOP_RUN(0x50),
    GOTO(0x51, 2),
    RETURN(0x52),
    GOSUB(0x53, 2),
    POWER_OF_TEN(0x15),
    LG(0x17),
    LN(0x18),
    EXP(0x16),
    ASIN(0x19),
    ACOS(0x1A),
    ATAN(0x1B),
    SIN(0x1C),
    COS(0x1D),
    TAN(0x1E),
    PI(0x20),
    SQRT(0x21),
    SQR(0x22),
    ONE_BY_X(0x23),
    POWER_OF_X(0x24),
    // Условный переход
    X_LT_0(0x5C, 2), X_EQ_0(0x5E, 2), X_GE_0(0x59, 2), X_NE_0(0x57, 2),
    // Цикл
    L0(0x5D, 2), L1(0x5B, 2), L2(0x58, 2), L3(0x5A, 2),
    // Сохранение в регистр
    STORE_R0(0x40, true), STORE_R1(0x41, true), STORE_R2(0x42, true), STORE_R3(0x43, true), STORE_R4(0x44, true),
    STORE_R5(0x45, true), STORE_R6(0x46, true), STORE_R7(0x47, true), STORE_R8(0x48, true), STORE_R9(0x49, true),
    STORE_RA(0x4A, true), STORE_RB(0x4B, true), STORE_RC(0x4C, true), STORE_RD(0x4D, true), STORE_RE(0x4E, true),
    // Косвенное сохранение в регистр
    IND_STORE_R0(0xB0, true), IND_STORE_R1(0xB1, true), IND_STORE_R2(0xB2, true), IND_STORE_R3(0xB3, true),
    IND_STORE_R4(0xB4, true), IND_STORE_R5(0xB5, true), IND_STORE_R6(0xB6, true), IND_STORE_R7(0xB7, true),
    IND_STORE_R8(0xB8, true), IND_STORE_R9(0xB9, true), IND_STORE_RA(0xBA, true), IND_STORE_RB(0xBB, true),
    IND_STORE_RC(0xBC, true), IND_STORE_RD(0xBD, true), IND_STORE_RE(0xBE, true),
    // Загрузка из регистра
    LOAD_R0(0x60, true), LOAD_R1(0x61, true), LOAD_R2(0x62, true), LOAD_R3(0x63, true), LOAD_R4(0x64, true),
    LOAD_R5(0x65, true), LOAD_R6(0x66, true), LOAD_R7(0x67, true), LOAD_R8(0x68, true), LOAD_R9(0x69, true),
    LOAD_RA(0x6A, true), LOAD_RB(0x6B, true), LOAD_RC(0x6C, true), LOAD_RD(0x6D, true), LOAD_RE(0x6E, true),
    // Косвенная загрузка из регистра
    IND_LOAD_R0(0xD0, true), IND_LOAD_R1(0xD1, true), IND_LOAD_R2(0xD2, true), IND_LOAD_R3(0xD3, true),
    IND_LOAD_R4(0xD4, true), IND_LOAD_R5(0xD5, true), IND_LOAD_R6(0xD6, true), IND_LOAD_R7(0xD7, true),
    IND_LOAD_R8(0xD8, true), IND_LOAD_R9(0xD9, true), IND_LOAD_RA(0xDA, true), IND_LOAD_RB(0xDB, true),
    IND_LOAD_RC(0xDC, true), IND_LOAD_RD(0xDD, true), IND_LOAD_RE(0xDE, true),
    // Пустая операция
    NOOP(0x54),
    // Переход по адресу в регистре
    GOTO_R0(0x80, true), GOTO_R1(0x81, true), GOTO_R2(0x82, true), GOTO_R3(0x83, true), GOTO_R4(0x84, true),
    GOTO_R5(0x85, true), GOTO_R6(0x86, true), GOTO_R7(0x87, true), GOTO_R8(0x88, true), GOTO_R9(0x89, true),
    GOTO_RA(0x8A, true), GOTO_RB(0x8B, true), GOTO_RC(0x8C, true), GOTO_RD(0x8D, true), GOTO_RE(0x8E, true),
    // Вызов подпрограммы в адресу в регистре
    GOSUB_R0(0xA0, true), GOSUB_R1(0xA1, true), GOSUB_R2(0xA2, true), GOSUB_R3(0xA3, true), GOSUB_R4(0xA4, true),
    GOSUB_R5(0xA5, true), GOSUB_R6(0xA6, true), GOSUB_R7(0xA7, true), GOSUB_R8(0xA8, true), GOSUB_R9(0xA9, true),
    GOSUB_RA(0xAA, true), GOSUB_RB(0xAB, true), GOSUB_RC(0xAC, true), GOSUB_RD(0xAD, true), GOSUB_RE(0xAE, true),
    // Косвенный переход по условию x = 0
    GOTO_EQ_0_R0(0xE0, true), GOTO_EQ_0_R1(0xE1, true), GOTO_EQ_0_R2(0xE2, true), GOTO_EQ_0_R3(0xE3, true),
    GOTO_EQ_0_R4(0xE4, true), GOTO_EQ_0_R5(0xE5, true), GOTO_EQ_0_R6(0xE6, true), GOTO_EQ_0_R7(0xE7, true),
    GOTO_EQ_0_R8(0xE8, true), GOTO_EQ_0_R9(0xE9, true), GOTO_EQ_0_RA(0xEA, true), GOTO_EQ_0_RB(0xEB, true),
    GOTO_EQ_0_RC(0xEC, true), GOTO_EQ_0_RD(0xED, true), GOTO_EQ_0_RE(0xEE, true),
    // Косвенный переход по условию x < 0
    GOTO_LT_0_R0(0xC0, true), GOTO_LT_0_R1(0xC1, true), GOTO_LT_0_R2(0xC2, true), GOTO_LT_0_R3(0xC3, true),
    GOTO_LT_0_R4(0xC4, true), GOTO_LT_0_R5(0xC5, true), GOTO_LT_0_R6(0xC6, true), GOTO_LT_0_R7(0xC7, true),
    GOTO_LT_0_R8(0xC8, true), GOTO_LT_0_R9(0xC9, true), GOTO_LT_0_RA(0xCA, true), GOTO_LT_0_RB(0xCB, true),
    GOTO_LT_0_RC(0xCC, true), GOTO_LT_0_RD(0xCD, true), GOTO_LT_0_RE(0xCE, true),
    // Косвенный переход по условию x >= 0
    GOTO_GE_0_R0(0x90, true), GOTO_GE_0_R1(0x91, true), GOTO_GE_0_R2(0x92, true), GOTO_GE_0_R3(0x93, true),
    GOTO_GE_0_R4(0x94, true), GOTO_GE_0_R5(0x95, true), GOTO_GE_0_R6(0x96, true), GOTO_GE_0_R7(0x97, true),
    GOTO_GE_0_R8(0x98, true), GOTO_GE_0_R9(0x99, true), GOTO_GE_0_RA(0x9A, true), GOTO_GE_0_RB(0x9B, true),
    GOTO_GE_0_RC(0x9C, true), GOTO_GE_0_RD(0x9D, true), GOTO_GE_0_RE(0x9E, true),
    // Косвенный переход по условию x != 0
    GOTO_NE_0_R0(0x70, true), GOTO_NE_0_R1(0x71, true), GOTO_NE_0_R2(0x72, true), GOTO_NE_0_R3(0x73, true),
    GOTO_NE_0_R4(0x74, true), GOTO_NE_0_R5(0x75, true), GOTO_NE_0_R6(0x76, true), GOTO_NE_0_R7(0x77, true),
    GOTO_NE_0_R8(0x78, true), GOTO_NE_0_R9(0x79, true), GOTO_NE_0_RA(0x7A, true), GOTO_NE_0_RB(0x7B, true),
    GOTO_NE_0_RC(0x7C, true), GOTO_NE_0_RD(0x7D, true), GOTO_NE_0_RE(0x7E, true),
    //
    INTEGER(0x34),
    FRACTIONAL(0x35),
    MAX(0x36),
    ABS(0x31),
    SIGNUM(0x32),
    RANDOM(0x3B),
    // Угловые значения
    DEG_TO_HH_MM(0x33),
    HH_MM_TO_DEG(0x26),
    HH_MM_SS_TO_DEG(0x2A),
    DEG_TO_HH_MM_SS(0x30),
    // Логические операции
    AND(0x37),
    OR(0x38),
    XOR(0x39),
    INVERSION(0x3A);

    private final int code;
    private final boolean register;
    private final int size;

    OpCode(int code, int size, boolean register) {
        this.code = code;
        this.register = register;
        this.size = size;
    }

    OpCode(int code, int size) {
        this(code, size, false);
    }

    OpCode(int code, boolean register) {
        this(code, 1, register);
    }

    OpCode(int code) {
        this(code, 1);
    }

    public int code() {
        return code;
    }

    public int size() {
        return size;
    }

    public boolean isRegister() {
        return register;
    }

    public int getRegisterIndex() {
        return code & 0xF;
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
}
