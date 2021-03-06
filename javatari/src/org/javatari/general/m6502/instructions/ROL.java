// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.general.m6502.instructions;

import org.javatari.general.m6502.Instruction;
import org.javatari.general.m6502.M6502;
import org.javatari.general.m6502.OperandType;

import static org.javatari.general.m6502.OperandType.ACC;

public final class ROL extends Instruction {

    public static final long serialVersionUID = 1L;
    private final int type;
    private int ea;

    public ROL(M6502 cpu, int type) {
        super(cpu);
        this.type = type;
    }

    @Override
    public int fetch() {
        if (type == OperandType.ACC) {
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
        throw new IllegalStateException("ROL Invalid Operand Type: " + type);
    }

    @Override
    public void execute() {
        // Special case for ACC
        if (type == ACC) {
            byte val = cpu.A;
            int oldCarry = cpu.CARRY ? 1 : 0;
            cpu.CARRY = val < 0;        // bit 7 was set
            val = (byte) ((val << 1) | oldCarry);
            cpu.A = val;
            cpu.ZERO = val == 0;
            cpu.NEGATIVE = val < 0;
        } else {
            byte val = cpu.bus.readByte(ea);
            int oldCarry = cpu.CARRY ? 1 : 0;
            cpu.CARRY = val < 0;        // bit 7 was set
            val = (byte) ((val << 1) | oldCarry);
            cpu.ZERO = val == 0;
            cpu.NEGATIVE = val < 0;
            cpu.bus.writeByte(ea, val);
        }
    }

}
