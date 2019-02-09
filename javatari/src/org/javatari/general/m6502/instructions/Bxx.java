// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.general.m6502.instructions;

import org.javatari.general.m6502.Instruction;
import org.javatari.general.m6502.M6502;

import static org.javatari.general.m6502.StatusBit.*;

public final class Bxx extends Instruction {


    public static final long serialVersionUID = 1L;
    private final int bit;
    private final boolean cond;
    private int newPC;
    private boolean branch;

    public Bxx(M6502 cpu, int bit, boolean cond) {
        super(cpu);
        this.bit = bit;
        this.cond = cond;
    }

    @Override
    public int fetch() {
        newPC = cpu.fetchRelativeAddress();        // Reads operand regardless of the branch being taken or not
        if (bit == bZERO) {
            branch = cpu.ZERO == cond;
        } else if (bit == bNEGATIVE) {
            branch = cpu.NEGATIVE == cond;
        } else if (bit == bCARRY) {
            branch = cpu.CARRY == cond;
        } else if (bit == bOVERFLOW) {
            branch = cpu.OVERFLOW == cond;
        } else throw new IllegalStateException("Bxx Invalid StatusBit: " + bit);

        return branch ? (cpu.pageCrossed ? 4 : 3) : 2;
    }

    @Override
    public void execute() {
        if (branch) cpu.PC = newPC;        // TODO Check if we have to make additional reads here
    }

}
