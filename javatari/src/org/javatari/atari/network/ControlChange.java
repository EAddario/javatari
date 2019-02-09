// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.network;

import org.javatari.atari.controls.ConsoleControls.Control;

import java.io.Serializable;


public class ControlChange implements Serializable {

    public static final long serialVersionUID = 1L;
    public Control control;
    public boolean state;
    ControlChange() {
        super();
    }

    ControlChange(Control control, boolean state) {
        this.control = control;
        this.state = state;
    }

}
