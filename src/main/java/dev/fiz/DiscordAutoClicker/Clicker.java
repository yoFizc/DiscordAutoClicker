package dev.fiz.DiscordAutoClicker;

import org.jnativehook.GlobalScreen;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseListener;

import java.awt.*;
import java.awt.event.InputEvent;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;


public class Clicker {

    private boolean state = false;
    private boolean holding = false;
    private boolean skipNextPress = false;
    private boolean skipNextRelease = false;
    private boolean chill = false;
    private int cps = 10;

    public Clicker() {Logger logger = Logger.getLogger(GlobalScreen.class.getPackage().getName());
        logger.setLevel(Level.WARNING);
        logger.setUseParentHandlers(false);

        try {
            GlobalScreen.registerNativeHook();
        } catch (Exception e) {
            e.printStackTrace();
        }

        GlobalScreen.addNativeMouseListener(new NativeMouseListener() {
            @Override
            public void nativeMouseClicked(NativeMouseEvent event) {
            }

            @Override
            public void nativeMousePressed(NativeMouseEvent e) {
                if (e.getButton() == NativeMouseEvent.BUTTON1) {
                    if (skipNextPress) {
                        skipNextPress = false;
                    } else {
                        holding = true;
                        chill = true;
                    }
                }
            }

            @Override
            public void nativeMouseReleased(NativeMouseEvent e) {
                if (e.getButton() == NativeMouseEvent.BUTTON1) {
                    if (skipNextRelease) {
                        skipNextRelease = false;
                    } else {
                        holding = false;
                    }
                }
            }
        });
    }

    public void start() {
        new Thread(() -> {
            try {
                Robot robot = new Robot();
                Random random = new Random();

                while (true) {
                    if (state && holding) {
                        if (!chill) {
                            skipNextPress = true;
                            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
                        }

                        long ms = calculateRandomInterval(cps, random);

                        Thread.sleep(ms / 2);

                        if (!chill) {
                            skipNextRelease = true;
                            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
                        }

                        Thread.sleep(ms / 2);

                        chill = false;
                    }

                    Thread.sleep(1);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private long calculateRandomInterval(int cps, Random random) {
        double baseInterval = 1000.0 / cps;
        double randomFactor = 0.5 + (1.5 * random.nextDouble());
        return (long) (baseInterval * randomFactor);
    }

    public void increaseCPS() {
        cps = Math.min(30, cps + 1);
    }

    public void decreaseCPS() {
        cps = Math.max(5, cps - 1);
    }

    public void enable() {
        state = true;
    }

    public void disable() {
        state = false;
    }

    public boolean isEnabled() {
        return state;
    }

    public int getCPS() {
        return cps;
    }

}
