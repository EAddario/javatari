// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge;


public class CartridgeFormatOption implements Comparable<CartridgeFormatOption> {

    public final CartridgeFormat format;
    int priority;

    public CartridgeFormatOption(int priority, CartridgeFormat format, ROM rom) {
        super();
        this.priority = priority;
        this.format = format;
    }

    @Override
    public int compareTo(CartridgeFormatOption o) {
        return Integer.compare(priority, o.priority);
    }

    @Override
    public String toString() {
        return "Format: " + format + ", priority: " + priority;
    }

}
