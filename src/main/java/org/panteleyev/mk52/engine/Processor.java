/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.panteleyev.mk52.math.Mk52Math;
import org.panteleyev.mk52.program.Address;
import org.panteleyev.mk52.program.Instruction;
import org.panteleyev.mk52.program.OpCode;
import org.panteleyev.mk52.program.ProgramCounter;
import org.panteleyev.mk52.program.ProgramMemory;
import org.panteleyev.mk52.program.StepExecutionCallback;
import org.panteleyev.mk52.program.StepExecutionResult;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

import static org.panteleyev.mk52.Mk52Application.logger;
import static org.panteleyev.mk52.engine.Constants.ERROR_DISPLAY;
import static org.panteleyev.mk52.engine.Constants.STORE_CODE_DURATION;
import static org.panteleyev.mk52.engine.Constants.TURN_OFF_DISPLAY_DELAY;

final class Processor {
    private enum ExecutionStatus {
        STOP,
        CONTINUE,
        ERROR
    }

    private static final Predicate<Long> LT_0 = Register::isNegative;
    private static final Predicate<Long> EQ_0 = Register::isZero;
    private static final Predicate<Long> GE_0 = x -> Register.isZero(x) || !Register.isNegative(x);
    private static final Predicate<Long> NE_0 = x -> !Register.isZero(x);

    private final ProgramCounter programCounter = new ProgramCounter();
    private final Stack stack;
    private final Registers registers;
    private final ProgramMemory memory;
    private final CallStack callStack;

    private final boolean async;
    private final AtomicBoolean running;
    private final AtomicReference<Engine.OperationMode> operationMode;
    private final StepExecutionCallback stepCallback;

    private final AtomicReference<OpCode> lastExecutedOpCode;

    private final AtomicBoolean fetchedInstruction = new AtomicBoolean(false);

    private final AtomicReference<TrigonometricMode> trigonometricMode =
            new AtomicReference<>(TrigonometricMode.RADIAN);

    public Processor(
            boolean async,
            Stack stack,
            Registers registers,
            ProgramMemory memory,
            CallStack callStack,
            AtomicBoolean running,
            AtomicReference<Engine.OperationMode> operationMode,
            AtomicReference<OpCode> lastExecutedOpCode,
            StepExecutionCallback stepCallback
    ) {
        this.async = async;
        this.stack = stack;
        this.memory = memory;
        this.registers = registers;
        this.callStack = callStack;
        this.running = running;
        this.operationMode = operationMode;
        this.lastExecutedOpCode = lastExecutedOpCode;
        this.stepCallback = stepCallback;
    }

    public Address getProgramCounter() {
        return programCounter.get();
    }

    public void setTrigonometricMode(TrigonometricMode trigonometricMode) {
        this.trigonometricMode.set(trigonometricMode);
    }

    public void reset() {
        programCounter.set(Address.ZERO);
        lastExecutedOpCode.set(null);
        stack.reset();
        registers.reset();
        callStack.reset();
    }

    public void step() {
        step(true);
    }

    private ExecutionStatus step(boolean single) {
        var instruction = memory.fetchInstruction(programCounter);
        fetchedInstruction.set(true);
        var status = execute(instruction, single);
        fetchedInstruction.set(false);
        return status;
    }

    public void run() {
        var status = ExecutionStatus.CONTINUE;
        while (true) {
            if (!running.get() || status != ExecutionStatus.CONTINUE) {
                running.set(false);
                if (status == ExecutionStatus.ERROR) {
                    stepCallback.after(newStepExecutionResult(ERROR_DISPLAY));
                } else {
                    stepCallback.after(newStepExecutionResult(Register.toString(stack.x())));
                }
                break;
            }
            status = step(false);
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
        programCounter.decrement();
    }

    private void stepRight() {
        programCounter.increment();
    }

    private void store(Address address) {
        registers.store(address, stack.x());
    }

    private void indirectStore(Address address) {
        registers.store(registers.modifyAndGetAddressValue(address), stack.x());
    }

    private void load(Address address) {
        stack.push();
        stack.setX(registers.load(address));
    }

    private void indirectLoad(Address address) {
        stack.push();
        stack.setX(registers.load(registers.modifyAndGetAddressValue(address)));
    }

    public void returnTo0() {
        programCounter.set(Address.ZERO);
    }

    private void goTo(Address pc) {
        programCounter.set(pc);
    }

    private void goSub(Address pc) {
        callStack.push(programCounter.get().decrement());
        goTo(pc);
    }

    public void returnFromSubroutine() {
        stack.x();
        var newPc = callStack.pop().increment();
        goTo(newPc);
    }

    private void conditionalGoto(Address pc, Predicate<Long> predicate) {
        if (!predicate.test(stack.x())) {
            goTo(pc);
        }
    }

    private void indirectGoto(Address address) {
        programCounter.set(registers.modifyAndGetAddressValue(address));
    }

    private void loop(Address pc, int register) {
        if (registers.modifyAndGetLoopValue(register) > 0) {
            programCounter.set(pc);
        }
    }

    private void conditionalIndirectGoto(Address address, Predicate<Long> predicate) {
        if (!predicate.test(stack.x())) {
            indirectGoto(address);
        }
    }

    private void indirectGoSub(Address address) {
        goSub(registers.modifyAndGetAddressValue(address));
    }

    private ExecutionStatus execute(OpCode opCode) {
        var codeHigh = opCode.code() & 0xF0;

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
            if (fetchedInstruction.get()) {
                returnFromSubroutine();
            } else {
                returnTo0();
            }
        } else if (opCode == OpCode.STOP_RUN) {
            stack.x();
            return ExecutionStatus.STOP;
        } else {
            switch (opCode) {
                // Инструкции эмулятора
                case OpCode.STEP_LEFT -> stepLeft();
                case OpCode.STEP_RIGHT -> stepRight();
                case OpCode.TO_EXECUTION_MODE -> operationMode.set(Engine.OperationMode.EXECUTION);
                case OpCode.TO_PROGRAMMING_MODE -> operationMode.set(Engine.OperationMode.PROGRAMMING);

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
                case OpCode.SIGN -> stack.negate();
                case OpCode.ENTER_EXPONENT -> stack.enterExponent();

                case OpCode.PUSH -> stack.push();
                case OpCode.SWAP -> stack.swap();
                case OpCode.ROTATE -> stack.rotate();
                case OpCode.RESTORE_X -> stack.restoreX();
                case OpCode.CLEAR_X -> stack.clearX();

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
                case OpCode.PI -> stack.pi();
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

                // Угловые
                case OpCode.HH_MM_TO_DEG -> stack.unaryOperation(Mk52Math::hoursMinutesToDegrees);
                case OpCode.HH_MM_SS_TO_DEG -> stack.unaryOperation(Mk52Math::hoursMinutesSecondsToDegrees);
                case OpCode.DEG_TO_HH_MM -> stack.unaryOperation(Mk52Math::degreesToHoursMinutes);
                case OpCode.DEG_TO_HH_MM_SS -> stack.unaryOperation(Mk52Math::degreesToHoursMinutesSeconds);

                // NOP
                case OpCode.NOOP, OpCode.K_1, OpCode.K_2 -> {
                }

                default -> {
                    logger().severe("Неизвестный код операции: " + Integer.toString(opCode.code(), 16));
                    throw new ArithmeticException();
                }
            }
        }
        return ExecutionStatus.CONTINUE;
    }

    public void execute(Instruction instruction) {
        execute(instruction, true);
    }

    private ExecutionStatus execute(Instruction instruction, boolean single) {
        if (async) {
            sleep(TURN_OFF_DISPLAY_DELAY);
        }

        stepCallback.before();

        var opCode = instruction.opCode();
        var status = ExecutionStatus.CONTINUE;
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
                status = execute(opCode);
            } catch (ArithmeticException ex) {
                status = ExecutionStatus.ERROR;
            }
        }

        lastExecutedOpCode.set(opCode);

        if (async) {
            sleep(instruction.opCode().duration().minus(TURN_OFF_DISPLAY_DELAY));
        }

        if (single) {
            running.set(false);
        }

        if (status == ExecutionStatus.ERROR) {
            stepCallback.after(newStepExecutionResult(ERROR_DISPLAY));
        } else {
            stepCallback.after(newStepExecutionResult(getCurrentDisplay()));
        }
        return status;
    }

    public String getCurrentDisplay() {
        return switch (operationMode.get()) {
            case EXECUTION -> stack.display();
            case PROGRAMMING -> memory.getStringValue(programCounter.get());
        };
    }

    public void storeCode(int code) {
        if (async) {
            sleep(TURN_OFF_DISPLAY_DELAY);
        }

        stepCallback.before();
        memory.storeCode(programCounter, code);

        if (async) {
            sleep(STORE_CODE_DURATION);
        }

        running.set(false);
        var pc = programCounter.get();
        stepCallback.after(newStepExecutionResult(memory.getStringValue(pc)));
    }

    private StepExecutionResult newStepExecutionResult(String display) {
        return new StepExecutionResult(
                display,
                programCounter.get(),
                stack.getSnapshot(),
                registers.getSnapshot(),
                callStack.getSnapshot()
        );
    }
}