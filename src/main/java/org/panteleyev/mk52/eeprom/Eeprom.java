/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.eeprom;

import org.panteleyev.mk52.engine.Registers;
import org.panteleyev.mk52.program.ProgramMemory;
import org.panteleyev.mk52.value.DecimalValue;
import org.panteleyev.mk52.value.Value;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.panteleyev.mk52.eeprom.EepromUtils.normalizeEepromIndex;
import static org.panteleyev.mk52.engine.Constants.DUR_023;
import static org.panteleyev.mk52.engine.Constants.EEPROM_SIZE;
import static org.panteleyev.mk52.engine.Constants.TETRADS_PER_REGISTER;
import static org.panteleyev.mk52.engine.Constants.ZERO_BYTE;
import static org.panteleyev.mk52.util.StringUtil.padToDisplay;

public final class Eeprom {
    public static final Duration SET_ADDRESS_DURATION = DUR_023;
    public static final Duration RW_DURATION = Duration.of(2, ChronoUnit.SECONDS);

    // Каждый byte хранит одну тетраду
    private final byte[] eeprom = new byte[EEPROM_SIZE];
    private static final int EEPROM_ADDRESS_STRING_SIZE = 8;       // 7 + пробел в начале
    private static final int MAX_STEPS = 98;
    public static final int EEPROM_LINE_SIZE = TETRADS_PER_REGISTER / 2;
    private static final int EEPROM_ADDRESS_ALIGNMENT = 16;

    private final ProgramMemory memory;
    private final Registers registers;

    private final AtomicReference<EepromAddress> address = new AtomicReference<>(
            valueToEepromAddress(DecimalValue.ZERO));

    public Eeprom(ProgramMemory memory, Registers registers) {
        this.memory = memory;
        this.registers = registers;
        clear();
    }

    // Для тестирования
    byte[] getEeprom() {
        return eeprom;
    }

    public void setAddress(Value address) {
        this.address.set(valueToEepromAddress(address));
    }

    public void exchange(EepromOperation operation, EepromMode mode) {
        switch (operation) {
            case ERASE -> erase(mode);
            case READ -> read(mode);
            case WRITE -> write(mode);
        }
    }

    static EepromAddress valueToEepromAddress(Value value) {
        if (value == null) {
            return new EepromAddress(0, 0);
        }

        var str = value.asString().replace(".", "");
        if (str.length() >= EEPROM_ADDRESS_STRING_SIZE) {
            str = str.substring(0, EEPROM_ADDRESS_STRING_SIZE).stripTrailing();
        }

        if (str.length() < EEPROM_ADDRESS_STRING_SIZE) {
            return new EepromAddress(0, 0);
        }

        var addrString = str.substring(2, 6);
        var addr = Integer.parseInt(addrString);
        var stepsString = str.substring(6);
        var steps = Integer.parseInt(stepsString);
        steps = steps - steps % EEPROM_LINE_SIZE;
        return new EepromAddress(addr, Math.min(MAX_STEPS, steps));
    }

    void clear() {
        Arrays.fill(eeprom, ZERO_BYTE);
    }

    public void erase(EepromMode mode) {
        var addr = address.get();
        synchronized (eeprom) {
            for (var index = addr.start(); index < 2 * addr.steps(); index++) {
                eeprom[normalizeEepromIndex(index)] = ZERO_BYTE;
            }

            switch (mode) {
                case PROGRAM -> memory.erase(addr.steps());
                case DATA -> registers.erase(addr.steps() / EEPROM_LINE_SIZE);
            }
        }
    }

    public void write(EepromMode mode) {
        var addr = address.get();
        synchronized (eeprom) {
            switch (mode) {
                case PROGRAM -> {
                    var mem = memory.getMemoryBytes();
                    for (int i = 0; i < addr.steps() / EEPROM_LINE_SIZE; i++) {
                        var line = EepromUtils.memoryToEepromLine(mem, i * EEPROM_LINE_SIZE);
                        EepromUtils.writeEepromLine(eeprom, addr.start() + i * TETRADS_PER_REGISTER, line, mode);
                    }
                    memory.erase(addr.steps());
                }
                case DATA -> {
                    var regCount = addr.steps() / EEPROM_LINE_SIZE;
                    for (int i = 0; i < regCount; i++) {
                        var registerValue = registers.load(i);
                        var line = registerValue.toByteArray();
                        EepromUtils.writeEepromLine(eeprom, addr.start() + i * TETRADS_PER_REGISTER, line, mode);
                    }
                    registers.erase(regCount);
                }
            }
        }
    }

    public void read(EepromMode mode) {
        var addr = address.get();
        synchronized (eeprom) {
            switch (mode) {
                case PROGRAM -> {
                    var newBytes = new int[addr.steps()];
                    for (int i = 0; i < addr.steps() / EEPROM_LINE_SIZE; i++) {
                        var line = EepromUtils.readEepromLine(eeprom, addr.start() + i * TETRADS_PER_REGISTER, mode);
                        EepromUtils.memoryFromEepromLine(line, newBytes, i * EEPROM_LINE_SIZE);
                    }
                    memory.storeCodes(newBytes);
                }
                case DATA -> {
                    for (int i = 0; i < addr.steps() / EEPROM_LINE_SIZE; i++) {
                        var line = EepromUtils.readEepromLine(eeprom, addr.start() + i * TETRADS_PER_REGISTER, mode);
                        var value = EepromUtils.valueFromEepromLine(line);
                        registers.store(i, value);
                    }
                }
            }
        }
    }

    public void exportDump(OutputStream out) {
        synchronized (eeprom) {
            try (var writer = new OutputStreamWriter(out)) {
                for (int i = 0; i < eeprom.length; i++) {
                    if (i != 0 && i % TETRADS_PER_REGISTER == 0) {
                        writer.write("\n");
                    }
                    writer.write(String.format("%1X ", eeprom[i] & 0xF));
                }
                writer.flush();
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    public void importDump(InputStream in) {
        synchronized (eeprom) {
            var newBytes = new byte[EEPROM_SIZE];
            Arrays.fill(newBytes, ZERO_BYTE);

            var byteIndex = new AtomicInteger(0);
            try (var reader = new BufferedReader(new InputStreamReader(in))) {
                reader.lines().forEach(line -> {
                    var byteStrings = line.split(" ");
                    for (var str : byteStrings) {
                        if (byteIndex.get() >= newBytes.length) {
                            break;
                        }
                        newBytes[byteIndex.getAndIncrement()] = Byte.parseByte(str, 16);
                    }
                });

                System.arraycopy(newBytes, 0, eeprom, 0, newBytes.length);
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        }
    }

    public static String convertDisplay(String text) {
        text = padToDisplay(text);
        var builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            var ch = text.charAt(i);
            var conv = switch (ch) {
                case ' ', '.' -> '-';
                case '0' -> '8';
                default -> ch;
            };
            builder.append(conv);
        }
        return builder.toString();
    }
}
