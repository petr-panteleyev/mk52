// Copyright (c) Petr Panteleyev
// SPDX-License-Identifier: MIT
package org.panteleyev.mk52.ui;

import javafx.scene.control.Label;

class RegisterContentLabel extends Label {
    public RegisterContentLabel(String text) {
        super(text);
        getStyleClass().add("registerContent");
    }
}
