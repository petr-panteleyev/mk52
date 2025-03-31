/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.ui;

import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import static org.panteleyev.mk52.engine.Constants.PROGRAM_MEMORY_SIZE;
import static org.panteleyev.mk52.util.StringUtil.pcToString;

class MemoryPanel extends VBox {
    private final Label[] addrs = new Label[PROGRAM_MEMORY_SIZE];
    private final Label[] cells = new Label[PROGRAM_MEMORY_SIZE];
    private int previousPc = 0;

    public MemoryPanel() {
        super(5);
        getStyleClass().add("memoryPanel");

        var grid = new GridPane(10, 5);

        int row = -1;
        int column = 0;

        for (int i = 0; i < PROGRAM_MEMORY_SIZE; i++) {
            if (i % 10 == 0) {
                row++;
                column = 0;
            }

            addrs[i] = new RegisterNameLabel(pcToString(i) + ":");
            cells[i] = new RegisterContentLabel("00");
            grid.add(addrs[i], column++, row);
            grid.add(cells[i], column++, row);
        }

        getChildren().addAll(new RegisterNameLabel("Память:"), grid);
    }

    public void clear() {
        for (var cell : cells) {
            cell.setText("00");
        }
    }

    public void turnOff() {
        for (var cell : cells) {
            cell.setText("  ");
        }
    }

    public void store(int address, int code) {
        cells[address].setText(String.format("%02X", code));
    }

    public void store(int[] codes) {
        for (int i = 0; i < Math.min(codes.length, cells.length); i++) {
            store(i, codes[i]);
        }
    }

    public void showPc(int pc) {
        addrs[previousPc].getStyleClass().remove("registerContentHighlighted");
        addrs[previousPc].getStyleClass().add("registerContentLabel");

        addrs[pc].getStyleClass().remove("registerContentLabel");
        addrs[pc].getStyleClass().add("registerContentHighlighted");

        previousPc = pc;
    }
}
