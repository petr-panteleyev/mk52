/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
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
