package com.chigix.automonitor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

/**
 * HotkeysDetection
 */
public class HotkeysDetection implements NativeKeyListener {

    private final List<Integer> possibleHotkeys = new ArrayList<>(10);
    private final List<String> possibleHotkeysStr = new ArrayList<>();

    private HotkeyEvent listener;

    @Override
    public void nativeKeyTyped(NativeKeyEvent e) {
    }

    @Override
    public void nativeKeyReleased(final NativeKeyEvent e) {
        final var distinctKeys = possibleHotkeys.stream().distinct().collect(Collectors.toList());
        possibleHotkeys.removeIf(new Predicate<Integer>() {

            @Override
            public boolean test(Integer t) {
                return t == e.getKeyCode() || t == 0;
            }
        });
        possibleHotkeysStr.removeIf(new Predicate<String>() {

            @Override
            public boolean test(String t) {
                return t == NativeKeyEvent.getKeyText(e.getKeyCode());
            }
        });
        Integer[] keys = new Integer[distinctKeys.size()];
        if (keys.length < 1) {
            return;
        }
        distinctKeys.toArray(keys);
        this.listener.onHotkey(keys);
    }

    @Override
    public void nativeKeyPressed(NativeKeyEvent e) {
        possibleHotkeys.add(e.getKeyCode());
        possibleHotkeysStr.add(NativeKeyEvent.getKeyText(e.getKeyCode()));
    }

    public HotkeysDetection addListener(HotkeyEvent listener) {
        this.listener = listener;
        return this;
    }

}