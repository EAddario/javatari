// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.joy;

import org.joy.Joy.Info;

import java.util.List;


public class TestJoystick {

    public static void main(String[] args) throws InterruptedException {

        List<Info> devices = Joy.listDevices();

        if (devices.isEmpty()) {
            System.out.println("No devices connected!");
            return;
        }

        for (Info info : devices) {
            System.out.println(info.description());
        }

        Joystick joystick = Joystick.create(devices.get(0));

        while (true) {
            joystick.update();
            System.out.println(joystick);

            Thread.sleep(30);
        }

    }

}
