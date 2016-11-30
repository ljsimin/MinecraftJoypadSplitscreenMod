package com.ivan.xinput;

import com.ivan.xinput.enums.XInputAxis;

/**
 * Represents the delta (change) of the axes between two successive polls.
 *
 * @author Ivan "StrikerX3" Oliveira
 */
public class XInputAxesDelta {
    private final XInputAxes lastAxes;
    private final XInputAxes axes;

    protected XInputAxesDelta(final XInputAxes lastAxes, final XInputAxes axes) {
        this.lastAxes = lastAxes;
        this.axes = axes;
    }

    /**
     * Returns the difference of the Left Thumb X axis between two consecutive polls. A positive value means the stick moved
     * to the right, while a negative value represents a movement to the left.
     *
     * @return the delta of the Left Thumb X axis
     */
    public float getLXDelta() {
        return lastAxes.lx - axes.lx;
    }

    /**
     * Returns the difference of the Left Thumb Y axis between two consecutive polls. A positive value means the stick moved
     * up, while a negative value represents a down movement.
     *
     * @return the delta of the Left Thumb Y axis
     */
    public float getLYDelta() {
        return lastAxes.ly - axes.ly;
    }

    /**
     * Returns the difference of the Right Thumb X axis between two consecutive polls. A positive value means the stick moved
     * to the right, while a negative value represents a movement to the left.
     *
     * @return the delta of the Right Thumb X axis
     */
    public float getRXDelta() {
        return lastAxes.rx - axes.rx;
    }

    /**
     * Returns the difference of the Right Thumb Y axis between two consecutive polls. A positive value means the stick moved
     * up, while a negative value represents a down movement.
     *
     * @return the delta of the Right Thumb Y axis
     */
    public float getRYDelta() {
        return lastAxes.ry - axes.ry;
    }

    /**
     * Returns the difference of the Left Trigger axis between two consecutive polls. A positive value means the trigger was
     * pressed, while a negative value indicates that the trigger was released.
     *
     * @return the delta of the Left Trigger axis
     */
    public float getLTDelta() {
        return lastAxes.lt - axes.lt;
    }

    /**
     * Returns the difference of the Right Trigger axis between two consecutive polls. A positive value means the trigger was
     * pressed, while a negative value indicates that the trigger was released.
     *
     * @return the delta of the Right Trigger axis
     */
    public float getRTDelta() {
        return lastAxes.rt - axes.rt;
    }

    /**
     * Returns the difference of the raw value of the Left Thumb X axis between two consecutive polls. A positive value
     * means the stick moved to the right, while a negative value represents a movement to the left.
     *
     * @return the delta of the raw value of the Left Thumb X axis
     */
    public int getLXRawDelta() {
        return lastAxes.lxRaw - axes.lxRaw;
    }

    /**
     * Returns the difference of the raw value of the Left Thumb Y axis between two consecutive polls. A positive value means
     * the stick moved up, while a negative value represents a downward movement.
     *
     * @return the delta of the raw value of the Left Thumb Y axis
     */
    public int getLYRawDelta() {
        return lastAxes.lyRaw - axes.lyRaw;
    }

    /**
     * Returns the difference of the raw value of the Right Thumb X axis between two consecutive polls. A positive value
     * means the stick moved to the right, while a negative value represents a movement to the left.
     *
     * @return the delta of the raw value of the Right Thumb X axis
     */
    public int getRXRawDelta() {
        return lastAxes.rxRaw - axes.rxRaw;
    }

    /**
     * Returns the difference of the raw value of the Right Thumb Y axis between two consecutive polls. A positive value
     * means the stick moved up, while a negative value represents a downward movement.
     *
     * @return the delta of the raw value of the Right Thumb Y axis
     */
    public int getRYRawDelta() {
        return lastAxes.ryRaw - axes.ryRaw;
    }

    /**
     * Returns the difference of the raw value of the Left Trigger axis between two consecutive polls. A positive value means
     * the trigger was pressed, while a negative value indicates that the trigger was released.
     *
     * @return the delta of the raw value of the Left Trigger axis
     */
    public int getLTRawDelta() {
        return lastAxes.ltRaw - axes.ltRaw;
    }

    /**
     * Returns the difference of the raw value of the Right Trigger axis between two consecutive polls. A positive value
     * means the trigger was pressed, while a negative value indicates that the trigger was released.
     *
     * @return the delta of the raw value of the Right Trigger axis
     */
    public int getRTRawDelta() {
        return lastAxes.rtRaw - axes.rtRaw;
    }

    /**
     * Returns the difference of the specified axis between two consecutive polls. Refer to the other methods of this class
     * to learn what positive and negative value means for each axis.
     *
     * @param axis the axis the get the delta from
     * @return the delta for the specified axis
     */
    public float getDelta(final XInputAxis axis) {
        switch (axis) {
            case LEFT_THUMBSTICK_X:
                return getRXDelta();
            case LEFT_THUMBSTICK_Y:
                return getLYDelta();
            case RIGHT_THUMBSTICK_X:
                return getRXDelta();
            case RIGHT_THUMBSTICK_Y:
                return getRYDelta();
            case LEFT_TRIGGER:
                return getLTDelta();
            case RIGHT_TRIGGER:
                return getRTDelta();
            default:
                return 0f;
        }
    }

    /**
     * Returns the difference of the specified axis between two consecutive polls. Refer to the other methods of this class
     * to learn what positive and negative value means for each axis.
     *
     * @param axis the axis the get the delta from
     * @return the delta for the specified axis
     */
    public int getRawDelta(final XInputAxis axis) {
        switch (axis) {
            case LEFT_THUMBSTICK_X:
                return getRXRawDelta();
            case LEFT_THUMBSTICK_Y:
                return getLYRawDelta();
            case RIGHT_THUMBSTICK_X:
                return getRXRawDelta();
            case RIGHT_THUMBSTICK_Y:
                return getRYRawDelta();
            case LEFT_TRIGGER:
                return getLTRawDelta();
            case RIGHT_TRIGGER:
                return getRTRawDelta();
            default:
                return 0;
        }
    }
}
