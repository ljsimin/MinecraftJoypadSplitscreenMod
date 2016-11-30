package com.ivan.xinput;

import java.nio.ByteBuffer;

/**
 * Contains the resolutions of the proportional axes of an XInput device.
 * Some number of the least significant bits may not be set, indicating that the control does not provide resolution to that level.
 *
 * @author Ivan "StrikerX3" Oliveira
 */
public class XInputCapsResolutions {
    private final byte leftTrigger;
    private final byte rightTrigger;
    private final short thumbLX;
    private final short thumbLY;
    private final short thumbRX;
    private final short thumbRY;

    private final short leftMotorSpeed;
    private final short rightMotorSpeed;

    XInputCapsResolutions(final ByteBuffer buffer) {
        leftTrigger = buffer.get();
        rightTrigger = buffer.get();
        thumbLX = buffer.getShort();
        thumbLY = buffer.getShort();
        thumbRX = buffer.getShort();
        thumbRY = buffer.getShort();

        leftMotorSpeed = buffer.getShort();
        rightMotorSpeed = buffer.getShort();
    }

    /**
     * Retrieves the resolution of the left trigger.
     *
     * @return the resolution of the left trigger
     */
    public byte getLeftTrigger() {
        return leftTrigger;
    }

    /**
     * Retrieves the resolution of the right trigger.
     *
     * @return the resolution of the right trigger
     */
    public byte getRightTrigger() {
        return rightTrigger;
    }

    /**
     * Retrieves the resolution of the left thumbstick's horizontal axis.
     *
     * @return the resolution of the left thumbstick's horizontal axis
     */
    public short getThumbLX() {
        return thumbLX;
    }

    /**
     * Retrieves the resolution of the left thumbstick's vertical axis.
     *
     * @return the resolution of the left thumbstick's vertical axis
     */
    public short getThumbLY() {
        return thumbLY;
    }

    /**
     * Retrieves the resolution of the right thumbstick's horizontal axis.
     *
     * @return the resolution of the right thumbstick's horizontal axis
     */
    public short getThumbRX() {
        return thumbRX;
    }

    /**
     * Retrieves the resolution of the right thumbstick's vertical axis.
     *
     * @return the resolution of the right thumbstick's vertical axis
     */
    public short getThumbRY() {
        return thumbRY;
    }

    /**
     * Retrieves the resolution of the left motor speed.
     *
     * @return the resolution of the left motor speed
     */
    public short getLeftMotorSpeed() {
        return leftMotorSpeed;
    }

    /**
     * Retrieves the resolution of the right motor speed.
     *
     * @return the resolution of the right motor speed
     */
    public short getRightMotorSpeed() {
        return rightMotorSpeed;
    }
}
