package com.ivan.xinput.natives;

/**
 * Contains constants used by the native library.
 *
 * @author Ivan "StrikerX3" Oliveira
 */
public final class XInputConstants {
    private XInputConstants() {}

    // -------------
    // XInput

    public static final int MAX_PLAYERS = 4;

    // Controller button masks
    public static final short XINPUT_GAMEPAD_DPAD_UP = 0x0001;
    public static final short XINPUT_GAMEPAD_DPAD_DOWN = 0x0002;
    public static final short XINPUT_GAMEPAD_DPAD_LEFT = 0x0004;
    public static final short XINPUT_GAMEPAD_DPAD_RIGHT = 0x0008;
    public static final short XINPUT_GAMEPAD_START = 0x0010;
    public static final short XINPUT_GAMEPAD_BACK = 0x0020;
    public static final short XINPUT_GAMEPAD_LEFT_THUMB = 0x0040;
    public static final short XINPUT_GAMEPAD_RIGHT_THUMB = 0x0080;
    public static final short XINPUT_GAMEPAD_LEFT_SHOULDER = 0x0100;
    public static final short XINPUT_GAMEPAD_RIGHT_SHOULDER = 0x0200;
    public static final short XINPUT_GAMEPAD_GUIDE_BUTTON = 0x0400;// undocumented
    public static final short XINPUT_GAMEPAD_UNKNOWN = 0x0800;// undocumented
    public static final short XINPUT_GAMEPAD_A = 0x1000;
    public static final short XINPUT_GAMEPAD_B = 0x2000;
    public static final short XINPUT_GAMEPAD_X = 0x4000;
    public static final short XINPUT_GAMEPAD_Y = (short) 0x8000;

    // Device types
    public static final byte XINPUT_DEVTYPE_GAMEPAD = 0x01;

    public static final byte XINPUT_DEVSUBTYPE_UNKNOWN = 0x00;
    public static final byte XINPUT_DEVSUBTYPE_GAMEPAD = 0x01;
    public static final byte XINPUT_DEVSUBTYPE_WHEEL = 0x02;
    public static final byte XINPUT_DEVSUBTYPE_ARCADE_STICK = 0x03;
    public static final byte XINPUT_DEVSUBTYPE_FLIGHT_STICK = 0x04;
    public static final byte XINPUT_DEVSUBTYPE_DANCE_PAD = 0x05;
    public static final byte XINPUT_DEVSUBTYPE_GUITAR = 0x06;
    public static final byte XINPUT_DEVSUBTYPE_GUITAR_ALTERNATE = 0x07;
    public static final byte XINPUT_DEVSUBTYPE_DRUM_KIT = 0x08;
    public static final byte XINPUT_DEVSUBTYPE_GUITAR_BASS = 0x0B;
    public static final byte XINPUT_DEVSUBTYPE_ARCADE_PAD = 0x13;

    public static final byte BATTERY_DEVTYPE_GAMEPAD = 0x00;
    public static final byte BATTERY_DEVTYPE_HEADSET = 0x01;

    // Capability flags
    public static final byte XINPUT_CAPS_FFB_SUPPORTED = 0x0001;
    public static final byte XINPUT_CAPS_WIRELESS = 0x0002;
    public static final byte XINPUT_CAPS_VOICE_SUPPORTED = 0x0004;
    public static final byte XINPUT_CAPS_PMD_SUPPORTED = 0x0008;
    public static final byte XINPUT_CAPS_NO_NAVIGATION = 0x0010;

    // Battery types
    public static final byte BATTERY_TYPE_DISCONNECTED = 0x00;// This device is not connected
    public static final byte BATTERY_TYPE_WIRED = 0x01;// Wired device, no battery
    public static final byte BATTERY_TYPE_ALKALINE = 0x02;// Alkaline battery source
    public static final byte BATTERY_TYPE_NIMH = 0x03;// Nickel Metal Hydride battery source
    public static final byte BATTERY_TYPE_UNKNOWN = (byte) 0xFF;// Cannot determine the battery type

    // Battery levels
    public static final byte BATTERY_LEVEL_EMPTY = 0x00;
    public static final byte BATTERY_LEVEL_LOW = 0x01;
    public static final byte BATTERY_LEVEL_MEDIUM = 0x02;
    public static final byte BATTERY_LEVEL_FULL = 0x03;

    // Keystroke flags
    public static final short XINPUT_KEYSTROKE_KEYDOWN = 0x0001;
    public static final short XINPUT_KEYSTROKE_KEYUP = 0x0002;
    public static final short XINPUT_KEYSTROKE_REPEAT = 0x0004;

    // Device flags
    public static final int XINPUT_FLAG_GAMEPAD = 0x00000001;

    // -------------
    // Windows

    // Error codes
    public static final int ERROR_SUCCESS = 0;
    public static final int ERROR_EMPTY = 4306;
    public static final int ERROR_DEVICE_NOT_CONNECTED = 1167;
}
