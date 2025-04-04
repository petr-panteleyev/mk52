/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.value;

public sealed interface Value permits DecimalValue, LogicalValue {
    String asString();

    Value toNormal();

    byte[] toByteArray();

    DecimalValue toDecimal();

    boolean invalid();
}
