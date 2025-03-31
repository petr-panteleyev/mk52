/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.ui;

import javafx.scene.control.Label;

class RegisterNameLabel extends Label {
    public RegisterNameLabel(String text) {
        super(text);
        getStyleClass().add("registerContentLabel");
    }
}