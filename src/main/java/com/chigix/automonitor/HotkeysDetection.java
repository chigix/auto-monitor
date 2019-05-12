package com.chigix.automonitor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

/**
 * HotkeysDetection
 */
public class HotkeysDetection implements NativeKeyListener {

    private final List<Integer> possibleHotkeys = new ArrayList<>(10);

    private HotkeyEvent listener;

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
    }

    @Override
    public void nativeKeyReleased(final NativeKeyEvent e) {
        Integer[] keys = new Integer[possibleHotkeys.size()];
        possibleHotkeys.toArray(keys);
        possibleHotkeys.removeIf(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t) {
                return t == e.getKeyCode();
            }
        });
        this.listener.onHotkey(keys);
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        possibleHotkeys.add(e.getKeyCode());
    }

    public HotkeysDetection addListener(HotkeyEvent listener) {
        this.listener = listener;
        return this;
    }

}