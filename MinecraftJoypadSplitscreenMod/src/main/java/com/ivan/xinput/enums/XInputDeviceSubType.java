package com.ivan.xinput.enums;

import com.ivan.xinput.natives.XInputConstants;

/**
 * Enumerates all XInput device subtypes.
 *
 * @author Ivan "StrikerX3" Oliveira
 */
public enum XInputDeviceSubType {
    UNKNOWN(XInputConstants.XINPUT_DEVSUBTYPE_UNKNOWN),
    GAMEPAD(XInputConstants.XINPUT_DEVSUBTYPE_GAMEPAD),
    WHEEL(XInputConstants.XINPUT_DEVSUBTYPE_WHEEL),
    ARCADE_STICK(XInputConstants.XINPUT_DEVSUBTYPE_ARCADE_STICK),
    FLIGHT_STICK(XInputConstants.XINPUT_DEVSUBTYPE_FLIGHT_STICK),
    DANCE_PAD(XInputConstants.XINPUT_DEVSUBTYPE_DANCE_PAD),
    GUITAR(XInputConstants.XINPUT_DEVSUBTYPE_GUITAR),
    GUITAR_ALTERNATE(XInputConstants.XINPUT_DEVSUBTYPE_GUITAR_ALTERNATE),
    DRUM_KIT(XInputConstants.XINPUT_DEVSUBTYPE_DRUM_KIT),
    GUITAR_BASS(XInputConstants.XINPUT_DEVSUBTYPE_GUITAR_BASS),
    ARCADE_PAD(XInputConstants.XINPUT_DEVSUBTYPE_ARCADE_PAD);

    private byte deviceTypeValue;

    XInputDeviceSubType(final byte deviceTypeValue) {
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
    public static XInputDeviceSubType fromNative(final byte value) {
        switch (value) {
            case XInputConstants.XINPUT_DEVSUBTYPE_UNKNOWN:
                return UNKNOWN;
            case XInputConstants.XINPUT_DEVSUBTYPE_GAMEPAD:
                return GAMEPAD;
            case XInputConstants.XINPUT_DEVSUBTYPE_WHEEL:
                return WHEEL;
            case XInputConstants.XINPUT_DEVSUBTYPE_ARCADE_STICK:
                return ARCADE_STICK;
            case XInputConstants.XINPUT_DEVSUBTYPE_FLIGHT_STICK:
                return FLIGHT_STICK;
            case XInputConstants.XINPUT_DEVSUBTYPE_DANCE_PAD:
                return DANCE_PAD;
            case XInputConstants.XINPUT_DEVSUBTYPE_GUITAR:
                return GUITAR;
            case XInputConstants.XINPUT_DEVSUBTYPE_GUITAR_ALTERNATE:
                return GUITAR_ALTERNATE;
            case XInputConstants.XINPUT_DEVSUBTYPE_DRUM_KIT:
                return DRUM_KIT;
            case XInputConstants.XINPUT_DEVSUBTYPE_GUITAR_BASS:
                return GUITAR_BASS;
            case XInputConstants.XINPUT_DEVSUBTYPE_ARCADE_PAD:
                return ARCADE_PAD;
            default:
                throw new IllegalArgumentException("Invalid native value " + value);
        }
    }
}
