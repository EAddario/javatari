// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.utils.slickframe;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;

public class SlickFrame extends JFrame {

    public static final long serialVersionUID = 1L;
    private static final int RESIZE_CORNER_SIZE = 18;
    private final boolean resizable;
    private final HotspotPanel contentHotspotPanel;
    private Point clickPosition = null;
    private Point startingLocation = null;
    private Dimension startingSize = null;
    private Dimension minimumSize = new Dimension(80, 40);

    public SlickFrame() throws HeadlessException {
        this(true);
    }

    public SlickFrame(boolean resizable) throws HeadlessException {
        super();
        setUndecorated(true);
        setResizable(false);
        this.resizable = resizable;
        contentHotspotPanel = new HotspotPanel();
        setContentPane(contentHotspotPanel);
        init();
        setupMoveAndResizeListener();
    }

    protected void minimunResize(Dimension minSize) {
        this.minimumSize = minSize;
    }

    protected MousePressAndMotionListener detachMouseListener() {
        return contentHotspotPanel.detachMouseListener();
    }

    protected HotspotPanel getContentHotspotPanel() {
        return contentHotspotPanel;
    }

    protected void init() {
    }

    protected void movingTo(int x, int y) {
        setLocation(x, y);
    }

    private void resizingTo(int width, int height) {
        setSize(width, height);
    }

    protected void finishedMoving() {
    }

    private void finishedResizing() {
        // Nothing
    }

    private void setupMoveAndResizeListener() {
        MousePressAndMotionListener moveAndResizeListener = buildMouseListener();
        addMouseListener(moveAndResizeListener);
        addMouseMotionListener(moveAndResizeListener);
        contentHotspotPanel.setForwardListener(moveAndResizeListener);
    }

    private MousePressAndMotionListener buildMouseListener() {
        return new MousePressAndMotionAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() != 1) return;
                clickPosition = e.getLocationOnScreen();
                if (!resizable) {
                    startingLocation = getLocation();
                    return;
                }
                Rectangle resizeArea = new Rectangle(getWidth() - RESIZE_CORNER_SIZE, getHeight() - RESIZE_CORNER_SIZE, getWidth(), getHeight());
                if (resizeArea.contains(e.getPoint()))
                    startingSize = getSize();
                else
                    startingLocation = getLocation();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() != 1) return;
                clickPosition = null;
                if (startingLocation != null)
                    finishedMoving();
                else if (startingSize != null)
                    finishedResizing();
                startingLocation = null;
                startingSize = null;
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (clickPosition == null) return;
                Point p = e.getLocationOnScreen();
                if (startingLocation != null)
                    movingTo(startingLocation.x + p.x - clickPosition.x, startingLocation.y + p.y - clickPosition.y);
                else
                    resizingTo(
                            Math.max(startingSize.width + p.x - clickPosition.x, minimumSize.width),
                            Math.max(startingSize.height + p.y - clickPosition.y, minimumSize.height
                            ));
            }
        };
    }

    public void paintHotspots(Graphics g) {
        contentHotspotPanel.paintHotspots(g);
    }


}
