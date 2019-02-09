// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.pia;


import org.javatari.general.board.BUS16Bits;
import org.javatari.parameters.Parameters;
import org.javatari.utils.Debugger;
import org.javatari.utils.Randomizer;

import java.io.Serializable;


public final class RAM implements BUS16Bits {

    public static final int CHIP_MASK = 0x1280;
    public static final int CHIP_SELECT = 0x0080;
    private static final int ADDRESS_MASK = 0x007f;
    private static final int FRY_ZERO_BITS = Parameters.RAM_FRY_ZERO_BITS;
    private static final int FRY_ONE_BITS = Parameters.RAM_FRY_ONE_BITS;
    private static final float FRY_VARIANCE = Parameters.RAM_FRY_VARIANCE;
    // State Variables --------------------------------------
    private final byte[] bytes = new byte[128];

    public RAM() {
        // RAM comes totally random at creation
        Randomizer.instance.nextBytes(bytes);
    }

    public void powerOn() {
        // Nothing
    }

    public void powerOff() {
        // Nothing
    }


    // Constants -------------------------------------------

    @Override
    public byte readByte(int address) {
        return bytes[(address & ADDRESS_MASK)];
    }

    @Override
    public void writeByte(int address, byte b) {
        bytes[(address & ADDRESS_MASK)] = b;
    }

    public void dump() {
        System.out.println("RAM DUMP:");
        Debugger.dumpBytes(bytes, 0, bytes.length);
    }

    public RAMState saveState() {
        RAMState state = new RAMState();
        state.bytes = bytes.clone();
        return state;
    }

    public void loadState(RAMState state) {
        System.arraycopy(state.bytes, 0, bytes, 0, bytes.length);
    }

    public void powerFry() {
        final float var = 1 - FRY_VARIANCE + 2 * Randomizer.instance.nextFloat() * FRY_VARIANCE;
        // Randomly put "0" in bits on the ram
        final int fryZeroBits = (int) (var * FRY_ZERO_BITS);
        for (int i = fryZeroBits; i > 0; i--)
            bytes[Randomizer.instance.nextInt(128)] &= (byte) Randomizer.instance.nextInt(256);
        // Randomly put "1" in bits on the ram
        final int fryOneBits = (int) (var * FRY_ONE_BITS);
        for (int i = fryOneBits; i > 0; i--)
            bytes[Randomizer.instance.nextInt(128)] |= (byte) (0x01 << Randomizer.instance.nextInt(8));
    }

    // Used to save/load states
    public static class RAMState implements Serializable {
        public static final long serialVersionUID = 2L;
        byte[] bytes;
    }

}
