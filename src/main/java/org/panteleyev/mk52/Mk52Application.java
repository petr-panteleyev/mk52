/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52;

import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.panteleyev.mk52.ui.Mk52Controller;

public class Mk52Application extends Application {
    @Override
    public void start(Stage stage) {
        Font.loadFont(
                Mk52Application.class.getResource("/fonts/neat-lcd.ttf").toString(),
                14
        );
        Font.loadFont(
                Mk52Application.class.getResource("/fonts/JetBrainsMono-Medium.ttf").toString(),
                14
        );

        new Mk52Controller(stage);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
