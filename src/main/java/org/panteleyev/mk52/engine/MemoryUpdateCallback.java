// Copyright (c) Petr Panteleyev
// SPDX-License-Identifier: MIT
package org.panteleyev.mk52.engine;

public interface MemoryUpdateCallback {
    MemoryUpdateCallback NOOP = new MemoryUpdateCallback() {
        @Override
        public void store(int address, int code) {
        }

        @Override
        public void store(int[] codes) {
        }
    };

    void store(int address, int code);

    void store(int[] codes);
}
