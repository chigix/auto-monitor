package com.chigix.automonitor;

import java.awt.AWTException;
import java.awt.GraphicsEnvironment;
import java.util.Arrays;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;

/**
 * Hello world!
 *
 */
public class App {

    public static void main(String[] args) {
        Context context;
        try {
            context = new Context(GraphicsEnvironment.getLocalGraphicsEnvironment());
            context.init();
        } catch (AWTException e) {
            e.printStackTrace();
            throw new RuntimeException("Unexpected Context Init Error");
        }
        final var hotKeysDetect = new HotkeysDetection();
        hotKeysDetect.addListener(new HotkeyEvent() {

            @Override
            public void onHotkey(Integer[] keyCodes) {
                if (Arrays.equals(new Integer[] { 3675, 41 }, keyCodes)) {
                    context.moveCursorToDefault();
                } else if (Arrays.equals(new Integer[] { 29, 3675, 41 }, keyCodes)) {
                    context.moveCursorToNextScreen();
                    // } else {
                    // System.out.println(Arrays.toString(keyCodes));
                }
            }
        });
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        GlobalScreen.addNativeKeyListener(hotKeysDetect);
    }
}
