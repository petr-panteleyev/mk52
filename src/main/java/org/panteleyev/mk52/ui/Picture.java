// Copyright (c) Petr Panteleyev
// SPDX-License-Identifier: MIT
package org.panteleyev.mk52.ui;

import javafx.scene.image.Image;

import static java.util.Objects.requireNonNull;

public enum Picture {
    ICON("icon.png");

    private final Image image;

    Picture(String fileName) {
        image = new Image(requireNonNull(getClass().getResourceAsStream("/images/" + fileName)));
    }

    public Image getImage() {
        return image;
    }
}
