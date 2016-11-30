package com.ivan.xinput.listener;

import com.ivan.xinput.enums.XInputButton;

/**
 * Provides empty implementations of all {@link XInputDeviceListener} methods for easier subclassing.
 *
 * @author Ivan "StrikerX3" Oliveira
 */
public class SimpleXInputDeviceListener implements XInputDeviceListener {
    @Override
    public void connected() {
    }

    @Override
    public void disconnected() {
    }

    @Override
    public void buttonChanged(final XInputButton button, final boolean pressed) {
    }
}
