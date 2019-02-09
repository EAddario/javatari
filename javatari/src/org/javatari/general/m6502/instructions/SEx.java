// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.general.m6502.instructions;

import org.javatari.general.m6502.Instruction;
import org.javatari.general.m6502.M6502;

import static org.javatari.general.m6502.StatusBit.*;

public final class SEx extends Instruction {

    public static final long serialVersionUID = 1L;
    private final int bit;

    public SEx(M6502 cpu, int bit) {
        super(cpu);
        this.bit = bit;
    }

    @Override
    public int fetch() {
        return 2;
    }

    @Override
    public void execute() {
        if (bit == bCARRY) cpu.CARRY = true;
        else if (bit == bDECIMAL_MODE) cpu.DECIMAL_MODE = true;
        else if (bit == bINTERRUPT_DISABLE) cpu.INTERRUPT_DISABLE = true;
        else throw new IllegalStateException("SEx Invalid StatusBit: " + bit);
    }

}
