/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.panteleyev.mk52.math.Mk52Math;
import org.panteleyev.mk52.program.Address;
import org.panteleyev.mk52.program.Instruction;
import org.panteleyev.mk52.program.OpCode;
import org.panteleyev.mk52.program.ProgramMemory;
import org.panteleyev.mk52.program.StepExecutionCallback;
import org.panteleyev.mk52.program.StepExecutionResult;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

import static org.panteleyev.mk52.Mk52Application.logger;
import static org.panteleyev.mk52.engine.Constants.STORE_CODE_DURATION;
import static org.panteleyev.mk52.engine.Constants.TURN_OFF_DISPLAY_DELAY;

final class Processor {
    private static final Predicate<Long> LT_0 = Register::isNegative;
    private static final Predicate<Long> EQ_0 = Register::isZero;
    private static final Predicate<Long> GE_0 = x -> Register.isZero(x) || !Register.isNegative(x);
    private static final Predicate<Long> NE_0 = x -> !Register.isZero(x);

    private final Engine engine;
    private final Stack stack;
    private final Registers registers;
    private final ProgramMemory memory;
    private final CallStack callStack;

    private final boolean async;
    private final AtomicBoolean automaticMode;
    private final StepExecutionCallback stepCallback;

    private final AtomicReference<OpCode> lastExecutedOpCode;

    private final AtomicReference<TrigonometricMode> trigonometricMode =
            new AtomicReference<>(TrigonometricMode.RADIAN);

    public Processor(
            Engine engine,
            boolean async,
            AtomicReference<OpCode> lastExecutedOpCode,
            StepExecutionCallback stepCallback
    ) {
        this.engine = engine;
        this.async = async;
        this.stack = engine.stack();
        this.memory = engine.programMemory();
        this.registers = engine.registers();
        this.callStack = engine.callStack();
        this.automaticMode = engine.automaticMode();
        this.lastExecutedOpCode = lastExecutedOpCode;
        this.stepCallback = stepCallback;
    }

    public void setTrigonometricMode(TrigonometricMode trigonometricMode) {
        this.trigonometricMode.set(trigonometricMode);
    }

    public void reset() {
        engine.programCounter().set(Address.ZERO);
        lastExecutedOpCode.set(null);
        stack.reset();
        registers.reset();
        callStack.reset();
    }

    public void step() {
        var instruction = memory.fetchInstruction(engine.programCounter());
        execute(instruction);
    }

    public void run() {
        while (automaticMode.get()) {
            step();
        }
    }

    void sleep(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (Exception ex) {
            //
        }
    }

    private void stepLeft() {
        engine.programCounter().decrement();
    }

    private void stepRight() {
        engine.programCounter().increment();
    }

    private void store(Address address) {
        registers.store(address, stack.normalizeX());
        checkResultAndDisplay();
    }

    private void indirectStore(Address address) {
        registers.store(registers.modifyAndGetAddressValue(address), stack.normalizeX());
        checkResultAndDisplay();
    }

    private void load(Address address) {
        stack.push();
        stack.loadX(registers.load(address));
    }

    private void indirectLoad(Address address) {
        stack.push();
        stack.loadX(registers.load(registers.modifyAndGetAddressValue(address)));
    }

    public void returnTo0() {
        engine.programCounter().set(Address.ZERO);
    }

    private void goTo(Address pc) {
        engine.programCounter().set(pc);
    }

    private void goSub(Address pc) {
        callStack.push(engine.programCounter().get().decrement());
        goTo(pc);
    }

    public void returnFromSubroutine() {
        stack.normalizeX();
        checkResultAndDisplay(false);
        var newPc = callStack.pop().increment();
        goTo(newPc);
    }

    private void conditionalGoto(Address pc, Predicate<Long> predicate) {
        if (!predicate.test(stack.xValue())) {
            goTo(pc);
        }
    }

    private void indirectGoto(Address address) {
        engine.programCounter().set(registers.modifyAndGetAddressValue(address));
    }

    private void loop(Address pc, int register) {
        if (registers.modifyAndGetLoopValue(register) > 0) {
            engine.programCounter().set(pc);
        }
    }

    private void conditionalIndirectGoto(Address address, Predicate<Long> predicate) {
        if (!predicate.test(stack.xValue())) {
            indirectGoto(address);
        }
    }

    private void indirectGoSub(Address address) {
        goSub(registers.modifyAndGetAddressValue(address));
    }

    private void execute(OpCode opCode) {
        if (opCode.isStore()) {
            store(Address.of(opCode.getRegister()));
        } else if (opCode.isLoad()) {
            load(Address.of(opCode.getRegister()));
        } else if (opCode.isIndirectStore()) {
            indirectStore(Address.of(opCode.getRegister()));
        } else if (opCode.isIndirectLoad()) {
            indirectLoad(Address.of(opCode.getRegister()));
        } else if (opCode.isIndirectGoto()) {
            indirectGoto(Address.of(opCode.getRegister()));
        } else if (opCode.isGotoLt0()) {
            conditionalIndirectGoto(Address.of(opCode.getRegister()), LT_0);
        } else if (opCode.isGotoEq0()) {
            conditionalIndirectGoto(Address.of(opCode.getRegister()), EQ_0);
        } else if (opCode.isGotoGe0()) {
            conditionalIndirectGoto(Address.of(opCode.getRegister()), GE_0);
        } else if (opCode.isGotoNe0()) {
            conditionalIndirectGoto(Address.of(opCode.getRegister()), NE_0);
        } else if (opCode.isIndirectGosub()) {
            indirectGoSub(Address.of(opCode.getRegister()));
        } else if (opCode == OpCode.RETURN) {
            returnFromSubroutine();
        } else if (opCode == OpCode.STOP_RUN) {
            automaticMode.set(false);
            checkResultAndDisplay(false);
        } else {
            switch (opCode) {
                // Инструкции эмулятора
                case OpCode.STEP_LEFT -> stepLeft();
                case OpCode.STEP_RIGHT -> stepRight();
                case OpCode.TO_EXECUTION_MODE -> engine.programming().set(false);
                case OpCode.TO_PROGRAMMING_MODE -> engine.programming().set(true);

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
                    if (engine.enteringExponent().get()) {
                        stack.addCharacter('-');
                    } else {
                        unaryOperation(Mk52Math::negate);
                    }
                }
                case OpCode.ENTER_EXPONENT -> stack.enterExponent();

                case OpCode.PUSH -> {
                    stack.push();
                    checkResultAndDisplay(false);
                }
                case OpCode.SWAP -> {
                    stack.swap();
                    checkResultAndDisplay();
                }
                case OpCode.ROTATE -> {
                    stack.rotate();
                    checkResultAndDisplay();
                }
                case OpCode.RESTORE_X -> {
                    stack.restoreX();
                    checkResultAndDisplay(false);
                }
                case OpCode.CLEAR_X -> {
                    stack.clearX();
                    checkResultAndDisplay(false);
                }

                // Арифметика
                case OpCode.ADD -> binaryOperation(Mk52Math::add);
                case OpCode.SUBTRACT -> binaryOperation(Mk52Math::subtract);
                case OpCode.MULTIPLY -> binaryOperation(Mk52Math::multiply);
                case OpCode.DIVIDE -> binaryOperation(Mk52Math::divide);

                // Логические операции
                case OpCode.INVERSION -> unaryOperation(Mk52Math::inversion);
                case OpCode.AND -> binaryKeepYOperation(Mk52Math::and);
                case OpCode.OR -> binaryKeepYOperation(Mk52Math::or);
                case OpCode.XOR -> binaryKeepYOperation(Mk52Math::xor);

                case OpCode.SQRT -> unaryOperation(Mk52Math::sqrt);
                case OpCode.SQR -> unaryOperation(Mk52Math::sqr);
                case OpCode.POWER_OF_TEN -> unaryOperation(Mk52Math::pow10);
                case OpCode.LG -> unaryOperation(Mk52Math::lg);
                case OpCode.LN -> unaryOperation(Mk52Math::ln);
                case OpCode.EXP -> unaryOperation(Mk52Math::exp);
                case OpCode.ONE_BY_X -> unaryOperation(Mk52Math::oneByX);
                case OpCode.POWER_OF_X -> binaryKeepYOperation(Mk52Math::pow);
                case OpCode.PI -> {
                    stack.pi();
                    checkResultAndDisplay();
                }
                case OpCode.RANDOM -> unaryOperation(_ -> Mk52Math.rand());

                case OpCode.ABS -> unaryOperation(Mk52Math::abs);
                case OpCode.INTEGER -> unaryOperation(Mk52Math::integer);
                case OpCode.FRACTIONAL -> unaryOperation(Mk52Math::fractional);
                case OpCode.MAX -> binaryKeepYOperation(Mk52Math::max);
                case OpCode.SIGNUM -> unaryOperation(Mk52Math::signum);

                // Тригонометрия
                case OpCode.SIN -> unaryOperation(x -> Mk52Math.sin(x, trigonometricMode.get()));
                case OpCode.ASIN -> unaryOperation(x -> Mk52Math.asin(x, trigonometricMode.get()));
                case OpCode.COS -> unaryOperation(x -> Mk52Math.cos(x, trigonometricMode.get()));
                case OpCode.ACOS -> unaryOperation(x -> Mk52Math.acos(x, trigonometricMode.get()));
                case OpCode.TAN -> unaryOperation(x -> Mk52Math.tan(x, trigonometricMode.get()));
                case OpCode.ATAN -> unaryOperation(x -> Mk52Math.atan(x, trigonometricMode.get()));

                // Угловые
                case OpCode.HH_MM_TO_DEG -> unaryOperation(Mk52Math::hoursMinutesToDegrees);
                case OpCode.HH_MM_SS_TO_DEG -> unaryOperation(Mk52Math::hoursMinutesSecondsToDegrees);
                case OpCode.DEG_TO_HH_MM -> unaryOperation(Mk52Math::degreesToHoursMinutes);
                case OpCode.DEG_TO_HH_MM_SS -> unaryOperation(Mk52Math::degreesToHoursMinutesSeconds);

                // NOP
                case OpCode.NOOP, OpCode.K_1, OpCode.K_2 -> unaryOperation(Mk52Math::noop);

                default -> {
                    logger().severe("Неизвестный код операции: " + Integer.toString(opCode.code(), 16));
                    throw new ArithmeticException();
                }
            }
        }
    }

    public void execute(Instruction instruction) {
        if (async) {
            sleep(TURN_OFF_DISPLAY_DELAY);
        }

        stepCallback.before();

        var opCode = instruction.opCode();
        if (opCode.hasAddress()) {
            var address = instruction.address();
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
            try {
                execute(opCode);
            } catch (ArithmeticException ex) {
                stack.setX2(IR.ERROR);
                automaticMode.set(false);
            }
        }

        lastExecutedOpCode.set(opCode);

        if (async) {
            sleep(instruction.opCode().duration().minus(TURN_OFF_DISPLAY_DELAY));
        }

        stepCallback.after(newStepExecutionResult(engine.getCurrentDisplay()));
    }

    public void storeCode(int code) {
        if (async) {
            sleep(TURN_OFF_DISPLAY_DELAY);
        }

        stepCallback.before();
        memory.storeCode(engine.programCounter(), code);

        if (async) {
            sleep(STORE_CODE_DURATION);
        }

        var pc = engine.programCounter().get();
        stepCallback.after(newStepExecutionResult(memory.getIndicator(pc)));
    }

    private StepExecutionResult newStepExecutionResult(IR display) {
        return new StepExecutionResult(
                display,
                engine.programCounter().get(),
                stack.getSnapshot(),
                registers.getSnapshot(),
                callStack.getSnapshot()
        );
    }

    private void unaryOperation(UnaryOperator<Long> operation) {
        stack.unaryOperation(operation);
        checkResultAndDisplay();
    }

    private void binaryOperation(BinaryOperator<Long> operation) {
        stack.binaryOperation(operation);
        checkResultAndDisplay();
    }

    private void binaryKeepYOperation(BinaryOperator<Long> operation) {
        stack.binaryKeepYOperation(operation);
        checkResultAndDisplay();
    }

    private void checkResultAndDisplay() {
        checkResultAndDisplay(automaticMode.get());
    }

    private void checkResultAndDisplay(boolean skip) {
        if (skip) {
            return;
        }

        IR ir;
        var x = stack.normalizeX();

        var exponent = Register.getExponent(x);
        if (exponent >= -99 && exponent <= 99) {
            ir = Register.xToIndicator(x);
        } else if (exponent <= 199) {
            // Ярус 1
            ir = IR.ERROR;
            automaticMode.set(false);
        } else if (exponent <= 299) {
            // Ярус 2
            ir = IR.ERROR_2;
            engine.programCounter().set(new Address((exponent - 200) / 10, 2));
            automaticMode.set(false);
        } else {
            // Верхние ярусы не реализованы, просто гасим экран и останавливаемся
            ir = IR.EMPTY;
            automaticMode.set(false);
        }

        stack.setX2(ir);
    }
}