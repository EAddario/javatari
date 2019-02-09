// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.pc.cartridge;

import org.javatari.parameters.Parameters;

import javax.swing.*;

public final class URLROMChooser {

    private static String lastURLChosen;

    public static String chooseURLToLoad() {
        if (lastURLChosen == null) lastURLChosen = Parameters.LAST_ROM_LOAD_URL_CHOSEN;
        String opt = JOptionPane.showInputDialog(
                "Load Cartridge from URL:                                                  ",
                lastURLChosen
        );
        if (opt == null || opt.trim().isEmpty()) return null;
        lastURLChosen = opt.trim();
        Parameters.LAST_ROM_LOAD_URL_CHOSEN = lastURLChosen;
        Parameters.savePreferences();
        return opt;
    }

}
