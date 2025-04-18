/*
 Copyright © 2025 Petr Panteleyev <petr@panteleyev.org>
 SPDX-License-Identifier: BSD-2-Clause
 */
package org.panteleyev.mk52.engine;

import org.panteleyev.mk52.program.OpCode;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

public class Stack {
    private final AtomicLong x = new AtomicLong(0);
    private final AtomicLong y = new AtomicLong(0);
    private final AtomicLong z = new AtomicLong(0);
    private final AtomicLong t = new AtomicLong(0);
    private final AtomicLong x1 = new AtomicLong(0);

    private final AtomicReference<IR> x2;

    private final AtomicReference<OpCode> lastExecutedOpCode;

    // При вводе экспоненты запоминаем текущую экспоненту регистра X
    private int xExponent = 0;
    private boolean enteringExponent = false;
    // Текущая позиция вводе
    private int currentDigit = 7;
    // Текущая позиция точки
    private int dot = 7;
    // Была ли введена точка
    private boolean hasDot = false;
    private long xBuffer = 0;

    Stack(Engine engine) {
        this.x2 = engine.getX2();
        this.lastExecutedOpCode = engine.getLastExecutedOpCode();
    }

    synchronized void reset() {
        x.set(0);
        x1.set(0);
        y.set(0);
        z.set(0);
        t.set(0);
        x2.set(Register.xToIndicator(x.get()));
        //
        enteringExponent = false;
    }

    synchronized long x() {
        var tmp = x.get();
        x.set(Register.normalize(x.get()));
        x2.set(Register.xToIndicator(x.get()));
        return tmp;
    }

    synchronized IR x2() {
        return x2.get();
    }

    synchronized long xOrBuffer() {
        return x.get();
    }

    synchronized public StackSnapshot getSnapshot() {
        return new StackSnapshot(
                x.get(),
                y.get(),
                z.get(),
                t.get(),
                x1.get(),
                x2.get()
        );
    }

    synchronized void setX(long x) {
        enteringExponent = false;
        this.x.set(x);
        x2.set(Register.xToIndicator(x));
    }

    public void setX2(IR ri) {
        x2.set(ri);
    }

    synchronized public void clearX() {
        enteringExponent = false;

        x.set(0);
        x2.set(Register.xToIndicator(x.get()));
    }

    synchronized public void pi() {
        enteringExponent = false;

        push();
        x1.set(x.get());
        x.set(Register.PI);
        x2.set(Register.xToIndicator(x.get()));
    }

    synchronized void push() {
        enteringExponent = false;

        t.set(z.get());
        z.set(y.get());
        y.set(Register.normalize(x.get()));
        x.set(Register.normalize(x.get()));
        x2.set(Register.xToIndicator(x.get()));
    }

    synchronized void rotate() {
        enteringExponent = false;

        var tempX = Register.normalize(x.get());
        x.set(y.get());
        y.set(z.get());
        z.set(t.get());
        t.set(tempX);
        x1.set(tempX);
        x2.set(Register.xToIndicator(x.get()));
    }

    synchronized void swap() {
        enteringExponent = false;

        var tempX = Register.normalize(x.get());
        x.set(y.get());
        y.set(tempX);
        x1.set(tempX);
        x2.set(Register.xToIndicator(x.get()));
    }

    synchronized void restoreX() {
        enteringExponent = false;

        t.set(z.get());
        z.set(y.get());
        y.set(x.get());
        x.set(x1.get());
        x2.set(Register.xToIndicator(x.get()));
    }

    synchronized void unaryOperation(UnaryOperator<Long> operation) {
        enteringExponent = false;

        x1.set(Register.normalize(x.get()));

        var result = operation.apply(x.get());
        x.set(Register.normalize(result));
        x2.set(Register.xToIndicator(result));
    }

    synchronized void binaryOperation(BinaryOperator<Long> operation) {
        enteringExponent = false;

        x1.set(Register.normalize(x.get()));

        var result = operation.apply(x.get(), y.get());

        x.set(Register.normalize(result));
        y.set(z.get());
        z.set(t.get());
        x2.set(Register.xToIndicator(result));
    }

    synchronized void binaryKeepYOperation(BinaryOperator<Long> operation) {
        enteringExponent = false;

        x1.set(x.get());

        var result = operation.apply(x.get(), y.get());

        x.set(Register.normalize(result));
        x2.set(Register.xToIndicator(result));
    }

    synchronized void negate() {
        if (!enteringExponent) {
            x.set(Register.normalize(Register.negate(x.get())));
            x2.set(Register.xToIndicator(x.get()));
        } else {
            addCharacter('-');
        }
    }

    synchronized void addCharacter(char c) {
        var lastOpCode = lastExecutedOpCode.get();
        if (!OpCode.isDigit(lastOpCode) && lastOpCode != OpCode.DOT && !enteringExponent) {
            if (lastOpCode != OpCode.PUSH && lastOpCode != OpCode.CLEAR_X) {
                push();
            }
            xBuffer = 0;
            currentDigit = 7;
            dot = 7;
            hasDot = false;
        }

        var ri = x2.get().indicator();
        if (enteringExponent) {
            if (c == '.') {
                x2.set(IR.ERROR);
                enteringExponent = false;
                return;
            }
            if (c == '-') {
                if (Register.getTetrad(ri, 11) == 0xA) {
                    ri = Register.setTetrad(ri, 11, 0xF);
                } else {
                    ri = Register.setTetrad(ri, 11, 0xA);
                }
                x2.set(new IR(ri, x2.get().dots()));
            } else {
                ri = Register.setTetrad(ri, 10, Register.getTetrad(ri, 9));
                ri = Register.setTetrad(ri, 9, c - '0');
            }
            // Обновляем экспоненту регистра X
            var expDelta = Register.getTetrad(ri, 10) * 10 + Register.getTetrad(ri, 9);
            if (Register.getTetrad(ri, 11) == 0xA) {
                expDelta = -expDelta;
            }
            var newExponent = xExponent + expDelta;
            if (newExponent < -99) {
                x.set(0);
                x2.set(Register.xToIndicator(0));
                enteringExponent = false;
            } else if (newExponent > 99) {
                x2.set(IR.ERROR);
                enteringExponent = false;
            } else {
                x2.set(new IR(ri, x2.get().dots()));
                var newX = Register.setExponent(x.get(), newExponent);
                x.set(Register.normalize(newX));
            }
        } else {
            if (currentDigit < 0) {
                return;
            }

            if (c == '.') {
                if (hasDot || currentDigit == 7) {
                    return;
                }
                hasDot = true;
            } else {
                if (currentDigit == 7) {
                    ri = IR.EMPTY.indicator();
                }

                ri = Register.setTetrad(ri, currentDigit, c - '0');
                xBuffer = Register.setTetrad(xBuffer, currentDigit, c - '0');
                if (!hasDot) {
                    if (currentDigit != 7) {
                        dot--;
                    }
                }
                xBuffer = Register.setExponent(xBuffer, 7 - dot);
                currentDigit--;
            }

            x.set(Register.normalize(xBuffer));
            x2.set(new IR(ri, 1 << dot));
        }
    }

    synchronized void enterExponent() {
        enteringExponent = true;

        // Ставим экспоненту +00
        var ri = x2.get();
        var ind = ri.indicator();
        var dots = ri.dots();
        ind = Register.setTetrad(ind, 11, 0xF);
        ind = Register.setTetrad(ind, 10, 0x0);
        ind = Register.setTetrad(ind, 9, 0x0);

        // Запоминаем текущую экспоненту
        xExponent = Register.getExponent(x.get());

        // Если X == 0., то ставим в 1.
        if (Register.isZero(x.get())) {
            xBuffer = 0;
            xBuffer = Register.setTetrad(xBuffer, 7, Register.getTetrad(ind, 7) + 1);
            ind = Register.setTetrad(ind, 7, Register.getTetrad(xBuffer, 7) + Register.getTetrad(ind, 7));
            dots = 1 << 7;

            x.set(xBuffer);
        }

        x2.set(new IR(ind, dots));
    }
}
