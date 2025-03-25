/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import java.util.Map;

import static org.panteleyev.mk52.engine.OpCode.EMPTY;

public enum KeyboardButton {
    F(OpCode.EMPTY, OpCode.EMPTY, OpCode.EMPTY),
    K(OpCode.EMPTY, OpCode.EMPTY, OpCode.EMPTY),
    UP_DOWN(OpCode.EMPTY, OpCode.EMPTY, OpCode.EMPTY),
    A_UP(OpCode.EMPTY, OpCode.EMPTY, OpCode.EMPTY),
    STEP_RIGHT(OpCode.EMPTY, OpCode.EMPTY, OpCode.EMPTY),
    STEP_LEFT(OpCode.EMPTY, OpCode.EMPTY, OpCode.EMPTY),
    RETURN(EMPTY, OpCode.EMPTY, OpCode.EMPTY),
    RUN_STOP(EMPTY, OpCode.EMPTY, OpCode.EMPTY),
    LOAD(EMPTY, OpCode.EMPTY, OpCode.EMPTY),
    STORE(EMPTY, OpCode.EMPTY, OpCode.EMPTY),
    GOTO(EMPTY, OpCode.EMPTY, OpCode.EMPTY),
    GOSUB(EMPTY, OpCode.EMPTY, OpCode.EMPTY),
    DIGIT_0(OpCode.ZERO, OpCode.POWER_OF_TEN, OpCode.EMPTY),
    DIGIT_1(OpCode.ONE, OpCode.EXP, OpCode.EMPTY),
    DIGIT_2(OpCode.TWO, OpCode.LG, OpCode.EMPTY),
    DIGIT_3(OpCode.THREE, OpCode.LN, OpCode.EMPTY),
    DIGIT_4(OpCode.FOUR, OpCode.ASIN, OpCode.ABS),
    DIGIT_5(OpCode.FIVE, OpCode.ACOS, OpCode.SIGNUM),
    DIGIT_6(OpCode.SIX, OpCode.ATAN, OpCode.EMPTY),
    DIGIT_7(OpCode.SEVEN, OpCode.SIN, OpCode.INTEGER),
    DIGIT_8(OpCode.EIGHT, OpCode.COS, OpCode.FRACTIONAL),
    DIGIT_9(OpCode.NINE, OpCode.TAN, OpCode.MAX),
    DOT(OpCode.DOT, OpCode.ROTATE, OpCode.EMPTY),
    SIGN(OpCode.SIGN, OpCode.EMPTY, OpCode.EMPTY),
    MINUS(OpCode.SUBTRACT, OpCode.SQRT, OpCode.EMPTY),
    PLUS(OpCode.ADD, OpCode.PI, OpCode.EMPTY),
    DIVISION(OpCode.DIVIDE, OpCode.ONE_BY_X, OpCode.EMPTY),
    MULTIPLICATION(OpCode.MULTIPLY, OpCode.SQR, OpCode.EMPTY),
    SWAP(OpCode.SWAP, OpCode.POWER_OF_X, OpCode.EMPTY),
    EE(OpCode.ENTER_EXPONENT, OpCode.EMPTY, OpCode.EMPTY),
    PUSH(OpCode.PUSH, OpCode.RESTORE_X, OpCode.RANDOM),
    CLEAR_X(OpCode.CLEAR_X, OpCode.EMPTY, OpCode.EMPTY);

    private final OpCode opCode;
    private final OpCode fOpCode;
    private final OpCode kOpCode;

    public static final Map<KeyboardButton, Integer> BUTTON_TO_REGISTER = Map.ofEntries(
            Map.entry(DIGIT_0, 0x0),
            Map.entry(DIGIT_1, 0x1),
            Map.entry(DIGIT_2, 0x2),
            Map.entry(DIGIT_3, 0x3),
            Map.entry(DIGIT_4, 0x4),
            Map.entry(DIGIT_5, 0x5),
            Map.entry(DIGIT_6, 0x6),
            Map.entry(DIGIT_7, 0x7),
            Map.entry(DIGIT_8, 0x8),
            Map.entry(DIGIT_9, 0x9),
            Map.entry(DOT, 0xA),
            Map.entry(SIGN, 0xB),
            Map.entry(EE, 0xC),
            Map.entry(CLEAR_X, 0xD),
            Map.entry(PUSH, 0xE)
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
