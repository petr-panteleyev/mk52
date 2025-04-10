/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.panteleyev.mk52.program.OpCode;

import java.util.Map;

public enum KeyboardButton {
    D0(0x80, OpCode.ZERO, OpCode.POWER_OF_TEN, OpCode.NOOP),
    D1(0x81, OpCode.ONE, OpCode.EXP, OpCode.K_1),
    D2(0x82, OpCode.TWO, OpCode.LG, OpCode.K_2),
    D3(0x83, OpCode.THREE, OpCode.LN, OpCode.DEG_TO_HH_MM_SS),
    D4(0x84, OpCode.FOUR, OpCode.ASIN, OpCode.ABS),
    D5(0x85, OpCode.FIVE, OpCode.ACOS, OpCode.SIGNUM),
    D6(0x86, OpCode.SIX, OpCode.ATAN, OpCode.DEG_TO_HH_MM),
    D7(0x87, OpCode.SEVEN, OpCode.SIN, OpCode.INTEGER),
    D8(0x88, OpCode.EIGHT, OpCode.COS, OpCode.FRACTIONAL),
    D9(0x89, OpCode.NINE, OpCode.TAN, OpCode.MAX),
    PLUS(0x10, OpCode.ADD, OpCode.PI, OpCode.HH_MM_TO_DEG),
    MINUS(0x11, OpCode.SUBTRACT, OpCode.SQRT, OpCode.EMPTY),
    MULTIPLICATION(0x12, OpCode.MULTIPLY, OpCode.SQR, OpCode.EMPTY),
    DIVISION(0x13, OpCode.DIVIDE, OpCode.ONE_BY_X, OpCode.EMPTY),
    SWAP(0x14, OpCode.SWAP, OpCode.POWER_OF_X, OpCode.HH_MM_SS_TO_DEG),
    DOT(0x15, OpCode.DOT, OpCode.ROTATE, OpCode.AND),
    SIGN(0x16, OpCode.SIGN, OpCode.TO_EXECUTION_MODE, OpCode.OR),
    EE(0x17, OpCode.ENTER_EXPONENT, OpCode.TO_PROGRAMMING_MODE, OpCode.XOR),
    CLEAR_X(0x18, OpCode.CLEAR_X, OpCode.EMPTY, OpCode.INVERSION),
    PUSH(0x19, OpCode.PUSH, OpCode.RESTORE_X, OpCode.RANDOM),
    RUN_STOP(0x90, OpCode.STOP_RUN, OpCode.X_NE_0, OpCode.GOTO_NE_0_R0),
    GOTO(0x91, OpCode.GOTO, OpCode.L2, OpCode.GOTO_R0),
    RETURN(0x92, OpCode.RETURN, OpCode.X_GE_0, OpCode.GOTO_GE_0_R0),
    GOSUB(0x93, OpCode.GOSUB, OpCode.L3, OpCode.GOSUB_R0),
    STORE(0x94, OpCode.STORE_R0, OpCode.L1, OpCode.IND_STORE_R0),
    STEP_RIGHT(0x95, OpCode.STEP_RIGHT, OpCode.X_LT_0, OpCode.GOTO_LT_0_R0),
    LOAD(0x96, OpCode.LOAD_R0, OpCode.L0, OpCode.IND_LOAD_R0),
    STEP_LEFT(0x97, OpCode.STEP_LEFT, OpCode.X_EQ_0, OpCode.GOTO_EQ_0_R0),
    K(0x98, OpCode.EMPTY, OpCode.EMPTY, OpCode.EMPTY),
    F(0x99, OpCode.EMPTY, OpCode.EMPTY, OpCode.EMPTY),
    // Коды этих клавиш мне неизвестны
    EEPROM_EXCHANGE(0x00, OpCode.EMPTY, OpCode.EMPTY, OpCode.EMPTY),
    EEPROM_ADDRESS(0x01, OpCode.EMPTY, OpCode.EMPTY, OpCode.EMPTY);

    private final int keyCode;
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

    KeyboardButton(int keyCode, OpCode opCode, OpCode fOpCode, OpCode kOpCode) {
        this.keyCode = keyCode;
        this.opCode = opCode;
        this.fOpCode = fOpCode;
        this.kOpCode = kOpCode;
    }

    public int getKeyCode() {
        return keyCode;
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
