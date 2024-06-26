// Copyright 2011-2012 Paulo Augusto Peccin. See licence.txt distributed with this file.

package org.javatari.pc.screen;

import org.javatari.atari.cartridge.Cartridge;
import org.javatari.atari.cartridge.CartridgeInsertionListener;
import org.javatari.atari.cartridge.CartridgeSocket;
import org.javatari.atari.console.savestate.SaveStateSocket;
import org.javatari.general.av.video.VideoMonitor;
import org.javatari.general.av.video.VideoSignal;
import org.javatari.general.av.video.VideoStandard;
import org.javatari.general.board.Clock;
import org.javatari.general.board.ClockDriven;
import org.javatari.parameters.Parameters;
import org.javatari.pc.cartridge.FileROMChooser;
import org.javatari.pc.cartridge.FileServiceROMChooser;
import org.javatari.pc.cartridge.ROMLoader;
import org.javatari.pc.cartridge.URLROMChooser;
import org.javatari.utils.Environment;
import org.javatari.utils.SwingHelper;

import javax.jnlp.FileContents;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.AccessControlException;
import java.util.Arrays;
import java.util.List;

public final class Monitor implements ClockDriven, VideoMonitor, CartridgeInsertionListener {

    public static final long serialVersionUID = 0L;
    static final int BUFFER_VSYNC = Parameters.SCREEN_BUFFER_VSYNC;
    static final int MULTI_BUFFERING = Parameters.SCREEN_MULTI_BUFFERING;
    static final boolean PAGE_FLIPPING = Parameters.SCREEN_PAGE_FLIPPING;
    static final float DEFAULT_SCALE_X = Parameters.SCREEN_DEFAULT_SCALE_X;
    static final float DEFAULT_SCALE_ASPECT_X = Parameters.SCREEN_DEFAULT_SCALE_ASPECT_X;
    private static final double DEFAULT_FPS = Parameters.SCREEN_DEFAULT_FPS;
    private static final String[] crtModeNames = {"OFF", "Phosphor", "Phosphor Scanlines", "RGB", "RGB Phosphor"};
    private static final int EXTRA_UPPER_VSYNC_TOLERANCE = 5;
    private static final int VSYNC_TOLERANCE = Parameters.SCREEN_VSYNC_TOLERANCE;
    private static final boolean BUFFER_SYNC_WAIT = Parameters.SCREEN_BUFFER_SYNC_WAIT;
    private static final int DEFAULT_ORIGIN_X = Parameters.SCREEN_DEFAULT_ORIGIN_X;
    private static final double DEFAULT_ORIGIN_Y_PCT = Parameters.SCREEN_DEFAULT_ORIGIN_Y_PCT;        // Percentage of height
    private static final int DEFAULT_WIDTH = Parameters.SCREEN_DEFAULT_WIDTH;
    private static final double DEFAULT_HEIGHT_PCT = Parameters.SCREEN_DEFAULT_HEIGHT_PCT;            // Percentage of height
    private static final float DEFAULT_SCALE_Y = Parameters.SCREEN_DEFAULT_SCALE_Y;
    private static final int OSD_FRAMES = Parameters.SCREEN_OSD_FRAMES;
    private static final boolean CRT_FILTER = Parameters.SCREEN_CRT_FILTER;
    private static final int CRT_MODE = Parameters.SCREEN_CRT_MODE;

    private static final float CRT_RETENTION_ALPHA = Parameters.SCREEN_CRT_RETENTION_ALPHA;
    private static final float SCANLINES_STRENGTH = Parameters.SCREEN_SCANLINES_STRENGTH;
    private static final float FRAME_ACCELERATION = Parameters.SCREEN_FRAME_ACCELERATION;
    private static final float IMTERM_FRAME_ACCELERATION = Parameters.SCREEN_INTERM_FRAME_ACCELERATION;
    private static final float SCANLINES_ACCELERATION = Parameters.SCREEN_SCANLINES_ACCELERATION;
    private static final boolean CARTRIDGE_CHANGE = Parameters.SCREEN_CARTRIDGE_CHANGE;
    private static final boolean FIXED_SIZE = Parameters.SCREEN_FIXED_SIZE;
    final String refreshMonitor = "refreshMonitor";        // Used only for synchronization
    private final double fps;
    public Clock clock;
    private MonitorControls monitorControls;
    private boolean fixedSizeMode = FIXED_SIZE;
    private boolean cartridgeChangeEnabled = CARTRIDGE_CHANGE;
    private VideoSignal videoSignal;
    private CartridgeSocket cartridgeSocket;
    private SaveStateSocket savestateSocket;
    private VideoStandard signalStandard;
    private int signalWidth;
    private int signalHeight;
    private VideoStandard videoStandardDetected;
    private int videoStandardDetectionFrameCount;
    private int videoStandardDetectionFrameLineCount = 0;
    private int videoStandardDetectionAdtLinesPerFrame = 0;
    private int[] backBuffer;
    private int[] frontBuffer;
    private int displayWidth;
    private int displayHeight;
    private double displayHeightPct;
    private int displayOriginX;
    private int displayOriginY;
    private double displayOriginYPct;
    private float displayScaleX;
    private float displayScaleY;
    private boolean powerOn = false;
    private boolean signalOn = false;
    private SignalOffRefresher signalOffRefresher;
    private int osdFramesLeft = -1;
    private String osdMessage;
    private JLabel osdComponent;
    private boolean crtFilter = CRT_FILTER;
    private int crtMode = Math.max(CRT_MODE, 0);
    private int debug = 0;
    private boolean showStats = false;
    private int line = 0;
    private int frame = 0;
    private int lastFrameRendered = -1;
    private MonitorDisplay display;
    private BufferedImage frameImage;
    private BufferedImage scanlinesTextureImage;
    private CRTTriadComposite crtTriadComposite;
    private BufferedImage intermFrameImage;
    private Image logoIcon;
    private final Runnable refresher = this::refresh;

    public Monitor() {
        super();
        this.fps = DEFAULT_FPS;
        init();
    }

    public void connect(VideoSignal videoSignal, CartridgeSocket cartridgeSocket, SaveStateSocket savestateSocket) {
        this.cartridgeSocket = cartridgeSocket;
        this.savestateSocket = savestateSocket;
        cartridgeSocket.addInsertionListener(this);
        this.videoSignal = videoSignal;
        videoSignal.connectMonitor(this);
        adjustToVideoSignal();
    }

    synchronized void setDisplay(MonitorDisplay monitorDisplay) {
        display = monitorDisplay;
        float scX = display.displayDefaultOpenningScaleX(displayWidth, displayHeight);
        setDisplayScale(scX, scX / DEFAULT_SCALE_ASPECT_X);
        displayCenter();
    }

    boolean isFixedSize() {
        return fixedSizeMode;
    }

    void setFixedSize(boolean fixed) {
        fixedSizeMode = fixed;
    }

    public boolean isCartridgeChangeEnabled() {
        return cartridgeChangeEnabled;
    }

    public void setCartridgeChangeEnabled(boolean state) {
        cartridgeChangeEnabled = state;
    }

    void addControlInputComponents(List<Component> inputs) {
        monitorControls.addInputComponents(inputs);
    }

    public void powerOn() {
        synchronized (refreshMonitor) {
            cleanFrontBuffer();
            cleanBackBuffer();
            powerOn = true;
            signalState(false);
            clock.go();
        }
    }

    public void powerOff() {
        synchronized (refreshMonitor) {
            clock.pause();
            powerOn = false;
            signalState(false);
        }
    }

    public void destroy() {
        synchronized (refreshMonitor) {
            clock.terminate();
        }
    }

    @Override
    // Synchronize to avoid changing the standard while receiving lines
    public synchronized boolean nextLine(final int[] pixels, boolean vSynchSignal) {
        // Adjusts to the new signal state (on or off) as necessary
        if (!signalState(pixels != null))        // If signal is off, we are done
            return false;
        // Process new line received
        boolean vSynched = false;
        if (line < signalHeight) {
            // Copy only contents that will be displayed
            if (line >= displayOriginY && line < displayOriginY + displayHeight)
                System.arraycopy(pixels, displayOriginX, backBuffer, (line - displayOriginY) * displayWidth, displayWidth);
        } else
            vSynched = maxLineExceeded();
        line++;
        if (videoStandardDetected == null) videoStandardDetectionFrameLineCount++;
        if (vSynchSignal) {
            if (videoStandardDetected == null) videoStandardDetectionNewFrame();
            vSynched = newFrame() || vSynched;
        }
        return vSynched;
    }

    @Override
    public VideoStandard videoStandardDetected() {
        return videoStandardDetected;
    }

    @Override
    public void videoStandardDetectionStart() {
        videoStandardDetected = null;
        videoStandardDetectionFrameCount = 0;
        videoStandardDetectionFrameLineCount = 0;
    }

    @Override
    public int currentLine() {
        return line;
    }

    @Override
    public void synchOutput() {
        if (fps < 0) clock.interrupt();        // Just ask for a refresh if in Adaptive mode
        else synchOutputInSwing();
    }

    @Override
    public void showOSD(String message, boolean overlap) {
        if (!overlap && osdFramesLeft > 0) return;
        osdMessage = message;
        osdFramesLeft = message == null ? 0 : OSD_FRAMES;
    }

    @Override
    public void clockPulse() {
        synchOutputInSwing();
        // If in "On Demand" mode (fps < 0) then just wait for the next frame to interrupt the sleep, but no more than 2 frames
        if (fps < 0 && !Thread.interrupted()) try {
            Thread.sleep(1000 / 60 * 2, 0);
        } catch (InterruptedException e) { /* Awake! */ }
    }

    void cartridgeInsert(Cartridge cart, boolean autoPower) {
        cartridgeSocket.insert(cart, autoPower);
        display.displayRequestFocus();
    }

    @Override
    public void cartridgeInserted(Cartridge cartridge) {
        // Only change mode if not forced
        if (CRT_MODE >= 0) return;
        if (crtMode == 0 || crtMode == 1)
            setCrtMode(cartridge == null ? 0 : cartridge.getInfo().crtMode == -1 ? 0 : cartridge.getInfo().crtMode);
    }

    private void synchOutputInSwing() {
        if (BUFFER_SYNC_WAIT) SwingHelper.edtSmartInvokeAndWait(refresher);
        else SwingHelper.edtInvokeLater(refresher);
    }

    private boolean newFrame() {
        if (line < signalHeight - VSYNC_TOLERANCE) return false;

        // Flip front and back buffers
        int[] aux = frontBuffer;
        frontBuffer = backBuffer;
        backBuffer = aux;

        // Start a new frame
        if (debug > 0) cleanBackBuffer();
        if (showStats)
            showOSD(videoSignal.standard() + "  " + line + " lines,  CRT mode: " + crtModeNames[crtMode], true);
        line = 0;
        frame++;
        return true;
    }

    private boolean maxLineExceeded() {
        if (line > signalHeight + VSYNC_TOLERANCE + EXTRA_UPPER_VSYNC_TOLERANCE) {
            // if (debug > 0) System.out.println("Display maximum scanlines exceeded: " + line);
            return newFrame();
        }
        return false;
    }

    private boolean signalState(boolean state) {
        if (state) {
            signalOn = true;
            adjustToVideoSignal();
        } else {
            signalOn = false;
            adjustToVideoSignalOff();
        }
        return state;
    }

    private void cleanFrontBuffer() {
        Arrays.fill(frontBuffer, Color.BLACK.getRGB());
    }

    private void cleanBackBuffer() {
        // If in debug mode, put a nice green for detection of undrawn lines
        Arrays.fill(backBuffer, debug > 0 ? Color.GREEN.getRGB() : Color.BLACK.getRGB());
    }

    private void videoStandardDetectionNewFrame() {
        int linesCount = videoStandardDetectionFrameLineCount;
        videoStandardDetectionFrameLineCount = 0;
        // Only consider frames with linesCount in range with tolerances (NTSC 262, PAL 312)
        if ((linesCount >= 250 && linesCount <= 281)
                || (linesCount >= 300 && linesCount <= 325))
            if (++videoStandardDetectionFrameCount >= 5)
                videoStandardDetectionFinish(linesCount);
    }

    private void videoStandardDetectionFinish(int linesCount) {
        videoStandardDetected = linesCount < 290 ? VideoStandard.NTSC : VideoStandard.PAL;

        // Compute an additional number of lines to make the display bigger, if needed
        // Only used when the detected number of lines per frame is bigger than standard by a reasonable amount
        int prevAdd = videoStandardDetectionAdtLinesPerFrame;
        int newAdd = linesCount - videoStandardDetected.height;
        if (newAdd > 2) newAdd = (Math.min(newAdd, 6)) - 2;
        else newAdd = 0;

        // Only sets size now if additional lines changed
        if (newAdd != prevAdd) {
            videoStandardDetectionAdtLinesPerFrame = newAdd;
            adjustToVideoStandard(videoStandardDetected);
        }
    }

    private void displayUpdateSize() {
        if (display == null) return;
        synchronized (refreshMonitor) {
            Dimension size = new Dimension((int) (displayWidth * displayScaleX), (int) (displayHeight * displayScaleY));
            display.displaySize(size);
            display.displayMinimumSize(new Dimension((int) (displayWidth * DEFAULT_SCALE_X / DEFAULT_SCALE_Y), displayHeight));
        }
    }

    private void displayCenter() {
        if (display == null) return;
        synchronized (refreshMonitor) {
            display.displayCenter();
        }
    }

    private Graphics2D displayGraphics() {
        if (display == null) return null;
        Graphics2D displayGraphics = display.displayGraphics();
        displayGraphics.setComposite(AlphaComposite.Src);
        // Adjusts the Render Quality if needed
        if (crtFilter)
            displayGraphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        return displayGraphics;
    }

    private void displayFrameFinished(Graphics2D graphics) {
        if (display == null) return;
        display.displayFinishFrame(graphics);
    }

    private void init() {
        monitorControls = new MonitorControls(this);
        prepareResources();
        adjustToVideoStandard(VideoStandard.NTSC);
        setDisplayDefaultSize();
        clock = new Clock("Video Monitor", this, fps);
        paintLogo();
    }

    private void prepareResources() {
        // Prepare Buffers and FrameImage with maximum possible sizes (PAL)
        backBuffer = new int[VideoStandard.PAL.width * VideoStandard.PAL.height];
        frontBuffer = new int[VideoStandard.PAL.width * VideoStandard.PAL.height];
        frameImage = new BufferedImage(VideoStandard.PAL.width, VideoStandard.PAL.height, BufferedImage.TYPE_INT_ARGB);
        if (FRAME_ACCELERATION >= 0) frameImage.setAccelerationPriority(FRAME_ACCELERATION);
        // Prepare the Logo image
        try {
            logoIcon = SwingHelper.loadAsCompatibleImage("org/javatari/pc/screen/images/Logo.png");
        } catch (IOException ignored) {
        }
        // Prepare the OSD paint component
        osdComponent = new JLabel();
        osdComponent.setForeground(Color.GREEN);
        osdComponent.setBackground(new Color(0x50000000, true));
        osdComponent.setFont(new Font(Environment.ARIAL_FONT ? "Arial"
                : Environment.LIBERATION_FONT ? "Liberation Sans" : "SansSerif", Font.BOLD, 15));
        osdComponent.setBorder(new EmptyBorder(5, 12, 5, 12));
        osdComponent.setOpaque(true);
        // Prepare CRT mode 2 texture
        scanlinesTextureImage = new BufferedImage(2048, 1280, BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g = scanlinesTextureImage.createGraphics();
        g.setColor(new Color((int) (SCANLINES_STRENGTH * 255) << 24, true));
        for (int i = 1; i < scanlinesTextureImage.getHeight(); i += 2)
            g.drawLine(0, i, scanlinesTextureImage.getWidth(), i);
        g.dispose();
        if (SCANLINES_ACCELERATION >= 0) scanlinesTextureImage.setAccelerationPriority(SCANLINES_ACCELERATION);
        // Prepare CRT mode 3 and 4 composition
        crtTriadComposite = new CRTTriadComposite();
        // Prepare intermediate image for CRT modes or OSD rendering in SingleBuffer mode
        intermFrameImage = new BufferedImage(2048, 1280, BufferedImage.TYPE_INT_RGB);
        if (IMTERM_FRAME_ACCELERATION >= 0) intermFrameImage.setAccelerationPriority(IMTERM_FRAME_ACCELERATION);
    }

    private void adjustToVideoSignal() {
        if (signalStandard != videoSignal.standard())
            adjustToVideoStandard(videoSignal.standard());
    }

    // Synchronize to avoid changing the standard while refreshing frame or receiving lines
    private synchronized void adjustToVideoStandard(VideoStandard videoStandard) {
        synchronized (refreshMonitor) {
            signalStandard = videoStandard;
            signalWidth = videoStandard.width;
            signalHeight = videoStandard.height;
            setDisplaySize(displayWidth, displayHeightPct);
            setDisplayOrigin(displayOriginX, displayOriginYPct);
        }
    }

    private void adjustToVideoSignalOff() {
        line = 0;
        display.displayClear();
        paintLogo();
        if (fps == 0 && powerOn && signalOffRefresher == null) {
            SignalOffRefresher ref = new SignalOffRefresher();
            signalOffRefresher = ref;
            ref.start();
        }
    }

    private void paintLogo() {
        synchronized (refreshMonitor) {
            Graphics2D canvasGraphics = displayGraphics();
            if (canvasGraphics == null) return;
            Dimension ces = display.displayEffectiveSize();
            int w = ces.width;
            int h = ces.height;
            Graphics2D intermGraphics = intermFrameImage.createGraphics();
            intermGraphics.setBackground(Color.BLACK);
            intermGraphics.clearRect(0, 0, w, h);
            int lw = logoIcon.getWidth(null);
            int lh = logoIcon.getHeight(null);
            float r = h < lh ? (float) h / lh : 1;
            lw *= r;
            lh *= r;
            intermGraphics.drawImage(logoIcon, (w - lw) / 2, (h - lh) / 2, lw, lh, null);
            paintOSD(intermGraphics);
            canvasGraphics.drawImage(intermFrameImage, 0, 0, w, h, 0, 0, w, h, null);
            intermGraphics.dispose();
            displayFrameFinished(canvasGraphics);
        }
    }

    private void paintOSD(Graphics2D canvasGraphics) {
        if (--osdFramesLeft < 0) return;
        canvasGraphics.setComposite(AlphaComposite.SrcOver);
        osdComponent.setText(osdMessage);
        Dimension s = osdComponent.getPreferredSize();
        SwingUtilities.paintComponent(
                canvasGraphics, osdComponent, display.displayContainer(),
                (display.displayEffectiveSize().width - s.width) - 12, 12,
                s.width, s.height
        );
    }

    private void refresh() {
        if (!signalOn) {
            paintLogo();
            return;
        }
        // Synchronize to avoid changing image properties while refreshing frame
        synchronized (refreshMonitor) {
            int f = frame;
            if (lastFrameRendered == f) return;

            int[] drawBuffer = frontBuffer;
            Graphics2D displayGraphics = displayGraphics();
            if (displayGraphics == null) return;
            // Get the entire Canvas
            Dimension ces = display.displayEffectiveSize();
            int displayEffectiveWidth = ces.width;
            int displayEffectiveHeight = ces.height;
            // CRT mode 3 OR no MultiBuffering active and needs to superimpose (CRT mode 1, 2 or OSD)
            // draw frameImage to intermediate image with composite then transfer to Canvas
            if (crtMode >= 3 || (MULTI_BUFFERING < 2 && (osdFramesLeft >= 0 || crtMode > 0))) {
                int intermWidth = Math.min(displayEffectiveWidth, 2048);
                int intermHeight = Math.min(displayEffectiveHeight, 1280);
                Graphics2D intermGraphics = intermFrameImage.createGraphics();
                intermGraphics.setComposite(AlphaComposite.Src);
                // Renders to intermediate image
                renderFrame(intermGraphics, drawBuffer, intermWidth, intermHeight);
                // If CRT mode 2, alpha-superimpose the prepared scanlines image
                if (crtMode == 2)
                    renderScanlines(intermGraphics, intermWidth, intermHeight);
                // If CRT mode 3 or 4, sets the CRTTriadComposite and rewrite
                if (crtMode >= 3) {
                    intermGraphics.setComposite(crtTriadComposite);
                    intermGraphics.drawImage(
                            intermFrameImage,
                            0, 0, intermWidth, intermHeight,
                            0, 0, intermWidth, intermHeight,
                            null);
                }
                paintOSD(intermGraphics);
                intermGraphics.dispose();
                // Then transfer to Canvas
                displayGraphics.drawImage(
                        intermFrameImage,
                        0, 0, intermWidth, intermHeight,
                        0, 0, intermWidth, intermHeight,
                        null);
            } else {
                // Renders directly to Canvas
                renderFrame(displayGraphics, drawBuffer, displayEffectiveWidth, displayEffectiveHeight);
                // If CRT mode 2, alpha-superimpose the prepared scanlines image
                if (crtMode == 2)
                    renderScanlines(displayGraphics, displayEffectiveWidth, displayEffectiveHeight);
                paintOSD(displayGraphics);
            }
            displayFrameFinished(displayGraphics);
            lastFrameRendered = f;
        }
    }

    private void renderFrame(Graphics2D graphics, int[] drawBuffer, int effectiveWidth, int effectiveHeight) {
        // If CRT mode 1, 2 or 4, set composite for last and new frame over each other, and draw old frame
        if (crtMode > 0 && crtMode != 3) {
            // Clear last image
            graphics.setBackground(Color.BLACK);
            graphics.clearRect(0, 0, effectiveWidth, effectiveHeight);
            // Draw old frame
            graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, CRT_RETENTION_ALPHA));
            graphics.drawImage(frameImage, 0, 0, effectiveWidth, effectiveHeight, 0, 0, displayWidth, displayHeight, null);
        }
        // Update the image to draw with contents stored in the frontBuffer
        frameImage.getRaster().setDataElements(0, 0, displayWidth, displayHeight, drawBuffer);
        // Draw new frame
        graphics.drawImage(frameImage, 0, 0, effectiveWidth, effectiveHeight, 0, 0, displayWidth, displayHeight, null);
    }

    private void renderScanlines(Graphics2D graphics, int effectiveWidth, int effectiveHeight) {
        graphics.setComposite(AlphaComposite.SrcOver);
        graphics.drawImage(scanlinesTextureImage, 0, 0, effectiveWidth, effectiveHeight, 0, 0, effectiveWidth, effectiveHeight, null);
    }

    private synchronized void setDisplayDefaultSize() {
        setDisplaySize(DEFAULT_WIDTH, DEFAULT_HEIGHT_PCT);
        setDisplayOrigin(DEFAULT_ORIGIN_X, DEFAULT_ORIGIN_Y_PCT);
        if (display != null) {
            float scX = display.displayDefaultOpenningScaleX(displayWidth, displayHeight);
            setDisplayScale(scX, scX / DEFAULT_SCALE_ASPECT_X);
        } else
            setDisplayScale(DEFAULT_SCALE_X, DEFAULT_SCALE_Y);
        displayCenter();
    }

    private synchronized void setDisplayOrigin(int x, double yPct) {
        displayOriginX = x;
        if (displayOriginX < 0) displayOriginX = 0;
        else if (displayOriginX > signalWidth - displayWidth) displayOriginX = signalWidth - displayWidth;

        displayOriginYPct = yPct;
        if (displayOriginYPct < 0) displayOriginYPct = 0;
        else if ((displayOriginYPct / 100 * signalHeight) > signalHeight - displayHeight)
            displayOriginYPct = ((double) signalHeight - displayHeight) / signalHeight * 100;

        // Compute final display originY, adding a little for additional lines as discovered in last video standard detection
        int adtOriginY = videoStandardDetectionAdtLinesPerFrame / 2;
        displayOriginY = (int) (displayOriginYPct / 100 * signalHeight) + adtOriginY;
        if ((displayOriginY + displayHeight) > signalHeight) displayOriginY = signalHeight - displayHeight;
    }

    private synchronized void setDisplaySize(int width, double heightPct) {
        displayWidth = width;
        if (displayWidth < 10) displayWidth = 10;
        else if (displayWidth > signalWidth) displayWidth = signalWidth;

        displayHeightPct = heightPct;
        if (displayHeightPct < 10) displayHeightPct = 10;
        else if (displayHeightPct > 100) displayHeightPct = 100;

        // Compute final display height, considering additional lines as discovered in last video standard detection
        displayHeight = (int) (displayHeightPct / 100 * (signalHeight + videoStandardDetectionAdtLinesPerFrame));
        if (displayHeight > signalHeight) displayHeight = signalHeight;

        setDisplayOrigin(displayOriginX, displayOriginYPct);
        displayUpdateSize();
    }

    private void setDisplayScale(float x, float y) {
        displayScaleX = x;
        if (displayScaleX < 1) displayScaleX = 1;
        displayScaleY = y;
        if (displayScaleY < 1) displayScaleY = 1;
        displayUpdateSize();
    }

    private void setDisplayScaleDefaultAspect(float y) {
        int scaleY = (int) y;
        if (scaleY < 1) scaleY = 1;
        setDisplayScale(scaleY * DEFAULT_SCALE_ASPECT_X, scaleY);
    }

    private void loadCartridgeFromFile(boolean autoPower) {
        if (cartridgeChangeDisabledWarning()) return;
        display.displayLeaveFullscreen();
        Cartridge cart = null;
        try {
            File file = FileROMChooser.chooseFileToLoad();
            if (file != null) cart = ROMLoader.load(file);
        } catch (AccessControlException e) {
            // Automatically tries FileServiceChooser if access is denied
            FileContents fileContents = FileServiceROMChooser.chooseFileToLoad();
            if (fileContents != null) cart = ROMLoader.load(fileContents);
        }
        if (cart != null) cartridgeInsert(cart, autoPower);
        else display.displayRequestFocus();
    }

    private void loadCartridgeFromURL(boolean autoPower) {
        if (cartridgeChangeDisabledWarning()) return;
        display.displayLeaveFullscreen();
        Cartridge cart = null;
        String url = URLROMChooser.chooseURLToLoad();
        if (url != null) cart = ROMLoader.load(url, false);
        if (cart != null) cartridgeInsert(cart, autoPower);
        else display.displayRequestFocus();
    }

    private void loadCartridgeEmpty() {
        if (cartridgeChangeDisabledWarning()) return;
        cartridgeSocket.insert(null, false);
    }

    private void loadCartridgePaste() {
        if (cartridgeChangeDisabledWarning()) return;
        try {
            Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
            Transferable transf = clip.getContents("Ignored");
            if (transf == null) return;
            Cartridge cart = ROMTransferHandlerUtil.importCartridgeData(transf);
            if (cart != null) cartridgeInsert(cart, true);
        } catch (Exception ex) {
            // Simply give up
        }
    }

    private void saveStateCartridge() {
        savestateSocket.saveStateFile();
        display.displayRequestFocus();
    }

    private boolean cartridgeChangeDisabledWarning() {
        if (!isCartridgeChangeEnabled()) {
            showOSD("Cartridge change is disabled", true);
            return true;
        }
        return false;
    }

    private void crtModeToggle() {
        setCrtMode(crtMode + 1);
    }

    private void setCrtMode(int mode) {
        synchronized (refreshMonitor) {
            int newMode = mode > 4 || mode < 0 ? 0 : mode;
            if (crtMode == newMode) return;
            crtMode = newMode;
            showOSD("CRT mode: " + crtModeNames[crtMode], true);
        }
    }

    void controlActivated(Control control) {
        // All controls are Press-only and repeatable
        switch (control) {
            case LOAD_CARTRIDGE_FILE:
                loadCartridgeFromFile(true);
                break;
            case LOAD_CARTRIDGE_FILE_NO_AUTO_POWER:
                loadCartridgeFromFile(false);
                break;
            case LOAD_CARTRIDGE_URL:
                loadCartridgeFromURL(true);
                break;
            case LOAD_CARTRIDGE_URL_NO_AUTO_POWER:
                loadCartridgeFromURL(false);
                break;
            case LOAD_CARTRIDGE_EMPTY:
                loadCartridgeEmpty();
                break;
            case LOAD_CARTRIDGE_PASTE:
                loadCartridgePaste();
                break;
            case SAVE_STATE_CARTRIDGE:
                saveStateCartridge();
                break;
            case CRT_FILTER:
                crtFilter = !crtFilter;
                showOSD(crtFilter ? "CRT Filter: ON" : "CRT Filter: OFF", true);
                break;
            case CRT_MODES:
                crtModeToggle();
                break;
            case STATS:
                showStats = !showStats;
                showOSD(null, true);
                break;
            case DEBUG:
                debug++;
                if (debug > 4) debug = 0;
                break;
            case ORIGIN_X_MINUS:
                setDisplayOrigin(displayOriginX + 1, displayOriginYPct);
                break;
            case ORIGIN_X_PLUS:
                setDisplayOrigin(displayOriginX - 1, displayOriginYPct);
                break;
            case ORIGIN_Y_MINUS:
                setDisplayOrigin(displayOriginX, displayOriginYPct + 0.5);
                break;
            case ORIGIN_Y_PLUS:
                setDisplayOrigin(displayOriginX, displayOriginYPct - 0.5);
                break;
            case SIZE_DEFAULT:
                setDisplayDefaultSize();
                break;
        }
        if (fixedSizeMode) return;
        switch (control) {
            case WIDTH_MINUS:
                setDisplaySize(displayWidth - 1, displayHeightPct);
                break;
            case WIDTH_PLUS:
                setDisplaySize(displayWidth + 1, displayHeightPct);
                break;
            case HEIGHT_MINUS:
                setDisplaySize(displayWidth, displayHeightPct - 0.5);
                break;
            case HEIGHT_PLUS:
                setDisplaySize(displayWidth, displayHeightPct + 0.5);
                break;
            case SCALE_X_MINUS:
                setDisplayScale(displayScaleX - 0.5f, displayScaleY);
                break;
            case SCALE_X_PLUS:
                setDisplayScale(displayScaleX + 0.5f, displayScaleY);
                break;
            case SCALE_Y_MINUS:
                setDisplayScale(displayScaleX, displayScaleY - 0.5f);
                break;
            case SCALE_Y_PLUS:
                setDisplayScale(displayScaleX, displayScaleY + 0.5f);
                break;
            case SIZE_MINUS:
                setDisplayScaleDefaultAspect(displayScaleY - 1);
                break;
            case SIZE_PLUS:
                setDisplayScaleDefaultAspect(displayScaleY + 1);
                break;
        }
    }

    public enum Control {
        WIDTH_PLUS, HEIGHT_PLUS,
        WIDTH_MINUS, HEIGHT_MINUS,
        ORIGIN_X_PLUS, ORIGIN_Y_PLUS,
        ORIGIN_X_MINUS, ORIGIN_Y_MINUS,
        SCALE_X_PLUS, SCALE_Y_PLUS,
        SCALE_X_MINUS, SCALE_Y_MINUS,
        SIZE_PLUS, SIZE_MINUS,
        SIZE_DEFAULT,
        LOAD_CARTRIDGE_FILE, LOAD_CARTRIDGE_FILE_NO_AUTO_POWER,
        LOAD_CARTRIDGE_URL, LOAD_CARTRIDGE_URL_NO_AUTO_POWER,
        LOAD_CARTRIDGE_EMPTY,
        LOAD_CARTRIDGE_PASTE,
        SAVE_STATE_CARTRIDGE,
        CRT_FILTER, CRT_MODES,
        DEBUG, STATS
    }

    private final class SignalOffRefresher extends Thread {
        @Override
        public void run() {
            try {
                sleep(1000 / 30);
            } catch (InterruptedException ignored) {
            }
            while (powerOn && !signalOn) {
                synchOutput();
                try {
                    sleep(1000 / 30);
                } catch (InterruptedException ignored) {
                }    // 30 fps
            }
            signalOffRefresher = null;
        }
    }

}
