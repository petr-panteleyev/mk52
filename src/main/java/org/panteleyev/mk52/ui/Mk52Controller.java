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
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.ToggleButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.controlsfx.control.SegmentedButton;
import org.panteleyev.fx.Controller;
import org.panteleyev.mk52.ApplicationFiles;
import org.panteleyev.mk52.eeprom.EepromMode;
import org.panteleyev.mk52.eeprom.EepromOperation;
import org.panteleyev.mk52.engine.Engine;
import org.panteleyev.mk52.engine.KeyboardButton;
import org.panteleyev.mk52.engine.MemoryUpdateCallback;
import org.panteleyev.mk52.engine.RegistersUpdateCallback;
import org.panteleyev.mk52.engine.TrigonometricMode;
import org.panteleyev.mk52.program.StepExecutionResult;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
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
import static org.panteleyev.mk52.ApplicationFiles.files;
import static org.panteleyev.mk52.engine.Constants.PROGRAM_MEMORY_SIZE;
import static org.panteleyev.mk52.ui.Accelerators.SHORTCUT_1;
import static org.panteleyev.mk52.ui.Accelerators.SHORTCUT_2;

public class Mk52Controller extends Controller {
    public static final String APP_TITLE = "МК-52";

    public static final FileChooser.ExtensionFilter EXTENSION_FILTER =
            new FileChooser.ExtensionFilter("Дамп памяти", "*.txt");

    private final RegistersUpdateCallback registersUpdateCallback = new RegistersUpdateCallback() {
        @Override
        public void update(StepExecutionResult snapshot, boolean running) {
            Platform.runLater(() -> {
                stackAndRegistersPanel.displaySnapshot(snapshot);
                memoryPanel.showPc(snapshot.programCounter().getEffectiveAddress());
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

    private final Engine engine = new Engine(true, registersUpdateCallback, memoryUpdateCallback);
    private final Consumer<KeyboardButton> keyboardButtonConsumer = engine::processButton;

    private final Label[] digitCells = new Label[]{
            new Label("F"), new Label("F"), new Label("F"), new Label("F"),
            new Label("F"), new Label("F"), new Label("F"), new Label("F"),
            new Label("F"), new Label("F"), new Label("F"), new Label("F")
    };
    private final Label[] dotCells = new Label[]{
            new Label(" "), new Label(" "), new Label(" "), new Label(" "),
            new Label(" "), new Label(" "), new Label(" "), new Label(" "),
            new Label(" "), new Label(" "), new Label(" "), new Label(" ")
    };

    private final ToggleButton onButton = new ToggleButton("Вкл");

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

        engine.displayProperty().addListener((_, oldValue, newValue) -> {
            if (oldValue == newValue || newValue == null) {
                return;
            }

            var ri = newValue.indicator();
            var dots = newValue.dots();
            var opacity = engine.automaticMode().get() ? 0.3 : 1.0;

            for (int i = 0; i < 12; i++) {
                digitCells[i].setText(Long.toString(ri & 0xF, 16).toUpperCase());
                digitCells[i].setOpacity(opacity);
                dotCells[i].setText((dots & 1) == 1 ? "." : " ");
                dotCells[i].setOpacity(opacity);
                ri = ri >> 4;
                dots = dots >> 1;
            }
        });

        setupWindow(root);
        getStage().sizeToScene();

        setupAccelerators();

        files().read(ApplicationFiles.AppFile.EEPROM, engine::importEeprom);

        onButton.fire();
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
                        new SeparatorMenuItem(),
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
        pane.setMouseTransparent(true);

        for (var l : digitCells) {
            l.getStyleClass().add("lcd");
        }
        for (var d : dotCells) {
            d.getStyleClass().add("dotLcd");
        }
        var cellsPane = new HBox(0);
        cellsPane.getChildren().addAll(
                // Знак мантиссы
                digitCells[8], dotCells[8],
                // Мантисса
                digitCells[7], dotCells[7],
                digitCells[6], dotCells[6],
                digitCells[5], dotCells[5],
                digitCells[4], dotCells[4],
                digitCells[3], dotCells[3],
                digitCells[2], dotCells[2],
                digitCells[1], dotCells[1],
                digitCells[0], dotCells[0],
                // Знак порядка
                digitCells[11], dotCells[11],
                // Порядок
                digitCells[10], dotCells[10],
                digitCells[9], dotCells[9]
        );
        cellsPane.setAlignment(Pos.BOTTOM_LEFT);
        for (var dc : digitCells) {
            HBox.setMargin(dc, new Insets(0, 0, 0, -3));
        }

        pane.setCenter(cellsPane);
        return pane;
    }

    private GridPane createSwitches() {
        var offButton = new ToggleButton(" ");
        offButton.setOnAction(_ -> onPowerOff());
        offButton.setFocusTraversable(false);
        onButton.setOnAction(_ -> onPowerOn());
        onButton.setFocusTraversable(false);
        var powerSwitch = new SegmentedButton(offButton, onButton);

        var eraseButton = new ToggleButton("С");
        eraseButton.setOnAction(_ -> engine.setEepromOperation(EepromOperation.ERASE));
        eraseButton.setFocusTraversable(false);
        var writeButton = new ToggleButton("З");
        writeButton.setOnAction(_ -> engine.setEepromOperation(EepromOperation.WRITE));
        writeButton.setFocusTraversable(false);
        var readButton = new ToggleButton("СЧ");
        readButton.setOnAction(_ -> engine.setEepromOperation(EepromOperation.READ));
        readButton.setFocusTraversable(false);
        var eepromModeSwitch = new SegmentedButton(eraseButton, writeButton, readButton);
        readButton.fire();

        var radianButton = new ToggleButton("Р");
        radianButton.setOnAction(_ -> engine.setTrigonometricMode(TrigonometricMode.RADIAN));
        radianButton.setFocusTraversable(false);
        var gRadianButton = new ToggleButton("ГРД");
        gRadianButton.setOnAction(_ -> engine.setTrigonometricMode(TrigonometricMode.GRADIAN));
        gRadianButton.setFocusTraversable(false);
        var degreeButton = new ToggleButton("Г");
        degreeButton.setOnAction(_ -> engine.setTrigonometricMode(TrigonometricMode.DEGREE));
        degreeButton.setFocusTraversable(false);
        var trigonometricSwitch = new SegmentedButton(radianButton, gRadianButton, degreeButton);
        radianButton.fire();

        var dataButton = new ToggleButton("Д");
        dataButton.setOnAction(_ -> engine.setEepromMode(EepromMode.DATA));
        dataButton.setFocusTraversable(false);
        var programButton = new ToggleButton("П");
        programButton.setOnAction(_ -> engine.setEepromMode(EepromMode.PROGRAM));
        programButton.setFocusTraversable(false);
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
                        new ButtonNode("6", "tg⁻¹", "°←′", "grayButton", KeyboardButton.D6,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("➕", "π", "°→′", "grayButton", KeyboardButton.PLUS,
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
                        new ButtonNode("3", "ln", "°←‴", "grayButton", KeyboardButton.D3,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("←→", "xy", "°→‴", "grayButton", KeyboardButton.SWAP,
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
                        new ButtonNode(".", "⟳", "⋀", "grayButton", KeyboardButton.DOT, keyboardButtonConsumer).node(),
                        new ButtonNode("/-/", "АВТ", "⋁", "grayButton", KeyboardButton.SIGN,
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

        try (var reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            var codes = new int[PROGRAM_MEMORY_SIZE];
            var index = 0;

            var lines = reader.lines().toList();
            outerLoop:
            for (var line : lines) {
                if (line.startsWith("#")) {
                    continue;
                }
                var strings = line.trim().split(" ");
                for (var str : strings) {
                    if (index >= codes.length) {
                        break outerLoop;
                    }
                    codes[index++] = Integer.parseInt(str, 16);
                }
            }
            engine.loadMemoryBytes(codes);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private void onPowerOn() {
        engine.togglePower(true);
        stackAndRegistersPanel.turnOn();
        memoryPanel.clear();
    }

    private void onPowerOff() {
        engine.togglePower(false);
        stackAndRegistersPanel.turnOff();
        memoryPanel.turnOff();
    }

    @Override
    protected void onWindowHiding() {
        files().write(ApplicationFiles.AppFile.EEPROM, engine::exportEeprom);
        super.onWindowHiding();
    }

    private void setupAccelerators() {
        // Цифры
        getStage().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.DIGIT0), () -> engine.processButton(KeyboardButton.D0));
        getStage().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.DIGIT1), () -> engine.processButton(KeyboardButton.D1));
        getStage().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.DIGIT2), () -> engine.processButton(KeyboardButton.D2));
        getStage().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.DIGIT3), () -> engine.processButton(KeyboardButton.D3));
        getStage().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.DIGIT4), () -> engine.processButton(KeyboardButton.D4));
        getStage().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.DIGIT5), () -> engine.processButton(KeyboardButton.D5));
        getStage().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.DIGIT6), () -> engine.processButton(KeyboardButton.D6));
        getStage().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.DIGIT7), () -> engine.processButton(KeyboardButton.D7));
        getStage().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.DIGIT8), () -> engine.processButton(KeyboardButton.D8));
        getStage().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.DIGIT9), () -> engine.processButton(KeyboardButton.D9));
        getStage().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.COMMA), () -> engine.processButton(KeyboardButton.DOT));

        getStage().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.UP), () -> engine.processButton(KeyboardButton.PUSH));

        getStage().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.F), () -> engine.processButton(KeyboardButton.F));
        getStage().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.K), () -> engine.processButton(KeyboardButton.K));

        getStage().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.SHIFT_DOWN),
                () -> engine.processButton(KeyboardButton.PLUS));
        getStage().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.MINUS),
                () -> engine.processButton(KeyboardButton.MINUS));
        getStage().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.DIGIT8, KeyCombination.SHIFT_DOWN),
                () -> engine.processButton(KeyboardButton.MULTIPLICATION));
        getStage().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.SLASH),
                () -> engine.processButton(KeyboardButton.DIVISION));

        getStage().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.LEFT),
                () -> engine.processButton(KeyboardButton.STEP_LEFT));
        getStage().getScene().getAccelerators().put(
                new KeyCodeCombination(KeyCode.RIGHT),
                () -> engine.processButton(KeyboardButton.STEP_RIGHT));
    }
}
