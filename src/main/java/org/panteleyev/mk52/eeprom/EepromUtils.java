/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.eeprom;

import java.util.Arrays;

import static org.panteleyev.mk52.eeprom.Eeprom.EEPROM_LINE_SIZE;
import static org.panteleyev.mk52.engine.Constants.BYTE_0;
import static org.panteleyev.mk52.engine.Constants.EEPROM_SIZE;
import static org.panteleyev.mk52.engine.Constants.PROGRAM_MEMORY_SIZE;
import static org.panteleyev.mk52.engine.Constants.TETRADS_PER_REGISTER;

final class EepromUtils {
    public static int normalizeEepromIndex(int index) {
        return index % EEPROM_SIZE;
    }

    public static byte[] readEepromLine(byte[] eeprom, int start) {
        var line = new byte[TETRADS_PER_REGISTER];
        line[0] = eeprom[normalizeEepromIndex(start + TETRADS_PER_REGISTER - 2)];
        line[1] = eeprom[normalizeEepromIndex(start + TETRADS_PER_REGISTER - 1)];
        for (var index = 2; index < line.length; index++) {
            line[index] = eeprom[normalizeEepromIndex(start + index - 2)];
        }
        return line;
    }

    public static void writeEepromLine(byte[] eeprom, int start, byte[] line, EepromMode mode) {
        if (line.length != TETRADS_PER_REGISTER) {
            throw new IllegalArgumentException("EEPROM line must be of size " + TETRADS_PER_REGISTER);
        }

        if (mode == EepromMode.PROGRAM) {
            eeprom[normalizeEepromIndex(start + TETRADS_PER_REGISTER - 2)] |= line[0];
            eeprom[normalizeEepromIndex(start + TETRADS_PER_REGISTER - 1)] |= line[1];

            for (var index = 2; index < line.length; index++) {
                eeprom[normalizeEepromIndex(start + index - 2)] |= line[index];
            }
        } else {
            for (var index = 0; index < line.length; index++) {
                eeprom[normalizeEepromIndex(start + index)] |= line[index];
            }
        }
    }

    public static long readRegisterFromEeprom(byte[] eeprom, int start) {
        long register = 0;
        for (int i = 0, shift = 0; i < TETRADS_PER_REGISTER; i++, shift += 4) {
            register |= (long) eeprom[normalizeEepromIndex(start + i)] << shift;
        }
        return register;
    }


    public static void writeRegisterToEeprom(byte[] eeprom, int start, long register) {
        for (var index = 0; index < TETRADS_PER_REGISTER; index++) {
            var tetrad = register & 0xF;
            eeprom[normalizeEepromIndex(start + index)] |= (byte) tetrad;
            register >>= 4;
        }
    }

    public static byte[] memoryToEepromLine(int[] memory, int start) {
        var line = new byte[TETRADS_PER_REGISTER];

        Arrays.fill(line, BYTE_0);

        var index = 0;
        for (int i = 0; i < EEPROM_LINE_SIZE; i++) {
            var mem = memory[(start + i) % PROGRAM_MEMORY_SIZE];
            line[index++] = (byte) (mem & 0xF);
            line[index++] = (byte) ((mem & 0xF0) >> 4);
        }

        return line;
    }

    public static void memoryFromEepromLine(byte[] line, int[] memory, int start) {
        var lineIndex = 0;
        for (int i = 0; i < EEPROM_LINE_SIZE; i++) {
            memory[start++] = line[lineIndex++] + (line[lineIndex++] << 4);
        }
    }

    private EepromUtils() {
    }
}
