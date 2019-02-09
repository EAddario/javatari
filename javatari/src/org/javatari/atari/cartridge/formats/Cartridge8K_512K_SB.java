// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge.formats;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.CartridgeFormat;
import org.javatari.atari.cartridge.CartridgeFormatOption;
import org.javatari.atari.cartridge.ROM;

/**
 * Implements the 8K-512K "SB" Superbanking format
 */
public class Cartridge8K_512K_SB extends CartridgeBankedByBusMonitoring {

    public static final long serialVersionUID = 1L;
    protected static final int BANK_SIZE = 4096;
    private static final int MIN_SIZE = 2 * BANK_SIZE;
    private static final int MAX_SIZE = 64 * BANK_SIZE;
    public static final CartridgeFormat FORMAT = new CartridgeFormat("SB", "8K-512K Superbanking") {
        private static final long serialVersionUID = 1L;

        @Override
        public Cartridge createCartridge(ROM rom) {
            return new Cartridge8K_512K_SB(rom);
        }

        @Override
        public CartridgeFormatOption getOption(ROM rom) {
            if (rom.content.length % BANK_SIZE != 0 || rom.content.length < MIN_SIZE || rom.content.length > MAX_SIZE)
                return null;
            return new CartridgeFormatOption(113, this, rom);
        }
    };
    private final int maxBank;

    private Cartridge8K_512K_SB(ROM rom) {
        super(rom, FORMAT);
        maxBank = bytes.length / BANK_SIZE - 1;
    }

    @Override
    protected void performBankSwitchOnMonitoredAccess(int address) {
        // Perform bank switching as needed
        if ((address & 0x1800) != 0x0800) return;
        int bank = address & 0x007f;
        if (bank > maxBank) return;
        bankAddressOffset = bank * BANK_SIZE;
    }

}
