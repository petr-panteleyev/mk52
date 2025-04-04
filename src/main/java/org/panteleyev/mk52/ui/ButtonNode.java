/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.panteleyev.mk52.engine.KeyboardButton;

import java.util.function.Consumer;

import static org.panteleyev.fx.BoxFactory.hBox;

public class ButtonNode {
    private final VBox layout = new VBox();

    public ButtonNode(String text,
            String fText,
            String kText,
            String buttonClass,
            KeyboardButton keyboardButton,
            Consumer<KeyboardButton> handler) {

        if (!fText.isEmpty() || !kText.isEmpty()) {
            var upperBox = hBox(5);
            upperBox.setAlignment(Pos.CENTER);
            if (!fText.isEmpty()) {
                var fLabel = new Label(fText);
                fLabel.getStyleClass().add("fLabel");
                upperBox.getChildren().add(fLabel);
            }
            if (!kText.isEmpty()) {
                var kLabel = new Label(kText);
                kLabel.getStyleClass().add("kLabel");
                upperBox.getChildren().add(kLabel);
            }
            layout.getChildren().add(upperBox);
        }

        var button = new Button(text);
        button.setPrefWidth(50);
        button.setPrefHeight(30);
        button.getStyleClass().add(buttonClass);
        button.setUserData(keyboardButton);
        button.setOnAction(_ -> handler.accept(keyboardButton));
        button.setFocusTraversable(false);

        layout.getChildren().add(button);
        layout.setAlignment(Pos.BOTTOM_CENTER);
    }

    public Node node() {
        return layout;
    }
}
