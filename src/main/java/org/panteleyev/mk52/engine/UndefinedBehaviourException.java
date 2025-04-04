/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

public class UndefinedBehaviourException extends RuntimeException {
    public UndefinedBehaviourException() {
    }

    public UndefinedBehaviourException(String message) {
        super(message);
    }
}
