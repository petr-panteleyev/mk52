/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.ui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.controlsfx.control.SegmentedButton;
import org.panteleyev.fx.Controller;
import org.panteleyev.mk52.eeprom.EepromMode;
import org.panteleyev.mk52.eeprom.EepromOperation;
import org.panteleyev.mk52.engine.DisplayUpdateCallback;
import org.panteleyev.mk52.engine.Engine;
import org.panteleyev.mk52.engine.KeyboardButton;
import org.panteleyev.mk52.engine.MemoryUpdateCallback;
import org.panteleyev.mk52.engine.TrigonometricMode;
import org.panteleyev.mk52.program.StepExecutionResult;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.util.List;
import java.util.function.Consumer;

import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.BoxFactory.vBox;
import static org.panteleyev.fx.LabelFactory.label;
import static org.panteleyev.fx.MenuFactory.checkMenuItem;
import static org.panteleyev.fx.MenuFactory.menu;
import static org.panteleyev.fx.MenuFactory.menuBar;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.dialogs.FileChooserBuilder.fileChooser;
import static org.panteleyev.fx.grid.GridBuilder.gridPane;
import static org.panteleyev.fx.grid.GridRowBuilder.gridRow;
import static org.panteleyev.mk52.engine.Constants.EMPTY_DISPLAY;
import static org.panteleyev.mk52.engine.Constants.INITIAL_DISPLAY;
import static org.panteleyev.mk52.ui.Accelerators.SHORTCUT_1;
import static org.panteleyev.mk52.ui.Accelerators.SHORTCUT_2;

public class Mk52Controller extends Controller {
    public static final String APP_TITLE = "МК-52";

    public static final FileChooser.ExtensionFilter EXTENSION_FILTER =
            new FileChooser.ExtensionFilter("Дамп памяти", "*.txt");

    private final DisplayUpdateCallback displayUpdateCallback = new DisplayUpdateCallback() {
        @Override
        public void clearDisplay() {
            Platform.runLater(() -> display.setText(EMPTY_DISPLAY));
        }

        @Override
        public void updateDisplay(String content, StepExecutionResult snapshot, boolean running) {
            Platform.runLater(() -> {
                display.setOpacity(running ? 0.3 : 1.0);
                display.setText(content);
                stackAndRegistersPanel.displaySnapshot(snapshot);
                memoryPanel.showPc(snapshot.programCounter());
            });
        }
    };

    private final MemoryUpdateCallback memoryUpdateCallback = new MemoryUpdateCallback() {
        @Override
        public void store(int address, int code) {
            Platform.runLater(() -> memoryPanel.store(address, code));
        }

        @Override
        public void store(int[] codes) {
            Platform.runLater(() -> memoryPanel.store(codes));
        }
    };

    private final StackAndRegistersPanel stackAndRegistersPanel = new StackAndRegistersPanel();
    private final MemoryPanel memoryPanel = new MemoryPanel();

    private final Engine engine = new Engine(true, displayUpdateCallback, memoryUpdateCallback);
    private final Consumer<KeyboardButton> keyboardButtonConsumer = engine::processButton;

    private final Label display = new Label(EMPTY_DISPLAY);

    private final BorderPane root = new BorderPane();
    private final VBox toolBox = new VBox(10);

    public Mk52Controller(Stage stage) {
        super(stage, "/main.css");
        stage.setResizable(false);

        stage.getIcons().add(Picture.ICON.getImage());

        root.getStyleClass().add("root");
        root.setTop(createMenuBar());

        var center = new BorderPane();
        var titleLabel = new Label("Э Л Е К Т Р О Н И К А     М К  5 2");
        titleLabel.getStyleClass().add("titleLabel");
        center.setTop(titleLabel);
        var centerHorizontal = hBox(10,
                vBox(10, createDisplay(), createSwitches()),
                createKeyboardGrid()
        );
        center.setCenter(centerHorizontal);

        root.setCenter(center);
        root.setBottom(toolBox);
        BorderPane.setMargin(toolBox, new Insets(10, 0, 0, 0));

        setupWindow(root);
        getStage().sizeToScene();
    }

    @Override
    public String getTitle() {
        return APP_TITLE;
    }

    private MenuBar createMenuBar() {
        return menuBar(
                menu("Файл",
                        menuItem("Сохранить...", _ -> onSaveMemoryDump()),
                        menuItem("Загрузить...", _ -> onLoadMemoryDump()),
                        menuItem("Выход", _ -> onExit())
                ),
                menu("Инструменты",
                        checkMenuItem("Регистры и стек", false, SHORTCUT_1, this::onRegistersAndStackPanel),
                        checkMenuItem("Память", false, SHORTCUT_2, this::onMemoryPanel)
                ),
                menu("Справка",
                        menuItem("О программе", _ -> new AboutDialog(this).showAndWait())
                )
        );
    }

    private BorderPane createDisplay() {
        var pane = new BorderPane();
        pane.getStyleClass().add("lcdPanel");
        pane.setCenter(display);
        display.getStyleClass().add("lcd");
        return pane;
    }

    private GridPane createSwitches() {
        var offButton = new ToggleButton(" ");
        offButton.setOnAction(_ -> onPowerOff());
        var onButton = new ToggleButton("Вкл");
        onButton.setOnAction(_ -> onPowerOf());
        var powerSwitch = new SegmentedButton(offButton, onButton);
        onButton.fire();

        var eraseButton = new ToggleButton("С");
        eraseButton.setOnAction(_ -> engine.setEepromOperation(EepromOperation.ERASE));
        var writeButton = new ToggleButton("З");
        writeButton.setOnAction(_ -> engine.setEepromOperation(EepromOperation.WRITE));
        var readButton = new ToggleButton("СЧ");
        readButton.setOnAction(_ -> engine.setEepromOperation(EepromOperation.READ));
        var eepromModeSwitch = new SegmentedButton(eraseButton, writeButton, readButton);
        readButton.fire();

        var radianButton = new ToggleButton("Р");
        radianButton.setOnAction(_ -> engine.setTrigonometricMode(TrigonometricMode.RADIAN));
        var gRadianButton = new ToggleButton("ГРД");
        gRadianButton.setOnAction(_ -> engine.setTrigonometricMode(TrigonometricMode.GRADIAN));
        var degreeButton = new ToggleButton("Г");
        degreeButton.setOnAction(_ -> engine.setTrigonometricMode(TrigonometricMode.DEGREE));
        var trigonometricSwitch = new SegmentedButton(radianButton, gRadianButton, degreeButton);
        radianButton.fire();

        var dataButton = new ToggleButton("Д");
        dataButton.setOnAction(_ -> engine.setEepromMode(EepromMode.DATA));
        var programButton = new ToggleButton("П");
        programButton.setOnAction(_ -> engine.setEepromMode(EepromMode.PROGRAM));
        var eepromTypeSwitch = new SegmentedButton(dataButton, programButton);
        dataButton.fire();

        var pane = gridPane(List.of(gridRow(
                powerSwitch,
                eepromModeSwitch,
                trigonometricSwitch,
                eepromTypeSwitch
        )));
        pane.setHgap(20);
        pane.setAlignment(Pos.CENTER);
        pane.getStyleClass().add("switchPanel");
        return pane;
    }

    private GridPane createKeyboardGrid() {
        var aLabel = new RegisterLabel("a");
        var bLabel = new RegisterLabel("b");
        var cLabel = new RegisterLabel("c");
        var dLabel = new RegisterLabel("d");

        var grid = gridPane(List.of(
                gridRow(
                        new ButtonNode("F", "", "", "fButton", KeyboardButton.F, keyboardButtonConsumer).node(),
                        new ButtonNode("ШГ>", "x<0", "", "blackButton", KeyboardButton.STEP_RIGHT,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("П→x", "L0", "", "blackButton", KeyboardButton.LOAD,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("7", "sin", "[x]", "grayButton", KeyboardButton.D7,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("8", "cos", "{x}", "grayButton", KeyboardButton.D8,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("9", "tg", "max", "grayButton", KeyboardButton.D9,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("➖", "√", "", "grayButton", KeyboardButton.MINUS, keyboardButtonConsumer).node(),
                        new ButtonNode("➗", "1/x", "", "grayButton", KeyboardButton.DIVISION,
                                keyboardButtonConsumer).node()
                ),
                gridRow(
                        new ButtonNode("K", "", "", "kButton", KeyboardButton.K, keyboardButtonConsumer).node(),
                        new ButtonNode("<ШГ", "x=0", "", "blackButton", KeyboardButton.STEP_LEFT,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("x→П", "L1", "", "blackButton", KeyboardButton.STORE,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("4", "sin⁻¹", "|x|", "grayButton", KeyboardButton.D4,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("5", "cos⁻¹", "ЗН", "grayButton", KeyboardButton.D5,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("6", "tg⁻¹", "o⃖′", "grayButton", KeyboardButton.D6,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("➕", "π", "o⃗'", "grayButton", KeyboardButton.PLUS,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("✖", "x²", "", "grayButton", KeyboardButton.MULTIPLICATION,
                                keyboardButtonConsumer).node()
                ),
                gridRow(
                        new ButtonNode("↑↓", "", "", "blackButton", KeyboardButton.EEPROM_EXCHANGE,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("В/О", "x≥0", "", "blackButton", KeyboardButton.RETURN,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("БП", "L2", "", "blackButton", KeyboardButton.GOTO,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("1", "eˣ", "", "grayButton", KeyboardButton.D1,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("2", "lg", "", "grayButton", KeyboardButton.D2,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("3", "ln", "o⃖‴", "grayButton", KeyboardButton.D3,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("←→", "xy", "o⃗‴", "grayButton", KeyboardButton.SWAP,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("В↑", "Вх", "СЧ", "grayButton", KeyboardButton.PUSH,
                                keyboardButtonConsumer).node(),
                        new RegisterLabel("e")
                ),
                gridRow(
                        new ButtonNode("A↑", "", "", "blackButton", KeyboardButton.EEPROM_ADDRESS,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("С/П", "x≠0", "", "blackButton", KeyboardButton.RUN_STOP,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("ПП", "L3", "", "blackButton", KeyboardButton.GOSUB,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("0", "10ˣ", "НОП", "grayButton", KeyboardButton.D0,
                                keyboardButtonConsumer).node(),
                        new ButtonNode(".", "↻", "∧", "grayButton", KeyboardButton.DOT, keyboardButtonConsumer).node(),
                        new ButtonNode("/-/", "АВТ", "∨", "grayButton", KeyboardButton.SIGN,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("ВП", "ПРГ", "⨁", "grayButton", KeyboardButton.EE,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("Cx", "CF", "ИНВ", "redButton", KeyboardButton.CLEAR_X,
                                keyboardButtonConsumer).node()
                ),
                gridRow(label(""), label(""), label(""), label(""),
                        aLabel, bLabel, cLabel, dLabel)
        ));

        grid.getStyleClass().add("buttonGrid");
        GridPane.setHalignment(aLabel, HPos.CENTER);
        GridPane.setHalignment(bLabel, HPos.CENTER);
        GridPane.setHalignment(cLabel, HPos.CENTER);
        GridPane.setHalignment(dLabel, HPos.CENTER);
        return grid;
    }

    private void onExit() {
        getStage().fireEvent(new WindowEvent(getStage(), WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    private void onRegistersAndStackPanel(ActionEvent event) {
        if (event.getSource() instanceof CheckMenuItem menuItem) {
            if (menuItem.isSelected()) {
                toolBox.getChildren().addFirst(stackAndRegistersPanel);
            } else {
                toolBox.getChildren().remove(stackAndRegistersPanel);
            }
            getStage().sizeToScene();
        }
    }

    private void onMemoryPanel(ActionEvent event) {
        if (event.getSource() instanceof CheckMenuItem menuItem) {
            if (menuItem.isSelected()) {
                toolBox.getChildren().addLast(memoryPanel);
            } else {
                toolBox.getChildren().remove(memoryPanel);
            }
            getStage().sizeToScene();
        }
    }

    private void onSaveMemoryDump() {
        var file = fileChooser("Сохранить дамп памяти", List.of(EXTENSION_FILTER)).showSaveDialog(getStage());
        if (file == null) {
            return;
        }

        try (var out = new OutputStreamWriter(new FileOutputStream(file))) {
            var bytes = engine.getMemoryBytes();
            for (int i = 0; i < bytes.length; i++) {
                if (i != 0 && i % 10 == 0) {
                    out.write("\n");
                }
                out.write(String.format("%02X ", bytes[i]));
            }
            out.flush();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void onLoadMemoryDump() {
        var file = fileChooser("Загрузить дамп памяти", List.of(EXTENSION_FILTER)).showOpenDialog(getStage());
        if (file == null) {
            return;
        }

        try {
            var content = Files.readString(file.toPath());
            var byteStrings = content.replace("\n", "").split(" ");
            var codes = new int[byteStrings.length];
            for (int i = 0; i < codes.length; i++) {
                codes[i] = Integer.parseInt(byteStrings[i], 16);
            }
            engine.loadMemoryBytes(codes);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void onPowerOf() {
        engine.togglePower(true);
        display.setText(INITIAL_DISPLAY);
        stackAndRegistersPanel.turnOn();
        memoryPanel.clear();
    }

    private void onPowerOff() {
        engine.togglePower(false);
        display.setText(EMPTY_DISPLAY);
        stackAndRegistersPanel.turnOff();
        memoryPanel.turnOff();
    }
}
