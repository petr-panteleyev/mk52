/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.panteleyev.mk52.program.Instruction;
import org.panteleyev.mk52.program.ProgramMemory;
import org.panteleyev.mk52.program.StepExecutionCallback;
import org.panteleyev.mk52.program.StepExecutionResult;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

final class Processor {
    private static final Predicate<Value> LT_0 = v -> v.value() < 0;
    private static final Predicate<Value> EQ_0 = v -> v.value() == 0;
    private static final Predicate<Value> GE_0 = v -> v.value() >= 0;
    private static final Predicate<Value> NE_0 = v -> v.value() != 0;

    private final AtomicInteger programCounter = new AtomicInteger(0);
    private final Stack stack;
    private final Registers registers;
    private final ProgramMemory programMemory;
    private final Deque<Integer> callStack = new ArrayDeque<>(5);

    private final boolean async;
    private final AtomicBoolean running;
    private final StepExecutionCallback stepCallback;

    private final AtomicReference<TrigonometricMode> trigonometricMode =
            new AtomicReference<>(TrigonometricMode.RADIAN);

    public Processor(
            boolean async,
            Stack stack,
            Registers registers,
            ProgramMemory programMemory,
            AtomicBoolean running,
            StepExecutionCallback stepCallback
    ) {
        this.async = async;
        this.stack = stack;
        this.registers = registers;
        this.programMemory = programMemory;
        this.running = running;
        this.stepCallback = stepCallback;
    }

    public int getProgramCounter() {
        return programCounter.get();
    }

    public void setTrigonometricMode(TrigonometricMode trigonometricMode) {
        this.trigonometricMode.set(trigonometricMode);
    }

    public void reset() {
        programCounter.set(0);
        stack.reset();
        registers.reset();
        callStack.clear();
    }

    public void step() {
        step(true);
    }

    private boolean step(boolean single) {
        var instruction = programMemory.fetchInstruction(programCounter);
        return execute(instruction, single);
    }

    public void run() {
        var cont = true;
        while (true) {
            if (!running.get() || !cont) {
                running.set(false);
                stepCallback.after(new StepExecutionResult(stack.x().asString()));
                break;
            }
            cont = step(false);
        }
    }

    private void sleep(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (Exception ex) {
            //
        }
    }

    public void stepLeft() {
        programCounter.decrementAndGet();
    }

    public void stepRight() {
        programCounter.incrementAndGet();
    }

    public void storeCode(int code) {
        programMemory.storeCode(programCounter, code);
    }

    private void store(int index) {
        registers.store(index, stack.x());
    }

    private void indirectStore(int index) {
        var indirectIndex = registers.modifyAndGetRegisterValue(index);
        registers.store(indirectIndex, stack.x());
    }

    private void load(int index) {
        stack.push();
        stack.setX(registers.load(index));
    }

    private void indirectLoad(int index) {
        var indirectIndex = registers.modifyAndGetRegisterValue(index);
        stack.push();
        stack.setX(registers.load(indirectIndex));
    }

    public void returnTo0() {
        programCounter.set(0);
    }

    private void goTo(int pc) {
        programCounter.set(pc);
    }

    private void goSub(int pc) {
        callStack.push(programCounter.get());
        goTo(pc);
    }

    public void returnFromSubroutine() {
        stack.x();
        var newPc = callStack.poll();
        if (newPc != null) {
            goTo(newPc);
        }
    }

    private void conditionalGoto(int pc, Predicate<Value> predicate) {
        var condition = predicate.test(stack.x());
        if (!condition) {
            goTo(pc);
        }
    }

    private void indirectGoto(int register) {
        var indirect = registers.modifyAndGetRegisterValue(register);
        programCounter.set(indirect);
    }

    private void loop(int pc, int register) {
        var counter = registers.modifyAndGetRegisterValue(register);
        if (counter != 0) {
            programCounter.set(pc);
        }
    }

    private void conditionalIndirectGoto(int register, Predicate<Value> predicate) {
        var condition = predicate.test(stack.x());
        if (!condition) {
            indirectGoto(register);
        }
    }

    private void indirectGoSub(int register) {
        var indirect = registers.modifyAndGetRegisterValue(register);
        goSub(indirect);
    }

    private boolean execute(OpCode opCode) {
        if (opCode.inRange(OpCode.STORE_R0, OpCode.STORE_RE)) {
            store(opCode.getRegisterIndex());
        } else if (opCode.inRange(OpCode.LOAD_R0, OpCode.LOAD_RE)) {
            load(opCode.getRegisterIndex());
        } else if (opCode.inRange(OpCode.IND_STORE_R0, OpCode.IND_STORE_RE)) {
            indirectStore(opCode.getRegisterIndex());
        } else if (opCode.inRange(OpCode.IND_LOAD_R0, OpCode.IND_LOAD_RE)) {
            indirectLoad(opCode.getRegisterIndex());
        } else if (opCode.inRange(OpCode.GOTO_R0, OpCode.GOTO_RE)) {
            indirectGoto(opCode.getRegisterIndex());
        } else if (opCode.inRange(OpCode.GOTO_LT_0_R0, OpCode.GOTO_LT_0_RE)) {
            conditionalIndirectGoto(opCode.getRegisterIndex(), LT_0);
        } else if (opCode.inRange(OpCode.GOTO_EQ_0_R0, OpCode.GOTO_EQ_0_RE)) {
            conditionalIndirectGoto(opCode.getRegisterIndex(), EQ_0);
        } else if (opCode.inRange(OpCode.GOTO_GE_0_R0, OpCode.GOTO_GE_0_RE)) {
            conditionalIndirectGoto(opCode.getRegisterIndex(), GE_0);
        } else if (opCode.inRange(OpCode.GOTO_NE_0_R0, OpCode.GOTO_NE_0_RE)) {
            conditionalIndirectGoto(opCode.getRegisterIndex(), NE_0);
        } else if (opCode.inRange(OpCode.GOSUB_R0, OpCode.GOSUB_RE)) {
            indirectGoSub(opCode.getRegisterIndex());
        } else if (opCode == OpCode.RETURN) {
            returnFromSubroutine();
        } else if (opCode == OpCode.STOP_RUN) {
            stack.x();
            return false;
        } else {
            switch (opCode) {
                case OpCode.ZERO -> stack.addCharacter('0');
                case OpCode.ONE -> stack.addCharacter('1');
                case OpCode.TWO -> stack.addCharacter('2');
                case OpCode.THREE -> stack.addCharacter('3');
                case OpCode.FOUR -> stack.addCharacter('4');
                case OpCode.FIVE -> stack.addCharacter('5');
                case OpCode.SIX -> stack.addCharacter('6');
                case OpCode.SEVEN -> stack.addCharacter('7');
                case OpCode.EIGHT -> stack.addCharacter('8');
                case OpCode.NINE -> stack.addCharacter('9');
                case OpCode.DOT -> stack.addCharacter('.');
                case OpCode.SIGN -> {
                    if (stack.numberBuffer().isInProgress()) {
                        stack.addCharacter('-');
                    } else {
                        stack.unaryOperation(Mk52Math::negate);
                    }
                }

                case OpCode.ENTER_EXPONENT -> stack.enterExponent();

                case OpCode.PUSH -> stack.push();
                case OpCode.SWAP -> stack.swap();
                case OpCode.ROTATE -> stack.rotate();
                case OpCode.RESTORE_X -> stack.restoreX();
                case OpCode.CLEAR_X -> stack.unaryOperation(_ -> Value.ZERO);

                // Арифметика
                case OpCode.ADD -> stack.binaryOperation(Mk52Math::add);
                case OpCode.SUBTRACT -> stack.binaryOperation(Mk52Math::subtract);
                case OpCode.MULTIPLY -> stack.binaryOperation(Mk52Math::multiply);
                case OpCode.DIVIDE -> stack.binaryOperation(Mk52Math::divide);

                // Логические операции
                case OpCode.INVERSION -> stack.unaryOperation(Mk52Math::inversion);
                case OpCode.AND -> stack.binaryKeepYOperation(Mk52Math::and);
                case OpCode.OR -> stack.binaryKeepYOperation(Mk52Math::or);
                case OpCode.XOR -> stack.binaryKeepYOperation(Mk52Math::xor);

                case OpCode.SQRT -> stack.unaryOperation(Mk52Math::sqrt);
                case OpCode.SQR -> stack.unaryOperation(Mk52Math::sqr);
                case OpCode.POWER_OF_TEN -> stack.unaryOperation(Mk52Math::pow10);
                case OpCode.LG -> stack.unaryOperation(Mk52Math::lg);
                case OpCode.LN -> stack.unaryOperation(Mk52Math::ln);
                case OpCode.EXP -> stack.unaryOperation(Mk52Math::exp);
                case OpCode.ONE_BY_X -> stack.unaryOperation(Mk52Math::oneByX);
                case OpCode.POWER_OF_X -> stack.binaryKeepYOperation(Mk52Math::pow);
                case OpCode.PI -> {
                    stack.push();
                    stack.addCharacters("3.1415926".toCharArray());
                }
                case OpCode.RANDOM -> stack.unaryOperation(_ -> Mk52Math.rand());

                case OpCode.ABS -> stack.unaryOperation(Mk52Math::abs);
                case OpCode.INTEGER -> stack.unaryOperation(Mk52Math::integer);
                case OpCode.FRACTIONAL -> stack.unaryOperation(Mk52Math::fractional);
                case OpCode.MAX -> stack.binaryKeepYOperation(Mk52Math::max);
                case OpCode.SIGNUM -> stack.unaryOperation(Mk52Math::signum);

                // Тригонометрия
                case OpCode.SIN -> stack.unaryOperation(x -> Mk52Math.sin(x, trigonometricMode.get()));
                case OpCode.ASIN -> stack.unaryOperation(x -> Mk52Math.asin(x, trigonometricMode.get()));
                case OpCode.COS -> stack.unaryOperation(x -> Mk52Math.cos(x, trigonometricMode.get()));
                case OpCode.ACOS -> stack.unaryOperation(x -> Mk52Math.acos(x, trigonometricMode.get()));
                case OpCode.TAN -> stack.unaryOperation(x -> Mk52Math.tan(x, trigonometricMode.get()));
                case OpCode.ATAN -> stack.unaryOperation(x -> Mk52Math.atan(x, trigonometricMode.get()));
            }
        }
        return true;
    }

    public void execute(Instruction instruction) {
        execute(instruction, true);
    }

    private boolean execute(Instruction instruction, boolean single) {
        if (async) {
            sleep(Duration.of(20, ChronoUnit.MILLIS));
        }

        stepCallback.before();

        var opCode = instruction.opCode();
        var cont = true;
        if (opCode.size() == 2) {
            var address = instruction.address() / 16 * 10 + instruction.address() % 16;
            if (opCode == OpCode.GOTO) {
                goTo(address);
            } else if (opCode == OpCode.GOSUB) {
                goSub(address);
            } else if (opCode.inRange(OpCode.L0, OpCode.L3)) {
                loop(address, opCode.ordinal() - OpCode.L0.ordinal());
            } else if (opCode == OpCode.X_LT_0) {
                conditionalGoto(address, LT_0);
            } else if (opCode == OpCode.X_EQ_0) {
                conditionalGoto(address, EQ_0);
            } else if (opCode == OpCode.X_GE_0) {
                conditionalGoto(address, GE_0);
            } else if (opCode == OpCode.X_NE_0) {
                conditionalGoto(address, NE_0);
            }
        } else {
            cont =  execute(opCode);
        }

        if (async) {
            sleep(instruction.opCode().duration());
        }

        if (single) {
            running.set(false);
        }
        stepCallback.after(new StepExecutionResult(stack.getStringValue()));
        return cont;
    }
}