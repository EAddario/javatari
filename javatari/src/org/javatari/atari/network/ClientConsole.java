// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.atari.network;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.console.Console;
import org.javatari.atari.console.savestate.ConsoleState;
import org.javatari.atari.console.savestate.SaveStateMedia;
import org.javatari.atari.controls.ConsoleControls.Control;
import org.javatari.atari.controls.ConsoleControlsSocket;
import org.javatari.general.board.ClockDriven;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public final class ClientConsole extends Console implements ClockDriven {

    private static final List<Control> DISABLED_CONTROLS = Collections.singletonList(Control.CARTRIDGE_FORMAT);
    private RemoteReceiver remoteReceiver;
    private boolean isPaused = false;        // To control the pause/go mechanism

    public ClientConsole(RemoteReceiver receiver) {
        super();
        setupReceiver(receiver);
    }

    public RemoteReceiver remoteReceiver() {
        return remoteReceiver;
    }

    @Override
    public synchronized void go() {
        // Clock is controlled remotely, but signal unpause to unblock clock pulses
        isPaused = false;
    }

    @Override
    public synchronized void pause() {
        // Clock is controlled remotely, but signal pause to block clock pulses
        isPaused = true;
    }

    @Override
    public void extendedPowerOff() {
        try {
            remoteReceiver.disconnect();
        } catch (IOException e) {
            // Ignore
        }
        super.extendedPowerOff();
    }

    @Override
    protected void mainClockCreate() {
        // Ignore, the clock is controlled remotely
    }

    @Override
    protected void mainClockAdjustToNormal() {
        // Ignore, the clock is controlled remotely
    }

    @Override
    protected void mainClockAdjustToAlternate() {
        // Ignore, the clock is controlled remotely
    }

    @Override
    protected void mainClockDestroy() {
        // Ignore, the clock is controlled remotely
    }

    @Override
    protected void socketsCreate() {
        controlsSocket = new ClientConsoleControlsSocketAdapter();
        controlsSocket.addForwardedInput(new ConsoleControlsInputAdapter());
        controlsSocket.addForwardedInput(tia);
        controlsSocket.addForwardedInput(pia);
        cartridgeSocket = new ClientConsoleCartridgeSocketAdapter();
        saveStateSocket = new ClientConsoleSaveStateSocketAdapter();
    }

    @Override
    public void clockPulse() {
        // Block clock pulses until Console is unpaused (go) is turned off
        // Important while Screen is dettached in Applet mode,
        // so no indeterministic behavior is introduced while the Screen is off!
        while (isPaused && powerOn) try {
            Thread.sleep(1000 / 120);        // Half a frame
        } catch (InterruptedException ignored) {
        }

        synchronized (this) {
            tia.clockPulse();
        }
    }

    @Override
    protected void cycleCartridgeFormat() {
        // Ignore, the new Cartridge state will come from the Server
    }

    @Override
    protected void powerFry() {
        // Ignore, the new RAM state will come from the Server
    }

    void connected() {
        showOSD("Connected to Player 1 Server", true);
    }

    void disconnected() {
        powerOff();
        showOSD("Disconnected from Player 1 Server", true);
    }

    void receiveServerUpdate(ServerUpdate update) {
        if (update.powerChange != null)
            receiveServerPower(update.powerChange);
        if (update.consoleState != null)
            receiveServerState(update.consoleState);
        if (update.controlChanges != null)
            ((ClientConsoleControlsSocketAdapter) controlsSocket).serverControlChanges(update.controlChanges);
        if (powerOn)
            clockPulse();
    }

    List<ControlChange> controlChangesToSend() {
        return ((ClientConsoleControlsSocketAdapter) controlsSocket).getChangesToSend();
    }

    private void receiveServerPower(boolean serverPowerOn) {
        if (serverPowerOn && !powerOn) powerOn();
        else if (!serverPowerOn && powerOn) powerOff();
    }

    private void receiveServerState(ConsoleState state) {
        loadState(state);
    }

    private void setupReceiver(RemoteReceiver receiver) {
        remoteReceiver = receiver;
        remoteReceiver.clientConsole(this);
    }

    private class ClientConsoleControlsSocketAdapter extends ConsoleControlsSocket {
        private final List<ControlChange> queuedChanges = new ArrayList<>();

        @Override
        public void controlStateChanged(Control control, boolean state) {
            if (DISABLED_CONTROLS.contains(control)) {
                showOSD("This function is only available on the Server", true);
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

        List<ControlChange> getChangesToSend() {
            List<ControlChange> changesToSend;
            synchronized (queuedChanges) {
                if (queuedChanges.isEmpty())
                    return null;
                else {
                    changesToSend = new ArrayList<>(queuedChanges);
                    queuedChanges.clear();
                    return changesToSend;
                }
            }
        }

        void serverControlChanges(List<ControlChange> changes) {
            // Effectively accepts the control changes, received from the server
            for (ControlChange change : changes)
                if (change instanceof ControlChangeForPaddle)
                    super.controlStateChanged(change.control, ((ControlChangeForPaddle) change).position);
                else
                    super.controlStateChanged(change.control, change.state);
        }
    }

    // Cartridge insertion is controlled only by the Server
    private class ClientConsoleCartridgeSocketAdapter extends CartridgeSocketAdapter {
        @Override
        public void insert(Cartridge cartridge, boolean autoPower) {
            showOSD("Only the Server can change Cartridges", true);
        }
    }

    private class ClientConsoleSaveStateSocketAdapter extends SaveStateSocketAdapter {
        @Override
        public void connectMedia(SaveStateMedia media) {
            // Ignore, savestates are controlled by the Server
        }

        @Override
        public void connectCartridge(Cartridge cartridge) {
            // No socket, savestates are controlled by the Server
            cartridge.connectSaveStateSocket(null);
        }
    }

}
