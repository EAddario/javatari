//Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.joy;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;


public class Joy {

    private static boolean nativeLibLoaded = false;

    public static synchronized List<Info> listDevices() throws UnsupportedOperationException {
        if (!nativeLibLoaded) init();

        try {
            ArrayList<Info> devices = new ArrayList<>();
            int numDevices = JoyInterface.getMaxDevices();
            for (int id = 0; id < numDevices; id++)
                if (JoyInterface.isConnected(id)) {
                    Info info = new Info(id);
                    devices.add(info);
                }
            return devices;
        } catch (Throwable e) {
            throw new UnsupportedOperationException("Joy: Could not access Native Library: " + e.getMessage(), e);
        }
    }

    private static synchronized void init() throws UnsupportedOperationException {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {
            public Void run() {
                try {
                    String libName = nativeLibName();
                    try {
                        // First try loading the library from default location
                        System.loadLibrary(libName);
                    } catch (Throwable e) {
                        // If not possible, extract the correct lib to a temp file
                        String libFileName = System.mapLibraryName(libName);
                        String libResource = "/org/joy/nativelibs/" + libFileName;
                        InputStream resInput = getClass().getResourceAsStream(libResource);
                        if (resInput == null) throw new UnsupportedOperationException("Unsupported Operating System");

                        File tempLibFile = File.createTempFile(
                                libFileName.substring(0, libFileName.lastIndexOf(".")),
                                libFileName.substring(libFileName.lastIndexOf(".")));
                        tempLibFile.deleteOnExit();

                        OutputStream tempOutput = Files.newOutputStream(tempLibFile.toPath());
                        int bytes;
                        byte[] buffer = new byte[16384];
                        while ((bytes = resInput.read(buffer)) != -1) tempOutput.write(buffer, 0, bytes);
                        tempOutput.close();
                        resInput.close();

                        // Then load the library
                        System.load(tempLibFile.getAbsolutePath());
                    }
                    nativeLibLoaded = true;
                    return null;
                } catch (Throwable e) {
                    throw new UnsupportedOperationException("Joy: Could not load Native Library: " + e.getMessage(), e);
                }
            }
        });
    }

    private static String nativeLibName() {
        String arch = System.getProperty("os.arch");
        if (arch == null || !arch.contains("64"))
            return "JoyInterface32";
        else
            return "JoyInterface64";
    }

    private static String printIntArray(int[] arr) {
        StringBuilder res = new StringBuilder("" + arr[0]);
        for (int i = 1; i < arr.length; i++) res.append(", ").append(arr[i]);
        return res.toString();
    }

    public static class Info {
        public int id;
        public String name = "Undefined";
        public boolean pov;
        public int buttons;
        int axes;
        int[] axesMinValues = new int[6];
        int[] axesMaxValues = new int[6];
        private State state;

        private Info(int id) {
            this.id = id;
            JoyInterface.updateInfo(id, this);
        }

        public State getState() {
            if (state == null) state = new State(id);
            state.update();
            return state;
        }

        public String toString() {
            return "" + id + ": " + name;
        }

        String description() {
            return
                    "" + id + ": " + name + "\n" +
                            "POV: " + (pov ? "yes" : "no") + ", Axes: " + axes + ", Buttons: " + buttons + "\n" +
                            "Mins: " + printIntArray(axesMinValues) + ", Maxes: " + printIntArray(axesMaxValues);
        }
    }

    static class State {
        private final int id;
        private final int[] data = new int[8];

        private State(int id) {
            this.id = id;
        }

        public boolean update() {
            return JoyInterface.updateState(id, this);
        }

        int getPOV() {
            return data[0];
        }

        int getButtons() {
            return data[1];
        }

        int getAxis(int axis) {
            assert (axis >= 0 && axis <= 5);
            return data[2 + axis];
        }

        public String toString() {
            return "State: " + printIntArray(data);
        }
    }


}
