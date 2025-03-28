/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.panteleyev.mk52.eeprom.EepromMode;
import org.panteleyev.mk52.eeprom.EepromOperation;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.panteleyev.mk52.engine.KeyboardButton.EEPROM_ADDRESS;
import static org.panteleyev.mk52.engine.KeyboardButton.D0;
import static org.panteleyev.mk52.engine.KeyboardButton.D1;
import static org.panteleyev.mk52.engine.KeyboardButton.D2;
import static org.panteleyev.mk52.engine.KeyboardButton.D3;
import static org.panteleyev.mk52.engine.KeyboardButton.D4;
import static org.panteleyev.mk52.engine.KeyboardButton.D5;
import static org.panteleyev.mk52.engine.KeyboardButton.D6;
import static org.panteleyev.mk52.engine.KeyboardButton.D7;
import static org.panteleyev.mk52.engine.KeyboardButton.D8;
import static org.panteleyev.mk52.engine.KeyboardButton.D9;
import static org.panteleyev.mk52.engine.KeyboardButton.DOT;
import static org.panteleyev.mk52.engine.KeyboardButton.EE;
import static org.panteleyev.mk52.engine.KeyboardButton.F;
import static org.panteleyev.mk52.engine.KeyboardButton.GOSUB;
import static org.panteleyev.mk52.engine.KeyboardButton.K;
import static org.panteleyev.mk52.engine.KeyboardButton.LOAD;
import static org.panteleyev.mk52.engine.KeyboardButton.MULTIPLICATION;
import static org.panteleyev.mk52.engine.KeyboardButton.PLUS;
import static org.panteleyev.mk52.engine.KeyboardButton.PUSH;
import static org.panteleyev.mk52.engine.KeyboardButton.RETURN;
import static org.panteleyev.mk52.engine.KeyboardButton.RUN_STOP;
import static org.panteleyev.mk52.engine.KeyboardButton.SIGN;
import static org.panteleyev.mk52.engine.KeyboardButton.STORE;
import static org.panteleyev.mk52.engine.KeyboardButton.SWAP;
import static org.panteleyev.mk52.engine.KeyboardButton.EEPROM_EXCHANGE;

public class EngineShortTest extends BaseTest {
    private static String displayContent = "";
    private static final Engine engine = new Engine((content, _) -> displayContent = content);

    @BeforeAll
    public static void beforeAll() {
        engine.init();
        engine.togglePower(true);
    }

    private static List<Arguments> testArguments() {
        return List.of(
                argumentSet("1:" + PASS, NOOP, List.of(D1, D2, D3, D4), " 1234.       "),
                argumentSet("2:" + PASS, NOOP, List.of(D5, D6, D7, D8), " 12345678.   "),
                argumentSet("3:" + PASS, NOOP, List.of(EE, D9, SIGN), " 12345678.-09"),
                argumentSet("4:" + PASS, NOOP, List.of(PUSH), " 1.2345678-02"),
                argumentSet("5:" + PASS, NOOP, List.of(D0, DOT, D9), " 0.9         "),
                argumentSet("6:" + PASS, NOOP, List.of(MULTIPLICATION), " 1.111111 -02"),
                argumentSet("7:" + DIFF, TR_DEGREE, List.of(F, D7), " 1.9392546-04"),
                argumentSet("8:" + PASS, TR_GRADIAN, List.of(F, D8), " 1.          "),
                argumentSet("9:" + DIFF, TR_RADIAN, List.of(F, D9), " 1.5574077   "),
                argumentSet("10:" + DIFF, NOOP, List.of(STORE, D1), " 1.5574077   "),
                argumentSet("11:" + PASS, NOOP, List.of(F, PLUS), " 3.1415926   "),
                argumentSet("12:" + PASS, (Consumer<Engine>) engine -> {
                    engine.setEepromOperation(EepromOperation.ERASE);
                    engine.setEepromMode(EepromMode.PROGRAM);
                }, List.of(EEPROM_ADDRESS), " 3.1415926   "),
                argumentSet("13:" + PASS, NOOP, List.of(EEPROM_EXCHANGE), " 3.1415926   "),
                argumentSet("14:" + DIFF, NOOP, List.of(F, D2), " 4.9714987-01"),
                argumentSet("15:" + DIFF, NOOP, List.of(STORE, D2), " 4.9714987-01"),
                argumentSet("16:" + PASS, NOOP, List.of(F, PLUS), " 3.1415926   "),
                argumentSet("17:" + PASS, (Consumer<Engine>) engine -> {
                    engine.setEepromOperation(EepromOperation.WRITE);
                    engine.setEepromMode(EepromMode.DATA);
                }, List.of(EEPROM_EXCHANGE), " 3.1415926   "),
                argumentSet("18:" + PASS, (Consumer<Engine>) engine -> {
                    engine.setEepromOperation(EepromOperation.READ);
                }, List.of(), " 3.1415926   "),
                argumentSet("19:" + PASS, POWEROFF, List.of(), "             "),
                argumentSet("20:" + PASS, (Consumer<Engine>) engine -> {
                    engine.togglePower(true);
                    engine.setEepromOperation(EepromOperation.READ);
                    engine.setEepromMode(EepromMode.DATA);
                }, List.of(F, PLUS), " 3.1415926   "),
                argumentSet("21:" + PASS, NOOP, List.of(EEPROM_ADDRESS), " 3.1415926   "),
                argumentSet("22:" + PASS, NOOP, List.of(EEPROM_EXCHANGE), " 3.1415926   "),
                argumentSet("23:" + DIFF, NOOP, List.of(LOAD, D1), " 1.5574077   "),
                argumentSet("24:" + DIFF, NOOP, List.of(LOAD, D2), " 4.9714987-01"),
                argumentSet("25:" + PASS, NOOP, List.of(F, EE), "           00"),
                argumentSet("26:" + PASS, NOOP, List.of(K, D9), "  36       01"),
                argumentSet("27:" + PASS, NOOP, List.of(K, D4), "  31 36    02"),
                argumentSet("28:" + PASS, NOOP, List.of(STORE, D3), "  43 31 36 03"),
                argumentSet("29:" + PASS, NOOP, List.of(F, SWAP), "  24 43 31 04"),
                argumentSet("30:" + PASS, NOOP, List.of(K, SIGN), "  38 24 43 05"),
                argumentSet("31:" + PASS, NOOP, List.of(F, GOSUB), "  5A 38 24 06"),
                argumentSet("32:" + PASS, NOOP, List.of(D0, D4), "  04 5A 38 07"),
                argumentSet("33:" + PASS, NOOP, List.of(RUN_STOP), "  50 04 5A 08"),
                argumentSet("34:" + DIFF, NOOP, List.of(F, SIGN), " 4.9714987-01"),
                argumentSet("34:" + DIFF, NOOP, List.of(RETURN), " 4.9714987-01"),
                argumentSet("34:" + DIFF, NOOP, List.of(RUN_STOP), " 8.DD764FF   ")
        );
    }

    @ParameterizedTest
    @MethodSource("testArguments")
    public void test(Consumer<Engine> preOperation, List<KeyboardButton> buttons, String expected) {
        preOperation.accept(engine);
        buttons.forEach(engine::processButton);
        assertEquals(expected, displayContent);
    }
}
