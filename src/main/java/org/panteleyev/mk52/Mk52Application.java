/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52;

import javafx.application.Application;
import javafx.scene.control.Alert;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.panteleyev.mk52.ui.Mk52Controller;

import java.io.ByteArrayInputStream;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import static java.lang.Thread.setDefaultUncaughtExceptionHandler;
import static java.nio.charset.StandardCharsets.UTF_8;
import static javafx.application.Platform.runLater;
import static org.panteleyev.mk52.ApplicationFiles.files;

public class Mk52Application extends Application {
    private static final Logger LOGGER = Logger.getLogger(Mk52Application.class.getName());

    private final static String LOG_PROPERTIES = """
            handlers                                = java.util.logging.FileHandler
            
            java.util.logging.FileHandler.level     = ALL
            java.util.logging.FileHandler.formatter = java.util.logging.SimpleFormatter
            java.util.logging.FileHandler.pattern   = %FILE_PATTERN%
            java.util.logging.FileHandler.append    = true
            
            java.util.logging.SimpleFormatter.format = %1$tF %1$tk:%1$tM:%1$tS %2$s%n%4$s: %5$s%6$s%n
            """;

    private static final String LOG_FILE_NAME = "mk52.log";

    @Override
    public void start(Stage stage) throws Exception {
        files().initialize();

        Font.loadFont(
                Mk52Application.class.getResource("/fonts/neat-lcd.ttf").toString(),
                14
        );
        Font.loadFont(
                Mk52Application.class.getResource("/fonts/JetBrainsMono-Medium.ttf").toString(),
                14
        );

        var logProperties = LOG_PROPERTIES.replace("%FILE_PATTERN%",
                files().getLogDirectory().resolve(LOG_FILE_NAME).toString().replace("\\", "/"));
        try (var inputStream = new ByteArrayInputStream(logProperties.getBytes(UTF_8))) {
            LogManager.getLogManager().readConfiguration(inputStream);
        }

        setDefaultUncaughtExceptionHandler((_, e) -> uncaughtException(e));

        new Mk52Controller(stage);
        stage.show();
    }

    private static void uncaughtException(Throwable e) {
        LOGGER.log(Level.SEVERE, "Uncaught exception", e);
        runLater(() -> new Alert(Alert.AlertType.ERROR, e.toString()).showAndWait());
    }

    public static Logger logger() {
        return LOGGER;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
