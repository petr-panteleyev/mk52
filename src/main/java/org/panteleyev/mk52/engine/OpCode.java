/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import java.util.Arrays;

public enum OpCode {
    // Коды эмулятора
    EMPTY(-1),
    // Цифры
    ZERO(0x00), ONE(0x01), TWO(0x02), THREE(0x03), FOUR(0x04),
    FIVE(0x05), SIX(0x06), SEVEN(0x07), EIGHT(0x08), NINE(0x09),
    // Арифметика
    ADD(0x10), SUBTRACT(0x11), MULTIPLY(0x12), DIVIDE(0x13),
    SWAP(0x14),
    PUSH(0x0E),
    DOT(0x0A),
    SIGN(0x0B),
    ENTER_EXPONENT(0x0C),
    CLEAR_X(0x0D),
    STOP_RUN(0x50),
    GOTO(0x51),
    RETURN(0x52),
    GOSUB(0x53),
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
    RESTORE_X(0),
    ROTATE(0x25),
    // Условный переход
    X_LT_0(0x5C), X_EQ_0(0x5E), X_GE_0(0x59), X_NE_0(0x57),
    // Цикл
    L0(0x5D), L1(0x5B), L2(0x58), L3(0x5A),
    // Сохранение в регистр
    STORE_R0(0x40), STORE_R1(0x41), STORE_R2(0x42), STORE_R3(0x43), STORE_R4(0x44),
    STORE_R5(0x45), STORE_R6(0x46), STORE_R7(0x47), STORE_R8(0x48), STORE_R9(0x49),
    STORE_RA(0x4A), STORE_RB(0x4B), STORE_RC(0x4C), STORE_RD(0x4D), STORE_RE(0x4E),
    // Косвенное сохранение в регистр
    IND_STORE_R0(0xB0), IND_STORE_R1(0xB1), IND_STORE_R2(0xB2), IND_STORE_R3(0xB3), IND_STORE_R4(0xB4),
    IND_STORE_R5(0xB5), IND_STORE_R6(0xB6), IND_STORE_R7(0xB7), IND_STORE_R8(0xB8), IND_STORE_R9(0xB9),
    IND_STORE_RA(0xBA), IND_STORE_RB(0xBB), IND_STORE_RC(0xBC), IND_STORE_RD(0xBD), IND_STORE_RE(0xBE),
    // Загрузка из регистра
    LOAD_R0(0x60), LOAD_R1(0x61), LOAD_R2(0x62), LOAD_R3(0x63), LOAD_R4(0x64),
    LOAD_R5(0x65), LOAD_R6(0x66), LOAD_R7(0x67), LOAD_R8(0x68), LOAD_R9(0x69),
    LOAD_RA(0x6A), LOAD_RB(0x6B), LOAD_RC(0x6C), LOAD_RD(0x6D), LOAD_RE(0x6E),
    // Косвенная загрузка из регистра
    IND_LOAD_R0(0xD0), IND_LOAD_R1(0xD1), IND_LOAD_R2(0xD2), IND_LOAD_R3(0xD3), IND_LOAD_R4(0xD4),
    IND_LOAD_R5(0xD5), IND_LOAD_R6(0xD6), IND_LOAD_R7(0xD7), IND_LOAD_R8(0xD8), IND_LOAD_R9(0xD9),
    IND_LOAD_RA(0xDA), IND_LOAD_RB(0xDB), IND_LOAD_RC(0xDC), IND_LOAD_RD(0xDD), IND_LOAD_RE(0xDE),
    // Пустая операция
    NOOP(0x54),
    // Переход по адресу в регистре
    GOTO_R0(0x80), GOTO_R1(0x81), GOTO_R2(0x82), GOTO_R3(0x83), GOTO_R4(0x84),
    GOTO_R5(0x85), GOTO_R6(0x86), GOTO_R7(0x87), GOTO_R8(0x88), GOTO_R9(0x89),
    GOTO_RA(0x8A), GOTO_RB(0x8B), GOTO_RC(0x8C), GOTO_RD(0x8D), GOTO_RE(0x8E),
    // Вызов подпрограммы в адресу в регистре
    CALL_R0(0xA0), CALL_R1(0xA1), CALL_R2(0xA2), CALL_R3(0xA3), CALL_R4(0xA4),
    CALL_R5(0xA5), CALL_R6(0xA6), CALL_R7(0xA7), CALL_R8(0xA8), CALL_R9(0xA9),
    CALL_RA(0xAA), CALL_RB(0xAB), CALL_RC(0xAC), CALL_RD(0xAD), CALL_RE(0xAE),
    // Косвенный переход по условию x = 0
    GOTO_EQ_0_R0(0xE0), GOTO_EQ_0_R1(0xE1), GOTO_EQ_0_R2(0xE2), GOTO_EQ_0_R3(0xE3), GOTO_EQ_0_R4(0xE4),
    GOTO_EQ_0_R5(0xE5), GOTO_EQ_0_R6(0xE6), GOTO_EQ_0_R7(0xE7), GOTO_EQ_0_R8(0xE8), GOTO_EQ_0_R9(0xE9),
    GOTO_EQ_0_RA(0xEA), GOTO_EQ_0_RB(0xEB), GOTO_EQ_0_RC(0xEC), GOTO_EQ_0_RD(0xED), GOTO_EQ_0_RE(0xEE),
    // Косвенный переход по условию x < 0
    GOTO_LT_0_R0(0xC0), GOTO_LT_0_R1(0xC1), GOTO_LT_0_R2(0xC2), GOTO_LT_0_R3(0xC3), GOTO_LT_0_R4(0xC4),
    GOTO_LT_0_R5(0xC5), GOTO_LT_0_R6(0xC6), GOTO_LT_0_R7(0xC7), GOTO_LT_0_R8(0xC8), GOTO_LT_0_R9(0xC9),
    GOTO_LT_0_RA(0xCA), GOTO_LT_0_RB(0xCB), GOTO_LT_0_RC(0xCC), GOTO_LT_0_RD(0xCD), GOTO_LT_0_RE(0xCE),
    // Косвенный переход по условию x >= 0
    GOTO_GE_0_R0(0x90), GOTO_GE_0_R1(0x91), GOTO_GE_0_R2(0x92), GOTO_GE_0_R3(0x93), GOTO_GE_0_R4(0x94),
    GOTO_GE_0_R5(0x95), GOTO_GE_0_R6(0x96), GOTO_GE_0_R7(0x97), GOTO_GE_0_R8(0x98), GOTO_GE_0_R9(0x99),
    GOTO_GE_0_RA(0x9A), GOTO_GE_0_RB(0x9B), GOTO_GE_0_RC(0x9C), GOTO_GE_0_RD(0x9D), GOTO_GE_0_RE(0x9E),
    // Косвенный переход по условию x != 0
    GOTO_NE_0_R0(0x70), GOTO_NE_0_R1(0x71), GOTO_NE_0_R2(0x72), GOTO_NE_0_R3(0x73), GOTO_NE_0_R4(0x74),
    GOTO_NE_0_R5(0x75), GOTO_NE_0_R6(0x76), GOTO_NE_0_R7(0x77), GOTO_NE_0_R8(0x78), GOTO_NE_0_R9(0x79),
    GOTO_NE_0_RA(0x7A), GOTO_NE_0_RB(0x7B), GOTO_NE_0_RC(0x7C), GOTO_NE_0_RD(0x7D), GOTO_NE_0_RE(0x7E),
    //
    INTEGER(0x34),
    FRACTIONAL(0x35),
    MAX(0x36),
    ABS(0x31),
    SIGNUM(0x32),
    RANDOM(0x3B),
    // Угловые значения
    X1(0x33),
    X2(0x26),
    X3(0x2A),
    X4(0x30),
    // Логические операции
    AND(0x37),
    OR(0x38),
    XOR(0x39),
    INVERSION(0x3A);

    private final int code;

    OpCode(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public int getRegisterIndex() {
        return code & 0xF;
    }

    public static OpCode findByCode(int code) {
        return Arrays.stream(OpCode.values())
                .filter(e -> e.code == code)
                .findAny()
                .orElse(OpCode.EMPTY);
    }
}
