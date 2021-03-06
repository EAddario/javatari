// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.tia.audio;

import org.javatari.parameters.Parameters;

public final class AudioMonoGenerator extends AudioGenerator {

    private static final float MAX_AMPLITUDE = Parameters.TIA_AUDIO_MAX_AMPLITUDE;
    private float lastSample = 0;

    @Override
    protected void generateNextSamples(int quant) {
        for (int i = quant; i > 0; i--) {
            float mixedSample = channel0.nextSample() - channel1.nextSample();

            // Add a little damper effect to round the edges of the square wave
            if (mixedSample != lastSample) {
                mixedSample = (mixedSample * 9 + lastSample) / 10;
                lastSample = mixedSample;
            }

            samples[generatedSamples++] = (byte) (mixedSample * MAX_AMPLITUDE * 127);
            frameSamples++;
        }
    }

    @Override
    public void signalOff() {
        lastSample = 0;
        super.signalOff();
    }

}
