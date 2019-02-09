package org.javatari.main;

import org.javatari.pc.room.EmbeddedRoom;
import org.javatari.pc.room.Room;


public final class AppletMultiplayerClient extends AbstractApplet {

    private static final long serialVersionUID = 1L;

    @Override
    protected Room buildRoom() {
        return EmbeddedRoom.buildClientRoom(this);
    }

    @Override
    public void start() {
        super.start();

        // Start connection to P1 Server
        if (room.isClientMode())
            MultiplayerClient.askUserForConnection(room);
    }

}
