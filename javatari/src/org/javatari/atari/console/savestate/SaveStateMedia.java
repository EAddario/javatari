// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.console.savestate;

public interface SaveStateMedia {

    boolean saveStateFile(ConsoleState state);

    boolean saveState(int slot, ConsoleState state);

    ConsoleState loadState(int slot);

    boolean saveResourceToFile(String name, Object data);

    Object loadResourceFromFile(String name);

}
