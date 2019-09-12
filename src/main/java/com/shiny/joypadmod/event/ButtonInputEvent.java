package com.shiny.joypadmod.event;

import com.shiny.joypadmod.ControllerSettings;

public class ButtonInputEvent extends ControllerInputEvent {
    public ButtonInputEvent(int controllerNumber, int buttonNumber, float threshold) {
        super(EventType.BUTTON, controllerNumber, buttonNumber, threshold, 0);
    }

    @Override
    protected boolean isTargetEvent() {
        return ControllerSettings.JoypadModInputLibrary.isEventButton() && ControllerSettings.JoypadModInputLibrary.getEventControlIndex() == buttonNumber;
    }

    @Override
    public float getAnalogReading() {
        if (ControllerSettings.JoypadModInputLibrary.getController(controllerNumber).isButtonPressed(buttonNumber)) {
            return 1.0f;
        }

        return 0f;
    }

    @Override
    public String getName() {
        if (!isValid())
            return "NONE";
        return ControllerSettings.JoypadModInputLibrary.getController(controllerNumber).getButtonName(buttonNumber);
    }

    @Override
    public String toString() {
        return "Event: " + getName() + " Type: " + getEventType() +
                " Current value: " + getAnalogReading() + " Is pressed: " + isPressed();
    }

    @Override
    public String getDescription() {
        return getName();
    }

    @Override
    public boolean isValid() {
        return controllerNumber >= 0 && buttonNumber >= 0
                && buttonNumber < ControllerSettings.JoypadModInputLibrary.getController(controllerNumber).getButtonCount();
    }
}
