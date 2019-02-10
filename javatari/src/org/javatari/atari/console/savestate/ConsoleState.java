// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.console.savestate;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.pia.PIA.PIAState;
import org.javatari.atari.pia.RAM.RAMState;
import org.javatari.atari.tia.TIA.TIAState;
import org.javatari.general.av.video.VideoStandard;
import org.javatari.general.m6502.M6502.M6502State;

import java.io.Serializable;


public final class ConsoleState implements Serializable {

    public static final long serialVersionUID = 2L;
    public final TIAState tiaState;
    public final PIAState piaState;
    public final RAMState ramState;
    public final M6502State cpuState;
    public final Cartridge cartridge;
    public final VideoStandard videoStandard;


    public ConsoleState(TIAState tia, PIAState pia, RAMState ram, M6502State cpu, Cartridge cartridge, VideoStandard videoStandard) {
        this.tiaState = tia;
        this.piaState = pia;
        this.ramState = ram;
        this.cpuState = cpu;
        this.cartridge = cartridge;
        this.videoStandard = videoStandard;
    }

}
