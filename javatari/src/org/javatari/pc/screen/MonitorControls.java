// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.pc.screen;

import org.javatari.pc.screen.Monitor.Control;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class MonitorControls implements KeyListener {

    public static final int KEY_VIDEO_STAND = KeyEvent.VK_V;
    private static final int KEY_UP = KeyEvent.VK_UP;
    private static final int KEY_DOWN = KeyEvent.VK_DOWN;
    private static final int KEY_LEFT = KeyEvent.VK_LEFT;
    private static final int KEY_RIGHT = KeyEvent.VK_RIGHT;
    private static final int KEY_SIZE_DEFAULT = KeyEvent.VK_BACK_SPACE;
    private static final int KEY_CART_FILE = KeyEvent.VK_F5;
    private static final int KEY_CART_URL = KeyEvent.VK_F6;
    private static final int KEY_CART_PASTE_V = KeyEvent.VK_V;
    private static final int KEY_CART_PASTE_INS = KeyEvent.VK_INSERT;
    private static final int KEY_CART_EMPTY = KeyEvent.VK_F7;
    private static final int KEY_CART_SAVESTATE = KeyEvent.VK_F8;
    private static final int KEY_CRT_FILTER = KeyEvent.VK_T;
    private static final int KEY_CRT_MODES = KeyEvent.VK_R;
    private static final int KEY_DEBUG = KeyEvent.VK_D;
    private static final int KEY_STATS = KeyEvent.VK_G;
    private final Map<Integer, Control> keyCodeMap = new HashMap<>();
    private final Map<Integer, Control> keyShiftCodeMap = new HashMap<>();
    private final Map<Integer, Control> keyAltCodeMap = new HashMap<>();
    private final Map<Integer, Control> keyShiftControlCodeMap = new HashMap<>();
    private final Map<Integer, Control> keyShiftAltCodeMap = new HashMap<>();
    private final Map<Integer, Control> keyControlCodeMap = new HashMap<>();
    private final Map<Integer, Control> keyControlAltCodeMap = new HashMap<>();
    private final Monitor monitor;

    MonitorControls(Monitor monitor) {
        this.monitor = monitor;
        init();
    }

    void addInputComponents(List<Component> inputs) {
        for (Component component : inputs)
            component.addKeyListener(this);
    }

    private void init() {
        keyCodeMap.put(KEY_CART_FILE, Control.LOAD_CARTRIDGE_FILE);
        keyCodeMap.put(KEY_CART_URL, Control.LOAD_CARTRIDGE_URL);
        keyCodeMap.put(KEY_CART_EMPTY, Control.LOAD_CARTRIDGE_EMPTY);
        keyCodeMap.put(KEY_CART_SAVESTATE, Control.SAVE_STATE_CARTRIDGE);

        keyAltCodeMap.put(KEY_CRT_FILTER, Control.CRT_FILTER);
        keyAltCodeMap.put(KEY_DEBUG, Control.DEBUG);
        keyAltCodeMap.put(KEY_STATS, Control.STATS);
        keyAltCodeMap.put(KEY_CRT_MODES, Control.CRT_MODES);
        keyAltCodeMap.put(KEY_CART_FILE, Control.LOAD_CARTRIDGE_FILE_NO_AUTO_POWER);
        keyAltCodeMap.put(KEY_CART_URL, Control.LOAD_CARTRIDGE_URL_NO_AUTO_POWER);

        keyShiftCodeMap.put(KEY_UP, Control.SIZE_MINUS);
        keyShiftCodeMap.put(KEY_DOWN, Control.SIZE_PLUS);
        keyShiftCodeMap.put(KEY_LEFT, Control.SIZE_MINUS);
        keyShiftCodeMap.put(KEY_RIGHT, Control.SIZE_PLUS);

        keyShiftAltCodeMap.put(KEY_UP, Control.SCALE_Y_MINUS);
        keyShiftAltCodeMap.put(KEY_DOWN, Control.SCALE_Y_PLUS);
        keyShiftAltCodeMap.put(KEY_LEFT, Control.SCALE_X_MINUS);
        keyShiftAltCodeMap.put(KEY_RIGHT, Control.SCALE_X_PLUS);

        keyControlAltCodeMap.put(KEY_UP, Control.ORIGIN_Y_MINUS);
        keyControlAltCodeMap.put(KEY_DOWN, Control.ORIGIN_Y_PLUS);
        keyControlAltCodeMap.put(KEY_LEFT, Control.ORIGIN_X_MINUS);
        keyControlAltCodeMap.put(KEY_RIGHT, Control.ORIGIN_X_PLUS);

        keyShiftControlCodeMap.put(KEY_UP, Control.HEIGHT_MINUS);
        keyShiftControlCodeMap.put(KEY_DOWN, Control.HEIGHT_PLUS);
        keyShiftControlCodeMap.put(KEY_LEFT, Control.WIDTH_MINUS);
        keyShiftControlCodeMap.put(KEY_RIGHT, Control.WIDTH_PLUS);

        keyShiftCodeMap.put(KEY_CART_PASTE_INS, Control.LOAD_CARTRIDGE_PASTE);
        keyControlCodeMap.put(KEY_CART_PASTE_V, Control.LOAD_CARTRIDGE_PASTE);

        keyCodeMap.put(KEY_SIZE_DEFAULT, Control.SIZE_DEFAULT);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        Control control = controlForEvent(e);
        if (control == null) return;
        monitor.controlActivated(control);
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Do nothing
    }

    @Override
    public void keyReleased(KeyEvent e) {
        // Do nothing
    }

    private Control controlForEvent(KeyEvent e) {
        switch (e.getModifiersEx()) {
            case 0:
                return keyCodeMap.get(e.getKeyCode());
            case KeyEvent.ALT_DOWN_MASK:
                return keyAltCodeMap.get(e.getKeyCode());
            case KeyEvent.SHIFT_DOWN_MASK:
                return keyShiftCodeMap.get(e.getKeyCode());
            case KeyEvent.CTRL_DOWN_MASK:
                return keyControlCodeMap.get(e.getKeyCode());
            case KeyEvent.CTRL_DOWN_MASK | KeyEvent.ALT_DOWN_MASK:
                return keyControlAltCodeMap.get(e.getKeyCode());
            case KeyEvent.SHIFT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK:
                return keyShiftControlCodeMap.get(e.getKeyCode());
            case KeyEvent.SHIFT_DOWN_MASK | KeyEvent.ALT_DOWN_MASK:
                return keyShiftAltCodeMap.get(e.getKeyCode());
        }
        return null;
    }

}
