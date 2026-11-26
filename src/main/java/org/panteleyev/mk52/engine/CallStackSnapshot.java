// Copyright (c) Petr Panteleyev
// SPDX-License-Identifier: MIT
package org.panteleyev.mk52.engine;

import org.panteleyev.mk52.program.Address;

public record CallStackSnapshot(Address[] stack) {
}
