package com.shiny.joypadmod.devices;

import com.github.strikerx3.jxinput.XInputBatteryInformation;
import com.github.strikerx3.jxinput.XInputButtons;
import com.github.strikerx3.jxinput.XInputDevice;
import com.github.strikerx3.jxinput.XInputDevice14;
import com.github.strikerx3.jxinput.enums.XInputAxis;
import com.github.strikerx3.jxinput.enums.XInputBatteryDeviceType;
import com.github.strikerx3.jxinput.enums.XInputButton;
import com.github.strikerx3.jxinput.exceptions.XInputNotLoadedException;
import com.shiny.joypadmod.JoypadMod;

public class XInputDeviceWrapper extends InputDevice {

    public XInputDevice theDevice;
    public Boolean xInput14 = false;

    float[] deadZones = new float[]{0.15f, 0.15f, 0.15f, 0.15f, 0.15f, 0.15f};

    public XInputDeviceWrapper(int index) {
        super(index);

    }

    @Override
    public String getName() {
        String name = "XInput Device";
        return name;
    }

    @Override
    public int getButtonCount() {
        return 15;
    }

    @Override
    public int getAxisCount() {
        return 6;
    }

    @Override
    public float getAxisValue(int axisIndex) {
        float value = theDevice.getComponents().getAxes().get(XInputAxis.values()[axisIndex]);

        if (Math.abs(value) > deadZones[axisIndex]) {
            XInputAxis axis = XInputAxis.values()[axisIndex];
            if (axis == XInputAxis.LEFT_THUMBSTICK_Y || axis == XInputAxis.RIGHT_THUMBSTICK_Y)
                value *= -1;

            return value;
        }

        return 0;
    }

    @Override
    public String getAxisName(int index) {

        String name = XInputAxis.values()[index].toString();
        String ret;
        if (name.length() > 11) {
            ret = String.format("%s %s", name.substring(0, 9), name.charAt(name.length() - 1));
        } else
            ret = name;
        return ret;
    }

    @Override
    public float getDeadZone(int index) {
        return deadZones[index];
    }

    @Override
    public String getButtonName(int index) {
        return XInputButton.values()[index].toString();
    }

    @Override
    public Boolean isButtonPressed(int index) {

        return isPressed(XInputButton.values()[index], theDevice.getComponents().getButtons());
    }

    @Override
    public Float getPovX() {

        if (theDevice.getDelta().getButtons().isPressed(XInputButton.DPAD_LEFT))
            return -1.0f;
        if (theDevice.getDelta().getButtons().isPressed(XInputButton.DPAD_RIGHT))
            return 1.0f;
        return 0f;
    }

    @Override
    public Float getPovY() {
        if (theDevice.getDelta().getButtons().isPressed(XInputButton.DPAD_UP))
            return 1.0f;
        if (theDevice.getDelta().getButtons().isPressed(XInputButton.DPAD_DOWN))
            return -1.0f;
        return 0f;
    }

    @Override
    public void setDeadZone(int axisIndex, float value) {
        deadZones[axisIndex] = value;
    }

    protected void setIndex(int index, Boolean useXInput14) {
        try {

            if (useXInput14) {
                xInput14 = true;
                theDevice = XInputDevice14.getDeviceFor(index);
            } else
                theDevice = XInputDevice.getDeviceFor(index);
            myIndex = index;
            theDevice.poll();
        } catch (XInputNotLoadedException e) {
            JoypadMod.logger.fatal("Failed calling setIndex on XInputDevice: " + e.toString());
        }
    }

    @Override
    public Boolean isConnected() {
        return theDevice.isConnected();
    }

    @Override
    public int getBatteryLevel() {
        try {
            XInputBatteryInformation gamepadBattInfo = ((XInputDevice14) theDevice).getBatteryInformation(XInputBatteryDeviceType.GAMEPAD);

            return gamepadBattInfo.getLevel().ordinal();
        } catch (Exception ex) {
            return -1;
        }
    }


    public Boolean isPressed(XInputButton buttonToCheck, XInputButtons buttons) {
        switch (buttonToCheck) {
            case A:
                return buttons.a;
            case B:
                return buttons.b;
            case X:
                return buttons.x;
            case Y:
                return buttons.y;
            case BACK:
                return buttons.back;
            case START:
                return buttons.start;
            case LEFT_SHOULDER:
                return buttons.lShoulder;
            case RIGHT_SHOULDER:
                return buttons.rShoulder;
            case LEFT_THUMBSTICK:
                return buttons.lThumb;
            case RIGHT_THUMBSTICK:
                return buttons.rThumb;
            case DPAD_UP:
                return buttons.up;
            case DPAD_DOWN:
                return buttons.down;
            case DPAD_LEFT:
                return buttons.left;
            case DPAD_RIGHT:
                return buttons.right;
            case GUIDE_BUTTON:
                return buttons.guide;

            default:
                return false;

        }
    }
}
