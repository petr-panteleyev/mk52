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
                }, List.of(), IR.ZERO),
                argumentSet("1:" + PASS, NOOP, List.of(D6, D1), new IR(0xFFFF_61_FFFFFFL, 1 << 6)),
                argumentSet("2:" + PASS, NOOP, List.of(STORE, CLEAR_X), new IR(0xFFFF_61_FFFFFFL, 1 << 6)),
                argumentSet("3:" + PASS, NOOP, List.of(D3, D1, D5), new IR(0xFFFF_315_FFFFFL, 1 << 5)),
                argumentSet("4:" + PASS, NOOP, List.of(DOT, D0, D7), new IR(0xFFFF_315_07FFFL, 1 << 5)),
                argumentSet("5:" + PASS, NOOP, List.of(F, PLUS), IR.PI),
                argumentSet("6:" + PASS, NOOP, List.of(EEPROM_ADDRESS), IR.PI),
                argumentSet("7:" + PASS, NOOP, List.of(EEPROM_EXCHANGE), IR.PI),
                argumentSet("8:" + PASS, NOOP, List.of(EE, D2), new IR(0xF02F_3_1415926L, 1 << 7)),
                argumentSet("9:" + PASS, NOOP, List.of(SWAP), new IR(0xFFFF_315_07FFFL, 1 << 5)),
                argumentSet("10:" + PASS, NOOP, List.of(MINUS), new IR(0xA01A_9_1074FFFL, 1 << 7)),
                // 6.131614 -01
                argumentSet("11:" + DIFF, TR_RADIAN, List.of(F, D8), new IR(0xA01F_6_1316135L, 1 << 7)),
                // 42.020499
                argumentSet("12:" + DIFF, TR_GRADIAN, List.of(F, D4), new IR(0xFFFF_42_020491L, 1 << 6)),
                // 42.020499
                argumentSet("13:" + DIFF, TR_DEGREE, List.of(SIGN), new IR(0xFFFA_42_020491L, 1 << 6)),
                argumentSet("14:" + PASS, NOOP, List.of(D9), new IR(0xFFFF_9_FFFFFFFL, 1 << 7)),
                // -4.6689443
                argumentSet("15:" + DIFF, NOOP, List.of(DIVISION), new IR(0xFFFA_4_6689434L, 1 << 7)),
                // -4.6689443
                argumentSet("16:" + DIFF, NOOP, List.of(K, STORE, D6), new IR(0xFFFA_4_6689434L, 1 << 7)),
                argumentSet("17:" + PASS, NOOP, List.of(F, PUSH), new IR(0xFFFF_9_FFFFFFFL, 1 << 7)),
                // -4.6689443
                argumentSet("18:" + DIFF, NOOP, List.of(LOAD, D1), new IR(0xFFFA_4_6689434L, 1 << 7)),
                // -42.020499
                argumentSet("19:" + DIFF, NOOP, List.of(MULTIPLICATION), new IR(0xFFFA_42_020491L, 1 << 6)),
                argumentSet("20:" + PASS, NOOP, List.of(F, EE), new IR(0xF00F_FFFF_FFFFL)),
                argumentSet("21:" + PASS, NOOP, List.of(RETURN), new IR(0xF01F_52_FF_FFFFL)),
                argumentSet("22:" + PASS, NOOP, List.of(F, DOT), new IR(0xF02F_25_F_52_FFFL)),
                argumentSet("23:" + PASS, NOOP, List.of(LOAD, D0), new IR(0xF03F_60_F_25_F_52L)),
                argumentSet("24:" + PASS, NOOP, List.of(F, GOTO), new IR(0xF04F_58_F_60_F_25L)),
                argumentSet("25:" + PASS, NOOP, List.of(D0, D6), new IR(0xF05F_06_F_58_F_60L)),
                argumentSet("26:" + PASS, NOOP, List.of(D2), new IR(0xF06F_02_F_06_F_58L)),
                argumentSet("27:" + PASS, NOOP, List.of(D4), new IR(0xF07F_04_F_02_F_06L)),
                argumentSet("28:" + PASS, NOOP, List.of(PLUS), new IR(0xF08F_10_F_04_F_02L)),
                argumentSet("29:" + PASS, NOOP, List.of(K, STORE, D1), new IR(0xF09F_B1_F_10_F_04L)),
                argumentSet("30:" + PASS, NOOP, List.of(F, LOAD), new IR(0xF10F_5D_F_B1_F_10L)),
                argumentSet("31:" + PASS, NOOP, List.of(D0, D1), new IR(0xF11F_01_F_5D_F_B1L)),
                argumentSet("32:" + PASS, NOOP, List.of(MINUS), new IR(0xF12F_11_F_01_F_5DL)),
                argumentSet("33:" + PASS, NOOP, List.of(F, DIVISION), new IR(0xF13F_23_F_11_F_01L)),
                argumentSet("34:" + PASS, NOOP, List.of(F, RETURN), new IR(0xF14F_59_F_23_F_11L)),
                argumentSet("35:" + PASS, NOOP, List.of(D7, D0), new IR(0xF15F_70_F_59_F_23L)),
                argumentSet("36:" + PASS, NOOP, List.of(LOAD, EE), new IR(0xF16F_6C_F_70_F_59L)),
                argumentSet("37:" + PASS, NOOP, List.of(PLUS), new IR(0xF17F_10_F_6C_F_70L)),
                argumentSet("38:" + PASS, NOOP, List.of(F, D6), new IR(0xF18F_1B_F_10_F_6CL)),
                argumentSet("39:" + PASS, NOOP, List.of(F, MINUS), new IR(0xF19F_21_F_1B_F_10L)),
                argumentSet("40:" + PASS, NOOP, List.of(F, D3), new IR(0xF20F_18_F_21_F_1BL)),
                argumentSet("41:" + PASS, NOOP, List.of(F, D0), new IR(0xF21F_15_F_18_F_21L)),
                argumentSet("42:" + PASS, NOOP, List.of(F, STEP_RIGHT), new IR(0xF22F_5C_F_15_F_18L)),
                argumentSet("43:" + PASS, NOOP, List.of(D7, D7), new IR(0xF23F_77_F_5C_F_15L)),
                argumentSet("44:" + PASS, NOOP, List.of(D1), new IR(0xF24F_01_F_77_F_5CL)),
                argumentSet("45:" + PASS, NOOP, List.of(D3), new IR(0xF25F_03_F_01_F_77L)),
                argumentSet("46:" + PASS, NOOP, List.of(STORE, D0), new IR(0xF26F_40_F_03_F_01L)),
                argumentSet("47:" + PASS, NOOP, List.of(STORE, D1), new IR(0xF27F_41_F_40_F_03L)),
                argumentSet("48:" + PASS, NOOP, List.of(F, DOT), new IR(0xF28F_25_F_41_F_40L)),
                argumentSet("49:" + PASS, NOOP, List.of(D4), new IR(0xF29F_04_F_25_F_41L)),
                argumentSet("50:" + PASS, NOOP, List.of(STORE, D2), new IR(0xF30F_42_F_04_F_25L)),
                argumentSet("51:" + PASS, NOOP, List.of(GOTO), new IR(0xF31F_51_F_42_F_04L)),
                argumentSet("52:" + PASS, NOOP, List.of(D0, D1), new IR(0xF32F_01_F_51_F_42L)),
                argumentSet("53:" + PASS, NOOP, List.of(F, D2), new IR(0xF33F_17_F_01_F_51L)),
                argumentSet("54:" + PASS, NOOP, List.of(F, MULTIPLICATION), new IR(0xF34F_22_F_17_F_01L)),
                argumentSet("55:" + PASS, NOOP, List.of(F, D7), new IR(0xF35F_1C_F_22_F_17L)),
                argumentSet("56:" + PASS, NOOP, List.of(F, D5), new IR(0xF36F_1A_F_1C_F_22L)),
                argumentSet("57:" + PASS, NOOP, List.of(F, D9), new IR(0xF37F_1E_F_1A_F_1CL)),
                argumentSet("58:" + PASS, NOOP, List.of(LOAD, CLEAR_X), new IR(0xF38F_6D_F_1E_F_1AL)),
                argumentSet("59:" + PASS, NOOP, List.of(D6), new IR(0xF39F_06_F_6D_F_1EL)),
                argumentSet("60:" + PASS, NOOP, List.of(D1), new IR(0xF40F_01_F_06_F_6DL)),
                argumentSet("61:" + PASS, NOOP, List.of(MINUS), new IR(0xF41F_11_F_01_F_06L)),
                argumentSet("62:" + PASS, NOOP, List.of(F, RUN_STOP), new IR(0xF42F57F11F01L)),
                argumentSet("63:" + PASS, NOOP, List.of(D4, D5), new IR(0xF43F45F57F11L)),
                argumentSet("64:" + PASS, NOOP, List.of(SWAP), new IR(0xF44F14F45F57L)),
                argumentSet("65:" + PASS, NOOP, List.of(RUN_STOP), new IR(0xF45F50F14F45L)),
                argumentSet("66:" + PASS, NOOP, List.of(K, SIGN), new IR(0xF46F38F50F14L)),
                argumentSet("67:" + PASS, NOOP, List.of(K, SWAP), new IR(0xF47F2AF38F50L)),
                argumentSet("68:" + PASS, NOOP, List.of(K, D8), new IR(0xF48F35F2AF38L)),
                argumentSet("69:" + PASS, NOOP, List.of(MINUS), new IR(0xF49F11F35F2AL)),
                argumentSet("70:" + PASS, NOOP, List.of(K, D4), new IR(0xF50F31F11F35L)),
                argumentSet("71:" + PASS, NOOP, List.of(K, PLUS), new IR(0xF51F26F31F11L)),
                argumentSet("72:" + PASS, NOOP, List.of(LOAD, D5), new IR(0xF52F65F26F31L)),
                argumentSet("73:" + PASS, NOOP, List.of(K, D5), new IR(0xF53F32F65F26L)),
                argumentSet("74:" + PASS, NOOP, List.of(PLUS), new IR(0xF54F10F32F65L)),
                argumentSet("75:" + PASS, NOOP, List.of(K, D3), new IR(0xF55F30F10F32L)),
                argumentSet("76:" + PASS, NOOP, List.of(F, D1), new IR(0xF56F16F30F10L)),
                argumentSet("77:" + PASS, NOOP, List.of(PUSH), new IR(0xF57F0EF16F30L)),
                argumentSet("78:" + PASS, NOOP, List.of(K, D6), new IR(0xF58F33F0EF16L)),
                argumentSet("79:" + PASS, NOOP, List.of(K, D9), new IR(0xF59F36F33F0EL)),
                argumentSet("80:" + PASS, NOOP, List.of(K, D7), new IR(0xF60F34F36F33L)),
                argumentSet("81:" + PASS, NOOP, List.of(MULTIPLICATION), new IR(0xF61F12F34F36L)),
                argumentSet("82:" + PASS, NOOP, List.of(K, EE), new IR(0xF62F39F12F34L)),
                argumentSet("83:" + PASS, NOOP, List.of(K, CLEAR_X), new IR(0xF63F3AF39F12L)),
                argumentSet("84:" + PASS, NOOP, List.of(K, DOT), new IR(0xF64F37F3AF39L)),
                argumentSet("85:" + PASS, NOOP, List.of(K, D0), new IR(0xF65F54F37F3AL)),
                argumentSet("86:" + PASS, NOOP, List.of(RUN_STOP), new IR(0xF66F50F54F37L)),
                // -42.020499
                argumentSet("87:" + DIFF, NOOP, List.of(F, SIGN), new IR(0xFFFA_42_020491L, 1 << 6)),
                // -42.020499
                argumentSet("88:" + DIFF, NOOP, List.of(GOTO, D7, D0), new IR(0xFFFA_42_020491L, 1 << 6)),
                argumentSet("89:" + PASS, NOOP, List.of(F, EE), new IR(0xF70F00F00F00L)),
                argumentSet("90:" + PASS, NOOP, List.of(K, GOSUB, DOT), new IR(0xF71FAAF00F00L)),
                argumentSet("91:" + PASS, NOOP, List.of(SWAP), new IR(0xF72F14FAAF00L)),
                argumentSet("92:" + PASS, NOOP, List.of(F, DOT), new IR(0xF73F25F14FAAL)),
                argumentSet("93:" + PASS, NOOP, List.of(K, D1), new IR(0xF74F55F25F14L)),
                argumentSet("94:" + PASS, NOOP, List.of(K, D2), new IR(0xF75F56F55F25L)),
                argumentSet("95:" + PASS, NOOP, List.of(F, SWAP), new IR(0xF76F24F56F55L)),
                argumentSet("96:" + PASS, NOOP, List.of(K, STEP_LEFT, D7), new IR(0xF77FE7F24F56L)),
                argumentSet("97:" + PASS, NOOP, List.of(K, D2), new IR(0xF78F56FE7F24L)),
                argumentSet("98:" + PASS, NOOP, List.of(K, D1), new IR(0xF79F55F56FE7L)),
                argumentSet("99:" + PASS, NOOP, List.of(LOAD, D0), new IR(0xF80F60F55F56L)),
                argumentSet("100:" + PASS, NOOP, List.of(RUN_STOP), new IR(0xF81F50F60F55L)),
                argumentSet("101:" + PASS, NOOP, List.of(D7), new IR(0xF82F07F50F60L)),
                argumentSet("102:" + PASS, NOOP, List.of(D2), new IR(0xF83F02F07F50L)),
                // -42.020499
                argumentSet("103:" + DIFF, NOOP, List.of(F, SIGN), new IR(0xFFFA_42_020491L, 1 << 6)),
                // -42.020499
                argumentSet("104:" + DIFF, NOOP, List.of(GOTO, D2, D4), new IR(0xFFFA_42_020491L, 1 << 6)),
                // -42.020499
                argumentSet("105:" + DIFF, NOOP, List.of(STEP_LEFT), new IR(0xFFFA_42_020491L, 1 << 6)),
                argumentSet("106:" + PASS, NOOP, List.of(RUN_STOP), IR.ONE),
                argumentSet("107:" + PASS, NOOP, List.of(STEP_RIGHT), IR.ONE),
                argumentSet("108:" + PASS, NOOP, List.of(GOSUB), IR.TWO),
                argumentSet("109:" + PASS, NOOP, List.of(RETURN), IR.TWO),
                argumentSet("110:" + PASS, NOOP, List.of(RUN_STOP), new IR(0xFFFF_8600_05FFL, 1 << 7)),
                argumentSet("111:" + PASS, (Consumer<Engine>) engine -> {
                    engine.setEepromOperation(EepromOperation.WRITE);
                    engine.setEepromMode(EepromMode.PROGRAM);
                }, List.of(F, PLUS), IR.PI),
                argumentSet("112:" + PASS, NOOP, List.of(EEPROM_ADDRESS, EEPROM_EXCHANGE), IR.PI),
                argumentSet("113:" + PASS, (Consumer<Engine>) engine -> {
                    engine.setEepromOperation(EepromOperation.READ);
                    engine.setEepromMode(EepromMode.DATA);
                }, List.of(EEPROM_EXCHANGE), IR.PI),
                argumentSet("114:" + PASS, NOOP, List.of(LOAD, D0), new IR(0xF40A_0658_6025L, 1 << 7)),
                argumentSet("115:" + PASS, POWEROFF, List.of(), IR.EMPTY),
                argumentSet("116:" + PASS, (Consumer<Engine>) engine -> {
                    engine.togglePower(true);
                    engine.setEepromOperation(EepromOperation.READ);
                    engine.setEepromMode(EepromMode.PROGRAM);
                    engine.setTrigonometricMode(TrigonometricMode.GRADIAN);
                }, List.of(), IR.ZERO),
                argumentSet("117:" + PASS, (Consumer<Engine>) engine -> {
                    engine.setEepromOperation(EepromOperation.ERASE);
                }, List.of(D1, D0, D0, D0, D0, D9, D8), new IR(0xFFFF_1000098FL, 1 << 1)),
                argumentSet("118:" + PASS, NOOP, List.of(EEPROM_ADDRESS, EEPROM_EXCHANGE),
                        new IR(0xFFFF_1000098FL, 1 << 1)),
                argumentSet("119:" + PASS, NOOP, List.of(CLEAR_X, D1, D0, D2, D1, D0, D8, D4),
                        new IR(0xFFFF_1021084FL, 1 << 1)),
                argumentSet("120:" + PASS, NOOP, List.of(EEPROM_ADDRESS, EEPROM_EXCHANGE),
                        new IR(0xFFFF_1021084FL, 1 << 1)),
                argumentSet("121:" + PASS, NOOP, List.of(CLEAR_X, D1, D0, D6, D3, D0, D9, D8),
                        new IR(0xFFFF_1063098FL, 1 << 1)),
                argumentSet("122:" + PASS, NOOP, List.of(EEPROM_ADDRESS, EEPROM_EXCHANGE),
                        new IR(0xFFFF_1063098FL, 1 << 1)),
                argumentSet("123:" + PASS, NOOP, List.of(CLEAR_X, D1, D0, D8, D4, D0, D9, D8),
                        new IR(0xFFFF_1084098FL, 1 << 1)),
                argumentSet("124:" + PASS, NOOP, List.of(EEPROM_ADDRESS, EEPROM_EXCHANGE),
                        new IR(0xFFFF_1084098FL, 1 << 1)),
                argumentSet("125:" + PASS, NOOP, List.of(F, PLUS), IR.PI),
                argumentSet("126:" + PASS, (Consumer<Engine>) engine -> {
                    engine.setEepromOperation(EepromOperation.READ);
                }, List.of(EEPROM_ADDRESS, EEPROM_EXCHANGE), IR.PI),
                argumentSet("127:" + PASS, NOOP, List.of(CLEAR_X, D1, D0, D0, D0, D0, D8, D4),
                        new IR(0xFFFF_1000084FL, 1 << 1)),
                argumentSet("128:" + PASS, (Consumer<Engine>) engine -> {
                    engine.setEepromOperation(EepromOperation.WRITE);
                }, List.of(EEPROM_ADDRESS, EEPROM_EXCHANGE), new IR(0xFFFF_1000084FL, 1 << 1)),
                argumentSet("129:" + PASS, (Consumer<Engine>) engine -> {
                    engine.setEepromOperation(EepromOperation.READ);
                }, List.of(EEPROM_EXCHANGE), new IR(0xFFFF_1000084FL, 1 << 1)),
                argumentSet("130:" + PASS, NOOP, List.of(CLEAR_X, D1, D0, D1, D9, D2, D8, D4),
                        new IR(0xFFFF_1019284FL, 1 << 1)),
                argumentSet("131:" + PASS, (Consumer<Engine>) engine -> {
                    engine.setEepromOperation(EepromOperation.WRITE);
                }, List.of(EEPROM_ADDRESS, EEPROM_EXCHANGE), new IR(0xFFFF_1019284FL, 1 << 1)),
                argumentSet("132:" + PASS, (Consumer<Engine>) engine -> {
                    engine.setEepromOperation(EepromOperation.READ);
                }, List.of(EEPROM_EXCHANGE), new IR(0xFFFF_1019284FL, 1 << 1)),
                argumentSet("133:" + PASS, NOOP, List.of(CLEAR_X, D1, D0, D5, D9, D2, D9, D8),
                        new IR(0xFFFF_1059298FL, 1 << 1)),
                argumentSet("134:" + PASS, (Consumer<Engine>) engine -> {
                    engine.setEepromOperation(EepromOperation.WRITE);
                }, List.of(EEPROM_ADDRESS, EEPROM_EXCHANGE), new IR(0xFFFF_1059298FL, 1 << 1)),
                argumentSet("135:" + PASS, (Consumer<Engine>) engine -> {
                    engine.setEepromOperation(EepromOperation.READ);
                }, List.of(EEPROM_EXCHANGE), new IR(0xFFFF_1059298FL, 1 << 1)),
                argumentSet("136:" + PASS, NOOP, List.of(CLEAR_X, D1, D0, D8, D0, D0, D9, D8),
                        new IR(0xFFFF_1080098FL, 1 << 1)),
                argumentSet("137:" + PASS, (Consumer<Engine>) engine -> {
                    engine.setEepromOperation(EepromOperation.WRITE);
                }, List.of(EEPROM_ADDRESS, EEPROM_EXCHANGE), new IR(0xFFFF_1080098FL, 1 << 1)),
                argumentSet("138:" + PASS, (Consumer<Engine>) engine -> {
                    engine.setEepromOperation(EepromOperation.READ);
                }, List.of(EEPROM_EXCHANGE), new IR(0xFFFF_1080098FL, 1 << 1)),
                argumentSet("139:" + PASS, NOOP, List.of(CLEAR_X, D6, D1), new IR(0xFFFF_61FF_FFFFL, 1 << 6)),
                argumentSet("140:" + PASS, NOOP, List.of(STORE, CLEAR_X), new IR(0xFFFF_61FF_FFFFL, 1 << 6)),
                argumentSet("141:" + PASS, NOOP, List.of(PUSH), new IR(0xFFFF_61FF_FFFFL, 1 << 6)),
                argumentSet("142:" + PASS, NOOP, List.of(D5, SIGN), new IR(0xFFFA_5FFF_FFFFL, 1 << 7)),
                argumentSet("143:" + PASS, NOOP, List.of(MULTIPLICATION), new IR(0xFFFA_305F_FFFFL, 1 << 5)),
                argumentSet("144:" + PASS, NOOP, List.of(F, PUSH), new IR(0xFFFA_5FFF_FFFFL, 1 << 7)),
                argumentSet("145:" + PASS, NOOP, List.of(GOTO, D2, D3), new IR(0xFFFA_5FFF_FFFFL, 1 << 7)),
                argumentSet("146:" + PASS, NOOP, List.of(RUN_STOP), IR.ONE),
                argumentSet("147:" + PASS, NOOP, List.of(D2), IR.TWO),
                argumentSet("148:" + PASS, NOOP, List.of(RETURN), IR.TWO),
                // 8.00001
                argumentSet("149:" + DIFF, NOOP, List.of(RUN_STOP), new IR(0xFFFF_8110_1FFFL, 1 << 7))
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
