// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.pc.screen;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.CartridgeSocket;
import org.javatari.atari.console.savestate.SaveStateSocket;
import org.javatari.atari.controls.ConsoleControlsSocket;
import org.javatari.general.av.video.VideoSignal;
import org.javatari.parameters.Parameters;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class PanelScreen extends JPanel implements Screen {

    private static final boolean CONSOLE_PANEL = Parameters.SCREEN_CONSOLE_PANEL;
    private static final long serialVersionUID = 1L;
    private MonitorPanel monitorPanel;
    private ConsolePanel consolePanel;

    public PanelScreen(boolean screenFixedSize) {
        super();
        monitorPanel = new MonitorPanel();
        monitorPanel.monitor().setFixedSize(screenFixedSize);
        if (CONSOLE_PANEL) consolePanel = new ConsolePanel(monitorPanel.monitor(), null);
        monitorPanel.monitor().addControlInputComponents(keyControlsInputComponents());
        setup();
    }

    @Override
    public void connect(VideoSignal videoSignal, ConsoleControlsSocket controlsSocket, CartridgeSocket cartridgeSocket, SaveStateSocket savestateSocket) {
        monitorPanel.connect(videoSignal, controlsSocket, cartridgeSocket, savestateSocket);
        if (consolePanel != null) consolePanel.connect(controlsSocket, cartridgeSocket);
    }

    @Override
    public void powerOn() {
        setVisible(true);
        monitorPanel.powerOn();
        monitorPanel.requestFocus();
    }

    @Override
    public void powerOff() {
        monitorPanel.powerOff();
    }

    @Override
    public void destroy() {
        close();
        monitorPanel.destroy();
    }

    @Override
    public void close() {
        setVisible(false);
    }

    @Override
    public Monitor monitor() {
        return monitorPanel.monitor();
    }

    @Override
    public List<Component> keyControlsInputComponents() {
        List<Component> comps = new ArrayList<>(Arrays.asList((Component) this));
        comps.addAll(monitorPanel.keyControlsInputComponents());
        if (consolePanel != null) comps.add(consolePanel);
        return comps;
    }

    private void setup() {
        setTransferHandler(new ROMDropTransferHandler());
        monitorPanel.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
                if (e.getComponent() == monitorPanel) validate();
            }
        });
        setLayout(new FlowLayout(
                FlowLayout.CENTER,
                0, 0
        ));
        setOpaque(false);
        add(monitorPanel);
        if (consolePanel != null) add(consolePanel);
        validate();
    }

    // To handle drag and drop of ROM files and links
    private class ROMDropTransferHandler extends TransferHandler {
        private static final long serialVersionUID = 1L;

        @Override
        public boolean canImport(TransferSupport support) {
            if (!monitorPanel.monitor().isCartridgeChangeEnabled()) return false;
            Transferable transf = support.getTransferable();
            if (!ROMTransferHandlerUtil.canAccept(transf)) return false;
            if (support.isDrop() && support.getUserDropAction() != LINK) support.setDropAction(COPY);
            return true;
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) return false;
            monitorPanel.monitor().showOSD("LOADING CARTRIDGE...", true);
            Cartridge cart = ROMTransferHandlerUtil.importCartridgeData(support.getTransferable());
            monitorPanel.monitor().showOSD(null, true);
            if (cart == null) return false;
            // LINK Action means load Cartridge without auto power! :-)
            boolean autoPower = !support.isDrop() || support.getDropAction() != LINK;
            monitorPanel.monitor().cartridgeInsert(cart, autoPower);
            return true;
        }
    }

}
