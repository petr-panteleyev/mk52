/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.panteleyev.mk52.program.Address;

import java.util.Arrays;

import static org.panteleyev.mk52.engine.Constants.CALL_STACK_SIZE;

public final class CallStack {
    private final static int LAST_INDEX = CALL_STACK_SIZE - 1;

    private final Address[] stack = new Address[CALL_STACK_SIZE];

    public void reset() {
        Arrays.fill(stack, Address.ZERO);
    }

    public void push(Address pc) {
        synchronized (stack) {
            System.arraycopy(stack, 0, stack, 1, stack.length - 1);
            stack[0] = pc;
        }
    }

    public Address pop() {
        synchronized (stack) {
            var pc = stack[0];
            System.arraycopy(stack, 1, stack, 0, stack.length - 1);
            stack[LAST_INDEX] = convertLastElement(stack[LAST_INDEX]);
            return pc;
        }
    }

    public CallStackSnapshot getSnapshot() {
        synchronized (stack) {
            var snapshot = new Address[CALL_STACK_SIZE];
            System.arraycopy(stack, 0, snapshot, 0, snapshot.length);
            return new CallStackSnapshot(snapshot);
        }
    }

    private Address convertLastElement(Address element) {
        return new Address(element.low(), element.low());
    }
}
