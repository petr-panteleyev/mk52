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
import org.panteleyev.mk52.BaseTest;

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
    private static final Engine engine = new Engine(false, _ -> {});

    @BeforeAll
    public static void beforeAll() {
        engine.init();
        engine.togglePower(true);
    }

    private static List<Arguments> testArguments() {
        return List.of(
                argumentSet("ПРГ", NOOP, List.of(F, EE), new IR(0xF00F_FFFF_FFFFL)),
                argumentSet("0 1 2", NOOP, List.of(D0, D1, D2), new IR(0xF03F_02_F_01_F_00L)),
                argumentSet("3 4 5", NOOP, List.of(D3, D4, D5), new IR(0xF06F_05_F_04_F_03L)),
                argumentSet("6 7 8", NOOP, List.of(D6, D7, D8), new IR(0xF09F_08_F_07_F_06L)),
                argumentSet("9 + -", NOOP, List.of(D9, PLUS, MINUS), new IR(0xF12F_11_F_10_F_09L)),
                argumentSet("* / ↔", NOOP, List.of(MULTIPLICATION, DIVISION, SWAP), new IR(0xF15F_14_F_13_F_12L)),
                argumentSet("В↑ . /-/", NOOP, List.of(PUSH, DOT, SIGN), new IR(0xF18F_0B_F_0A_F_0EL)),
                argumentSet("ВП Cx С/П", NOOP, List.of(EE, CLEAR_X, RUN_STOP), new IR(0xF21F_50_F_0D_F_0CL)),
                argumentSet("БП 12 В/О", NOOP, List.of(GOTO, D1, D2, RETURN), new IR(0xF24F_52_F_12_F_51L)),
                argumentSet("ПП -2", NOOP, List.of(GOSUB, DOT, D2), new IR(0xF26F_A2_F_53_F_52L)),
                argumentSet("F 10ˣ F lg F ln", NOOP, List.of(F, D0, F, D2, F, D3), new IR(0xF29F_18_F_17_F_15L)),
                argumentSet("F eˣ F sin⁻¹ F cos⁻¹", NOOP, List.of(F, D1, F, D4, F, D5), new IR(0xF32F_1A_F_19_F_16L)),
                argumentSet("F tg⁻¹ F sin F cos", NOOP, List.of(F, D6, F, D7, F, D8), new IR(0xF35F_1D_F_1C_F_1BL)),
                argumentSet("F tg F π F √", NOOP, List.of(F, D9, F, PLUS, F, MINUS), new IR(0xF38F_21_F_20_F_1EL)),
                argumentSet("F x² F 1/x F xy", NOOP, List.of(F, MULTIPLICATION, F, DIVISION, F, SWAP),
                        new IR(0xF41F_24_F_23_F_22L)),
                argumentSet("F CF F Вх F o", NOOP, List.of(F, CLEAR_X, F, PUSH, F, DOT), new IR(0xF43F_25_F_0F_F_24L)),
                argumentSet("F x<0 12", NOOP, List.of(F, STEP_RIGHT, D1, D2), new IR(0xF45F_12_F_5C_F_25L)),
                argumentSet("F x=0 34", NOOP, List.of(F, STEP_LEFT, D3, D4), new IR(0xF47F_34_F_5E_F_12L)),
                argumentSet("F x≥0 56", NOOP, List.of(F, RETURN, D5, D6), new IR(0xF49F_56_F_59_F_34L)),
                argumentSet("F x≠0 -3", NOOP, List.of(F, RUN_STOP, DOT, D3), new IR(0xF51F_A3_F_57_F_56L)),
                argumentSet("F L0 12", NOOP, List.of(F, LOAD, D1, D2), new IR(0xF53F_12_F_5D_F_A3L)),
                argumentSet("F L1 34", NOOP, List.of(F, STORE, D3, D4), new IR(0xF55F_34_F_5B_F_12L)),
                argumentSet("F L2 56", NOOP, List.of(F, GOTO, D5, D6), new IR(0xF57F_56_F_58_F_34L)),
                argumentSet("F L3 -4", NOOP, List.of(F, GOSUB, DOT, D4), new IR(0xF59F_A4_F_5A_F_56L)),
                // x→П
                argumentSet("x→П 0 x→П 1 x→П 2",
                        NOOP, List.of(STORE, D0, STORE, D1, STORE, D2), new IR(0xF62F_42_F_41_F_40L)),
                argumentSet("x→П 3 x→П 4 x→П 5",
                        NOOP, List.of(STORE, D3, STORE, D4, STORE, D5), new IR(0xF65F_45_F_44_F_43L)),
                argumentSet("x→П 6 x→П 7 x→П 8",
                        NOOP, List.of(STORE, D6, STORE, D7, STORE, D8), new IR(0xF68F_48_F_47_F_46L)),
                argumentSet("x→П 9 x→П a x→П b",
                        NOOP, List.of(STORE, D9, STORE, DOT, STORE, SIGN), new IR(0xF71F_4B_F_4A_F_49L)),
                argumentSet("x→П c x→П d x→П e",
                        NOOP, List.of(STORE, EE, STORE, CLEAR_X, STORE, PUSH), new IR(0xF74F_4E_F_4D_F_4CL)),
                // П→x
                argumentSet("П→x 0 П→x 1 П→x 2",
                        NOOP, List.of(LOAD, D0, LOAD, D1, LOAD, D2), new IR(0xF77F_62_F_61_F_60L)),
                argumentSet("П→x 3 П→x 4 П→x 5",
                        NOOP, List.of(LOAD, D3, LOAD, D4, LOAD, D5), new IR(0xF80F_65_F_64_F_63L)),
                argumentSet("П→x 6 П→x 7 П→x 8",
                        NOOP, List.of(LOAD, D6, LOAD, D7, LOAD, D8), new IR(0xF83F_68_F_67_F_66L)),
                argumentSet("П→x 9 П→x a П→x b",
                        NOOP, List.of(LOAD, D9, LOAD, DOT, LOAD, SIGN), new IR(0xF86F_6B_F_6A_F_69L)),
                argumentSet("П→x c П→x d П→x e",
                        NOOP, List.of(LOAD, EE, LOAD, CLEAR_X, LOAD, PUSH), new IR(0xF89F_6E_F_6D_F_6CL)),
                //
                argumentSet("K НОП", NOOP, List.of(K, D0), new IR(0xF90F_54_F_6E_F_6DL)),
                // K БП
                argumentSet("K БП 0 K БП 1 K БП 2",
                        NOOP, List.of(K, GOTO, D0, K, GOTO, D1, K, GOTO, D2), new IR(0xF93F_82_F_81_F_80L)),
                argumentSet("K БП 3 K БП 4 K БП 5",
                        NOOP, List.of(K, GOTO, D3, K, GOTO, D4, K, GOTO, D5), new IR(0xF96F_85_F_84_F_83L)),
                argumentSet("K БП 6 K БП 7 K БП 8",
                        NOOP, List.of(K, GOTO, D6, K, GOTO, D7, K, GOTO, D8), new IR(0xF99F_88_F_87_F_86L)),
                argumentSet("K БП 9 K БП A K БП B",
                        NOOP, List.of(K, GOTO, D9, K, GOTO, DOT, K, GOTO, SIGN), new IR(0xFA2F_8B_F_8A_F_89L)),
                argumentSet("K БП c K БП d K БП e",
                        NOOP, List.of(K, GOTO, EE, K, GOTO, CLEAR_X, K, GOTO, PUSH), new IR(0xFA5F_8E_F_8D_F_8CL)),
                // Возврат на 0
                argumentSet("АВТ В/0", NOOP, List.of(F, SIGN, RETURN, F, EE), new IR(0xF00F_FFFF_FFFFL)),
                // K ПП
                argumentSet("K ПП 0 K ПП 1 K ПП 2",
                        NOOP, List.of(K, GOSUB, D0, K, GOSUB, D1, K, GOSUB, D2), new IR(0xF03F_A2_F_A1_F_A0L)),
                argumentSet("K ПП 3 K ПП 4 K ПП 5",
                        NOOP, List.of(K, GOSUB, D3, K, GOSUB, D4, K, GOSUB, D5), new IR(0xF06F_A5_F_A4_F_A3L)),
                argumentSet("K ПП 6 K ПП 7 K ПП 8",
                        NOOP, List.of(K, GOSUB, D6, K, GOSUB, D7, K, GOSUB, D8), new IR(0xF09F_A8_F_A7_F_A6L)),
                argumentSet("K ПП 9 K ПП A K ПП B",
                        NOOP, List.of(K, GOSUB, D9, K, GOSUB, DOT, K, GOSUB, SIGN), new IR(0xF12F_AB_F_AA_F_A9L)),
                argumentSet("K ПП c K ПП d K ПП e",
                        NOOP, List.of(K, GOSUB, EE, K, GOSUB, CLEAR_X, K, GOSUB, PUSH), new IR(0xF15F_AE_F_AD_F_ACL)),
                // K x=0
                argumentSet("K x=0 0 K x=0 1 K x=0 2", NOOP,
                        List.of(K, STEP_LEFT, D0, K, STEP_LEFT, D1, K, STEP_LEFT, D2), new IR(0xF18F_E2_F_E1_F_E0L)),
                argumentSet("K x=0 3 K x=0 4 K x=0 5", NOOP,
                        List.of(K, STEP_LEFT, D3, K, STEP_LEFT, D4, K, STEP_LEFT, D5), new IR(0xF21F_E5_F_E4_F_E3L)),
                argumentSet("K x=0 6 K x=0 7 K x=0 8", NOOP,
                        List.of(K, STEP_LEFT, D6, K, STEP_LEFT, D7, K, STEP_LEFT, D8), new IR(0xF24F_E8_F_E7_F_E6L)),
                argumentSet("K x=0 9 K x=0 A K x=0 B", NOOP,
                        List.of(K, STEP_LEFT, D9, K, STEP_LEFT, DOT, K, STEP_LEFT, SIGN), new IR(0xF27F_EB_F_EA_F_E9L)),
                argumentSet("K x=0 c K x=0 d K x=0 e", NOOP,
                        List.of(K, STEP_LEFT, EE, K, STEP_LEFT, CLEAR_X, K, STEP_LEFT, PUSH),
                        new IR(0xF30F_EE_F_ED_F_ECL)),
                // K x<0
                argumentSet("K x<0 0 K x<0 1 K x<0 2", NOOP,
                        List.of(K, STEP_RIGHT, D0, K, STEP_RIGHT, D1, K, STEP_RIGHT, D2),
                        new IR(0xF33F_C2_F_C1_F_C0L)),
                argumentSet("K x<0 3 K x<0 4 K x<0 5", NOOP,
                        List.of(K, STEP_RIGHT, D3, K, STEP_RIGHT, D4, K, STEP_RIGHT, D5),
                        new IR(0xF36F_C5_F_C4_F_C3L)),
                argumentSet("K x<0 6 K x<0 7 K x<0 8", NOOP,
                        List.of(K, STEP_RIGHT, D6, K, STEP_RIGHT, D7, K, STEP_RIGHT, D8),
                        new IR(0xF39F_C8_F_C7_F_C6L)),
                argumentSet("K x<0 9 K x<0 A K x<0 B", NOOP,
                        List.of(K, STEP_RIGHT, D9, K, STEP_RIGHT, DOT, K, STEP_RIGHT, SIGN),
                        new IR(0xF42F_CB_F_CA_F_C9L)),
                argumentSet("K x<0 c K x<0 d K x<0 e", NOOP,
                        List.of(K, STEP_RIGHT, EE, K, STEP_RIGHT, CLEAR_X, K, STEP_RIGHT, PUSH),
                        new IR(0xF45F_CE_F_CD_F_CCL)),
                // K x≥0
                argumentSet("K x≥0 0 K x≥0 1 K x≥0 2", NOOP,
                        List.of(K, RETURN, D0, K, RETURN, D1, K, RETURN, D2), new IR(0xF48F_92_F_91_F_90L)),
                argumentSet("K x≥0 3 K x≥0 4 K x≥0 5", NOOP,
                        List.of(K, RETURN, D3, K, RETURN, D4, K, RETURN, D5), new IR(0xF51F_95_F_94_F_93L)),
                argumentSet("K x≥0 6 K x≥0 7 K x≥0 8", NOOP,
                        List.of(K, RETURN, D6, K, RETURN, D7, K, RETURN, D8), new IR(0xF54F_98_F_97_F_96L)),
                argumentSet("K x≥0 9 K x≥0 A K x≥0 B", NOOP,
                        List.of(K, RETURN, D9, K, RETURN, DOT, K, RETURN, SIGN), new IR(0xF57F_9B_F_9A_F_99L)),
                argumentSet("K x≥0 c K x≥0 d K x≥0 e", NOOP,
                        List.of(K, RETURN, EE, K, RETURN, CLEAR_X, K, RETURN, PUSH), new IR(0xF60F_9E_F_9D_F_9CL)),
                // K x≠0
                argumentSet("K x≠0 0 K x≠0 1 K x≠0 2", NOOP,
                        List.of(K, RUN_STOP, D0, K, RUN_STOP, D1, K, RUN_STOP, D2),
                        new IR(0xF63F_72_F_71_F_70L)),
                argumentSet("K x≠0 3 K x≠0 4 K x≠0 5", NOOP,
                        List.of(K, RUN_STOP, D3, K, RUN_STOP, D4, K, RUN_STOP, D5),
                        new IR(0xF66F_75_F_74_F_73L)),
                argumentSet("K x≠0 6 K x≠0 7 K x≠0 8", NOOP,
                        List.of(K, RUN_STOP, D6, K, RUN_STOP, D7, K, RUN_STOP, D8),
                        new IR(0xF69F_78_F_77_F_76L)),
                argumentSet("K x≠0 9 K x≠0 A K x≠0 B", NOOP,
                        List.of(K, RUN_STOP, D9, K, RUN_STOP, DOT, K, RUN_STOP, SIGN),
                        new IR(0xF72F_7B_F_7A_F_79L)),
                argumentSet("K x≠0 c K x≠0 d K x≠0 e", NOOP,
                        List.of(K, RUN_STOP, EE, K, RUN_STOP, CLEAR_X, K, RUN_STOP, PUSH),
                        new IR(0xF75F_7E_F_7D_F_7CL)),
                // K x→П
                argumentSet("K x→П 0 0 K x→П 0 1 K x→П 0 2", NOOP,
                        List.of(K, STORE, D0, K, STORE, D1, K, STORE, D2),
                        new IR(0xF78F_B2_F_B1_F_B0L)),
                argumentSet("K x→П 0 3 K x→П 0 4 K x→П 0 5", NOOP,
                        List.of(K, STORE, D3, K, STORE, D4, K, STORE, D5),
                        new IR(0xF81F_B5_F_B4_F_B3L)),
                argumentSet("K x→П 0 6 K x→П 0 7 K x→П 0 8", NOOP,
                        List.of(K, STORE, D6, K, STORE, D7, K, STORE, D8),
                        new IR(0xF84F_B8_F_B7_F_B6L)),
                argumentSet("K x→П 0 9 K x→П 0 A K x→П 0 B", NOOP,
                        List.of(K, STORE, D9, K, STORE, DOT, K, STORE, SIGN),
                        new IR(0xF87F_BB_F_BA_F_B9L)),
                argumentSet("K x→П 0 c K x→П 0 d K x→П 0 e", NOOP,
                        List.of(K, STORE, EE, K, STORE, CLEAR_X, K, STORE, PUSH),
                        new IR(0xF90F_BE_F_BD_F_BCL)),
                // K П→x
                argumentSet("K П→x 0 0 K П→x 0 1 K П→x 0 2", NOOP,
                        List.of(K, LOAD, D0, K, LOAD, D1, K, LOAD, D2),
                        new IR(0xF93F_D2_F_D1_F_D0L)),
                argumentSet("K П→x 0 3 K П→x 0 4 K П→x 0 5", NOOP,
                        List.of(K, LOAD, D3, K, LOAD, D4, K, LOAD, D5),
                        new IR(0xF96F_D5_F_D4_F_D3L)),
                argumentSet("K П→x 0 6 K П→x 0 7 K П→x 0 8", NOOP,
                        List.of(K, LOAD, D6, K, LOAD, D7, K, LOAD, D8),
                        new IR(0xF99F_D8_F_D7_F_D6L)),
                argumentSet("K П→x 0 9 K П→x 0 A K П→x 0 B", NOOP,
                        List.of(K, LOAD, D9, K, LOAD, DOT, K, LOAD, SIGN),
                        new IR(0xFA2F_DB_F_DA_F_D9L)),
                argumentSet("K П→x 0 c K П→x 0 d K П→x 0 e", NOOP,
                        List.of(K, LOAD, EE, K, LOAD, CLEAR_X, K, LOAD, PUSH),
                        new IR(0xFA5F_DE_F_DD_F_DCL)),
                // Возврат на 0
                argumentSet("АВТ В/0", NOOP, List.of(F, SIGN, RETURN, F, EE), new IR(0xF00F_FFFF_FFFFL)),
                //
                argumentSet("K [x] K {x} K MAX", NOOP, List.of(K, D7, K, D8, K, D9), new IR(0xF03F_36_F_35_F_34L)),
                argumentSet("K |x| K ЗН K o⃖′", NOOP, List.of(K, D4, K, D5, K, D6), new IR(0xF06F_33_F_32_F_31L)),
                argumentSet("K o⃗' K o⃗′″ K o⃖′″", NOOP, List.of(K, PLUS, K, SWAP, K, D3),
                        new IR(0xF09F_30_F_2A_F_26L)),
                argumentSet("K СЧ K ∧ K ∨", NOOP, List.of(K, PUSH, K, DOT, K, SIGN), new IR(0xF12F_38_F_37_F_3BL)),
                argumentSet("K ⊕ K ИНВ", NOOP, List.of(K, EE, K, CLEAR_X), new IR(0xF14F_3A_F_39_F_38L))
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