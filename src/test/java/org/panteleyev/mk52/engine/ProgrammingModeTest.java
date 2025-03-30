/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

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

@DisplayName("Ввод в режиме Программирование")
public class ProgrammingModeTest extends BaseTest {
    private static String displayContent = "";
    private static final Engine engine = new Engine(false, (content, _, _) -> displayContent = content);

    @BeforeAll
    public static void beforeAll() {
        engine.init();
        engine.togglePower(true);
    }

    private static List<Arguments> testArguments() {
        return List.of(
                argumentSet("ПРГ", NOOP, List.of(F, EE), "           00"),
                argumentSet("0 1 2",
                        NOOP, List.of(D0, D1, D2), "  02 01 00 03"),
                argumentSet("3 4 5",
                        NOOP, List.of(D3, D4, D5), "  05 04 03 06"),
                argumentSet("6 7 8",
                        NOOP, List.of(D6, D7, D8), "  08 07 06 09"),
                argumentSet("9 + -",
                        NOOP, List.of(D9, PLUS, MINUS), "  11 10 09 12"),
                argumentSet("* / ↔",
                        NOOP, List.of(MULTIPLICATION, DIVISION, SWAP), "  14 13 12 15"),
                argumentSet("В↑ . /-/",
                        NOOP, List.of(PUSH, DOT, SIGN), "  0B 0A 0E 18"),
                argumentSet("ВП Cx С/П",
                        NOOP, List.of(EE, CLEAR_X, RUN_STOP), "  50 0D 0C 21"),
                argumentSet("БП 12 В/О",
                        NOOP, List.of(GOTO, D1, D2, RETURN), "  52 12 51 24"),
                argumentSet("ПП -2",
                        NOOP, List.of(GOSUB, DOT, D2), "  A2 53 52 26"),
                argumentSet("F 10ˣ F lg F ln",
                        NOOP, List.of(F, D0, F, D2, F, D3), "  18 17 15 29"),
                argumentSet("F eˣ F sin⁻¹ F cos⁻¹",
                        NOOP, List.of(F, D1, F, D4, F, D5), "  1A 19 16 32"),
                argumentSet("F tg⁻¹ F sin F cos",
                        NOOP, List.of(F, D6, F, D7, F, D8), "  1D 1C 1B 35"),
                argumentSet("F tg F π F √",
                        NOOP, List.of(F, D9, F, PLUS, F, MINUS), "  21 20 1E 38"),
                argumentSet("F x² F 1/x F xy",
                        NOOP, List.of(F, MULTIPLICATION, F, DIVISION, F, SWAP), "  24 23 22 41"),
                argumentSet("F CF F Вх F o",
                        NOOP, List.of(F, CLEAR_X, F, PUSH, F, DOT), "  25 0F 24 43"),
                argumentSet("F x<0 12",
                        NOOP, List.of(F, STEP_RIGHT, D1, D2), "  12 5C 25 45"),
                argumentSet("F x=0 34",
                        NOOP, List.of(F, STEP_LEFT, D3, D4), "  34 5E 12 47"),
                argumentSet("F x≥0 56",
                        NOOP, List.of(F, RETURN, D5, D6), "  56 59 34 49"),
                argumentSet("F x≠0 -3",
                        NOOP, List.of(F, RUN_STOP, DOT, D3), "  A3 57 56 51"),
                argumentSet("F L0 12",
                        NOOP, List.of(F, LOAD, D1, D2), "  12 5D A3 53"),
                argumentSet("F L1 34",
                        NOOP, List.of(F, STORE, D3, D4), "  34 5B 12 55"),
                argumentSet("F L2 56",
                        NOOP, List.of(F, GOTO, D5, D6), "  56 58 34 57"),
                argumentSet("F L3 -4",
                        NOOP, List.of(F, GOSUB, DOT, D4), "  A4 5A 56 59"),
                // x→П
                argumentSet("x→П 0 x→П 1 x→П 2",
                        NOOP, List.of(STORE, D0, STORE, D1, STORE, D2), "  42 41 40 62"),
                argumentSet("x→П 3 x→П 4 x→П 5",
                        NOOP, List.of(STORE, D3, STORE, D4, STORE, D5), "  45 44 43 65"),
                argumentSet("x→П 6 x→П 7 x→П 8",
                        NOOP, List.of(STORE, D6, STORE, D7, STORE, D8), "  48 47 46 68"),
                argumentSet("x→П 9 x→П a x→П b",
                        NOOP, List.of(STORE, D9, STORE, DOT, STORE, SIGN), "  4B 4A 49 71"),
                argumentSet("x→П c x→П d x→П e",
                        NOOP, List.of(STORE, EE, STORE, CLEAR_X, STORE, PUSH), "  4E 4D 4C 74"),
                // П→x
                argumentSet("П→x 0 П→x 1 П→x 2",
                        NOOP, List.of(LOAD, D0, LOAD, D1, LOAD, D2), "  62 61 60 77"),
                argumentSet("П→x 3 П→x 4 П→x 5",
                        NOOP, List.of(LOAD, D3, LOAD, D4, LOAD, D5), "  65 64 63 80"),
                argumentSet("П→x 6 П→x 7 П→x 8",
                        NOOP, List.of(LOAD, D6, LOAD, D7, LOAD, D8), "  68 67 66 83"),
                argumentSet("П→x 9 П→x a П→x b",
                        NOOP, List.of(LOAD, D9, LOAD, DOT, LOAD, SIGN), "  6B 6A 69 86"),
                argumentSet("П→x c П→x d П→x e",
                        NOOP, List.of(LOAD, EE, LOAD, CLEAR_X, LOAD, PUSH), "  6E 6D 6C 89"),
                //
                argumentSet("K НОП",
                        NOOP, List.of(K, D0), "  54 6E 6D 90"),
                // K БП
                argumentSet("K БП 0 K БП 1 K БП 2",
                        NOOP, List.of(K, GOTO, D0, K, GOTO, D1, K, GOTO, D2), "  82 81 80 93"),
                argumentSet("K БП 3 K БП 4 K БП 5",
                        NOOP, List.of(K, GOTO, D3, K, GOTO, D4, K, GOTO, D5), "  85 84 83 96"),
                argumentSet("K БП 6 K БП 7 K БП 8",
                        NOOP, List.of(K, GOTO, D6, K, GOTO, D7, K, GOTO, D8), "  88 87 86 99"),
                argumentSet("K БП 9 K БП A K БП B",
                        NOOP, List.of(K, GOTO, D9, K, GOTO, DOT, K, GOTO, SIGN), "  8B 8A 89 A2"),
                argumentSet("K БП c K БП d K БП e",
                        NOOP, List.of(K, GOTO, EE, K, GOTO, CLEAR_X, K, GOTO, PUSH), "  8E 8D 8C A5"),
                // Возврат на 0
                argumentSet("АВТ В/0", NOOP, List.of(F, SIGN, RETURN, F, EE), "           00"),
                // K ПП
                argumentSet("K ПП 0 K ПП 1 K ПП 2",
                        NOOP, List.of(K, GOSUB, D0, K, GOSUB, D1, K, GOSUB, D2), "  A2 A1 A0 03"),
                argumentSet("K ПП 3 K ПП 4 K ПП 5",
                        NOOP, List.of(K, GOSUB, D3, K, GOSUB, D4, K, GOSUB, D5), "  A5 A4 A3 06"),
                argumentSet("K ПП 6 K ПП 7 K ПП 8",
                        NOOP, List.of(K, GOSUB, D6, K, GOSUB, D7, K, GOSUB, D8), "  A8 A7 A6 09"),
                argumentSet("K ПП 9 K ПП A K ПП B",
                        NOOP, List.of(K, GOSUB, D9, K, GOSUB, DOT, K, GOSUB, SIGN), "  AB AA A9 12"),
                argumentSet("K ПП c K ПП d K ПП e",
                        NOOP, List.of(K, GOSUB, EE, K, GOSUB, CLEAR_X, K, GOSUB, PUSH), "  AE AD AC 15"),
                // K x=0
                argumentSet("K x=0 0 K x=0 1 K x=0 2", NOOP,
                        List.of(K, STEP_LEFT, D0, K, STEP_LEFT, D1, K, STEP_LEFT, D2), "  E2 E1 E0 18"),
                argumentSet("K x=0 3 K x=0 4 K x=0 5", NOOP,
                        List.of(K, STEP_LEFT, D3, K, STEP_LEFT, D4, K, STEP_LEFT, D5), "  E5 E4 E3 21"),
                argumentSet("K x=0 6 K x=0 7 K x=0 8", NOOP,
                        List.of(K, STEP_LEFT, D6, K, STEP_LEFT, D7, K, STEP_LEFT, D8), "  E8 E7 E6 24"),
                argumentSet("K x=0 9 K x=0 A K x=0 B", NOOP,
                        List.of(K, STEP_LEFT, D9, K, STEP_LEFT, DOT, K, STEP_LEFT, SIGN), "  EB EA E9 27"),
                argumentSet("K x=0 c K x=0 d K x=0 e", NOOP,
                        List.of(K, STEP_LEFT, EE, K, STEP_LEFT, CLEAR_X, K, STEP_LEFT, PUSH), "  EE ED EC 30"),
                // K x<0
                argumentSet("K x<0 0 K x<0 1 K x<0 2", NOOP,
                        List.of(K, STEP_RIGHT, D0, K, STEP_RIGHT, D1, K, STEP_RIGHT, D2),
                        "  C2 C1 C0 33"),
                argumentSet("K x<0 3 K x<0 4 K x<0 5", NOOP,
                        List.of(K, STEP_RIGHT, D3, K, STEP_RIGHT, D4, K, STEP_RIGHT, D5),
                        "  C5 C4 C3 36"),
                argumentSet("K x<0 6 K x<0 7 K x<0 8", NOOP,
                        List.of(K, STEP_RIGHT, D6, K, STEP_RIGHT, D7, K, STEP_RIGHT, D8),
                        "  C8 C7 C6 39"),
                argumentSet("K x<0 9 K x<0 A K x<0 B", NOOP,
                        List.of(K, STEP_RIGHT, D9, K, STEP_RIGHT, DOT, K, STEP_RIGHT, SIGN), "  CB CA C9 42"),
                argumentSet("K x<0 c K x<0 d K x<0 e", NOOP,
                        List.of(K, STEP_RIGHT, EE, K, STEP_RIGHT, CLEAR_X, K, STEP_RIGHT, PUSH), "  CE CD CC 45"),
                // K x≥0
                argumentSet("K x≥0 0 K x≥0 1 K x≥0 2", NOOP,
                        List.of(K, RETURN, D0, K, RETURN, D1, K, RETURN, D2), "  92 91 90 48"),
                argumentSet("K x≥0 3 K x≥0 4 K x≥0 5", NOOP,
                        List.of(K, RETURN, D3, K, RETURN, D4, K, RETURN, D5), "  95 94 93 51"),
                argumentSet("K x≥0 6 K x≥0 7 K x≥0 8", NOOP,
                        List.of(K, RETURN, D6, K, RETURN, D7, K, RETURN, D8), "  98 97 96 54"),
                argumentSet("K x≥0 9 K x≥0 A K x≥0 B", NOOP,
                        List.of(K, RETURN, D9, K, RETURN, DOT, K, RETURN, SIGN), "  9B 9A 99 57"),
                argumentSet("K x≥0 c K x≥0 d K x≥0 e", NOOP,
                        List.of(K, RETURN, EE, K, RETURN, CLEAR_X, K, RETURN, PUSH), "  9E 9D 9C 60"),
                // K x≠0
                argumentSet("K x≠0 0 K x≠0 1 K x≠0 2", NOOP,
                        List.of(K, RUN_STOP, D0, K, RUN_STOP, D1, K, RUN_STOP, D2), "  72 71 70 63"),
                argumentSet("K x≠0 3 K x≠0 4 K x≠0 5", NOOP,
                        List.of(K, RUN_STOP, D3, K, RUN_STOP, D4, K, RUN_STOP, D5), "  75 74 73 66"),
                argumentSet("K x≠0 6 K x≠0 7 K x≠0 8", NOOP,
                        List.of(K, RUN_STOP, D6, K, RUN_STOP, D7, K, RUN_STOP, D8), "  78 77 76 69"),
                argumentSet("K x≠0 9 K x≠0 A K x≠0 B", NOOP,
                        List.of(K, RUN_STOP, D9, K, RUN_STOP, DOT, K, RUN_STOP, SIGN), "  7B 7A 79 72"),
                argumentSet("K x≠0 c K x≠0 d K x≠0 e", NOOP,
                        List.of(K, RUN_STOP, EE, K, RUN_STOP, CLEAR_X, K, RUN_STOP, PUSH), "  7E 7D 7C 75"),
                // K x→П
                argumentSet("K x→П 0 0 K x→П 0 1 K x→П 0 2", NOOP,
                        List.of(K, STORE, D0, K, STORE, D1, K, STORE, D2), "  B2 B1 B0 78"),
                argumentSet("K x→П 0 3 K x→П 0 4 K x→П 0 5", NOOP,
                        List.of(K, STORE, D3, K, STORE, D4, K, STORE, D5), "  B5 B4 B3 81"),
                argumentSet("K x→П 0 6 K x→П 0 7 K x→П 0 8", NOOP,
                        List.of(K, STORE, D6, K, STORE, D7, K, STORE, D8), "  B8 B7 B6 84"),
                argumentSet("K x→П 0 9 K x→П 0 A K x→П 0 B", NOOP,
                        List.of(K, STORE, D9, K, STORE, DOT, K, STORE, SIGN), "  BB BA B9 87"),
                argumentSet("K x→П 0 c K x→П 0 d K x→П 0 e", NOOP,
                        List.of(K, STORE, EE, K, STORE, CLEAR_X, K, STORE, PUSH), "  BE BD BC 90"),
                // K П→x
                argumentSet("K П→x 0 0 K П→x 0 1 K П→x 0 2", NOOP,
                        List.of(K, LOAD, D0, K, LOAD, D1, K, LOAD, D2), "  D2 D1 D0 93"),
                argumentSet("K П→x 0 3 K П→x 0 4 K П→x 0 5", NOOP,
                        List.of(K, LOAD, D3, K, LOAD, D4, K, LOAD, D5), "  D5 D4 D3 96"),
                argumentSet("K П→x 0 6 K П→x 0 7 K П→x 0 8", NOOP,
                        List.of(K, LOAD, D6, K, LOAD, D7, K, LOAD, D8), "  D8 D7 D6 99"),
                argumentSet("K П→x 0 9 K П→x 0 A K П→x 0 B", NOOP,
                        List.of(K, LOAD, D9, K, LOAD, DOT, K, LOAD, SIGN), "  DB DA D9 A2"),
                argumentSet("K П→x 0 c K П→x 0 d K П→x 0 e", NOOP,
                        List.of(K, LOAD, EE, K, LOAD, CLEAR_X, K, LOAD, PUSH), "  DE DD DC A5"),
                // Возврат на 0
                argumentSet("АВТ В/0", NOOP, List.of(F, SIGN, RETURN, F, EE), "           00"),
                //
                argumentSet("K [x] K {x} K MAX", NOOP,
                        List.of(K, D7, K, D8, K, D9), "  36 35 34 03"),
                argumentSet("K |x| K ЗН K o⃖′", NOOP,
                        List.of(K, D4, K, D5, K, D6), "  33 32 31 06"),
                argumentSet("K o⃗' K o⃗′″ K o⃖′″", NOOP,
                        List.of(K, PLUS, K, SWAP, K, D3), "  30 2A 26 09"),
                argumentSet("K СЧ K ∧ K ∨", NOOP,
                        List.of(K, PUSH, K, DOT, K, SIGN), "  38 37 3B 12"),
                argumentSet("K ⊕ K ИНВ", NOOP,
                        List.of(K, EE, K, CLEAR_X), "  3A 39 38 14")

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