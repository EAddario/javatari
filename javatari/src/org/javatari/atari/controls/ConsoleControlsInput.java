// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.controls;

import java.util.Map;

public interface ConsoleControlsInput {

    void controlStateChanged(ConsoleControls.Control control, boolean state);

    void controlStateChanged(ConsoleControls.Control control, int position);

    void controlsStateReport(Map<ConsoleControls.Control, Boolean> report);

}
