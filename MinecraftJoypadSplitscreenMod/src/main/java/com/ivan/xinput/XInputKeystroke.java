package com.ivan.xinput;

import java.nio.ByteBuffer;

import com.ivan.xinput.natives.XInputConstants;
import com.ivan.xinput.natives.XInputVirtualKeyCodes;

/**
 * Represents a keystroke from an XInput device.
 *
 * @author Ivan "StrikerX3" Oliveira
 */
public class XInputKeystroke {
    private final short virtualKey;
    private final char unicode;

    private final boolean keyDown;
    private final boolean keyUp;
    private final boolean repeat;

    private final byte userIndex;
    private final byte hidCode;

    XInputKeystroke(final ByteBuffer buffer) {
        // typedef struct _XINPUT_KEYSTROKE
        // {
        //     WORD    VirtualKey;
        //     WCHAR   Unicode;
        //     WORD    Flags;
        //     BYTE    UserIndex;
        //     BYTE    HidCode;
        // } XINPUT_KEYSTROKE, *PXINPUT_KEYSTROKE;

        virtualKey = buffer.getShort();
        unicode = buffer.getChar();

        final short flags = buffer.getShort();

        keyDown = (flags & XInputConstants.XINPUT_KEYSTROKE_KEYDOWN) != 0;
        keyUp = (flags & XInputConstants.XINPUT_KEYSTROKE_KEYUP) != 0;
        repeat = (flags & XInputConstants.XINPUT_KEYSTROKE_REPEAT) != 0;

        userIndex = buffer.get();
        hidCode = buffer.get();
    }

    /**
     * Retrieves the virtual key code.
     * Virtual key codes are defined in the {@link XInputVirtualKeyCodes} class.
     *
     * @return the virtual key code
     */
    public short getVirtualKey() {
        return virtualKey;
    }

    /**
     * Retrieves the Unicode character.
     *
     * @return the Unicode character
     */
    public char getUnicode() {
        return unicode;
    }

    /**
     * Determines whether this is a key down event.
     *
     * @return <code>true</code> if the key was pressed
     */
    public boolean isKeyDown() {
        return keyDown;
    }

    /**
     * Determines whether this is a key up event.
     *
     * @return <code>true</code> if the key was released
     */
    public boolean isKeyUp() {
        return keyUp;
    }

    /**
     * Determines whether this is a key repeat event.
     *
     * @return <code>true</code> if this is a key repetition
     */
    public boolean isRepeat() {
        return repeat;
    }

    /**
     * Retrieves the user index (a.k.a. player number).
     *
     * @return the user index
     */
    public byte getUserIndex() {
        return userIndex;
    }

    /**
     * Retrieves the HID code.
     *
     * @return the HID code
     */
    public byte getHidCode() {
        return hidCode;
    }
}
