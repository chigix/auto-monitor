package com.chigix.automonitor;

import java.awt.AWTException;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.mouse.NativeMouseEvent;
import org.jnativehook.mouse.NativeMouseInputListener;

/**
 * Context
 */
public class Context {

    private NativeMouseInputListener mouseListener = new NativeMouseInputListener() {

        @Override
        public void nativeMouseMoved(NativeMouseEvent e) {
            currentCursor.replace("CURRENT_CURSOR", MouseInfo.getPointerInfo().getLocation());
        }

        @Override
        public void nativeMouseDragged(NativeMouseEvent e) {
            System.out.println("Mouse Dragged");
        }

        @Override
        public void nativeMouseReleased(NativeMouseEvent e) {
            System.out.println("Mouse Dragged");
        }

        @Override
        public void nativeMousePressed(NativeMouseEvent e) {
            System.out.println("Mouse Dragged");
        }

        @Override
        public void nativeMouseClicked(NativeMouseEvent e) {
            System.out.println("Mouse Dragged");
        }
    };

    private List<GraphicsConfiguration> devices = new ArrayList<>();

    private final GraphicsEnvironment env;
    private final GraphicsDevice defaultDevice;

    private final Robot robot;

    private final Map<String, Point> currentCursor = new HashMap<>();

    public Context(GraphicsEnvironment env) throws AWTException {
        this.env = env;
        this.defaultDevice = env.getDefaultScreenDevice();
        robot = new Robot();
    }

    public void init() {
        for (GraphicsDevice device : env.getScreenDevices()) {
            devices.add(device.getDefaultConfiguration());
        }
        currentCursor.put("CURRENT_CURSOR", MouseInfo.getPointerInfo().getLocation());
        GlobalScreen.addNativeMouseMotionListener(mouseListener);
        Robot robot;
    }

    public void destroy() {
        GlobalScreen.removeNativeMouseMotionListener(mouseListener);
        try {
            GlobalScreen.unregisterNativeHook();
        } catch (NativeHookException e) {
            e.printStackTrace();
        }
    }

    public void moveCursorToDefault() {
        var bounds = defaultDevice.getDefaultConfiguration().getBounds();
        robot.mouseMove((int) bounds.getCenterX(), (int) bounds.getCenterY());
        System.out.println(currentCursor.get("CURRENT_CURSOR"));
    }

    public void moveCursorToNextScreen() {
        int index = 0;
        double horizontalRatio = 0;
        double verticalRatio = 0;
        for (GraphicsConfiguration device : devices) {
            final var bounds = device.getBounds();
            final var cursor = currentCursor.get("CURRENT_CURSOR");
            if (bounds.getMinX() < cursor.getX() && bounds.getMaxX() > cursor.getX() && bounds.getMinY() < cursor.getY()
                    && bounds.getMaxY() > cursor.getY()) {
                horizontalRatio = (cursor.getX() - bounds.getMinX()) / bounds.getWidth();
                verticalRatio = (cursor.getY() - bounds.getMinY()) / bounds.getHeight();
                break;
            }
            index++;
        }
        if (index >= devices.size()) {
            return;
        }
        final var nextDeviceBounds = devices.get((index + 1) % devices.size()).getBounds();
        robot.mouseMove((int) nextDeviceBounds.getCenterX(), (int) nextDeviceBounds.getCenterY());
    }
}