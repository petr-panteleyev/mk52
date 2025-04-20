/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.manual;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.panteleyev.mk52.BaseTest;
import org.panteleyev.mk52.eeprom.EepromMode;
import org.panteleyev.mk52.eeprom.EepromOperation;
import org.panteleyev.mk52.engine.Engine;
import org.panteleyev.mk52.engine.IR;
import org.panteleyev.mk52.engine.KeyboardButton;

import java.util.List;
import java.util.function.Consumer;

import static java.util.List.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
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
import static org.panteleyev.mk52.engine.KeyboardButton.EEPROM_ADDRESS;
import static org.panteleyev.mk52.engine.KeyboardButton.EEPROM_EXCHANGE;
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

@DisplayName("Таблица 1")
public class Table1Test extends BaseTest {
    private static final Engine engine = new Engine(false, (_) -> {});

    @BeforeAll
    public static void beforeAll() {
        engine.init();
        engine.togglePower(true);
    }

    private static List<Arguments> testArguments() {
        return of(
                argumentSet("1:" + PASS, NOOP, of(D1, D2, D3, D4), new IR(0xFFFF_1234_FFFFL, 1 << 4)),
                argumentSet("2:" + PASS, NOOP, of(D5, D6, D7, D8), new IR(0xFFFF_12345678L, 1)),
                argumentSet("3:" + PASS, NOOP, of(EE, D9, SIGN), new IR(0xA09F_12345678L, 1)),
                argumentSet("4:" + PASS, NOOP, of(PUSH), new IR(0xA02F_1_2345678L, 1 << 7)),
                argumentSet("5:" + PASS, NOOP, of(D0, DOT, D9), new IR(0xFFFF_0_9FFFFFFL, 1 << 7)),
                argumentSet("6:" + PASS, NOOP, of(MULTIPLICATION), new IR(0xA02F_1_111111FL, 1 << 7)),
                argumentSet("7:" + PASS, TR_DEGREE, of(F, D7), new IR(0xA04F_1_9392545L, 1 << 7)),
                argumentSet("8:" + PASS, TR_GRADIAN, of(F, D8), new IR(0xFFFF_1FFF_FFFFL, 1 << 7)),
                // 1.5574078
                argumentSet("9:" + DIFF, TR_RADIAN, of(F, D9), new IR(0xFFFF_1_5574077L, 1 << 7)),
                // 1.5574078
                argumentSet("10:" + DIFF, NOOP, of(STORE, D1), new IR(0xFFFF_1_5574077L, 1 << 7)),
                argumentSet("11:" + PASS, NOOP, of(F, PLUS), IR.PI),
                argumentSet("12:" + PASS, (Consumer<Engine>) engine -> {
                    engine.setEepromOperation(EepromOperation.ERASE);
                    engine.setEepromMode(EepromMode.PROGRAM);
                }, of(EEPROM_ADDRESS), IR.PI),
                argumentSet("13:" + PASS, NOOP, of(EEPROM_EXCHANGE), IR.PI),
                // 4.9714983-01
                argumentSet("14:" + DIFF, NOOP, of(F, D2), new IR(0xA01F_4_9714987L, 1 << 7)),
                // 4.9714983-01
                argumentSet("15:" + DIFF, NOOP, of(STORE, D2), new IR(0xA01F_4_9714987L, 1 << 7)),
                argumentSet("16:" + PASS, NOOP, of(F, PLUS), IR.PI),
                argumentSet("17:" + PASS, (Consumer<Engine>) engine -> {
                    engine.setEepromOperation(EepromOperation.WRITE);
                    engine.setEepromMode(EepromMode.DATA);
                }, of(EEPROM_EXCHANGE), IR.PI),
                argumentSet("18:" + PASS,
                        (Consumer<Engine>) engine -> engine.setEepromOperation(EepromOperation.READ),
                        of(), IR.PI),
                argumentSet("19:" + PASS, POWEROFF, of(), IR.EMPTY),
                argumentSet("20:" + PASS, (Consumer<Engine>) engine -> {
                    engine.togglePower(true);
                    engine.setEepromOperation(EepromOperation.READ);
                    engine.setEepromMode(EepromMode.DATA);
                }, of(F, PLUS), IR.PI),
                argumentSet("21:" + PASS, NOOP, of(EEPROM_ADDRESS), IR.PI),
                argumentSet("22:" + PASS, NOOP, of(EEPROM_EXCHANGE), IR.PI),
                // 1.5574078
                argumentSet("23:" + DIFF, NOOP, of(LOAD, D1), new IR(0xFFFF_1_5574077L, 1 << 7)),
                // 4.9714983-01
                argumentSet("24:" + DIFF, NOOP, of(LOAD, D2), new IR(0xA01F_4_9714987L, 1 << 7)),
                argumentSet("25:" + PASS, NOOP, of(F, EE), new IR(0xF00F_FFFF_FFFFL)),
                argumentSet("26:" + PASS, NOOP, of(K, D9), new IR(0xF01F_36_FFFFFFL)),
                argumentSet("27:" + PASS, NOOP, of(K, D4), new IR(0xF02F_31_F_36_FFFL)),
                argumentSet("28:" + PASS, NOOP, of(STORE, D3), new IR(0xF03F_43_F_31_F_36L)),
                argumentSet("29:" + PASS, NOOP, of(F, SWAP), new IR(0xF04F_24_F_43_F_31L)),
                argumentSet("30:" + PASS, NOOP, of(K, SIGN), new IR(0xF05F_38_F_24_F_43L)),
                argumentSet("31:" + PASS, NOOP, of(F, GOSUB), new IR(0xF06F_5A_F_38_F_24L)),
                argumentSet("32:" + PASS, NOOP, of(D0, D4), new IR(0xF07F_04_F_5A_F_38L)),
                argumentSet("33:" + PASS, NOOP, of(RUN_STOP), new IR(0xF08F_50_F_04_F_5AL)),
                // 4.9714983-01
                argumentSet("34:" + DIFF, NOOP, of(F, SIGN), new IR(0xA01F_4_9714987L, 1 << 7)),
                // 4.9714983-01
                argumentSet("35:" + DIFF, NOOP, of(RETURN), new IR(0xA01F_4_9714987L, 1 << 7)),
                // 8.DD76578
                argumentSet("36:" + DIFF, NOOP, of(RUN_STOP), new IR(0xFFFF_8_DD764F7L, 1 << 7))
        );
    }

    @ParameterizedTest
    @MethodSource("testArguments")
    public void test(Consumer<Engine> preOperation, List<KeyboardButton> buttons, IR expected) {
        preOperation.accept(engine);
        buttons.forEach(engine::processButton);
        assertEquals(expected, engine.displayProperty().get());
    }
}
