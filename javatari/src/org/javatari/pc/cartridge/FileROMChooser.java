// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.pc.cartridge;

import org.javatari.parameters.Parameters;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.security.AccessControlException;

/**
 * This class must ONLY be used from Swing Event Dispatcher Thread
 */
public final class FileROMChooser {

    private static JFileChooser chooser;
    private static File lastLoadFileChosen;
    private static File lastSaveFileChosen;

    public static File chooseFileToLoad() throws AccessControlException {
        if (chooser != null && chooser.isShowing()) return null;
        if (lastLoadFileChosen == null) lastLoadFileChosen = new File(Parameters.LAST_ROM_LOAD_FILE_CHOSEN);
        if (chooser == null) createChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(ROMLoader.VALID_LOAD_FILES_DESC, ROMLoader.VALID_LOAD_FILE_EXTENSIONS));
        chooser.setSelectedFile(lastLoadFileChosen);
        int res = chooser.showOpenDialog(null);
        if (res != 0) return null;
        lastLoadFileChosen = chooser.getSelectedFile();
        Parameters.LAST_ROM_LOAD_FILE_CHOSEN = lastLoadFileChosen.toString();
        Parameters.savePreferences();
        return lastLoadFileChosen;
    }

    public static File chooseFileToSavestate() throws AccessControlException {
        if (chooser != null && chooser.isShowing()) return null;
        if (lastSaveFileChosen == null) lastSaveFileChosen = new File(Parameters.LAST_ROM_SAVE_FILE_CHOSEN);
        if (chooser == null) createChooser();
        chooser.setFileFilter(new FileNameExtensionFilter(ROMLoader.VALID_STATE_FILE_DESC, ROMLoader.VALID_STATE_FILE_EXTENSION));
        chooser.setSelectedFile(lastSaveFileChosen);
        int res = chooser.showSaveDialog(null);
        if (res != 0) return null;
        lastSaveFileChosen = chooser.getSelectedFile();
        if (!lastSaveFileChosen.toString().toUpperCase().endsWith(ROMLoader.VALID_STATE_FILE_EXTENSION.toUpperCase()))
            lastSaveFileChosen = new File(lastSaveFileChosen + "." + ROMLoader.VALID_STATE_FILE_EXTENSION);
        Parameters.LAST_ROM_SAVE_FILE_CHOSEN = lastSaveFileChosen.toString();
        Parameters.savePreferences();
        return lastSaveFileChosen;
    }

    private static synchronized void createChooser() throws AccessControlException {
        if (chooser != null) return;
        chooser = new JFileChooser();
        chooser.setPreferredSize(new Dimension(580, 400));
    }

}
