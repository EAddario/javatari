// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.network;

import org.javatari.atari.console.savestate.ConsoleState;

import java.io.Serializable;
import java.util.List;


public final class ServerUpdate implements Serializable {

    public static final long serialVersionUID = 2L;
    Boolean powerChange = null;
    List<ControlChange> controlChanges = null;
    ConsoleState consoleState = null;

}
