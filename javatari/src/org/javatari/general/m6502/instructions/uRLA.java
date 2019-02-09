// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.general.m6502.instructions;

import org.javatari.general.m6502.Instruction;
import org.javatari.general.m6502.M6502;
import org.javatari.general.m6502.OperandType;

public final class uRLA extends Instruction {

    public static final long serialVersionUID = 1L;
    private final int type;
    private int ea;

    public uRLA(M6502 cpu, int type) {
        super(cpu);
        this.type = type;
    }

    @Override
    public int fetch() {

        cpu.debug(">>> Undocumented opcode RLA");

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
        if (type == OperandType.ABS_Y) {
            ea = cpu.fetchAbsoluteYAddress();
            return 7;
        }
        if (type == OperandType.IND_X) {
            ea = cpu.fetchIndirectXAddress();
            return 8;
        }
        if (type == OperandType.IND_Y) {
            ea = cpu.fetchIndirectYAddress();
            return 8;
        }
        throw new IllegalStateException("uRLA Invalid Operand Type: " + type);
    }

    @Override
    public void execute() {
        byte val = cpu.bus.readByte(ea);
        final int oldCarry = cpu.CARRY ? 1 : 0;
        cpu.CARRY = val < 0;        // bit 7 was set
        val = (byte) ((val << 1) | oldCarry);
        cpu.bus.writeByte(ea, val);
        cpu.A = (byte) (cpu.A & val);
        cpu.ZERO = val == 0;            // TODO Verify. May be A instead of val in the flags setting
        cpu.NEGATIVE = val < 0;
    }

}
