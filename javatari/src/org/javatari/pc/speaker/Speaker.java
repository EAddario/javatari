// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.pc.speaker;

import org.javatari.general.av.audio.AudioMonitor;
import org.javatari.general.av.audio.AudioSignal;
import org.javatari.general.board.Clock;
import org.javatari.general.board.ClockDriven;
import org.javatari.parameters.Parameters;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;
import java.nio.ByteBuffer;


public final class Speaker implements ClockDriven, AudioMonitor {

    private static final double FPS = Parameters.SPEAKER_DEFAULT_FPS;
    private static final int SAMPLE_RATE = Parameters.TIA_AUDIO_SAMPLE_RATE;
    private static final int INPUT_BUFFER_SIZE = Parameters.SPEAKER_INPUT_BUFFER_SIZE;                            // In frames (samples)
    private static final int OUTPUT_BUFFER_SIZE = Parameters.SPEAKER_OUTPUT_BUFFER_SIZE;                        // In frames (samples)
    private static final int OUTPUT_BUFFER_FULL_SLEEP_TIME = Parameters.SPEAKER_OUTPUT_BUFFER_FULL_SLEEP_TIME;    // In milliseconds
    private static final int NO_DATA_SLEEP_TIME = Parameters.SPEAKER_NO_DATA_SLEEP_TIME;                        // In milliseconds
    private static final int ADDED_THREAD_PRIORITY = Parameters.SPEAKER_ADDED_THREAD_PRIORITY;
    private final AudioFormat AUDIO_FORMAT = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
    public Clock clock;
    private AudioSignal signal;
    private SourceDataLine dataLine;
    private ByteBuffer inputBuffer;
    private byte[] tempBuffer;
    private boolean triedToGetLine = false;

    public void connect(AudioSignal signal) {    // Must be powered off to connect a signal
        this.signal = signal;
        signal.connectMonitor(this);
    }

    public void powerOn() {
        if (!triedToGetLine) getLine();
        if (dataLine == null) return;
        dataLine.start();
        clock.go();
    }

    public void powerOff() {
        if (dataLine == null) return;
        clock.pause();
        dataLine.flush();
        dataLine.stop();
    }

    public void destroy() {
        if (dataLine == null) return;
        clock.terminate();
        dataLine.close();
        dataLine = null;
    }

    @Override
    public synchronized int nextSamples(byte[] buffer, int quant) {
        if (dataLine == null) return -1;
        if (buffer == null) {        // Signal is off
            dataLine.flush();
            return -1;
        }
        // Drop samples that don't fit the input buffer available capacity
        int ava = inputBuffer.remaining();
        if (ava > quant)
            ava = quant;
        // else
        // if (ava < quant) System.out.println(">>>> DROPPED: " + quant + " - " + ava + " = " + (quant - ava));
        inputBuffer.put(buffer, 0, ava);

        // System.out.println(">>>> Available: " + inputBuffer.position());
        return inputBuffer.position();
    }

    @Override
    public void synchOutput() {
        refresh();
    }

    @Override
    public void clockPulse() {
        synchOutput();
    }

    private void getLine() {
        if (signal == null) return;
        try {
            triedToGetLine = true;
            dataLine = AudioSystem.getSourceDataLine(AUDIO_FORMAT);
            dataLine.open(AUDIO_FORMAT, OUTPUT_BUFFER_SIZE);
            inputBuffer = ByteBuffer.allocateDirect(INPUT_BUFFER_SIZE);
            tempBuffer = new byte[inputBuffer.capacity()];
            if (ADDED_THREAD_PRIORITY != 0) clock.setPriority(Thread.NORM_PRIORITY + ADDED_THREAD_PRIORITY);
            System.out.println("Sound Mixer Line: " + dataLine.getClass().getSimpleName());
            clock = new Clock("Speaker", this, FPS);
        } catch (Exception ex) {
            System.out.println("Unable to acquire audio line:\n" + ex);
            dataLine = null;
        }
    }

    private synchronized int getFromInputBuffer(byte[] buffer, int quant) {
        inputBuffer.flip();
        int ava = inputBuffer.remaining();
        if (ava > quant)
            ava = quant;
        inputBuffer.get(buffer, 0, ava);
        inputBuffer.compact();
        return ava;
    }

    private void refresh() {
        if (dataLine == null) return;
        int ava = dataLine.available();        // this is a little expensive... :-(

        // System.out.println(">> Out: " + (OUTPUT_BUFFER_SIZE - ava) + "\tIn: " + inputBuffer.position());

        if (ava == 0) {
            // System.out.println("+ OutputBuffer FULL, InputBuffer: " + inputBuffer.position());
            if (OUTPUT_BUFFER_FULL_SLEEP_TIME > 0 && FPS < 0)
                try {
                    Thread.sleep(OUTPUT_BUFFER_FULL_SLEEP_TIME, 0);
                } catch (InterruptedException ignored) {
                }
            return;
        }
        int data = getFromInputBuffer(tempBuffer, ava);
        if (data == 0) {
            // System.out.println("- InputBuffer EMPTY, OutputBuffer: " + (OUTPUT_BUFFER_SIZE - ava));
            if (NO_DATA_SLEEP_TIME > 0 && FPS < 0)
                try {
                    Thread.sleep(NO_DATA_SLEEP_TIME, 0);
                } catch (InterruptedException ignored) {
                }
            return;
        }

        dataLine.write(tempBuffer, 0, data);
        if (FPS < 0)
            try {
                Thread.sleep(OUTPUT_BUFFER_FULL_SLEEP_TIME, 0);
            } catch (InterruptedException ignored) {
            }
    }

}
