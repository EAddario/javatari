// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.general.av.video;

public interface VideoMonitor {

    boolean nextLine(int[] pixels, boolean vSynch);

    void showOSD(String message, boolean overlap);

    void synchOutput();

    int currentLine();

    void videoStandardDetectionStart();

    VideoStandard videoStandardDetected();

}
