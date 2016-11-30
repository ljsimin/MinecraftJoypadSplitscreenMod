package com.ivan.xinput.enums;

import com.ivan.xinput.natives.XInputConstants;

/**
 * Enumerates all XInput battery types.
 *
 * @author Ivan "StrikerX3" Oliveira
 */
public enum XInputBatteryType {
    DISCONNECTED, WIRED, ALKALINE, NIMH, UNKNOWN;

    /**
     * Retrieves the appropriate enum value from the native value.
     *
     * @param value the native value
     * @return the corresponding enum constant
     * @throws IllegalArgumentException if the given native value does not correspond
     * to an enum value
     */
    public static XInputBatteryType fromNative(final byte value) {
        switch (value) {
            case XInputConstants.BATTERY_TYPE_DISCONNECTED:
                return DISCONNECTED;
            case XInputConstants.BATTERY_TYPE_WIRED:
                return WIRED;
            case XInputConstants.BATTERY_TYPE_ALKALINE:
                return ALKALINE;
            case XInputConstants.BATTERY_TYPE_NIMH:
                return NIMH;
            case XInputConstants.BATTERY_TYPE_UNKNOWN:
                return UNKNOWN;
            default:
                throw new IllegalArgumentException("Invalid native value " + value);
        }
    }
}
