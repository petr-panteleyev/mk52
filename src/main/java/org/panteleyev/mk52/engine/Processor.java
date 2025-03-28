/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.panteleyev.mk52.program.Instruction;

import java.util.Map;
import java.util.function.Predicate;

import static java.util.Map.entry;

class Processor {
    private static final Predicate<Value> LT_0 = v -> v.value() < 0;
    private static final Predicate<Value> EQ_0 = v -> v.value() == 0;
    private static final Predicate<Value> GE_0 = v -> v.value() >= 0;
    private static final Predicate<Value> NE_0 = v -> v.value() != 0;

    private static final Map<OpCode, OpcodeHandler> OPERATIONS = Map.ofEntries(
            entry(OpCode.ZERO, e -> e.getStack().addCharacter('0')),
            entry(OpCode.ONE, e -> e.getStack().addCharacter('1')),
            entry(OpCode.TWO, e -> e.getStack().addCharacter('2')),
            entry(OpCode.THREE, e -> e.getStack().addCharacter('3')),
            entry(OpCode.FOUR, e -> e.getStack().addCharacter('4')),
            entry(OpCode.FIVE, e -> e.getStack().addCharacter('5')),
            entry(OpCode.SIX, e -> e.getStack().addCharacter('6')),
            entry(OpCode.SEVEN, e -> e.getStack().addCharacter('7')),
            entry(OpCode.EIGHT, e -> e.getStack().addCharacter('8')),
            entry(OpCode.NINE, e -> e.getStack().addCharacter('9')),
            entry(OpCode.DOT, e -> e.getStack().addCharacter('.')),
            entry(OpCode.SIGN, e -> {
                        if (e.getStack().numberBuffer().isInProgress()) {
                            e.getStack().addCharacter('-');
                        } else {
                            e.unary(Mk52Math::negate);
                        }
                    }
            ),
            entry(OpCode.ENTER_EXPONENT, e -> e.getStack().enterExponent()),

            entry(OpCode.PUSH, e -> e.getStack().push()),
            entry(OpCode.SWAP, e -> e.getStack().swap()),
            entry(OpCode.ROTATE, e -> e.getStack().rotate()),
            entry(OpCode.RESTORE_X, e -> e.getStack().restoreX()),
            entry(OpCode.CLEAR_X, e -> e.unary(_ -> Value.ZERO)),

            // Арифметика
            entry(OpCode.ADD, e -> e.binary(Mk52Math::add)),
            entry(OpCode.SUBTRACT, e -> e.binary(Mk52Math::subtract)),
            entry(OpCode.MULTIPLY, e -> e.binary(Mk52Math::multiply)),
            entry(OpCode.DIVIDE, e -> e.binary(Mk52Math::divide)),

            // Логические операции
            entry(OpCode.INVERSION, e -> e.unary(Mk52Math::inversion)),
            entry(OpCode.AND, e -> e.binary(Mk52Math::and)),
            entry(OpCode.OR, e -> e.binary(Mk52Math::or)),
            entry(OpCode.XOR, e -> e.binary(Mk52Math::xor)),

            entry(OpCode.SQRT, e -> e.unary(Mk52Math::sqrt)),
            entry(OpCode.SQR, e -> e.unary(Mk52Math::sqr)),
            entry(OpCode.POWER_OF_TEN, e -> e.unary(Mk52Math::pow10)),
            entry(OpCode.LG, e -> e.unary(Mk52Math::lg)),
            entry(OpCode.LN, e -> e.unary(Mk52Math::ln)),
            entry(OpCode.EXP, e -> e.unary(Mk52Math::exp)),
            entry(OpCode.ONE_BY_X, e -> e.unary(Mk52Math::oneByX)),
            entry(OpCode.POWER_OF_X, e -> e.binaryKeepY(Mk52Math::pow)),
            entry(OpCode.PI, e -> {
                e.getStack().push();
                e.getStack().addCharacters("3.1415926".toCharArray());
            }),
            entry(OpCode.RANDOM, e -> e.unary(_ -> Mk52Math.rand())),

            entry(OpCode.ABS, e -> e.unary(Mk52Math::abs)),
            entry(OpCode.INTEGER, e -> e.unary(Mk52Math::integer)),
            entry(OpCode.FRACTIONAL, e -> e.unary(Mk52Math::fractional)),
            entry(OpCode.MAX, e -> e.binaryKeepY(Mk52Math::max)),
            entry(OpCode.SIGNUM, e -> e.unary(Mk52Math::signum)),

            // Тригонометрия
            entry(OpCode.SIN, e -> e.unary(x -> Mk52Math.sin(x, e.getTrigonometricMode()))),
            entry(OpCode.ASIN, e -> e.unary(x -> Mk52Math.asin(x, e.getTrigonometricMode()))),
            entry(OpCode.COS, e -> e.unary(x -> Mk52Math.cos(x, e.getTrigonometricMode()))),
            entry(OpCode.ACOS, e -> e.unary(x -> Mk52Math.acos(x, e.getTrigonometricMode()))),
            entry(OpCode.TAN, e -> e.unary(x -> Mk52Math.tan(x, e.getTrigonometricMode()))),
            entry(OpCode.ATAN, e -> e.unary(x -> Mk52Math.atan(x, e.getTrigonometricMode())))
    );

    public static boolean execute(OpCode opCode, Engine engine) {
        System.out.println("Executing: " + opCode);

        if (opCode.inRange(OpCode.STORE_R0, OpCode.STORE_RE)) {
            engine.store(opCode.getRegisterIndex());
        } else if (opCode.inRange(OpCode.LOAD_R0, OpCode.LOAD_RE)) {
            engine.load(opCode.getRegisterIndex());
        } else if (opCode.inRange(OpCode.IND_STORE_R0, OpCode.IND_STORE_RE)) {
            engine.indirectStore(opCode.getRegisterIndex());
        } else if (opCode.inRange(OpCode.IND_LOAD_R0, OpCode.IND_LOAD_RE)) {
            engine.indirectLoad(opCode.getRegisterIndex());
        } else if (opCode.inRange(OpCode.GOTO_R0, OpCode.GOTO_RE)) {
            engine.indirectGoto(opCode.getRegisterIndex());
        } else if (opCode.inRange(OpCode.GOTO_LT_0_R0, OpCode.GOTO_LT_0_RE)) {
            engine.conditionalIndirectGoto(opCode.getRegisterIndex(), LT_0);
        } else if (opCode.inRange(OpCode.GOTO_EQ_0_R0, OpCode.GOTO_EQ_0_RE)) {
            engine.conditionalIndirectGoto(opCode.getRegisterIndex(), EQ_0);
        } else if (opCode.inRange(OpCode.GOTO_GE_0_R0, OpCode.GOTO_GE_0_RE)) {
            engine.conditionalIndirectGoto(opCode.getRegisterIndex(), GE_0);
        } else if (opCode.inRange(OpCode.GOTO_NE_0_R0, OpCode.GOTO_NE_0_RE)) {
            engine.conditionalIndirectGoto(opCode.getRegisterIndex(), NE_0);
        } else if (opCode.inRange(OpCode.GOSUB_R0, OpCode.GOSUB_RE)) {
            engine.indirectGoSub(opCode.getRegisterIndex());
        } else if (opCode == OpCode.RETURN) {
            engine.returnFromSubroutine();
        } else if (opCode == OpCode.STOP_RUN) {
            return false;
        } else {
            var operation = OPERATIONS.get(opCode);
            if (operation == null) {
                return false;
            }

            operation.handle(engine);
        }
        return true;
    }

    public static boolean execute(Instruction instruction, Engine engine) {
        var opCode = instruction.opCode();
        if (opCode.size() == 2) {
            System.out.println("Executing: " + instruction);

            var address = instruction.address() / 16 * 10 + instruction.address() % 16;
            if (opCode == OpCode.GOTO) {
                engine.goTo(address);
            } else if (opCode == OpCode.GOSUB) {
                engine.goSub(address);
            } else if (opCode.inRange(OpCode.L0, OpCode.L3)) {
                engine.loop(address, opCode.getRegisterIndex());
            } else if (opCode == OpCode.X_LT_0) {
                engine.conditionalGoto(address, LT_0);
            } else if (opCode == OpCode.X_EQ_0) {
                engine.conditionalGoto(address, EQ_0);
            } else if (opCode == OpCode.X_GE_0) {
                engine.conditionalGoto(address, GE_0);
            } else if (opCode == OpCode.X_NE_0) {
                engine.conditionalGoto(address, NE_0);
            }
            return true;
        } else {
            return execute(opCode, engine);
        }
    }
}