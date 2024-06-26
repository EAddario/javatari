// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.console;

import org.javatari.atari.board.BUS;
import org.javatari.atari.cartridge.*;
import org.javatari.atari.cartridge.formats.CartridgeSavestate;
import org.javatari.atari.console.savestate.ConsoleState;
import org.javatari.atari.console.savestate.SaveStateMedia;
import org.javatari.atari.console.savestate.SaveStateSocket;
import org.javatari.atari.controls.ConsoleControls;
import org.javatari.atari.controls.ConsoleControls.Control;
import org.javatari.atari.controls.ConsoleControlsInput;
import org.javatari.atari.controls.ConsoleControlsSocket;
import org.javatari.atari.pia.PIA;
import org.javatari.atari.pia.RAM;
import org.javatari.atari.tia.TIA;
import org.javatari.general.av.audio.AudioSignal;
import org.javatari.general.av.video.VideoSignal;
import org.javatari.general.av.video.VideoStandard;
import org.javatari.general.board.Clock;
import org.javatari.general.m6502.M6502;
import org.javatari.parameters.Parameters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class Console {

    private static final float ALTERNATE_CLOCK_FACTOR = Parameters.CONSOLE_ALTERNATE_CLOCK_FACTOR;
    public boolean powerOn = false;
    protected BUS bus;
    protected M6502 cpu;
    protected TIA tia;
    protected PIA pia;
    protected RAM ram;
    protected ConsoleControlsSocket controlsSocket;
    protected CartridgeSocketAdapter cartridgeSocket;
    protected SaveStateSocketAdapter saveStateSocket;
    protected Clock mainClock;
    private VideoStandard videoStandard;
    private boolean videoStandardAuto = true;
    private boolean videoStandardAutoDetectionInProgress = false;

    public Console() {
        mainComponentsCreate();
        socketsCreate();
        mainClockCreate();
        videoStandardAuto();
    }

    public VideoSignal videoOutput() {
        return tia.videoOutput();
    }

    public AudioSignal audioOutput() {
        return tia.audioOutput();
    }

    public ConsoleControlsSocket controlsSocket() {
        return controlsSocket;
    }

    public CartridgeSocket cartridgeSocket() {
        return cartridgeSocket;
    }

    public SaveStateSocket saveStateSocket() {
        return saveStateSocket;
    }

    public void powerOn() {
        if (powerOn) powerOff();
        cpu.powerOn();
        bus.powerOn();
        powerOn = true;
        controlsSocket.controlsStatesRedefined();
        go();
        videoStandardAutoDetectionStart();
    }

    public void powerOff() {
        pause();
        bus.powerOff();
        cpu.powerOff();
        powerOn = false;
        controlsSocket.controlsStatesRedefined();
    }

    public void extendedPowerOff() {
        powerOff();
    }

    public void destroy() {
        extendedPowerOff();
        mainClockDestroy();
    }

    public void showOSD(String message, boolean overlap) {
        tia.videoOutput().showOSD(message, overlap);
    }

    private VideoStandard videoStandard() {
        return videoStandard;
    }

    private void videoStandard(VideoStandard videoStandard) {
        if (videoStandard != this.videoStandard) {
            this.videoStandard = videoStandard;
            tia.videoStandard(this.videoStandard);
            mainClockAdjustToNormal();
        }
        showOSD((videoStandardAuto ? "AUTO: " : "") + videoStandard.toString(), false);
    }

    public void go() {
        mainClock.go();
    }

    public void pause() {
        mainClock.pause();
    }

    // For debug purposes
    public Clock mainClock() {
        return mainClock;
    }

    protected Cartridge cartridge() {
        return bus.cartridge;
    }

    protected void cartridge(Cartridge cartridge) {
        controlsSocket.removeForwardedInput(cartridge());
        bus.cartridge(cartridge);
        cartridgeSocket.cartridgeInserted(cartridge);
        if (cartridge != null) {
            controlsSocket.addForwardedInput(cartridge);
            saveStateSocket.connectCartridge(cartridge);
        }
    }

    private void videoStandardAuto() {
        videoStandardAuto = true;
        if (powerOn) videoStandardAutoDetectionStart();
        else videoStandard(VideoStandard.NTSC);
    }

    private void videoStandardAutoDetectionStart() {
        if (!videoStandardAuto || videoStandardAutoDetectionInProgress) return;
        // If no Cartridge present, use NTSC
        if (bus.cartridge == null) {
            videoStandard(VideoStandard.NTSC);
            return;
        }
        // Otherwise use the VideoStandard detected by the monitor
        if (tia.videoOutput().monitor() == null) return;
        videoStandardAutoDetectionInProgress = true;
        tia.videoOutput().monitor().videoStandardDetectionStart();
        // TODO This thread is a source of indeterminism. Potential problem in multiplayer sync
        new Thread("Console VideoStd Detection") {
            public void run() {
                VideoStandard std;
                int tries = 0;
                do {
                    try {
                        Thread.sleep(20);
                    } catch (InterruptedException ignored) {
                    }
                    std = tia.videoOutput().monitor().videoStandardDetected();
                } while (std == null && ++tries < 1500 / 20);
                if (std != null) videoStandard(std);
                else showOSD("AUTO: FAILED", false);
                videoStandardAutoDetectionInProgress = false;
            }
        }.start();
    }

    private void videoStandardForced(VideoStandard forcedVideoStandard) {
        videoStandardAuto = false;
        videoStandard(forcedVideoStandard);
    }

    private void mainComponentsCreate() {
        cpu = new M6502();
        tia = new TIA();
        pia = new PIA();
        ram = new RAM();
        bus = new BUS(cpu, tia, pia, ram);
    }

    protected void mainClockCreate() {
        mainClock = new Clock("Console(TIA)", tia, 0);
    }

    protected void mainClockAdjustToNormal() {
        double fps = tia.desiredClockForVideoStandard();
        mainClock.speed(fps);
        tia.audioOutput().fps(fps);
    }

    protected void mainClockAdjustToAlternate() {
        double fps = tia.desiredClockForVideoStandard() * ALTERNATE_CLOCK_FACTOR;
        mainClock.speed(fps);
        tia.audioOutput().fps(fps);
    }

    protected void mainClockDestroy() {
        mainClock.terminate();
    }

    protected void socketsCreate() {
        controlsSocket = new ConsoleControlsSocket();
        controlsSocket.addForwardedInput(new ConsoleControlsInputAdapter());
        controlsSocket.addForwardedInput(tia);
        controlsSocket.addForwardedInput(pia);
        cartridgeSocket = new CartridgeSocketAdapter();
        saveStateSocket = new SaveStateSocketAdapter();
    }

    protected void loadState(ConsoleState state) {
        tia.loadState(state.tiaState);
        pia.loadState(state.piaState);
        ram.loadState(state.ramState);
        cpu.loadState(state.cpuState);
        cartridge(state.cartridge);
        videoStandard(state.videoStandard);
        controlsSocket.controlsStatesRedefined();
    }

    protected ConsoleState saveState() {
        return new ConsoleState(
                tia.saveState(),
                pia.saveState(),
                ram.saveState(),
                cpu.saveState(),
                cartridge() != null ? cartridge().saveState() : null,
                videoStandard()
        );
    }

    protected void cycleCartridgeFormat() {
        if (cartridge() == null) {
            showOSD("NO CARTRIDGE INSERTED!", true);
            return;
        }
        ArrayList<CartridgeFormatOption> options;
        try {
            options = CartridgeDatabase.getFormatOptions(cartridge().rom());
        } catch (ROMFormatUnsupportedException e) {
            return;
        }
        CartridgeFormatOption currOption = null;
        for (CartridgeFormatOption option : options)
            if (option.format.equals(cartridge().format())) currOption = option;
        int pos = options.indexOf(currOption) + 1;        // cycle through options
        if (pos >= options.size()) pos = 0;
        CartridgeFormatOption newOption = options.get(pos);
        Cartridge newCart = newOption.format.createCartridge(cartridge().rom());
        cartridgeSocket().insert(newCart, true);
        showOSD(newOption.format.toString(), true);
    }

    protected void powerFry() {
        ram.powerFry();
    }

    private ConsoleState pauseAndSaveState() {
        pause();
        ConsoleState state = saveState();
        go();
        return state;
    }

    private void pauseAndLoadState(ConsoleState state) {
        if (powerOn) {
            pause();
            loadState(state);
            go();
        } else {
            powerOn();
            pauseAndLoadState(state);
        }
    }

    protected class ConsoleControlsInputAdapter implements ConsoleControlsInput {
        public ConsoleControlsInputAdapter() {
        }

        @Override
        public void controlStateChanged(Control control, boolean state) {
            // Normal state controls
            if (control == Control.FAST_SPEED) {
                if (state) {
                    showOSD("FAST FORWARD", true);
                    mainClockAdjustToAlternate();
                } else {
                    showOSD(null, true);
                    mainClockAdjustToNormal();
                }
                return;
            }
            // Toggles
            if (!state) return;
            switch (control) {
                case POWER:
                    if (powerOn) powerOff();
                    else powerOn();
                    break;
                case POWER_FRY:
                    powerFry();
                    break;
                case SAVE_STATE_0:
                case SAVE_STATE_1:
                case SAVE_STATE_2:
                case SAVE_STATE_3:
                case SAVE_STATE_4:
                case SAVE_STATE_5:
                case SAVE_STATE_6:
                case SAVE_STATE_7:
                case SAVE_STATE_8:
                case SAVE_STATE_9:
                case SAVE_STATE_10:
                case SAVE_STATE_11:
                case SAVE_STATE_12:
                    saveStateSocket.saveState(control.slot);
                    break;
                case LOAD_STATE_0:
                case LOAD_STATE_1:
                case LOAD_STATE_2:
                case LOAD_STATE_3:
                case LOAD_STATE_4:
                case LOAD_STATE_5:
                case LOAD_STATE_6:
                case LOAD_STATE_7:
                case LOAD_STATE_8:
                case LOAD_STATE_9:
                case LOAD_STATE_10:
                case LOAD_STATE_11:
                case LOAD_STATE_12:
                    saveStateSocket.loadState(control.slot);
                    break;
                case VIDEO_STANDARD:
                    showOSD(null, true);    // Prepares for the upcoming "AUTO" OSD to always show
                    if (videoStandardAuto) videoStandardForced(VideoStandard.NTSC);
                    else if (videoStandard() == VideoStandard.NTSC) videoStandardForced(VideoStandard.PAL);
                    else videoStandardAuto();
                    break;
                case CARTRIDGE_FORMAT:
                    cycleCartridgeFormat();
            }
        }

        @Override
        public void controlStateChanged(ConsoleControls.Control control, int position) {
            // No positional controls here
        }

        @Override
        public void controlsStateReport(Map<ConsoleControls.Control, Boolean> report) {
            //  Only Power Control is visible from outside
            report.put(Control.POWER, powerOn);
        }
    }

    protected class CartridgeSocketAdapter implements CartridgeSocket {
        private final List<CartridgeInsertionListener> insertionListeners = new ArrayList<>();

        @Override
        public void insert(Cartridge cartridge, boolean autoPower) {
            // Special case for Savestates
            if (cartridge instanceof CartridgeSavestate) {
                insertSavestateCartridge((CartridgeSavestate) cartridge);
                return;
            }
            // Normal case
            if (autoPower && powerOn) powerOff();
            cartridge(cartridge);
            if (autoPower && !powerOn) powerOn();
        }

        @Override
        public Cartridge inserted() {
            return cartridge();
        }

        @Override
        public void cartridgeInserted(Cartridge cartridge) {
            for (CartridgeInsertionListener listener : insertionListeners)
                listener.cartridgeInserted(cartridge);
        }

        @Override
        public void addInsertionListener(CartridgeInsertionListener listener) {
            if (!insertionListeners.contains(listener)) {
                insertionListeners.add(listener);
                listener.cartridgeInserted(inserted());        // Fire a insertion event
            }
        }

        @Override
        public void removeInsertionListener(CartridgeInsertionListener listener) {
            insertionListeners.remove(listener);
        }

        protected void insertSavestateCartridge(CartridgeSavestate cartridge) {
            ConsoleState state = cartridge.getConsoleState();
            if (state != null) {
                pauseAndLoadState(state);
                showOSD("Savestate Cartridge loaded", true);
            }
        }
    }

    protected class SaveStateSocketAdapter implements SaveStateSocket {
        private SaveStateMedia media;

        @Override
        public void connectMedia(SaveStateMedia media) {
            this.media = media;
        }

        @Override
        public SaveStateMedia media() {
            return media;
        }

        @Override
        public void externalStateChange() {
            // Nothing
        }

        public void connectCartridge(Cartridge cartridge) {
            cartridge.connectSaveStateSocket(this);
        }

        @Override
        public void saveStateFile() {
            if (!powerOn || media == null) return;
            ConsoleState state = pauseAndSaveState();
            if (media.saveStateFile(state))
                showOSD("State file saved", true);
            else
                showOSD("State file save failed", true);
        }

        void saveState(int slot) {
            if (!powerOn || media == null) return;
            ConsoleState state = pauseAndSaveState();
            if (media.saveState(slot, state))
                showOSD("State " + slot + " saved", true);
            else
                showOSD("State " + slot + " save failed", true);
        }

        void loadState(int slot) {
            if (media == null) return;
            ConsoleState state = media.loadState(slot);
            if (state == null) {
                showOSD("State " + slot + " load failed", true);
                return;
            }
            pauseAndLoadState(state);
            showOSD("State " + slot + " loaded", true);
        }
    }

}
