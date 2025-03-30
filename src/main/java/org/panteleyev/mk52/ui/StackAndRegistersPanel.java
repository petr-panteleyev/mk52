/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.ui;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import org.panteleyev.mk52.program.StepExecutionResult;

import java.util.ArrayList;
import java.util.List;

import static org.panteleyev.fx.BoxFactory.hBox;
import static org.panteleyev.fx.BoxFactory.vBox;
import static org.panteleyev.fx.grid.GridBuilder.gridPane;
import static org.panteleyev.fx.grid.GridRowBuilder.gridRow;
import static org.panteleyev.mk52.engine.Constants.CALL_STACK_SIZE;
import static org.panteleyev.mk52.engine.Constants.DISPLAY_SIZE;
import static org.panteleyev.mk52.engine.Constants.INITIAL_DISPLAY;
import static org.panteleyev.mk52.engine.Constants.REGISTERS_SIZE;
import static org.panteleyev.mk52.util.StringUtil.pcToString;

public class StackAndRegistersPanel extends HBox {
    private static class RegisterNameLabel extends Label {
        public RegisterNameLabel(String text) {
            super(text);
            getStyleClass().add("registerContentLabel");
        }
    }

    private static class RegisterContentLabel extends Label {
        public RegisterContentLabel(String text) {
            super(text);
            getStyleClass().add("registerContent");
        }
    }

    private static final String INITIAL_ADDRESS = "00";

    private final List<Label> registerNumbers = new ArrayList<>(REGISTERS_SIZE);
    private final List<Label> registers = new ArrayList<>(REGISTERS_SIZE);
    private final List<Label> callStack = new ArrayList<>(CALL_STACK_SIZE);

    private final Label xLabel = new RegisterContentLabel(INITIAL_DISPLAY);
    private final Label yLabel = new RegisterContentLabel(INITIAL_DISPLAY);
    private final Label zLabel = new RegisterContentLabel(INITIAL_DISPLAY);
    private final Label tLabel = new RegisterContentLabel(INITIAL_DISPLAY);
    private final Label x1Label = new RegisterContentLabel(INITIAL_DISPLAY);

    private final Label pcLabel = new RegisterContentLabel(INITIAL_ADDRESS);

    public StackAndRegistersPanel() {
        super(10);
        getStyleClass().add("registerAndStackPanel");

        for (int i = 0; i < REGISTERS_SIZE; i++) {
            registers.add(new RegisterContentLabel(INITIAL_DISPLAY));
            registerNumbers.add(new RegisterNameLabel(" " + (Integer.toString(i, 16) + ":").toUpperCase()));
        }

        for (int i = 0; i < CALL_STACK_SIZE; i++) {
            callStack.add(new RegisterContentLabel(INITIAL_ADDRESS));
        }

        this.getChildren().addAll(
                vBox(5.0,
                        hBox(10.0,
                                buildStackPanel(),
                                buildCallStackPanel()
                        ),
                        buildPcPanel()
                ),
                buildRegistersPanel());
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
                gridRow(callStack.get(0)),
                gridRow(callStack.get(1)),
                gridRow(callStack.get(2)),
                gridRow(callStack.get(3)),
                gridRow(callStack.get(4))
        ));
        return vBox(5.0,
                new RegisterNameLabel("Возвраты:"),
                grid
        );
    }

    private Node buildRegistersPanel() {
        var grid = gridPane(List.of(
                gridRow(registerNumbers.get(0), registers.get(0), registerNumbers.get(8), registers.get(8)),
                gridRow(registerNumbers.get(1), registers.get(1), registerNumbers.get(9), registers.get(9)),
                gridRow(registerNumbers.get(2), registers.get(2), registerNumbers.get(10), registers.get(10)),
                gridRow(registerNumbers.get(3), registers.get(3), registerNumbers.get(11), registers.get(11)),
                gridRow(registerNumbers.get(4), registers.get(4), registerNumbers.get(12), registers.get(12)),
                gridRow(registerNumbers.get(5), registers.get(5), registerNumbers.get(13), registers.get(13)),
                gridRow(registerNumbers.get(6), registers.get(6), registerNumbers.get(14), registers.get(14)),
                gridRow(registerNumbers.get(7), registers.get(7))
        ));
        return vBox(5.0,
                new RegisterNameLabel(" Регистры:"),
                grid
        );
    }

    private Node buildPcPanel() {
        return hBox(5.0, new RegisterNameLabel("Счетчик команд:"), pcLabel);
    }

    public void displaySnapshot(StepExecutionResult snapshot) {
        for (int i = 0; i < REGISTERS_SIZE; i++) {
            registers.get(i).setText(padRight(snapshot.registers().get(i)));
        }

        xLabel.setText(padRight(snapshot.stack().x()));
        yLabel.setText(padRight(snapshot.stack().y()));
        zLabel.setText(padRight(snapshot.stack().z()));
        tLabel.setText(padRight(snapshot.stack().t()));
        x1Label.setText(padRight(snapshot.stack().x1()));

        pcLabel.setText(pcToString(snapshot.programCounter()));

        var i = CALL_STACK_SIZE - 1;
        for (; i > 0; i--) {
            var addr = snapshot.callStack().pollFirst();
            if (addr == null) {
                break;
            }
            callStack.get(i).setText(pcToString(addr));
        }
        for (;i > 0; i--) {
            callStack.get(i).setText(pcToString(0));
        }
    }

    private static String padRight(String s) {
        return s + " ".repeat(DISPLAY_SIZE - s.length());
    }
}
