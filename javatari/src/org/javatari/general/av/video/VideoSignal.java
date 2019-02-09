// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.general.av.video;

public interface VideoSignal {

    VideoStandard standard();

    void connectMonitor(VideoMonitor monitor);

}

