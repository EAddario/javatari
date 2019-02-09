package org.javatari.utils;

import javax.swing.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public abstract class KeyFilteredRepeatsAdapter implements KeyListener {

    private KeyEvent pendingRelease = null;
    private final Runnable sender = () -> {
        if (pendingRelease != null) {
            filteredKeyReleased(pendingRelease);
            pendingRelease = null;
        }
    };
    private final Runnable trigger = () -> {
        if (pendingRelease != null) SwingHelper.edtInvokeLater(sender);
    };

    public abstract void filteredKeyPressed(KeyEvent e);

    public abstract void filteredKeyReleased(KeyEvent e);

    @Override
    public final void keyTyped(KeyEvent e) {
        // Nothing
    }

    @Override
    public final void keyPressed(KeyEvent e) {
        if (pendingRelease != null && pendingRelease.getKeyCode() == e.getKeyCode() && pendingRelease.getModifiersEx() == e.getModifiersEx())
            pendingRelease = null;
        filteredKeyPressed(e);
    }

    @Override
    public final void keyReleased(KeyEvent e) {
        if (pendingRelease != null) filteredKeyReleased(pendingRelease);
        pendingRelease = e;
        SwingUtilities.invokeLater(trigger);
    }

}
