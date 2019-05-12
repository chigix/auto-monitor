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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

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
            if (!boundSnapLock.writeLock().tryLock()) {
                return;
            }
            final var tempCurrCursor = MouseInfo.getPointerInfo().getLocation();
            final var currentDeviceBounds = devices.get(currentDeviceIndex.get()).getBounds();
            if (tempCurrCursor.getY() < currentDeviceBounds.getMinY()) {
                robot.mouseMove((int) getCurrentCursor().getX(), (int) currentDeviceBounds.getMinY());
            } else if (tempCurrCursor.getY() == currentDeviceBounds.getMinY()) {
                robot.mouseMove((int) tempCurrCursor.getX(), (int) currentDeviceBounds.getMinY());
                currentCursor.replace("CURRENT_CURSOR", tempCurrCursor);
            } else if (tempCurrCursor.getY() >= currentDeviceBounds.getMaxY() - 1) {
                robot.mouseMove((int) getCurrentCursor().getX(), (int) currentDeviceBounds.getMaxY() - 1);
            } else {
                currentCursor.replace("CURRENT_CURSOR", tempCurrCursor);
            }
            try {
                Thread.sleep(5);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            boundSnapLock.writeLock().unlock();
        }

        @Override
        public void nativeMouseDragged(NativeMouseEvent e) {
            if (!boundSnapLock.writeLock().tryLock()) {
                return;
            }
            final var tempCurrCursor = MouseInfo.getPointerInfo().getLocation();
            final var currentDeviceBounds = devices.get(currentDeviceIndex.get()).getBounds();
            if (tempCurrCursor.getY() <= currentDeviceBounds.getMinY()) {
                robot.mouseMove((int) getCurrentCursor().getX(), (int) currentDeviceBounds.getMinY());
            } else if (tempCurrCursor.getY() >= currentDeviceBounds.getMaxY() - 1) {
                robot.mouseMove((int) getCurrentCursor().getX(), (int) currentDeviceBounds.getMaxY() - 1);
            } else {
                currentCursor.replace("CURRENT_CURSOR", tempCurrCursor);
            }
            try {
                Thread.sleep(5);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
            boundSnapLock.writeLock().unlock();
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
    private final ReadWriteLock boundSnapLock = new ReentrantReadWriteLock();

    private final AtomicInteger currentDeviceIndex = new AtomicInteger(0);

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

    private final Point getCurrentCursor() {
        return currentCursor.get("CURRENT_CURSOR");
    }

    private final void updateCurrentDeviceIndex() {
        int index = 0;
        for (GraphicsConfiguration device : devices) {
            final var bounds = device.getBounds();
            final var cursor = currentCursor.get("CURRENT_CURSOR");
            if (bounds.getMinX() < cursor.getX() && bounds.getMaxX() > cursor.getX() && bounds.getMinY() < cursor.getY()
                    && bounds.getMaxY() > cursor.getY()) {
                break;
            }
            index++;
        }
        if (index > devices.size()) {
            return;
        }
        this.currentDeviceIndex.set(index);
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
        boundSnapLock.writeLock().lock();
        var bounds = defaultDevice.getDefaultConfiguration().getBounds();
        robot.mouseMove((int) bounds.getCenterX(), (int) bounds.getCenterY());
        currentDeviceIndex.set(0);
        boundSnapLock.writeLock().unlock();
    }

    public void moveCursorToNextScreen() {
        boundSnapLock.writeLock().lock();
        updateCurrentDeviceIndex();
        final var nextDeviceIndex = (currentDeviceIndex.get() + 1) % devices.size();
        final var nextDeviceBounds = devices.get(nextDeviceIndex).getBounds();
        robot.mouseMove((int) nextDeviceBounds.getCenterX(), (int) nextDeviceBounds.getCenterY());
        currentDeviceIndex.set(nextDeviceIndex);
        try {
            Thread.sleep(5);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
        }
        boundSnapLock.writeLock().unlock();
    }
}