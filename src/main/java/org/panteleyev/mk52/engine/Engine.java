/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.panteleyev.mk52.eeprom.Eeprom;
import org.panteleyev.mk52.eeprom.EepromMode;
import org.panteleyev.mk52.eeprom.EepromOperation;
import org.panteleyev.mk52.program.Address;
import org.panteleyev.mk52.program.Instruction;
import org.panteleyev.mk52.program.OpCode;
import org.panteleyev.mk52.program.ProgramMemory;
import org.panteleyev.mk52.program.StepExecutionCallback;
import org.panteleyev.mk52.program.StepExecutionResult;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.panteleyev.mk52.eeprom.Eeprom.RW_DURATION;
import static org.panteleyev.mk52.engine.Constants.EMPTY_DISPLAY;
import static org.panteleyev.mk52.engine.Constants.INITIAL_DISPLAY;
import static org.panteleyev.mk52.engine.KeyboardButton.BUTTON_TO_ADDRESS;
import static org.panteleyev.mk52.engine.KeyboardButton.EEPROM_ADDRESS;
import static org.panteleyev.mk52.engine.KeyboardButton.EEPROM_EXCHANGE;
import static org.panteleyev.mk52.engine.KeyboardButton.GOSUB;
import static org.panteleyev.mk52.engine.KeyboardButton.RETURN;
import static org.panteleyev.mk52.engine.KeyboardButton.RUN_STOP;
import static org.panteleyev.mk52.program.OpCode.EMPTY;
import static org.panteleyev.mk52.util.StringUtil.padToDisplay;

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

    private static class EepromThread extends Thread {
        EepromThread(Runnable runnable) {
            super(runnable);
            setDaemon(true);
            setName("EEPROM");
        }
    }

    private final boolean async;

    private boolean powered = false;

    private final AtomicReference<OpCode> lastExecutedOpCode = new AtomicReference<>(null);

    // Stack
    private final Stack stack = new Stack(lastExecutedOpCode);
    // Регистры
    private final Registers registers = new Registers();
    // Память программ
    private final ProgramMemory programMemory = new ProgramMemory();
    // Стек вызовов
    private final CallStack callStack = new CallStack();
    // ППЗУ
    private final Eeprom eeprom = new Eeprom(programMemory, registers);
    private final Executor eepromExecutor = Executors.newSingleThreadExecutor(EepromThread::new);

    private final StepExecutionCallback stepCallback = new StepExecutionCallback() {
        @Override
        public void before() {
            setDisplay(EMPTY_DISPLAY);
        }

        @Override
        public void after(StepExecutionResult stepExecutionResult) {
            setDisplay(stepExecutionResult.display());
            registersUpdateCallback.update(stepExecutionResult, running.get());
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

    private final RegistersUpdateCallback registersUpdateCallback;
    private final MemoryUpdateCallback memoryUpdateCallback;

    private final StringProperty displayProperty = new SimpleStringProperty(EMPTY_DISPLAY);

    public Engine(boolean async, RegistersUpdateCallback registersUpdateCallback) {
        this(async, registersUpdateCallback, MemoryUpdateCallback.NOOP);
    }

    public Engine(boolean async, RegistersUpdateCallback registersUpdateCallback,
            MemoryUpdateCallback memoryUpdateCallback) {
        this.async = async;
        this.processor = new Processor(
                async,
                stack,
                registers,
                programMemory,
                callStack,
                running,
                operationMode,
                lastExecutedOpCode,
                stepCallback
        );

        this.registersUpdateCallback = registersUpdateCallback;
        this.memoryUpdateCallback = memoryUpdateCallback;
        init();
    }

    public StringProperty displayProperty() {
        return displayProperty;
    }

    private void setDisplay(String display) {
        var text = padToDisplay(display);
        if (!async || Platform.isFxApplicationThread()) {
            displayProperty.set(text);
        } else {
            Platform.runLater(() -> displayProperty.set(text));
        }
    }

    public void init() {
        operationMode.set(OperationMode.EXECUTION);
        keyboardMode = KeyboardMode.NORMAL;
        processor.reset();
    }

    public int[] getMemoryBytes() {
        return programMemory.getMemoryBytes();
    }

    public Address getProgramCounter() {
        return processor.getProgramCounter();
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
            setEepromAddress();
            return;
        }

        if (button == EEPROM_EXCHANGE) {
            handleEepromOperation(async);
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
                        execute(new Instruction(addressOpCode, Address.of(
                                new byte[] {(byte)addressBuffer[1], (byte)addressBuffer[0]}
                        )));
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
            setDisplay(INITIAL_DISPLAY);
        }
        if (!on) {
            powered = false;
            setDisplay(EMPTY_DISPLAY);
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

        memoryUpdateCallback.store(pc.getEffectiveAddress(), code);
    }

    private void setEepromAddress() {
        if (async) {
            running.set(true);
            var eepromDisplay = Eeprom.convertDisplay(processor.getCurrentDisplay());
            setDisplay(eepromDisplay);
            eepromExecutor.execute(() -> {
                eeprom.setAddress(stack.xOrBuffer());
                processor.sleep(Eeprom.SET_ADDRESS_DURATION);
                setDisplay(processor.getCurrentDisplay());
                running.set(false);
            });
        } else {
            eeprom.setAddress(stack.xOrBuffer());
        }
    }

    private void handleEepromOperation(boolean async) {
        if (async) {
            running.set(true);
            var eepromDisplay = Eeprom.convertDisplay(processor.getCurrentDisplay());
            setDisplay(eepromDisplay);
            eepromExecutor.execute(() -> {
                eeprom.exchange(eepromOperation, eepromMode);
                processor.sleep(RW_DURATION);
                memoryUpdateCallback.store(getMemoryBytes());
                setDisplay(processor.getCurrentDisplay());
                running.set(false);
            });
        } else {
            eeprom.exchange(eepromOperation, eepromMode);
        }
    }

    public void setEepromOperation(EepromOperation eepromOperation) {
        this.eepromOperation = eepromOperation;
    }

    public void setEepromMode(EepromMode eepromMode) {
        this.eepromMode = eepromMode;
    }

    public void loadMemoryBytes(int[] bytes) {
        programMemory.storeCodes(bytes);
        memoryUpdateCallback.store(bytes);
    }

    public void exportEeprom(OutputStream out) {
        eeprom.exportDump(out);
    }

    public void importEeprom(InputStream in) {
        eeprom.importDump(in);
    }
}
