// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.controls;

import org.javatari.atari.controls.ConsoleControls.Control;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ConsoleControlsSocket implements ConsoleControlsInput {

    private final List<ConsoleControlsRedefinitionListener> redefinitonListeners = new ArrayList<>();
    private List<ConsoleControlsInput> forwardedInputs = new ArrayList<>();

    @Override
    public void controlStateChanged(Control control, boolean state) {
        for (ConsoleControlsInput input : forwardedInputs)
            input.controlStateChanged(control, state);
    }

    @Override
    public void controlStateChanged(ConsoleControls.Control control, int position) {
        for (ConsoleControlsInput input : forwardedInputs)
            input.controlStateChanged(control, position);
    }

    @Override
    public void controlsStateReport(Map<Control, Boolean> report) {
        for (ConsoleControlsInput input : forwardedInputs)
            input.controlsStateReport(report);
    }

    public void addForwardedInput(ConsoleControlsInput input) {
        forwardedInputs = new ArrayList<>(forwardedInputs);    // To prevent comodification
        forwardedInputs.add(input);
    }

    public void removeForwardedInput(ConsoleControlsInput input) {
        forwardedInputs = new ArrayList<>(forwardedInputs);    // To prevent comodification
        forwardedInputs.remove(input);
    }

    public void addRedefinitionListener(ConsoleControlsRedefinitionListener listener) {
        if (!redefinitonListeners.contains(listener)) {
            redefinitonListeners.add(listener);
            listener.controlsStatesRedefined();        // Fire a redefinition event
        }
    }

    public void controlsStatesRedefined() {
        for (ConsoleControlsRedefinitionListener listener : redefinitonListeners)
            listener.controlsStatesRedefined();
    }

}
