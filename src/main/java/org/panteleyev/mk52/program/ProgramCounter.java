/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.program;

public final class ProgramCounter {
    private Address address = Address.of(0);

    synchronized public Address get() {
        return address;
    }

    synchronized public void set(Address address) {
        this.address = address;
    }

    synchronized public Address getAndIncrement() {
        var get = address;
        address = address.increment();
        return get;
    }

    synchronized public void increment() {
        address = address.increment();
    }

    synchronized public void decrement() {
        address = address.decrement();
    }
}
