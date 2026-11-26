// Copyright (c) Petr Panteleyev
// SPDX-License-Identifier: MIT
package org.panteleyev.mk52.ui;

import javafx.scene.control.Label;

class RegisterLabel extends Label {
    public RegisterLabel(String text) {
        super(text);
        getStyleClass().add("registerLabel");
    }
}
