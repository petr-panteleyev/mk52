/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.eeprom;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.panteleyev.mk52.engine.Registers;
import org.panteleyev.mk52.program.ProgramMemory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.panteleyev.mk52.engine.Constants.EEPROM_SIZE;

@DisplayName("Импорт/экспорт дампа ППЗУ")
public class EepromImportExportTest {

    private static final Random RANDOM = new Random(System.currentTimeMillis());

    @Test
    public void test() throws IOException {
        var eeprom = new Eeprom(new ProgramMemory(), new Registers());

        var testBytes = new byte[EEPROM_SIZE];
        for (var i = 0; i < EEPROM_SIZE; i++) {
            testBytes[i] = (byte) RANDOM.nextInt(0x10);
        }

        System.arraycopy(testBytes, 0, eeprom.getEeprom(), 0, EEPROM_SIZE);

        try (var out = new ByteArrayOutputStream()) {
            eeprom.exportDump(out);
            eeprom.clear();

            try (var in = new ByteArrayInputStream(out.toByteArray())) {
                eeprom.importDump(in);
                assertArrayEquals(testBytes, eeprom.getEeprom());
            }
        }
    }
}
