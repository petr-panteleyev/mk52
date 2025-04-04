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
import org.panteleyev.mk52.engine.KeyboardButton;
import org.panteleyev.mk52.engine.TrigonometricMode;

import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.panteleyev.mk52.engine.KeyboardButton.CLEAR_X;
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
import static org.panteleyev.mk52.engine.KeyboardButton.DIVISION;
import static org.panteleyev.mk52.engine.KeyboardButton.DOT;
import static org.panteleyev.mk52.engine.KeyboardButton.EE;
import static org.panteleyev.mk52.engine.KeyboardButton.EEPROM_ADDRESS;
import static org.panteleyev.mk52.engine.KeyboardButton.EEPROM_EXCHANGE;
import static org.panteleyev.mk52.engine.KeyboardButton.F;
import static org.panteleyev.mk52.engine.KeyboardButton.GOSUB;
import static org.panteleyev.mk52.engine.KeyboardButton.GOTO;
import static org.panteleyev.mk52.engine.KeyboardButton.K;
import static org.panteleyev.mk52.engine.KeyboardButton.LOAD;
import static org.panteleyev.mk52.engine.KeyboardButton.MINUS;
import static org.panteleyev.mk52.engine.KeyboardButton.MULTIPLICATION;
import static org.panteleyev.mk52.engine.KeyboardButton.PLUS;
import static org.panteleyev.mk52.engine.KeyboardButton.PUSH;
import static org.panteleyev.mk52.engine.KeyboardButton.RETURN;
import static org.panteleyev.mk52.engine.KeyboardButton.RUN_STOP;
import static org.panteleyev.mk52.engine.KeyboardButton.SIGN;
import static org.panteleyev.mk52.engine.KeyboardButton.STEP_LEFT;
import static org.panteleyev.mk52.engine.KeyboardButton.STEP_RIGHT;
import static org.panteleyev.mk52.engine.KeyboardButton.STORE;
import static org.panteleyev.mk52.engine.KeyboardButton.SWAP;

@DisplayName("Таблица 1a")
public class Table1aTest extends BaseTest {
    private static final Engine engine = new Engine(false, (_, _) -> {});

    @BeforeAll
    public static void beforeAll() {
        engine.init();
        engine.togglePower(true);
    }

    private static List<Arguments> testArguments() {
        return List.of(
                argumentSet("0:" + PASS, (Consumer<Engine>) engine -> {
                    engine.setEepromOperation(EepromOperation.ERASE);
                    engine.setEepromMode(EepromMode.PROGRAM);
                }, List.of(), " 0.          "),
                argumentSet("1:" + PASS, NOOP, List.of(D6, D1), " 61.         "),
                argumentSet("2:" + PASS, NOOP, List.of(STORE, CLEAR_X), " 61.         "),
                argumentSet("3:" + PASS, NOOP, List.of(D3, D1, D5), " 315.        "),
                argumentSet("4:" + PASS, NOOP, List.of(DOT, D0, D7), " 315.07      "),
                argumentSet("5:" + PASS, NOOP, List.of(F, PLUS), " 3.1415926   "),
                argumentSet("6:" + PASS, NOOP, List.of(EEPROM_ADDRESS), " 3.1415926   "),
                argumentSet("7:" + PASS, NOOP, List.of(EEPROM_EXCHANGE), " 3.1415926   "),
                argumentSet("8:" + PASS, NOOP, List.of(EE, D2), " 3.1415926 02"),
                argumentSet("9:" + PASS, NOOP, List.of(SWAP), " 315.07      "),
                argumentSet("10:" + PASS, NOOP, List.of(MINUS), "-9.1074   -01"),
                argumentSet("11:" + DIFF, TR_RADIAN, List.of(F, D8), " 6.131614 -01"),
                argumentSet("12:" + DIFF, TR_GRADIAN, List.of(F, D4), " 42.020499   "),
                argumentSet("13:" + DIFF, TR_DEGREE, List.of(SIGN), "-42.020499   "),
                argumentSet("14:" + PASS, NOOP, List.of(D9), " 9.          "),
                argumentSet("15:" + DIFF, NOOP, List.of(DIVISION), "-4.6689443   "),
                argumentSet("16:" + DIFF, NOOP, List.of(K, STORE, D6), "-4.6689443   "),
                argumentSet("17:" + PASS, NOOP, List.of(F, PUSH), " 9.          "),
                argumentSet("18:" + DIFF, NOOP, List.of(LOAD, D1), "-4.6689443   "),
                argumentSet("19:" + DIFF, NOOP, List.of(MULTIPLICATION), "-42.020499   "),
                argumentSet("20:" + PASS, NOOP, List.of(F, EE), "           00"),
                argumentSet("21:" + PASS, NOOP, List.of(RETURN), "  52       01"),
                argumentSet("22:" + PASS, NOOP, List.of(F, DOT), "  25 52    02"),
                argumentSet("23:" + PASS, NOOP, List.of(LOAD, D0), "  60 25 52 03"),
                argumentSet("24:" + PASS, NOOP, List.of(F, GOTO), "  58 60 25 04"),
                argumentSet("25:" + PASS, NOOP, List.of(D0, D6), "  06 58 60 05"),
                argumentSet("26:" + PASS, NOOP, List.of(D2), "  02 06 58 06"),
                argumentSet("27:" + PASS, NOOP, List.of(D4), "  04 02 06 07"),
                argumentSet("28:" + PASS, NOOP, List.of(PLUS), "  10 04 02 08"),
                argumentSet("29:" + PASS, NOOP, List.of(K, STORE, D1), "  B1 10 04 09"),
                argumentSet("30:" + PASS, NOOP, List.of(F, LOAD), "  5D B1 10 10"),
                argumentSet("31:" + PASS, NOOP, List.of(D0, D1), "  01 5D B1 11"),
                argumentSet("32:" + PASS, NOOP, List.of(MINUS), "  11 01 5D 12"),
                argumentSet("33:" + PASS, NOOP, List.of(F, DIVISION), "  23 11 01 13"),
                argumentSet("34:" + PASS, NOOP, List.of(F, RETURN), "  59 23 11 14"),
                argumentSet("35:" + PASS, NOOP, List.of(D7, D0), "  70 59 23 15"),
                argumentSet("36:" + PASS, NOOP, List.of(LOAD, EE), "  6C 70 59 16"),
                argumentSet("37:" + PASS, NOOP, List.of(PLUS), "  10 6C 70 17"),
                argumentSet("38:" + PASS, NOOP, List.of(F, D6), "  1B 10 6C 18"),
                argumentSet("39:" + PASS, NOOP, List.of(F, MINUS), "  21 1B 10 19"),
                argumentSet("40:" + PASS, NOOP, List.of(F, D3), "  18 21 1B 20"),
                argumentSet("41:" + PASS, NOOP, List.of(F, D0), "  15 18 21 21"),
                argumentSet("42:" + PASS, NOOP, List.of(F, STEP_RIGHT), "  5C 15 18 22"),
                argumentSet("43:" + PASS, NOOP, List.of(D7, D7), "  77 5C 15 23"),
                argumentSet("44:" + PASS, NOOP, List.of(D1), "  01 77 5C 24"),
                argumentSet("45:" + PASS, NOOP, List.of(D3), "  03 01 77 25"),
                argumentSet("46:" + PASS, NOOP, List.of(STORE, D0), "  40 03 01 26"),
                argumentSet("47:" + PASS, NOOP, List.of(STORE, D1), "  41 40 03 27"),
                argumentSet("48:" + PASS, NOOP, List.of(F, DOT), "  25 41 40 28"),
                argumentSet("49:" + PASS, NOOP, List.of(D4), "  04 25 41 29"),
                argumentSet("50:" + PASS, NOOP, List.of(STORE, D2), "  42 04 25 30"),
                argumentSet("51:" + PASS, NOOP, List.of(GOTO), "  51 42 04 31"),
                argumentSet("52:" + PASS, NOOP, List.of(D0, D1), "  01 51 42 32"),
                argumentSet("53:" + PASS, NOOP, List.of(F, D2), "  17 01 51 33"),
                argumentSet("54:" + PASS, NOOP, List.of(F, MULTIPLICATION), "  22 17 01 34"),
                argumentSet("55:" + PASS, NOOP, List.of(F, D7), "  1C 22 17 35"),
                argumentSet("56:" + PASS, NOOP, List.of(F, D5), "  1A 1C 22 36"),
                argumentSet("57:" + PASS, NOOP, List.of(F, D9), "  1E 1A 1C 37"),
                argumentSet("58:" + PASS, NOOP, List.of(LOAD, CLEAR_X), "  6D 1E 1A 38"),
                argumentSet("59:" + PASS, NOOP, List.of(D6), "  06 6D 1E 39"),
                argumentSet("60:" + PASS, NOOP, List.of(D1), "  01 06 6D 40"),
                argumentSet("61:" + PASS, NOOP, List.of(MINUS), "  11 01 06 41"),
                argumentSet("62:" + PASS, NOOP, List.of(F, RUN_STOP), "  57 11 01 42"),
                argumentSet("63:" + PASS, NOOP, List.of(D4, D5), "  45 57 11 43"),
                argumentSet("64:" + PASS, NOOP, List.of(SWAP), "  14 45 57 44"),
                argumentSet("65:" + PASS, NOOP, List.of(RUN_STOP), "  50 14 45 45"),
                argumentSet("66:" + PASS, NOOP, List.of(K, SIGN), "  38 50 14 46"),
                argumentSet("67:" + PASS, NOOP, List.of(K, SWAP), "  2A 38 50 47"),
                argumentSet("68:" + PASS, NOOP, List.of(K, D8), "  35 2A 38 48"),
                argumentSet("69:" + PASS, NOOP, List.of(MINUS), "  11 35 2A 49"),
                argumentSet("70:" + PASS, NOOP, List.of(K, D4), "  31 11 35 50"),
                argumentSet("71:" + PASS, NOOP, List.of(K, PLUS), "  26 31 11 51"),
                argumentSet("72:" + PASS, NOOP, List.of(LOAD, D5), "  65 26 31 52"),
                argumentSet("73:" + PASS, NOOP, List.of(K, D5), "  32 65 26 53"),
                argumentSet("74:" + PASS, NOOP, List.of(PLUS), "  10 32 65 54"),
                argumentSet("75:" + PASS, NOOP, List.of(K, D3), "  30 10 32 55"),
                argumentSet("76:" + PASS, NOOP, List.of(F, D1), "  16 30 10 56"),
                argumentSet("77:" + PASS, NOOP, List.of(PUSH), "  0E 16 30 57"),
                argumentSet("78:" + PASS, NOOP, List.of(K, D6), "  33 0E 16 58"),
                argumentSet("79:" + PASS, NOOP, List.of(K, D9), "  36 33 0E 59"),
                argumentSet("80:" + PASS, NOOP, List.of(K, D7), "  34 36 33 60"),
                argumentSet("81:" + PASS, NOOP, List.of(MULTIPLICATION), "  12 34 36 61"),
                argumentSet("82:" + PASS, NOOP, List.of(K, EE), "  39 12 34 62"),
                argumentSet("83:" + PASS, NOOP, List.of(K, CLEAR_X), "  3A 39 12 63"),
                argumentSet("84:" + PASS, NOOP, List.of(K, DOT), "  37 3A 39 64"),
                argumentSet("85:" + PASS, NOOP, List.of(K, D0), "  54 37 3A 65"),
                argumentSet("86:" + PASS, NOOP, List.of(RUN_STOP), "  50 54 37 66"),
                argumentSet("87:" + DIFF, NOOP, List.of(F, SIGN), "-42.020491   "),
                argumentSet("88:" + PASS, NOOP, List.of(GOTO, D7, D0), "-42.020491   "),
                argumentSet("89:" + PASS, NOOP, List.of(F, EE), "  00 00 00 70"),
                argumentSet("90:" + PASS, NOOP, List.of(K, GOSUB, DOT), "  AA 00 00 71"),
                argumentSet("91:" + PASS, NOOP, List.of(SWAP), "  14 AA 00 72"),
                argumentSet("92:" + PASS, NOOP, List.of(F, DOT), "  25 14 AA 73"),
                argumentSet("93:" + PASS, NOOP, List.of(K, D1), "  55 25 14 74"),
                argumentSet("94:" + PASS, NOOP, List.of(K, D2), "  56 55 25 75"),
                argumentSet("95:" + PASS, NOOP, List.of(F, SWAP), "  24 56 55 76"),
                argumentSet("96:" + PASS, NOOP, List.of(K, STEP_LEFT, D7), "  E7 24 56 77"),
                argumentSet("97:" + PASS, NOOP, List.of(K, D2), "  56 E7 24 78"),
                argumentSet("98:" + PASS, NOOP, List.of(K, D1), "  55 56 E7 79"),
                argumentSet("99:" + PASS, NOOP, List.of(LOAD, D0), "  60 55 56 80"),
                argumentSet("100:" + PASS, NOOP, List.of(RUN_STOP), "  50 60 55 81"),
                argumentSet("101:" + PASS, NOOP, List.of(D7), "  07 50 60 82"),
                argumentSet("102:" + PASS, NOOP, List.of(D2), "  02 07 50 83"),
                argumentSet("103:" + DIFF, NOOP, List.of(F, SIGN), "-42.020499   "),
                argumentSet("104:" + DIFF, NOOP, List.of(GOTO, D2, D4), "-42.020499   "),
                argumentSet("105:" + DIFF, NOOP, List.of(STEP_LEFT), "-42.020499   "),
                argumentSet("106:" + PASS, NOOP, List.of(RUN_STOP), " 1.          "),
                argumentSet("107:" + PASS, NOOP, List.of(STEP_RIGHT), " 1.          "),
                argumentSet("108:" + PASS, NOOP, List.of(GOSUB), " 2.          "),
                argumentSet("109:" + PASS, NOOP, List.of(RETURN), " 2.          "),
                argumentSet("110:" + DIFF, NOOP, List.of(RUN_STOP), " 8.60005     "),
                argumentSet("111:" + PASS, (Consumer<Engine>) engine -> {
                    engine.setEepromOperation(EepromOperation.WRITE);
                    engine.setEepromMode(EepromMode.PROGRAM);
                }, List.of(F, PLUS), " 3.1415926   "),
                argumentSet("112:" + PASS, NOOP, List.of(EEPROM_ADDRESS, EEPROM_EXCHANGE), " 3.1415926   "),
                argumentSet("113:" + PASS, (Consumer<Engine>) engine -> {
                    engine.setEepromOperation(EepromOperation.READ);
                    engine.setEepromMode(EepromMode.DATA);
                }, List.of(EEPROM_EXCHANGE), " 3.1415926   "),
                argumentSet("114:" + DIFF, NOOP, List.of(LOAD, D0), "-0.6586025 40"),
                argumentSet("115:" + PASS, POWEROFF, List.of(), "             "),
                argumentSet("116:" + PASS, (Consumer<Engine>) engine -> {
                    engine.togglePower(true);
                    engine.setEepromOperation(EepromOperation.READ);
                    engine.setEepromMode(EepromMode.PROGRAM);
                    engine.setTrigonometricMode(TrigonometricMode.GRADIAN);
                }, List.of(), " 0.          "),
                argumentSet("117:" + PASS, (Consumer<Engine>) engine -> {
                    engine.setEepromOperation(EepromOperation.ERASE);
                }, List.of(D1, D0, D0, D0, D0, D9, D8), " 1000098.    "),
                argumentSet("118:" + PASS, NOOP, List.of(EEPROM_ADDRESS, EEPROM_EXCHANGE), " 1000098.    "),
                argumentSet("119:" + PASS, NOOP, List.of(CLEAR_X, D1, D0, D2, D1, D0, D8, D4), " 1021084.    "),
                argumentSet("120:" + PASS, NOOP, List.of(EEPROM_ADDRESS, EEPROM_EXCHANGE), " 1021084.    "),
                argumentSet("121:" + PASS, NOOP, List.of(CLEAR_X, D1, D0, D6, D3, D0, D9, D8), " 1063098.    "),
                argumentSet("122:" + PASS, NOOP, List.of(EEPROM_ADDRESS, EEPROM_EXCHANGE), " 1063098.    "),
                argumentSet("123:" + PASS, NOOP, List.of(CLEAR_X, D1, D0, D8, D4, D0, D9, D8), " 1084098.    "),
                argumentSet("124:" + PASS, NOOP, List.of(EEPROM_ADDRESS, EEPROM_EXCHANGE), " 1084098.    "),
                argumentSet("125:" + PASS, NOOP, List.of(F, PLUS), " 3.1415926   "),
                argumentSet("126:" + PASS, (Consumer<Engine>) engine -> {
                    engine.setEepromOperation(EepromOperation.READ);
                }, List.of(EEPROM_ADDRESS, EEPROM_EXCHANGE), " 3.1415926   "),
                argumentSet("127:" + PASS, NOOP, List.of(CLEAR_X, D1, D0, D0, D0, D0, D8, D4), " 1000084.    "),
                argumentSet("128:" + PASS, (Consumer<Engine>) engine -> {
                    engine.setEepromOperation(EepromOperation.WRITE);
                }, List.of(EEPROM_ADDRESS, EEPROM_EXCHANGE), " 1000084.    "),
                argumentSet("129:" + PASS, (Consumer<Engine>) engine -> {
                    engine.setEepromOperation(EepromOperation.READ);
                }, List.of(EEPROM_EXCHANGE), " 1000084.    "),
                argumentSet("130:" + PASS, NOOP, List.of(CLEAR_X, D1, D0, D1, D9, D2, D8, D4), " 1019284.    "),
                argumentSet("131:" + PASS, (Consumer<Engine>) engine -> {
                    engine.setEepromOperation(EepromOperation.WRITE);
                }, List.of(EEPROM_ADDRESS, EEPROM_EXCHANGE), " 1019284.    "),
                argumentSet("132:" + PASS, (Consumer<Engine>) engine -> {
                    engine.setEepromOperation(EepromOperation.READ);
                }, List.of(EEPROM_EXCHANGE), " 1019284.    "),
                argumentSet("133:" + PASS, NOOP, List.of(CLEAR_X, D1, D0, D5, D9, D2, D9, D8), " 1059298.    "),
                argumentSet("134:" + PASS, (Consumer<Engine>) engine -> {
                    engine.setEepromOperation(EepromOperation.WRITE);
                }, List.of(EEPROM_ADDRESS, EEPROM_EXCHANGE), " 1059298.    "),
                argumentSet("135:" + PASS, (Consumer<Engine>) engine -> {
                    engine.setEepromOperation(EepromOperation.READ);
                }, List.of(EEPROM_EXCHANGE), " 1059298.    "),
                argumentSet("136:" + PASS, NOOP, List.of(CLEAR_X, D1, D0, D8, D0, D0, D9, D8), " 1080098.    "),
                argumentSet("137:" + PASS, (Consumer<Engine>) engine -> {
                    engine.setEepromOperation(EepromOperation.WRITE);
                }, List.of(EEPROM_ADDRESS, EEPROM_EXCHANGE), " 1080098.    "),
                argumentSet("138:" + PASS, (Consumer<Engine>) engine -> {
                    engine.setEepromOperation(EepromOperation.READ);
                }, List.of(EEPROM_EXCHANGE), " 1080098.    "),
                argumentSet("139:" + PASS, NOOP, List.of(CLEAR_X, D6, D1), " 61.         "),
                argumentSet("140:" + PASS, NOOP, List.of(STORE, CLEAR_X), " 61.         "),
                argumentSet("141:" + PASS, NOOP, List.of(PUSH), " 61.         "),
                argumentSet("142:" + PASS, NOOP, List.of(D5, SIGN), "-5.          "),
                argumentSet("143:" + PASS, NOOP, List.of(MULTIPLICATION), "-305.        "),
                argumentSet("144:" + PASS, NOOP, List.of(F, PUSH), "-5.          "),
                argumentSet("145:" + PASS, NOOP, List.of(GOTO, D2, D3), "-5.          "),
                argumentSet("146:" + PASS, NOOP, List.of(RUN_STOP), " 1.          "),
                argumentSet("147:" + PASS, NOOP, List.of(D2), " 2.          "),
                argumentSet("148:" + PASS, NOOP, List.of(RETURN), " 2.          "),
                argumentSet("149:" + DIFF, NOOP, List.of(RUN_STOP), " 8.00001     ")
        );
    }

    @ParameterizedTest
    @MethodSource("testArguments")
    public void test(Consumer<Engine> preOperation, List<KeyboardButton> buttons, String expected) {
        preOperation.accept(engine);
        buttons.forEach(engine::processButton);
        assertEquals(expected, engine.displayProperty().get());
    }
}
