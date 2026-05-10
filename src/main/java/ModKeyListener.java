import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import java.awt.*;
import java.awt.event.InputEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ModKeyListener implements NativeKeyListener {

    // ---- Config (loaded from config.properties next to the jar, optional) ----
    private static String keyUp = "W";
    private static String keyLeft = "A";
    private static String keyDown = "S";
    private static String keyRight = "D";
    private static String keyPause = "P";
    private static boolean mouseRecenter = false;   // original hardcoded true; default flipped
    private static boolean useCurrentMonitor = true; // original was primary-only

    public boolean wPressed = false;
    public boolean aPressed = false;
    public boolean sPressed = false;
    public boolean dPressed = false;
    public boolean pause = false;

    private static Robot robot;

    private int originX = 0;
    private int originY = 0;
    private int screenWidth = 0;
    private int screenHeight = 0;
    private Point screenCentre = new Point(0, 0);

    /** Recompute screen rectangle (handles multi-monitor by using the screen the cursor is on). */
    private void recomputeScreen() {
        if (useCurrentMonitor) {
            PointerInfo pi = MouseInfo.getPointerInfo();
            if (pi != null) {
                Rectangle r = pi.getDevice().getDefaultConfiguration().getBounds();
                originX = r.x;
                originY = r.y;
                screenWidth = r.width;
                screenHeight = r.height;
            }
        } else {
            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            originX = 0;
            originY = 0;
            screenWidth = d.width;
            screenHeight = d.height;
        }
        screenCentre = new Point(originX + screenWidth / 2, originY + screenHeight / 2);
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        String k = NativeKeyEvent.getKeyText(e.getKeyCode());
        if (k.equalsIgnoreCase(keyPause)) { pause = !pause; return; }

        boolean changed = false;
        if (k.equalsIgnoreCase(keyUp))    { wPressed = true; changed = true; }
        if (k.equalsIgnoreCase(keyLeft))  { aPressed = true; changed = true; }
        if (k.equalsIgnoreCase(keyDown))  { sPressed = true; changed = true; }
        if (k.equalsIgnoreCase(keyRight)) { dPressed = true; changed = true; }

        if (changed) {
            recomputeScreen();
            doMouseInput();
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        }
    }

    @Override
    public void nativeKeyReleased(NativeKeyEvent e) {
        String k = NativeKeyEvent.getKeyText(e.getKeyCode());
        boolean changed = false;
        if (k.equalsIgnoreCase(keyUp))    { wPressed = false; changed = true; }
        if (k.equalsIgnoreCase(keyLeft))  { aPressed = false; changed = true; }
        if (k.equalsIgnoreCase(keyDown))  { sPressed = false; changed = true; }
        if (k.equalsIgnoreCase(keyRight)) { dPressed = false; changed = true; }

        if (!changed) return;
        recomputeScreen();

        if (!wPressed && !aPressed && !sPressed && !dPressed) {
            if (mouseRecenter) robot.mouseMove(screenCentre.x, screenCentre.y);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        } else {
            doMouseInput();
        }
    }

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) { }

    private void doMouseInput() {
        if (pause) return;
        Point v = generateInputVector();
        robot.mouseMove(v.x, v.y);
    }

    private Point generateInputVector() {
        int wInput = wPressed ? -screenHeight / 2 : 0;
        int aInput = aPressed ? -screenWidth / 2 : 0;
        int sInput = sPressed ?  screenHeight / 2 : 0;
        int dInput = dPressed ?  screenWidth / 2 : 0;
        int x = originX + (screenWidth / 2) + aInput + dInput;
        int y = originY + (screenHeight / 2) + wInput + sInput;
        return new Point(x, y);
    }

    private static void loadConfig() {
        File cfg = new File("config.properties");
        if (!cfg.exists()) return;
        try (FileInputStream fis = new FileInputStream(cfg)) {
            Properties p = new Properties();
            p.load(fis);
            keyUp    = p.getProperty("key.up",    keyUp).toUpperCase();
            keyLeft  = p.getProperty("key.left",  keyLeft).toUpperCase();
            keyDown  = p.getProperty("key.down",  keyDown).toUpperCase();
            keyRight = p.getProperty("key.right", keyRight).toUpperCase();
            keyPause = p.getProperty("key.pause", keyPause).toUpperCase();
            mouseRecenter     = Boolean.parseBoolean(p.getProperty("mouse.recenter",     String.valueOf(mouseRecenter)));
            useCurrentMonitor = Boolean.parseBoolean(p.getProperty("monitor.useCurrent", String.valueOf(useCurrentMonitor)));
        } catch (IOException ignore) { /* keep defaults */ }
    }

    public static void main(String[] args) throws AWTException {
        Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);
        logger.setUseParentHandlers(false);

        loadConfig();
        robot = new Robot();

        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("There was a problem registering the native hook.");
            System.err.println(ex.getMessage());
            System.exit(1);
        }

        ModKeyListener listener = new ModKeyListener();
        listener.recomputeScreen();
        GlobalScreen.addNativeKeyListener(listener);

        System.out.println("WASDungeons fork ready.");
        System.out.println("  keys  = " + keyUp + keyLeft + keyDown + keyRight + "   pause = " + keyPause);
        System.out.println("  mouse.recenter     = " + mouseRecenter);
        System.out.println("  monitor.useCurrent = " + useCurrentMonitor);
        System.out.println("Hook ativo. Para sair: feche esta janela ou Ctrl+C.");
    }
}
