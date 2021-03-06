// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.general.m6502.instructions;

import org.javatari.general.m6502.Instruction;
import org.javatari.general.m6502.M6502;
import org.javatari.general.m6502.OperandType;

import static org.javatari.general.m6502.OperandType.ACC;

public final class LSR extends Instruction {

    public static final long serialVersionUID = 1L;
    private final int type;
    private int ea;

    public LSR(M6502 cpu, int type) {
        super(cpu);
        this.type = type;
    }

    @Override
    public int fetch() {
        if (type == OperandType.ACC) {
            ea = -1;
            return 2;
        }
        if (type == OperandType.Z_PAGE) {
            ea = cpu.fetchZeroPageAddress();
            return 5;
        }
        if (type == OperandType.Z_PAGE_X) {
            ea = cpu.fetchZeroPageXAddress();
            return 6;
        }
        if (type == OperandType.ABS) {
            ea = cpu.fetchAbsoluteAddress();
            return 6;
        }
        if (type == OperandType.ABS_X) {
            ea = cpu.fetchAbsoluteXAddress();
            return 7;
        }
        throw new IllegalStateException("LSR Invalid Operand Type: " + type);
    }

    @Override
    public void execute() {
        // Special case for ACC
        if (type == ACC) {
            byte val = cpu.A;
            cpu.CARRY = (val & 0x01) > 0;        // bit 0 was set
            val = (byte) ((val & 0xff) >>> 1);
            cpu.A = val;
            cpu.ZERO = val == 0;
            cpu.NEGATIVE = false;
        } else {
            byte val = cpu.bus.readByte(ea);
            cpu.CARRY = (val & 0x01) != 0;        // bit 0 was set
            val = (byte) ((val & 0xff) >>> 1);
            cpu.ZERO = val == 0;
            cpu.NEGATIVE = false;
            cpu.bus.writeByte(ea, val);
        }
    }

}
