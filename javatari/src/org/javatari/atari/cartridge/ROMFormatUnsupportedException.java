// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge;

public final class ROMFormatUnsupportedException extends Exception {

    private static final long serialVersionUID = 1L;

    ROMFormatUnsupportedException(String message) {
        super(message);
    }

}
