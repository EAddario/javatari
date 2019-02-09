package org.javatari.main;

import org.javatari.pc.room.EmbeddedRoom;
import org.javatari.pc.room.Room;


public final class AppletStandalone extends AbstractApplet {

    private static final long serialVersionUID = 1L;

    @Override
    protected Room buildRoom() {
        return EmbeddedRoom.buildStandaloneRoom(this);
    }

}
