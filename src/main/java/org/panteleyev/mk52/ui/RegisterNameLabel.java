// Copyright (c) Petr Panteleyev
// SPDX-License-Identifier: MIT
package org.panteleyev.mk52.ui;

import javafx.scene.control.Label;

class RegisterNameLabel extends Label {
    public RegisterNameLabel(String text) {
        super(text);
        getStyleClass().add("registerContentLabel");
    }
}