/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.panteleyev.mk52.eeprom.Eeprom;
import org.panteleyev.mk52.eeprom.EepromMode;
import org.panteleyev.mk52.eeprom.EepromOperation;
import org.panteleyev.mk52.program.Instruction;
import org.panteleyev.mk52.program.ProgramMemory;
import org.panteleyev.mk52.program.StepExecutionCallback;
import org.panteleyev.mk52.program.StepExecutionResult;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.panteleyev.mk52.engine.Constants.DISPLAY_SIZE;
import static org.panteleyev.mk52.engine.Constants.EMPTY_DISPLAY;
import static org.panteleyev.mk52.engine.KeyboardButton.BUTTON_TO_ADDRESS;
import static org.panteleyev.mk52.engine.KeyboardButton.EEPROM_ADDRESS;
import static org.panteleyev.mk52.engine.KeyboardButton.EEPROM_EXCHANGE;
import static org.panteleyev.mk52.engine.KeyboardButton.GOSUB;
import static org.panteleyev.mk52.engine.KeyboardButton.RETURN;
import static org.panteleyev.mk52.engine.KeyboardButton.RUN_STOP;
import static org.panteleyev.mk52.engine.KeyboardButton.STEP_LEFT;
import static org.panteleyev.mk52.engine.KeyboardButton.STEP_RIGHT;
import static org.panteleyev.mk52.engine.OpCode.EMPTY;

public final class Engine {
    public enum OperationMode {
        EXECUTION,
        PROGRAMMING
    }

    private static class ExecutionThread extends Thread {
        ExecutionThread(Runnable runnable) {
            super(runnable);
            setDaemon(true);
        }
    }

    private final boolean async;

    private final ProgramMemory programMemory = new ProgramMemory();

    private boolean powered = false;

    // Stack
    private final Stack stack = new Stack();
    // Регистры
    private final Registers registers = new Registers();

    private final StepExecutionCallback stepCallback = new StepExecutionCallback() {
        @Override
        public void before() {
            displayUpdateCallback.updateDisplay(EMPTY_DISPLAY, true);
        }

        @Override
        public void after(StepExecutionResult stepExecutionResult) {
            System.out.println(stepExecutionResult);
            var content = stepExecutionResult.display() + " ".repeat(
                    DISPLAY_SIZE - stepExecutionResult.display().length());
            displayUpdateCallback.updateDisplay(content, running.get());
        }
    };
    private final Processor processor;

    private final AtomicBoolean running = new AtomicBoolean(false);

    private OperationMode operationMode = OperationMode.EXECUTION;
    private KeyboardMode keyboardMode = KeyboardMode.NORMAL;

    private EepromOperation eepromOperation = EepromOperation.READ;
    private EepromMode eepromMode = EepromMode.DATA;

    // Сюда сохраняем цифры адреса при вводе двухбайтовой команды
    private final int[] addressBuffer = new int[2];
    // Сюда сохраняем начало регистровой команды
    private OpCode registerOpCode = OpCode.EMPTY;
    // Сюда сохраняем начало адресной команды
    private OpCode addressOpCode = OpCode.EMPTY;

    private Value eepromAddressValue = null;

    private final DisplayUpdateCallback displayUpdateCallback;

    public Engine(boolean async, DisplayUpdateCallback displayUpdateCallback) {
        this.async = async;
        this.processor = new Processor(async, stack, registers, programMemory, running, stepCallback);
        this.displayUpdateCallback = displayUpdateCallback;
        init();
    }

    public void init() {
        operationMode = OperationMode.EXECUTION;
        keyboardMode = KeyboardMode.NORMAL;

        processor.reset();

        displayUpdateCallback.updateDisplay(stack.getStringValue(), false);
    }

    public void processButton(KeyboardButton button) {
        if (!powered) {
            return;
        }

        if (running.get()) {
            if (keyboardMode == KeyboardMode.NORMAL && button == RUN_STOP) {
                running.set(false);
            }
            return;
        }

        switch (operationMode) {
            case EXECUTION -> processButtonExecutionMode(button);
            case PROGRAMMING -> processButtonProgrammingMode(button);
        }

        switch (operationMode) {
            case EXECUTION -> displayUpdateCallback.updateDisplay(stack.getStringValue(), false);
            case PROGRAMMING ->
                    displayUpdateCallback.updateDisplay(programMemory.getStringValue(processor.getProgramCounter()),
                            false);
        }
    }

    private void processButtonExecutionMode(KeyboardButton button) {
        if (button == EEPROM_ADDRESS) {
            eepromAddressValue = stack.xOrBuffer();
            return;
        }

        if (button == EEPROM_EXCHANGE) {
            handleEepromOperation();
            return;
        }

        if (keyboardMode == KeyboardMode.NORMAL && button == RETURN) {
            processor.returnTo0();
            return;
        }
        if (keyboardMode == KeyboardMode.NORMAL && button == STEP_LEFT) {
            processor.stepLeft();
            return;
        }
        if (keyboardMode == KeyboardMode.NORMAL && button == STEP_RIGHT) {
            processor.stepRight();
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
                        execute(new Instruction(addressOpCode, code));
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
                        execute(new Instruction(OpCode.findByCode(effectiveCode)));
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
                    execute(new Instruction(opCode));
                }
                keyboardMode = KeyboardMode.NORMAL;
            }
        }
    }

    private void processButtonProgrammingMode(KeyboardButton button) {
        if (keyboardMode == KeyboardMode.NORMAL && button == STEP_LEFT) {
            processor.stepLeft();
            return;
        }
        if (keyboardMode == KeyboardMode.NORMAL && button == STEP_RIGHT) {
            processor.stepRight();
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
                        processor.storeCode(code);
                        keyboardMode = KeyboardMode.NORMAL;
                        yield OpCode.EMPTY;
                    }
                    case KeyboardMode.REGISTER -> {
                        var register = KeyboardButton.BUTTON_TO_REGISTER.get(button);
                        if (register == null) {
                            register = 0;
                        }
                        var effectiveCode = registerOpCode.code() + register;
                        processor.storeCode(effectiveCode);
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
                    processor.storeCode(opCode.code());
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
            displayUpdateCallback.updateDisplay(EMPTY_DISPLAY, false);
            powered = false;
        }
    }

    public void setTrigonometricMode(TrigonometricMode trigonometricMode) {
        processor.setTrigonometricMode(trigonometricMode);
    }

    private void execute(Instruction instruction) {
        running.set(true);
        if (async) {
            new ExecutionThread(() -> processor.execute(instruction)).start();
        } else {
            processor.execute(instruction);
        }
    }

    private void step() {
        running.set(true);
        if (async) {
            new ExecutionThread(processor::step).start();
        } else {
            processor.step();
        }
    }

    public void run() {
        running.set(true);
        if (async) {
            new ExecutionThread(processor::run).start();
        } else {
            processor.run();
        }
    }

    private void handleEepromOperation() {
        switch (eepromOperation) {
            case ERASE -> Eeprom.erase(eepromAddressValue, eepromMode);

            case READ -> {
                if (eepromMode == EepromMode.DATA) {
                    registers.copyFrom(Eeprom.readRegisters(eepromAddressValue));
                }
            }

            case WRITE -> {
                if (eepromMode == EepromMode.DATA) {
                    Eeprom.write(eepromAddressValue, registers);
                } else {
                    //
                }
            }
        }

        if (eepromOperation == EepromOperation.ERASE) {
            Eeprom.erase(eepromAddressValue, eepromMode);
        }
    }

    public void setEepromOperation(EepromOperation eepromOperation) {
        this.eepromOperation = eepromOperation;
    }

    public void setEepromMode(EepromMode eepromMode) {
        this.eepromMode = eepromMode;
    }
}
