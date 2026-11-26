// Copyright (c) Petr Panteleyev
// SPDX-License-Identifier: MIT
package org.panteleyev.mk52.ui;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

final class Accelerators {
    public static final KeyCombination SHORTCUT_1
            = new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.SHORTCUT_DOWN);
    public static final KeyCombination SHORTCUT_2
            = new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.SHORTCUT_DOWN);

    private Accelerators() {
    }
}
