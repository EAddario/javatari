// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.utils;

import org.javatari.parameters.Parameters;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;
import java.util.Locale;


public class Environment {

    public static boolean NIMBUS_LAF = false;
    public static boolean ARIAL_FONT = false;
    public static boolean LIBERATION_FONT = false;
    public static Font cartridgeLabelFont = null;

    public static void init() {
        System.out.println(Parameters.TITLE + " " + Parameters.VERSION + " on " + vmInfo());
        try {
            // Set Locale
            try {
                Locale.setDefault(Locale.ENGLISH);
            } catch (Exception ignored) {
            }

            SwingHelper.edtSmartInvokeAndWait(() -> {
                // Set Look and Feel
                try {
                    UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");    // 215, 220, 221
                    NIMBUS_LAF = true;
                } catch (Exception ignored) {
                }

                ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);

                // Grab info about installed fonts
                try {
                    String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
                    for (String font1 : fonts) {
                        String font = font1.toUpperCase();
                        if (font.equals("ARIAL")) ARIAL_FONT = true;
                        if (font.equals("LIBERATION SANS")) LIBERATION_FONT = true;
                    }
                } catch (Exception ignored) {
                }

                // Create font used to render Cartridge Labels
                try {
                    InputStream fontStream = Environment.class.getClassLoader()
                            .getResourceAsStream("org/javatari/pc/screen/images/LiberationSans-Bold.ttf");
                    if (fontStream != null) {
                        try {
                            cartridgeLabelFont = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(Font.BOLD, 15f);
                        } finally {
                            fontStream.close();
                        }
                    }
                } catch (Exception ignored) {
                }
            });
        } catch (Exception ignored) {
        }
    }

    public static String vmInfo() {
        try {
            return System.getProperty("java.vm.name") + " ver: " + System.getProperty("java.version") + " (" + System.getProperty("os.arch") + ")";
        } catch (Throwable e) {
            return "VM info unavailable";
        }
    }

}
