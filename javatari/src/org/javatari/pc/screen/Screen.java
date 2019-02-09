// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.pc.screen;

import org.javatari.atari.cartridge.CartridgeSocket;
import org.javatari.atari.console.savestate.SaveStateSocket;
import org.javatari.atari.controls.ConsoleControlsSocket;
import org.javatari.general.av.video.VideoSignal;

import java.awt.*;
import java.util.List;

public interface Screen {

    void connect(VideoSignal videoSignal, ConsoleControlsSocket controlsSocket, CartridgeSocket cartridgeSocket, SaveStateSocket savestateSocket);

    Monitor monitor();

    List<Component> keyControlsInputComponents();

    void powerOn();

    void powerOff();

    void close();

    void destroy();

}
