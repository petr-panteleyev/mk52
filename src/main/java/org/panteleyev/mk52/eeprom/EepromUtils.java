/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.eeprom;

import org.panteleyev.mk52.engine.UndefinedBehaviourException;
import org.panteleyev.mk52.value.DecimalValue;

import java.util.Arrays;

import static org.panteleyev.mk52.eeprom.Eeprom.EEPROM_LINE_SIZE;
import static org.panteleyev.mk52.engine.Constants.EEPROM_SIZE;
import static org.panteleyev.mk52.engine.Constants.PROGRAM_MEMORY_SIZE;
import static org.panteleyev.mk52.engine.Constants.TETRADS_PER_REGISTER;
import static org.panteleyev.mk52.engine.Constants.ZERO_BYTE;

final class EepromUtils {
    public static int normalizeEepromIndex(int index) {
        return index % EEPROM_SIZE;
    }

    public static byte[] readEepromLine(byte[] eeprom, int start, EepromMode mode) {
        var line = new byte[TETRADS_PER_REGISTER];

        if (mode == EepromMode.PROGRAM) {
            line[0] = eeprom[normalizeEepromIndex(start + TETRADS_PER_REGISTER - 2)];
            line[1] = eeprom[normalizeEepromIndex(start + TETRADS_PER_REGISTER - 1)];
            for (var index = 2; index < line.length; index++) {
                line[index] = eeprom[normalizeEepromIndex(start + index - 2)];
            }
        } else {
            for (int i = 0; i < line.length; i++) {
                line[i] = eeprom[normalizeEepromIndex(start + i)];
            }
        }
        return line;
    }

    public static void writeEepromLine(byte[] eeprom, int start, byte[] line, EepromMode mode) {
        if (line.length != TETRADS_PER_REGISTER) {
            throw new UndefinedBehaviourException("EEPROM line must be of size " + TETRADS_PER_REGISTER);
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

    public static DecimalValue valueFromEepromLine(byte[] line) {
        if (line.length != TETRADS_PER_REGISTER) {
            throw new UndefinedBehaviourException("EEPROM line must be of size " + TETRADS_PER_REGISTER);
        }

        var expSign = line[11] & 0x9;
        var sign = line[8] & 0x9;

        var exp = line[10] * 10 + line[9];

        var builder = new StringBuilder();
        for (int i = 7; i >= 0; i--) {
            char ch = (char) ((line[i] & 0xF) + '0');
            builder.append(ch);
        }

        builder.insert(1, '.');

        if (sign == 9) {
            builder.insert(0, '-');
        }

        if (exp != 0) {
            if (expSign == 9) {
                builder.append("e-").append(100 - exp);
            } else {
                builder.append("e").append(exp);
            }
        }


        try {
            return new DecimalValue(Double.parseDouble(builder.toString()));
        } catch (Exception ex) {
            return DecimalValue.ZERO;
        }
    }

    public static byte[] memoryToEepromLine(int[] memory, int start) {
        var line = new byte[TETRADS_PER_REGISTER];

        Arrays.fill(line, ZERO_BYTE);

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
