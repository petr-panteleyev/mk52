/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.panteleyev.mk52.eeprom.Eeprom;
import org.panteleyev.mk52.eeprom.EepromMode;
import org.panteleyev.mk52.eeprom.EepromOperation;
import org.panteleyev.mk52.program.Instruction;
import org.panteleyev.mk52.program.StepExecutionCallback;
import org.panteleyev.mk52.program.StepExecutionResult;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.panteleyev.mk52.engine.Constants.DISPLAY_SIZE;
import static org.panteleyev.mk52.engine.KeyboardButton.BUTTON_TO_ADDRESS;
import static org.panteleyev.mk52.engine.KeyboardButton.EEPROM_ADDRESS;
import static org.panteleyev.mk52.engine.KeyboardButton.EEPROM_EXCHANGE;
import static org.panteleyev.mk52.engine.KeyboardButton.GOSUB;
import static org.panteleyev.mk52.engine.KeyboardButton.RETURN;
import static org.panteleyev.mk52.engine.KeyboardButton.RUN_STOP;
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
            setName("Processor");
        }
    }

    private final boolean async;

    private boolean powered = false;

    private final AtomicReference<OpCode> lastExecutedOpCode = new AtomicReference<>(null);

    // Stack
    private final Stack stack = new Stack(lastExecutedOpCode);
    // Регистры
    private final Registers registers = new Registers();

    private final StepExecutionCallback stepCallback = new StepExecutionCallback() {
        @Override
        public void before() {
            displayUpdateCallback.clearDisplay();
        }

        @Override
        public void after(StepExecutionResult stepExecutionResult) {
            var content = stepExecutionResult.display() + " ".repeat(
                    DISPLAY_SIZE - stepExecutionResult.display().length());
            displayUpdateCallback.updateDisplay(content, stepExecutionResult, running.get());
        }
    };
    private final Processor processor;
    private final Executor processorExecutor = Executors.newSingleThreadExecutor(ExecutionThread::new);

    private final AtomicBoolean running = new AtomicBoolean(false);

    private final AtomicReference<OperationMode> operationMode = new AtomicReference<>(OperationMode.EXECUTION);
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
    private final MemoryUpdateCallback memoryUpdateCallback;

    public Engine(boolean async, DisplayUpdateCallback displayUpdateCallback) {
        this(async, displayUpdateCallback, MemoryUpdateCallback.NOOP);
    }

    public Engine(boolean async, DisplayUpdateCallback displayUpdateCallback,
            MemoryUpdateCallback memoryUpdateCallback) {
        this.async = async;
        this.processor = new Processor(async, stack, registers, running, operationMode, lastExecutedOpCode,
                stepCallback);
        this.displayUpdateCallback = displayUpdateCallback;
        this.memoryUpdateCallback = memoryUpdateCallback;
        init();
    }

    public void init() {
        operationMode.set(OperationMode.EXECUTION);
        keyboardMode = KeyboardMode.NORMAL;
        processor.reset();
    }

    public int[] getMemoryBytes() {
        return processor.getProgramMemory().getMemoryBytes();
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

        switch (operationMode.get()) {
            case EXECUTION -> processButtonExecutionMode(button);
            case PROGRAMMING -> processButtonProgrammingMode(button);
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
            execute(new Instruction(OpCode.RETURN));
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

                if (opCode.hasAddress()) {
                    keyboardMode = KeyboardMode.ADDRESS_DIGIT_1;
                    addressOpCode = opCode;
                    return;
                }

                execute(new Instruction(opCode));
                keyboardMode = KeyboardMode.NORMAL;
            }
        }
    }

    private void processButtonProgrammingMode(KeyboardButton button) {
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
                        storeCode(code);
                        keyboardMode = KeyboardMode.NORMAL;
                        yield OpCode.EMPTY;
                    }
                    case KeyboardMode.REGISTER -> {
                        var register = KeyboardButton.BUTTON_TO_REGISTER.get(button);
                        if (register == null) {
                            register = 0;
                        }
                        var effectiveCode = registerOpCode.code() + register;
                        storeCode(effectiveCode);
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

                if (opCode == OpCode.TO_EXECUTION_MODE || opCode == OpCode.STEP_LEFT || opCode == OpCode.STEP_RIGHT) {
                    execute(new Instruction(opCode));
                } else {
                    storeCode(opCode.code());
                }

                if (opCode.hasAddress()) {
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
            powered = false;
        }
    }

    public void setTrigonometricMode(TrigonometricMode trigonometricMode) {
        processor.setTrigonometricMode(trigonometricMode);
    }

    private void execute(Instruction instruction) {
        running.set(true);
        if (async) {
            processorExecutor.execute(() -> processor.execute(instruction));
        } else {
            processor.execute(instruction);
        }
    }

    private void step() {
        running.set(true);
        if (async) {
            processorExecutor.execute(processor::step);
        } else {
            processor.step();
        }
    }

    public void run() {
        running.set(true);
        if (async) {
            processorExecutor.execute(processor::run);
        } else {
            processor.run();
        }
    }

    public void storeCode(int code) {
        running.set(true);
        var pc = processor.getProgramCounter();

        if (async) {
            processorExecutor.execute(() -> processor.storeCode(code));
        } else {
            processor.storeCode(code);
        }

        memoryUpdateCallback.store(pc, code);
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

    public void loadMemoryBytes(int[] bytes) {
        processor.getProgramMemory().storeCodes(bytes);
        memoryUpdateCallback.store(bytes);
    }
}
