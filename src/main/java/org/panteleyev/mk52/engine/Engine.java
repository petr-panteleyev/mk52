/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

import static org.panteleyev.mk52.engine.KeyboardButton.LOAD;
import static org.panteleyev.mk52.engine.KeyboardButton.STORE;

public final class Engine {
    private static final int PROGRAM_MEMORY_SIZE = 105;

    private final OpCode[] programMemory = new OpCode[PROGRAM_MEMORY_SIZE];

    private boolean powered = false;

    // Stack
    private final NumberBuffer numberBuffer = new NumberBuffer();
    private final Stack stack = new Stack(numberBuffer);
    // Регистры
    private final Registers registers = new Registers();
    // Дисплей
    private final Display display = new Display();

    private TrigonometricMode trigonometricMode = TrigonometricMode.DEGREE;
    private OperationMode operationMode = OperationMode.EXECUTION;
    private KeyboardMode keyboardMode = KeyboardMode.NORMAL;
    private int programCounter = 0;

    private KeyboardButton previousButton = null;

    public Engine() {
        init();
    }

    Stack getStack() {
        return stack;
    }

    Registers getRegisters() {
        return registers;
    }

    public void init() {
        programCounter = 0;
        operationMode = OperationMode.EXECUTION;
        keyboardMode = KeyboardMode.NORMAL;

        stack.reset();
        numberBuffer.reset();
        registers.reset();

        display.represent(stack.x());
    }

    public String getDisplayString() {
        return display.asString();
    }

    public void processButton(KeyboardButton button) {
        if (!powered) {
            return;
        }

        switch (operationMode) {
            case EXECUTION -> processButtonExecutionMode(button);
            case PROGRAMMING -> processButtonProgrammingMode(button);
        }
        if (numberBuffer.isInProgress()) {
            display.represent(numberBuffer);
        } else {
            display.represent(stack.x());
        }
    }

    private void processButtonExecutionMode(KeyboardButton button) {
        switch (button) {
            case F -> keyboardMode = KeyboardMode.F;
            case K -> keyboardMode = KeyboardMode.K;

            case STORE, LOAD -> {
                if (keyboardMode == KeyboardMode.F) {
                    // Обработать L0, L1
                    keyboardMode = KeyboardMode.NORMAL;
                } else {
                    previousButton = button;
                }
            }

            default -> {
                var opCode = OpCode.EMPTY;

                if (previousButton == STORE) {
                    var register = KeyboardButton.BUTTON_TO_REGISTER.get(button);
                    if (register != null) {
                        if (keyboardMode == KeyboardMode.K) {
                            opCode = OpCode.values()[OpCode.IND_STORE_R0.ordinal() + register];
                        } else {
                            opCode = OpCode.values()[OpCode.STORE_R0.ordinal() + register];
                        }
                    }
                } else if (previousButton == LOAD) {
                    var register = KeyboardButton.BUTTON_TO_REGISTER.get(button);
                    if (register != null) {
                        if (keyboardMode == KeyboardMode.K) {
                            opCode = OpCode.values()[OpCode.IND_LOAD_R0.ordinal() + register];
                        } else {
                            opCode = OpCode.values()[OpCode.LOAD_R0.ordinal() + register];
                        }
                    }
                } else {
                    opCode = switch (keyboardMode) {
                        case KeyboardMode.NORMAL -> button.getOpCode();
                        case KeyboardMode.F -> button.getfOpCode();
                        case KeyboardMode.K -> button.getkOpCode();
                    };
                }

                stack.printStack();
                Processor.execute(opCode, this);
                keyboardMode = KeyboardMode.NORMAL;
                previousButton = null;
            }
        }
    }

    private void processButtonProgrammingMode(KeyboardButton button) {
    }

    public void togglePower(boolean on) {
        if (!powered && on) {
            init();
            powered = true;
        }
        if (!on) {
            // turn off display
            display.clear();
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
        stack.setX(registers.load(index));
    }

    public void indirectLoad(int index) {
        var indirectIndex = registers.modifyAndGetIndirectIndex(index);
        stack.setX(registers.load(indirectIndex));
    }
}
