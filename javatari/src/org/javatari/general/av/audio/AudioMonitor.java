// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.general.av.audio;

public interface AudioMonitor {

    int nextSamples(byte[] samples, int quant);

    void synchOutput();

}
