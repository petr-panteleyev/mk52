/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.ui;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import org.panteleyev.mk52.engine.Register;
import org.panteleyev.mk52.program.StepExecutionResult;

import java.util.ArrayList;
import java.util.List;

import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.BoxFactory.vBox;
import static org.panteleyev.fx.grid.GridBuilder.gridPane;
import static org.panteleyev.fx.grid.GridRowBuilder.gridRow;
import static org.panteleyev.mk52.engine.Constants.CALL_STACK_SIZE;
import static org.panteleyev.mk52.engine.Constants.REGISTERS_SIZE;
import static org.panteleyev.mk52.util.StringUtil.padToDisplay;
import static org.panteleyev.mk52.util.StringUtil.pcToString;

class StackAndRegistersPanel extends BorderPane {
    private static final String INITIAL_ADDRESS = "00";
    private static final String INITIAL_REGISTER = padToDisplay(Register.toString(0));
    private static final String REGISTER_OFF = padToDisplay("");

    private final List<Label> registers = new ArrayList<>(REGISTERS_SIZE);
    private final List<Label> callStack = new ArrayList<>(CALL_STACK_SIZE);

    private final Label xLabel = new RegisterContentLabel("");
    private final Label yLabel = new RegisterContentLabel("");
    private final Label zLabel = new RegisterContentLabel("");
    private final Label tLabel = new RegisterContentLabel("");
    private final Label x1Label = new RegisterContentLabel("");

    private final Label pcLabel = new RegisterContentLabel(INITIAL_ADDRESS);

    public StackAndRegistersPanel() {
        getStyleClass().add("registerAndStackPanel");

        for (int i = 0; i < REGISTERS_SIZE; i++) {
            registers.add(new RegisterContentLabel(""));
        }

        for (int i = 0; i < CALL_STACK_SIZE; i++) {
            callStack.add(new RegisterContentLabel(INITIAL_ADDRESS));
        }

        this.setCenter(vBox(10.0,
                hBox(10.0,
                        buildStackPanel(),
                        buildCallStackPanel(),
                        buildRegistersPanel()
                ),
                buildPcPanel()
        ));
    }

    public void turnOn() {
        for (var label : registers) {
            label.setText(INITIAL_REGISTER);
        }

        for (var label : callStack) {
            label.setText("00");
        }

        pcLabel.setText("00");

        xLabel.setText(INITIAL_REGISTER);
        yLabel.setText(INITIAL_REGISTER);
        zLabel.setText(INITIAL_REGISTER);
        tLabel.setText(INITIAL_REGISTER);
        x1Label.setText(INITIAL_REGISTER);
    }

    public void turnOff() {
        for (var label : registers) {
            label.setText(REGISTER_OFF);
        }

        for (var label : callStack) {
            label.setText("  ");
        }

        pcLabel.setText("  ");

        xLabel.setText(REGISTER_OFF);
        yLabel.setText(REGISTER_OFF);
        zLabel.setText(REGISTER_OFF);
        tLabel.setText(REGISTER_OFF);
        x1Label.setText(REGISTER_OFF);
    }

    private Node buildStackPanel() {
        var grid = gridPane(List.of(
                gridRow(new RegisterNameLabel("T:"), tLabel),
                gridRow(new RegisterNameLabel("Z:"), zLabel),
                gridRow(new RegisterNameLabel("Y:"), yLabel),
                gridRow(new RegisterNameLabel("X:"), xLabel),
                gridRow(new RegisterNameLabel("X1:"), x1Label)
        ));
        return vBox(5.0,
                new RegisterNameLabel("Стек:"),
                grid
        );
    }

    private Node buildCallStackPanel() {
        var grid = gridPane(List.of(
                gridRow(callStack.get(4)),
                gridRow(callStack.get(3)),
                gridRow(callStack.get(2)),
                gridRow(callStack.get(1)),
                gridRow(callStack.get(0))
        ));
        return vBox(5.0,
                new RegisterNameLabel("В/О:"),
                grid
        );
    }

    private Node buildRegistersPanel() {
        var grid1 = new GridPane();

        int row = 0;
        int column = 0;
        for (var i = 0; i < REGISTERS_SIZE; i++) {
            if (i != 0 && i % 5 == 0) {
                row = 0;
                column += 2;
            }
            grid1.add(new RegisterNameLabel(" " + (Integer.toString(i, 16) + ":").toUpperCase()), column, row);
            grid1.add(registers.get(i), column + 1, row++);
        }

        return vBox(5.0,
                new RegisterNameLabel(" Регистры:"),
                grid1
        );
    }

    private Node buildPcPanel() {
        return hBox(5.0, new RegisterNameLabel("Счетчик команд:"), pcLabel);
    }

    public void displaySnapshot(StepExecutionResult snapshot) {
        if (snapshot.registers().size() == REGISTERS_SIZE) {
            for (int i = 0; i < REGISTERS_SIZE; i++) {
                registers.get(i).setText(padToDisplay(snapshot.registers().get(i)));
            }
        }

        xLabel.setText(padToDisplay(Register.toString(snapshot.stack().x())));
        yLabel.setText(padToDisplay(Register.toString(snapshot.stack().y())));
        zLabel.setText(padToDisplay(Register.toString(snapshot.stack().z())));
        tLabel.setText(padToDisplay(Register.toString(snapshot.stack().t())));
        x1Label.setText(padToDisplay(Register.toString(snapshot.stack().x1())));

        pcLabel.setText(pcToString(snapshot.programCounter()));

        var callStackAddr = snapshot.callStack().stack();
        for (int i = 0; i < callStackAddr.length; i++) {
            callStack.get(i).setText(pcToString(callStackAddr[i]));
        }
    }
}
