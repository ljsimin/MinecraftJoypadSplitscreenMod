package com.ivan.xinput.enums;

import com.ivan.xinput.natives.XInputConstants;

/**
 * Enumerates all XInput device types.
 *
 * @author Ivan "StrikerX3" Oliveira
 */
public enum XInputDeviceType {
    GAMEPAD(XInputConstants.XINPUT_DEVTYPE_GAMEPAD);

    private byte deviceTypeValue;

    XInputDeviceType(final byte deviceTypeValue) {
        this.deviceTypeValue = deviceTypeValue;
    }

    public byte getDeviceTypeValue() {
        return deviceTypeValue;
    }

    /**
     * Retrieves the appropriate enum value from the native value.
     *
     * @param value the native value
     * @return the corresponding enum constant
     * @throws IllegalArgumentException if the given native value does not correspond
     * to an enum value
     */
    public static XInputDeviceType fromNative(final byte value) {
        switch (value) {
            case XInputConstants.XINPUT_DEVTYPE_GAMEPAD:
                return GAMEPAD;
            default:
                throw new IllegalArgumentException("Invalid native value " + value);
        }
    }
}
