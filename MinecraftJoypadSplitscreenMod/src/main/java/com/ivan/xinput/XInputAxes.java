package com.ivan.xinput;

import com.ivan.xinput.enums.XInputAxis;

/**
 * Contains the states of all XInput axes.
 *
 * @author Ivan "StrikerX3" Oliveira
 */
public class XInputAxes {
    public int lxRaw, lyRaw;
    public int rxRaw, ryRaw;

    public int ltRaw, rtRaw;

    public float lx, ly;
    public float rx, ry;

    public float lt, rt;

    public int dpad;

    public static final int DPAD_CENTER = -1;
    public static final int DPAD_UP_LEFT = 0;
    public static final int DPAD_UP = 1;
    public static final int DPAD_UP_RIGHT = 2;
    public static final int DPAD_RIGHT = 3;
    public static final int DPAD_DOWN_RIGHT = 4;
    public static final int DPAD_DOWN = 5;
    public static final int DPAD_DOWN_LEFT = 6;
    public static final int DPAD_LEFT = 7;

    protected XInputAxes() {
        reset();
    }

    /**
     * Gets the value from the specified axis.
     *
     * @param axis the axis
     * @return the value of the axis
     */
    public float get(final XInputAxis axis) {
        switch (axis) {
            case LEFT_THUMBSTICK_X:
                return lx;
            case LEFT_THUMBSTICK_Y:
                return ly;
            case RIGHT_THUMBSTICK_X:
                return rx;
            case RIGHT_THUMBSTICK_Y:
                return ry;
            case LEFT_TRIGGER:
                return lt;
            case RIGHT_TRIGGER:
                return rt;
            case DPAD:
                return dpad;
            default:
                return 0f;
        }
    }

    /**
     * Gets the raw value from the specified axis.
     *
     * @param axis the axis
     * @return the rawvalue of the axis
     */
    public int getRaw(final XInputAxis axis) {
        switch (axis) {
            case LEFT_THUMBSTICK_X:
                return lxRaw;
            case LEFT_THUMBSTICK_Y:
                return lyRaw;
            case RIGHT_THUMBSTICK_X:
                return rxRaw;
            case RIGHT_THUMBSTICK_Y:
                return ryRaw;
            case LEFT_TRIGGER:
                return ltRaw;
            case RIGHT_TRIGGER:
                return rtRaw;
            case DPAD:
                return dpad;
            default:
                return 0;
        }
    }

    /**
     * Resets the state of all axes.
     */
    protected void reset() {
        lxRaw = lyRaw = 0;
        rxRaw = ryRaw = 0;

        ltRaw = rtRaw = 0;

        lx = ly = 0f;
        rx = ry = 0f;

        lt = rt = 0f;

        dpad = DPAD_CENTER;
    }

    /**
     * Copies the state of all axes from the specified state.
     *
     * @param axes the state to copy from
     */
    protected void copy(final XInputAxes axes) {
        lxRaw = axes.lxRaw;
        lyRaw = axes.lyRaw;

        rxRaw = axes.rxRaw;
        ryRaw = axes.ryRaw;

        ltRaw = axes.ltRaw;
        rtRaw = axes.rtRaw;

        lx = axes.lx;
        ly = axes.ly;

        rx = axes.rx;
        ry = axes.ry;

        lt = axes.lt;
        rt = axes.rt;

        dpad = axes.dpad;
    }

    /**
     * Returns an integer representing the current direction of the D-Pad.
     *
     * @param up the up button state
     * @param down the down button state
     * @param left the left button state
     * @param right the right button state
     * @return one of the <code>DPAD_*</code> values of this class
     */
    public static int dpadFromButtons(final boolean up, final boolean down, final boolean left, final boolean right) {
        boolean u = up;
        boolean d = down;
        boolean l = left;
        boolean r = right;

        // Fix invalid buttons (cancel up-down and left-right)
        if (u && d) {
            u = d = false;
        }
        if (l && r) {
            l = r = false;
        }

        // Now we have 9 cases:
        //         left             center        right
        // up      DPAD_UP_LEFT     DPAD_UP       DPAD_UP_RIGHT
        // center  DPAD_LEFT        DPAD_CENTER   DPAD_RIGHT
        // down    DPAD_DOWN_LEFT   DPAD_DOWN     DPAD_DOWN_RIGHT

        if (u) {
            if (l) {
                return DPAD_UP_LEFT;
            }
            if (r) {
                return DPAD_UP_RIGHT;
            }
            return DPAD_UP;
        }
        if (d) {
            if (l) {
                return DPAD_DOWN_LEFT;
            }
            if (r) {
                return DPAD_DOWN_RIGHT;
            }
            return DPAD_DOWN;
        }
        // vertical center
        if (l) {
            return DPAD_LEFT;
        }
        if (r) {
            return DPAD_RIGHT;
        }
        return DPAD_CENTER;
    }
}
