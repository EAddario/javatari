// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.network;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.formats.CartridgeSavestate;
import org.javatari.atari.console.Console;
import org.javatari.atari.console.savestate.ConsoleState;
import org.javatari.atari.controls.ConsoleControls.Control;
import org.javatari.atari.controls.ConsoleControlsSocket;
import org.javatari.general.av.video.VideoStandard;
import org.javatari.general.board.Clock;
import org.javatari.general.board.ClockDriven;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public final class ServerConsole extends Console implements ClockDriven {

    private RemoteTransmitter remoteTransmitter;
    private boolean stateUpdateRequested = false;

    public ServerConsole(RemoteTransmitter transmitter) {
        super();
        setupTransmitter(transmitter);
    }

    public RemoteTransmitter remoteTransmitter() {
        return remoteTransmitter;
    }

    @Override
    public void powerOff() {
        // The server clock is always running
        super.powerOff();
        go();
    }

    @Override
    public void extendedPowerOff() {
        super.powerOff();
        try {
            remoteTransmitter.stop();
        } catch (IOException e) {
            // Ignore
        }
        super.extendedPowerOff();
    }

    @Override
    protected void mainClockCreate() {
        // The server clock is always running
        mainClock = new Clock("Server Console", this, VideoStandard.NTSC.fps);
        mainClock.go();
    }

    @Override
    protected void socketsCreate() {
        controlsSocket = new ServerConsoleControlsSocketAdapter();
        controlsSocket.addForwardedInput(new ConsoleControlsInputAdapter());
        controlsSocket.addForwardedInput(tia);
        controlsSocket.addForwardedInput(pia);
        cartridgeSocket = new ServerConsoleCartridgeSocketAdapter();
        saveStateSocket = new ServerConsoleSaveStateSocketAdapter();
    }

    @Override
    protected synchronized void cartridge(Cartridge cartridge) {
        super.cartridge(cartridge);
        stateUpdateRequested = true;
    }

    @Override
    protected void loadState(ConsoleState state) {
        super.loadState(state);
        stateUpdateRequested = true;
    }

    @Override
    public synchronized void clockPulse() {
        Boolean powerChange = null;
        ConsoleState state = null;
        if (stateUpdateRequested) {
            powerChange = powerOn;
            state = saveState();
            stateUpdateRequested = false;
        }
        List<ControlChange> controlChanges = ((ServerConsoleControlsSocketAdapter) controlsSocket).commitAndGetChangesToSend();
        if (powerOn) tia.clockPulse();
        if (remoteTransmitter != null && remoteTransmitter.isClientConnected()) {
            ServerUpdate update = new ServerUpdate();
            update.powerChange = powerChange;
            update.consoleState = state;
            update.controlChanges = controlChanges;
            remoteTransmitter.sendUpdate(update);
        }
    }

    @Override
    protected synchronized void powerFry() {
        super.powerFry();
        stateUpdateRequested = true;
    }

    synchronized void clientConnected() {
        showOSD("Player 2 Client Connected", true);
        stateUpdateRequested = true;
    }

    void clientDisconnected() {
        showOSD("Player 2 Client Disconnected", true);
    }

    void receiveClientControlChanges(List<ControlChange> clientControlChages) {
        for (ControlChange change : clientControlChages)
            if (change instanceof ControlChangeForPaddle)
                controlsSocket.controlStateChanged(change.control, ((ControlChangeForPaddle) change).position);
            else
                controlsSocket.controlStateChanged(change.control, change.state);
    }

    private void setupTransmitter(RemoteTransmitter transmitter) {
        remoteTransmitter = transmitter;
        remoteTransmitter.serverConsole(this);
    }

    private static class ServerConsoleControlsSocketAdapter extends ConsoleControlsSocket {
        private final List<ControlChange> queuedChanges = new ArrayList<>();

        @Override
        public void controlStateChanged(Control control, boolean state) {
            // Send some controls directly and locally only
            if (control == Control.FAST_SPEED || control == Control.POWER_FRY || control.isStateControl()) {
                super.controlStateChanged(control, state);
                return;
            }
            synchronized (queuedChanges) {
                queuedChanges.add(new ControlChange(control, state));
            }
        }

        @Override
        public void controlStateChanged(Control control, int position) {
            synchronized (queuedChanges) {
                queuedChanges.add(new ControlChangeForPaddle(control, position));
            }
        }

        private List<ControlChange> commitAndGetChangesToSend() {
            List<ControlChange> changesToSend;
            synchronized (queuedChanges) {
                if (queuedChanges.isEmpty())
                    return null;
                else {
                    changesToSend = new ArrayList<>(queuedChanges);
                    queuedChanges.clear();
                }
            }
            // Effectively process the control changes
            for (ControlChange change : changesToSend)
                if (change instanceof ControlChangeForPaddle)
                    super.controlStateChanged(change.control, ((ControlChangeForPaddle) change).position);
                else
                    super.controlStateChanged(change.control, change.state);
            return changesToSend;
        }
    }

    private class ServerConsoleCartridgeSocketAdapter extends CartridgeSocketAdapter {
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
            // Send powerOn as a user event so the Console don't get turned on before the state update is sent
            if (autoPower && !powerOn) controlsSocket.controlStateChanged(Control.POWER, true);
        }
    }

    private class ServerConsoleSaveStateSocketAdapter extends SaveStateSocketAdapter {
        @Override
        public void externalStateChange() {
            // Make sure any state changed is reflected on the Client
            stateUpdateRequested = true;
        }
    }

}
