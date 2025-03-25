/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.controlsfx.control.SegmentedButton;
import org.panteleyev.fx.Controller;
import org.panteleyev.fx.grid.GridRowBuilder;
import org.panteleyev.mk52.engine.Engine;
import org.panteleyev.mk52.engine.KeyboardButton;
import org.panteleyev.mk52.engine.TrigonometricMode;

import java.util.List;
import java.util.function.Consumer;

import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.BoxFactory.vBox;
import static org.panteleyev.fx.MenuFactory.menu;
import static org.panteleyev.fx.MenuFactory.menuBar;
import static org.panteleyev.fx.MenuFactory.menuItem;
import static org.panteleyev.fx.grid.GridBuilder.gridPane;

public class Mk52Controller extends Controller {
    public static final String APP_TITLE = "МК-52";

    private final Engine engine = new Engine();

    private final Consumer<KeyboardButton> keyboardButtonConsumer = button -> {
        engine.processButton(button);
        updateDisplay();
    };

    private final Label display = new Label("                    ");

    public Mk52Controller(Stage stage) {
        super(stage, "/main.css");
        stage.setResizable(false);
        stage.getIcons().add(Picture.ICON.getImage());

        var root = new BorderPane();
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

        setupWindow(root);
    }

    @Override
    public String getTitle() {
        return APP_TITLE;
    }

    private MenuBar createMenuBar() {
        return menuBar(
                menu("Файл",
                        menuItem("Выход", _ -> onExit())
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
        offButton.setOnAction(_ -> {
            engine.togglePower(false);
            updateDisplay();
        });
        var onButton = new ToggleButton("Вкл");
        onButton.setOnAction(_ -> {
            engine.togglePower(true);
            updateDisplay();
        });
        var powerSwitch = new SegmentedButton(offButton, onButton);
        offButton.fire();

        var calcButton = new ToggleButton("С");
        var storeButton = new ToggleButton("З");
        var loadButton = new ToggleButton("СЧ");
        var eepromModeSwitch = new SegmentedButton(calcButton, storeButton, loadButton);
        calcButton.fire();

        var radianButton = new ToggleButton("Р");
        radianButton.setOnAction(_ -> engine.setTrigonometricMode(TrigonometricMode.RADIAN));
        var gRadianButton = new ToggleButton("ГРД");
        gRadianButton.setOnAction(_ -> engine.setTrigonometricMode(TrigonometricMode.GRADIAN));
        var degreeButton = new ToggleButton("Г");
        degreeButton.setOnAction(_ -> engine.setTrigonometricMode(TrigonometricMode.DEGREE));
        var trigonometricSwitch = new SegmentedButton(radianButton, gRadianButton, degreeButton);
        radianButton.fire();

        var dataButton = new ToggleButton("Д");
        var programButton = new ToggleButton("П");
        var eepromTypeSwitch = new SegmentedButton(dataButton, programButton);
        dataButton.fire();

        var pane = gridPane(List.of(GridRowBuilder.gridRow(
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
        var grid = gridPane(List.of(
                GridRowBuilder.gridRow(
                        new ButtonNode("F", "", "", "fButton", KeyboardButton.F, keyboardButtonConsumer).node(),
                        new ButtonNode("->", "x<0", "", "blackButton", KeyboardButton.STEP_RIGHT,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("П→x", "L0", "", "blackButton", KeyboardButton.LOAD,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("7", "sin", "[x]", "grayButton", KeyboardButton.DIGIT_7,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("8", "cos", "{x}", "grayButton", KeyboardButton.DIGIT_8,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("9", "tg", "max", "grayButton", KeyboardButton.DIGIT_9,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("-", "√", "", "grayButton", KeyboardButton.MINUS, keyboardButtonConsumer).node(),
                        new ButtonNode("/", "1/x", "", "grayButton", KeyboardButton.DIVISION,
                                keyboardButtonConsumer).node()
                ),
                GridRowBuilder.gridRow(
                        new ButtonNode("K", "", "", "kButton", KeyboardButton.K, keyboardButtonConsumer).node(),
                        new ButtonNode("<-", "x=0", "", "blackButton", KeyboardButton.STEP_LEFT,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("x→П", "L1", "", "blackButton", KeyboardButton.STORE,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("4", "sin⁻¹", "|x|", "grayButton", KeyboardButton.DIGIT_4,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("5", "cos⁻¹", "ЗН", "grayButton", KeyboardButton.DIGIT_5,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("6", "tg⁻¹", ".,<-", "grayButton", KeyboardButton.DIGIT_6,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("+", "π", ".,->", "grayButton", KeyboardButton.PLUS,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("X", "x²", "", "grayButton", KeyboardButton.MULTIPLICATION,
                                keyboardButtonConsumer).node()
                ),
                GridRowBuilder.gridRow(
                        new ButtonNode("⇅", "", "", "blackButton", KeyboardButton.UP_DOWN,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("В/О", "x≥0", "", "blackButton", KeyboardButton.RETURN,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("БП", "L2", "", "blackButton", KeyboardButton.GOTO,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("1", "eˣ", "", "grayButton", KeyboardButton.DIGIT_1,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("2", "lg", "", "grayButton", KeyboardButton.DIGIT_2,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("3", "ln", ".,,<-", "grayButton", KeyboardButton.DIGIT_3,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("↔", "xy", ".,,->", "grayButton", KeyboardButton.SWAP,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("В↑", "Вх", "СЧ", "grayButton", KeyboardButton.PUSH,
                                keyboardButtonConsumer).node()
                ),
                GridRowBuilder.gridRow(
                        new ButtonNode("A↑", "", "", "blackButton", KeyboardButton.A_UP, keyboardButtonConsumer).node(),
                        new ButtonNode("С/П", "x≠0", "", "blackButton", KeyboardButton.RUN_STOP,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("ПП", "L3", "", "blackButton", KeyboardButton.GOSUB,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("0", "10ˣ", "НОП", "grayButton", KeyboardButton.DIGIT_0,
                                keyboardButtonConsumer).node(),
                        new ButtonNode(".", "o", "∧", "grayButton", KeyboardButton.DOT, keyboardButtonConsumer).node(),
                        new ButtonNode("/-/", "АВТ", "∨", "grayButton", KeyboardButton.SIGN,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("ВП", "ПРГ", "⊕", "grayButton", KeyboardButton.EE,
                                keyboardButtonConsumer).node(),
                        new ButtonNode("Cx", "CF", "ИНВ", "redButton", KeyboardButton.CLEAR_X,
                                keyboardButtonConsumer).node()
                )
        ));
        grid.getStyleClass().add("buttonGrid");
        return grid;
    }

    private void onExit() {
        getStage().fireEvent(new WindowEvent(getStage(), WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    private void updateDisplay() {
        display.setText(engine.getDisplayString());
    }
}
