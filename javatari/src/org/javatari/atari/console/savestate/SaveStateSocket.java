// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.console.savestate;

public interface SaveStateSocket {

    void connectMedia(SaveStateMedia media);

    SaveStateMedia media();

    void externalStateChange();

    void saveStateFile();

}
