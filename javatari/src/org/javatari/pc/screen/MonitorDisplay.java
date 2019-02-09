// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.pc.screen;

import java.awt.*;

public interface MonitorDisplay {

    void displayCenter();

    void displaySize(Dimension size);

    void displayMinimumSize(Dimension size);

    void displayFinishFrame(Graphics2D graphics);

    void displayClear();

    Dimension displayEffectiveSize();

    Graphics2D displayGraphics();

    Container displayContainer();

    float displayDefaultOpenningScaleX(int displayWidth, int displayHeight);

    void displayRequestFocus();

    void displayLeaveFullscreen();

}
