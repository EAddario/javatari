// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.main;

import org.javatari.parameters.Parameters;
import org.javatari.pc.room.Room;
import org.javatari.utils.Environment;


public final class TestStandalone {

    public static void main(String[] args) {

        // Initialize application environment
        Environment.init();

        // Load Parameters from properties file and process arguments
        Parameters.init(args);

        // Force test Cartridge as main argument
        Parameters.mainArg = "file:./javatari/src/roms/Maze.bin";

        // Build a Room for Standalone play
        final Room room = Room.buildStandaloneRoom();

        // Turn everything on
        room.powerOn();

        // Keep logging info about clocks speeds achieved
        (new Thread(() -> {
            while (true) {
                System.out.print("Main Clock " + room.currentConsole().mainClock() + ", ");
                System.out.print("Monitor Clock " + room.screen().monitor().clock + ", ");
                System.out.println("Audio Clock " + room.speaker().clock);

                System.out.print("Video Output Height " + room.currentConsole().videoOutput().standard().height + ", ");
                System.out.print("Video Output Width " + room.currentConsole().videoOutput().standard().width + ", ");
                System.out.println("Video Output FPS " + room.currentConsole().videoOutput().standard().fps);

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }
            }
        })).start();

    }

}
