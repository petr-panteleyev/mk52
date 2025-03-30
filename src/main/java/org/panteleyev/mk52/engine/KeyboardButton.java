/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import java.util.Map;

public enum KeyboardButton {
    F(OpCode.EMPTY, OpCode.EMPTY, OpCode.EMPTY),
    K(OpCode.EMPTY, OpCode.EMPTY, OpCode.EMPTY),
    EEPROM_EXCHANGE(OpCode.EMPTY, OpCode.EMPTY, OpCode.EMPTY),
    EEPROM_ADDRESS(OpCode.EMPTY, OpCode.EMPTY, OpCode.EMPTY),
    STEP_RIGHT(OpCode.STEP_RIGHT, OpCode.X_LT_0, OpCode.GOTO_LT_0_R0),
    STEP_LEFT(OpCode.STEP_LEFT, OpCode.X_EQ_0, OpCode.GOTO_EQ_0_R0),
    RETURN(OpCode.RETURN, OpCode.X_GE_0, OpCode.GOTO_GE_0_R0),
    RUN_STOP(OpCode.STOP_RUN, OpCode.X_NE_0, OpCode.GOTO_NE_0_R0),
    LOAD(OpCode.LOAD_R0, OpCode.L0, OpCode.IND_LOAD_R0),
    STORE(OpCode.STORE_R0, OpCode.L1, OpCode.IND_STORE_R0),
    GOTO(OpCode.GOTO, OpCode.L2, OpCode.GOTO_R0),
    GOSUB(OpCode.GOSUB, OpCode.L3, OpCode.GOSUB_R0),
    D0(OpCode.ZERO, OpCode.POWER_OF_TEN, OpCode.NOOP),
    D1(OpCode.ONE, OpCode.EXP, OpCode.K_1),
    D2(OpCode.TWO, OpCode.LG, OpCode.K_2),
    D3(OpCode.THREE, OpCode.LN, OpCode.DEG_TO_HH_MM_SS),
    D4(OpCode.FOUR, OpCode.ASIN, OpCode.ABS),
    D5(OpCode.FIVE, OpCode.ACOS, OpCode.SIGNUM),
    D6(OpCode.SIX, OpCode.ATAN, OpCode.DEG_TO_HH_MM),
    D7(OpCode.SEVEN, OpCode.SIN, OpCode.INTEGER),
    D8(OpCode.EIGHT, OpCode.COS, OpCode.FRACTIONAL),
    D9(OpCode.NINE, OpCode.TAN, OpCode.MAX),
    DOT(OpCode.DOT, OpCode.ROTATE, OpCode.AND),
    SIGN(OpCode.SIGN, OpCode.TO_EXECUTION_MODE, OpCode.OR),
    MINUS(OpCode.SUBTRACT, OpCode.SQRT, OpCode.EMPTY),
    PLUS(OpCode.ADD, OpCode.PI, OpCode.HH_MM_TO_DEG),
    DIVISION(OpCode.DIVIDE, OpCode.ONE_BY_X, OpCode.EMPTY),
    MULTIPLICATION(OpCode.MULTIPLY, OpCode.SQR, OpCode.EMPTY),
    SWAP(OpCode.SWAP, OpCode.POWER_OF_X, OpCode.HH_MM_SS_TO_DEG),
    EE(OpCode.ENTER_EXPONENT, OpCode.TO_PROGRAMMING_MODE, OpCode.XOR),
    PUSH(OpCode.PUSH, OpCode.RESTORE_X, OpCode.RANDOM),
    CLEAR_X(OpCode.CLEAR_X, OpCode.EMPTY, OpCode.INVERSION);

    private final OpCode opCode;
    private final OpCode fOpCode;
    private final OpCode kOpCode;

    public static final Map<KeyboardButton, Integer> BUTTON_TO_REGISTER = Map.ofEntries(
            Map.entry(D0, 0x0),
            Map.entry(D1, 0x1),
            Map.entry(D2, 0x2),
            Map.entry(D3, 0x3),
            Map.entry(D4, 0x4),
            Map.entry(D5, 0x5),
            Map.entry(D6, 0x6),
            Map.entry(D7, 0x7),
            Map.entry(D8, 0x8),
            Map.entry(D9, 0x9),
            Map.entry(DOT, 0xA),
            Map.entry(SIGN, 0xB),
            Map.entry(EE, 0xC),
            Map.entry(CLEAR_X, 0xD),
            Map.entry(PUSH, 0xE)
    );

    public static final Map<KeyboardButton, Integer> BUTTON_TO_ADDRESS = Map.ofEntries(
            Map.entry(D0, 0x0),
            Map.entry(D1, 0x1),
            Map.entry(D2, 0x2),
            Map.entry(D3, 0x3),
            Map.entry(D4, 0x4),
            Map.entry(D5, 0x5),
            Map.entry(D6, 0x6),
            Map.entry(D7, 0x7),
            Map.entry(D8, 0x8),
            Map.entry(D9, 0x9),
            Map.entry(DOT, 0xA)
    );

    KeyboardButton(OpCode opCode, OpCode fOpCode, OpCode kOpCode) {
        this.opCode = opCode;
        this.fOpCode = fOpCode;
        this.kOpCode = kOpCode;
    }

    public OpCode getOpCode() {
        return opCode;
    }

    public OpCode getfOpCode() {
        return fOpCode;
    }

    public OpCode getkOpCode() {
        return kOpCode;
    }
}
