package com.ivan.xinput.enums;

import com.ivan.xinput.natives.XInputConstants;

/**
 * Enumerates all XInput battery levels.
 *
 * @author Ivan "StrikerX3" Oliveira
 */
public enum XInputBatteryLevel {
    EMPTY, LOW, MEDIUM, FULL;

    /**
     * Retrieves the appropriate enum value from the native value.
     *
     * @param value the native value
     * @return the corresponding enum constant
     * @throws IllegalArgumentException if the given native value does not correspond
     * to an enum value
     */
    public static XInputBatteryLevel fromNative(final byte value) {
        switch (value) {
            case XInputConstants.BATTERY_LEVEL_EMPTY:
                return EMPTY;
            case XInputConstants.BATTERY_LEVEL_LOW:
                return LOW;
            case XInputConstants.BATTERY_LEVEL_MEDIUM:
                return MEDIUM;
            case XInputConstants.BATTERY_LEVEL_FULL:
                return FULL;
            default:
                throw new IllegalArgumentException("Invalid native value " + value);
        }
    }
}
