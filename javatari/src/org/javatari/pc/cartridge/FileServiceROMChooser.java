// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.pc.cartridge;

import javax.jnlp.FileContents;
import javax.jnlp.FileOpenService;
import javax.jnlp.ServiceManager;


public final class FileServiceROMChooser {

    public static FileContents chooseFileToLoad() {
        try {
            FileOpenService fos = (FileOpenService) ServiceManager.lookup("javax.jnlp.FileOpenService");
            return fos.openFileDialog(null, ROMLoader.VALID_LOAD_FILE_EXTENSIONS);
        } catch (Exception ex) {
            System.out.println("File Service Cartridge Chooser: unable to open dialog\n" + ex);
            return null;
        }
    }

}
