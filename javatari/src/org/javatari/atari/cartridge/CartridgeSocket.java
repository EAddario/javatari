// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.cartridge;

public interface CartridgeSocket extends CartridgeInsertionListener {

    void insert(Cartridge cartridge, boolean autoPowerControl);

    Cartridge inserted();

    void addInsertionListener(CartridgeInsertionListener listener);

    void removeInsertionListener(CartridgeInsertionListener listener);

}
