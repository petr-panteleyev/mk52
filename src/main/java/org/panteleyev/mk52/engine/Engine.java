/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.panteleyev.mk52.program.Instruction;
import org.panteleyev.mk52.program.ProgramMemory;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import static org.panteleyev.mk52.engine.Constants.EMPTY_DISPLAY;
import static org.panteleyev.mk52.engine.KeyboardButton.BUTTON_TO_ADDRESS;
import static org.panteleyev.mk52.engine.KeyboardButton.GOSUB;
import static org.panteleyev.mk52.engine.KeyboardButton.RETURN;
import static org.panteleyev.mk52.engine.KeyboardButton.RUN_STOP;
import static org.panteleyev.mk52.engine.KeyboardButton.STEP_LEFT;
import static org.panteleyev.mk52.engine.KeyboardButton.STEP_RIGHT;
import static org.panteleyev.mk52.engine.OpCode.EMPTY;

public final class Engine {
    public enum TrigonometricMode {
        RADIAN,
        GRADIAN,
        DEGREE
    }

    public enum OperationMode {
        EXECUTION,
        PROGRAMMING
    }

    private final ProgramMemory programMemory = new ProgramMemory();

    private boolean powered = false;

    // Stack
    private final Stack stack = new Stack();
    // Регистры
    private final Registers registers = new Registers();

    private TrigonometricMode trigonometricMode = TrigonometricMode.DEGREE;
    private OperationMode operationMode = OperationMode.EXECUTION;
    private KeyboardMode keyboardMode = KeyboardMode.NORMAL;
    private final AtomicInteger programCounter = new AtomicInteger(0);

    // Сюда сохраняем цифры адреса при вводе двухбайтовой команды
    private final int[] addressBuffer = new int[2];
    // Сюда сохраняем начало регистровой команды
    private OpCode registerOpCode = OpCode.EMPTY;
    // Сюда сохраняем начало адресной команды
    private OpCode addressOpCode = OpCode.EMPTY;

    private final DisplayUpdateCallback displayUpdateCallback;

    public Engine(DisplayUpdateCallback displayUpdateCallback) {
        this.displayUpdateCallback = displayUpdateCallback;
        init();
    }

    Stack getStack() {
        return stack;
    }

    Registers getRegisters() {
        return registers;
    }

    public AtomicInteger getProgramCounter() {
        return programCounter;
    }

    public void init() {
        programCounter.set(0);
        operationMode = OperationMode.EXECUTION;
        keyboardMode = KeyboardMode.NORMAL;

        stack.reset();
        registers.reset();

        displayUpdateCallback.updateDisplay(stack.getStringValue(), operationMode);
    }

    public void processButton(KeyboardButton button) {
        if (!powered) {
            return;
        }

        switch (operationMode) {
            case EXECUTION -> processButtonExecutionMode(button);
            case PROGRAMMING -> processButtonProgrammingMode(button);
        }

        switch (operationMode) {
            case EXECUTION -> displayUpdateCallback.updateDisplay(stack.getStringValue(), operationMode);
            case PROGRAMMING ->
                    displayUpdateCallback.updateDisplay(programMemory.getStringValue(programCounter), operationMode);
        }
    }

    private void processButtonExecutionMode(KeyboardButton button) {
        if (keyboardMode == KeyboardMode.NORMAL && button == RETURN) {
            programCounter.set(0);
            return;
        }
        if (keyboardMode == KeyboardMode.NORMAL && button == STEP_LEFT) {
            programCounter.decrementAndGet();
            return;
        }
        if (keyboardMode == KeyboardMode.NORMAL && button == STEP_RIGHT) {
            programCounter.incrementAndGet();
            return;
        }

        if (keyboardMode == KeyboardMode.NORMAL && button == GOSUB) {
            step();
            return;
        }

        if (keyboardMode == KeyboardMode.NORMAL && button == RUN_STOP) {
            run();
            return;
        }

        switch (button) {
            case F -> keyboardMode = KeyboardMode.F;
            case K -> keyboardMode = KeyboardMode.K;

            default -> {
                var opCode = EMPTY;
                opCode = switch (keyboardMode) {
                    case KeyboardMode.NORMAL -> button.getOpCode();
                    case KeyboardMode.F -> button.getfOpCode();
                    case KeyboardMode.K -> button.getkOpCode();
                    case KeyboardMode.ADDRESS_DIGIT_1 -> {
                        var val = BUTTON_TO_ADDRESS.get(button);
                        if (val != null) {
                            addressBuffer[0] = val;
                        }
                        keyboardMode = KeyboardMode.ADDRESS_DIGIT_2;
                        yield OpCode.EMPTY;
                    }
                    case KeyboardMode.ADDRESS_DIGIT_2 -> {
                        var val = BUTTON_TO_ADDRESS.get(button);
                        if (val != null) {
                            addressBuffer[1] = val;
                        }
                        var code = addressBuffer[0] * 16 + addressBuffer[1];
                        Processor.execute(new Instruction(addressOpCode, code), this);
                        keyboardMode = KeyboardMode.NORMAL;
                        yield OpCode.EMPTY;
                    }
                    case KeyboardMode.REGISTER -> {
                        var register = KeyboardButton.BUTTON_TO_REGISTER.get(button);
                        if (register == null) {
                            register = 0;
                        }
                        var effectiveCode = registerOpCode.code() + register;
                        keyboardMode = KeyboardMode.NORMAL;
                        Processor.execute(OpCode.findByCode(effectiveCode), this);
                        yield OpCode.EMPTY;
                    }
                };

                if (opCode == EMPTY) {
                    return;
                }

                if (opCode.isRegister()) {
                    keyboardMode = KeyboardMode.REGISTER;
                    registerOpCode = opCode;
                    return;
                }

                if (opCode.size() == 2) {
                    keyboardMode = KeyboardMode.ADDRESS_DIGIT_1;
                    addressOpCode = opCode;
                    return;
                }

                if (opCode == OpCode.TO_PROGRAMMING_MODE) {
                    operationMode = OperationMode.PROGRAMMING;
                } else {
                    stack.printStack();
                    Processor.execute(opCode, this);
                }
                keyboardMode = KeyboardMode.NORMAL;
            }
        }
    }

    private void processButtonProgrammingMode(KeyboardButton button) {
        if (keyboardMode == KeyboardMode.NORMAL && button == STEP_LEFT) {
            programCounter.decrementAndGet();
            return;
        }
        if (keyboardMode == KeyboardMode.NORMAL && button == STEP_RIGHT) {
            programCounter.incrementAndGet();
            return;
        }

        switch (button) {
            case F -> keyboardMode = KeyboardMode.F;
            case K -> keyboardMode = KeyboardMode.K;

            default -> {
                var opCode = EMPTY;

                opCode = switch (keyboardMode) {
                    case KeyboardMode.NORMAL -> button.getOpCode();
                    case KeyboardMode.F -> button.getfOpCode();
                    case KeyboardMode.K -> button.getkOpCode();
                    case KeyboardMode.ADDRESS_DIGIT_1 -> {
                        var val = BUTTON_TO_ADDRESS.get(button);
                        if (val != null) {
                            addressBuffer[0] = val;
                        }
                        keyboardMode = KeyboardMode.ADDRESS_DIGIT_2;
                        yield OpCode.EMPTY;
                    }
                    case KeyboardMode.ADDRESS_DIGIT_2 -> {
                        var val = BUTTON_TO_ADDRESS.get(button);
                        if (val != null) {
                            addressBuffer[1] = val;
                        }
                        var code = addressBuffer[0] * 16 + addressBuffer[1];
                        programMemory.storeCode(programCounter, code);
                        keyboardMode = KeyboardMode.NORMAL;
                        yield OpCode.EMPTY;
                    }
                    case KeyboardMode.REGISTER -> {
                        var register = KeyboardButton.BUTTON_TO_REGISTER.get(button);
                        if (register == null) {
                            register = 0;
                        }
                        var effectiveCode = registerOpCode.code() + register;
                        programMemory.storeCode(programCounter, effectiveCode);
                        keyboardMode = KeyboardMode.NORMAL;
                        yield OpCode.EMPTY;
                    }
                };

                if (opCode == OpCode.EMPTY) {
                    return;
                }

                if (opCode.isRegister()) {
                    keyboardMode = KeyboardMode.REGISTER;
                    registerOpCode = opCode;
                    return;
                }

                if (opCode == OpCode.TO_EXECUTION_MODE) {
                    operationMode = OperationMode.EXECUTION;
                } else {
                    stack.printStack();
                    programMemory.storeCode(programCounter, opCode.code());
                }

                if (opCode.size() == 2) {
                    keyboardMode = KeyboardMode.ADDRESS_DIGIT_1;
                } else {
                    keyboardMode = KeyboardMode.NORMAL;
                }
            }
        }
    }

    public void togglePower(boolean on) {
        if (!powered && on) {
            init();
            powered = true;
        }
        if (!on) {
            // turn off display
            displayUpdateCallback.updateDisplay(EMPTY_DISPLAY, operationMode);
            powered = false;
        }
    }

    public void setTrigonometricMode(TrigonometricMode trigonometricMode) {
        this.trigonometricMode = trigonometricMode;
    }

    public TrigonometricMode getTrigonometricMode() {
        return trigonometricMode;
    }

    public void unary(UnaryOperator<Value> operation) {
        stack.unaryOperation(operation);
    }

    public void binary(BinaryOperator<Value> operation) {
        stack.binaryOperation(operation);
    }

    public void store(int index) {
        registers.store(index, stack.x());
    }

    public void indirectStore(int index) {
        var indirectIndex = registers.modifyAndGetIndirectIndex(index);
        registers.store(indirectIndex, stack.x());
    }

    public void load(int index) {
        stack.push();
        stack.setX(registers.load(index));
    }

    public void indirectLoad(int index) {
        var indirectIndex = registers.modifyAndGetIndirectIndex(index);
        stack.push();
        stack.setX(registers.load(indirectIndex));
    }

    private void step() {
        var instruction = programMemory.fetchInstruction(programCounter);
        Processor.execute(instruction, this);
    }

    public void goTo(int pc) {
        programCounter.set(pc);
    }

    public void loop(int pc, int register) {
        var counter = registers.modifyAndGetIndirectIndex(register);
        if (counter != 0) {
            programCounter.set(pc);
        }
    }

    public void run() {
        while (true) {
            var instruction = programMemory.fetchInstruction(programCounter);
            if (!Processor.execute(instruction, this)) {
                break;
            }
        }
    }
}
