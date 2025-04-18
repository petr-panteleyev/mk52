/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.eeprom;

import org.panteleyev.mk52.engine.IR;
import org.panteleyev.mk52.engine.Register;
import org.panteleyev.mk52.engine.Registers;
import org.panteleyev.mk52.program.Address;
import org.panteleyev.mk52.program.ProgramMemory;

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
import static org.panteleyev.mk52.engine.Constants.BYTE_0;
import static org.panteleyev.mk52.engine.Constants.DUR_023;
import static org.panteleyev.mk52.engine.Constants.EEPROM_LINE_SIZE;
import static org.panteleyev.mk52.engine.Constants.EEPROM_SIZE;
import static org.panteleyev.mk52.engine.Constants.TETRADS_PER_EEPROM_LINE;

public final class Eeprom {
    public static final Duration SET_ADDRESS_DURATION = DUR_023;
    public static final Duration RW_DURATION = Duration.of(2, ChronoUnit.SECONDS);

    private static final long EEPROM_STEPS_SHIFT = 4;

    // Каждый byte хранит одну тетраду
    private final byte[] eeprom = new byte[EEPROM_SIZE];
    private static final int MAX_STEPS = 98;

    private final ProgramMemory memory;
    private final Registers registers;

    private final AtomicReference<EepromAddress> address = new AtomicReference<>(
            valueToEepromAddress(0));

    public Eeprom(ProgramMemory memory, Registers registers) {
        this.memory = memory;
        this.registers = registers;
        clear();
    }

    // Для тестирования
    byte[] getEeprom() {
        return eeprom;
    }

    public void setAddress(long address) {
        this.address.set(valueToEepromAddress(address));
    }

    public void exchange(EepromOperation operation, EepromMode mode) {
        switch (operation) {
            case ERASE -> erase(mode);
            case READ -> read(mode);
            case WRITE -> write(mode);
        }
    }

    static EepromAddress valueToEepromAddress(long x) {
        if (Register.isZero(x)) {
            return new EepromAddress(0, 0);
        }

        x >>= EEPROM_STEPS_SHIFT;
        int steps = (int) (x & 0xF);
        x >>= 4;
        steps += (int) ((x & 0xF) * 10);
        x >>= 4;
        var addr = (int) (x & 0xF);
        x >>= 4;
        addr += (int) ((x & 0xF) * 10);
        x >>= 4;
        addr += (int) ((x & 0xF) * 100);
        x >>= 4;
        addr += (int) ((x & 0xF) * 1000);

        steps = steps - steps % EEPROM_LINE_SIZE;
        return new EepromAddress(addr, Math.min(MAX_STEPS, steps));
    }

    void clear() {
        Arrays.fill(eeprom, BYTE_0);
    }

    public void erase(EepromMode mode) {
        var addr = address.get();

        var alignedStart = EepromUtils.alignEraseStart(addr.start());
        var alignedEnd = EepromUtils.alignEraseEnd(addr.start() + 2 * addr.steps());

        synchronized (eeprom) {
            for (var index = alignedStart; index < alignedEnd; index++) {
                eeprom[normalizeEepromIndex(index)] = BYTE_0;
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
                        EepromUtils.writeEepromLine(eeprom, addr.start() + i * TETRADS_PER_EEPROM_LINE, line, mode);
                    }
                    memory.erase(addr.steps());
                }
                case DATA -> {
                    var regCount = addr.steps() / EEPROM_LINE_SIZE;
                    for (int i = 0; i < regCount; i++) {
                        var value = registers.load(new Address((byte) i, BYTE_0));
                        EepromUtils.writeRegisterToEeprom(eeprom, addr.start() + i * TETRADS_PER_EEPROM_LINE, value);
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
                        var line = EepromUtils.readEepromLine(eeprom, addr.start() + i * TETRADS_PER_EEPROM_LINE);
                        EepromUtils.memoryFromEepromLine(line, newBytes, i * EEPROM_LINE_SIZE);
                    }
                    memory.storeCodes(newBytes);
                }
                case DATA -> {
                    for (int i = 0; i < addr.steps() / EEPROM_LINE_SIZE; i++) {
                        var value = EepromUtils.readRegisterFromEeprom(eeprom,
                                addr.start() + i * TETRADS_PER_EEPROM_LINE);
                        registers.store(new Address((byte) i, BYTE_0), value);
                    }
                }
            }
        }
    }

    public void exportDump(OutputStream out) {
        synchronized (eeprom) {
            try (var writer = new OutputStreamWriter(out)) {
                for (int i = 0; i < eeprom.length; i++) {
                    if (i != 0 && i % TETRADS_PER_EEPROM_LINE == 0) {
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
            Arrays.fill(newBytes, BYTE_0);

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

    public static IR convertDisplay(IR ir) {
        long newInd = 0;
        long ind = ir.indicator();

        for (int i = 0; i < 12; i++) {
            int t = (int) (ind & 0xF);
            int newT = switch (t) {
                case 0xF -> 0xA;
                case 0 -> 0x8;
                default -> t;
            };
            newInd = Register.setTetrad(newInd, i, newT);
            ind = ind >> 4;
        }

        return new IR(newInd, ir.dots());
    }
}
