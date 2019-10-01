package com.shiny.joypadmod.event;

import com.shiny.joypadmod.ControllerSettings;

public class PovInputEvent extends ControllerInputEvent {
    int povNumber;

    public PovInputEvent(int controllerId, int povNumber, float threshold) {
        // check if valid povNumber
        super(EventType.POV, controllerId, povNumber, threshold, 0);
        this.povNumber = povNumber;
    }

    @Override
    protected boolean isTargetEvent() {
        if (povNumber == 0)
            return ControllerSettings.JoypadModInputLibrary.isEventPovX();
        return ControllerSettings.JoypadModInputLibrary.isEventPovY();
    }

    @Override
    public float getAnalogReading() {
        return povNumber == 0 ? ControllerSettings.JoypadModInputLibrary.getController(controllerNumber).getPovX() : ControllerSettings.JoypadModInputLibrary.getController(
                controllerNumber).getPovY();
    }

    @Override
    public String getName() {
        return povNumber == 0 ? "POV X" : "POV Y";
    }

    @Override
    public String toString() {
        return "Event: " + getName() + " Type: " + getEventType() +
                " Max Value: " + threshold + " Current value: " + getAnalogReading() +
                " Is pressed: " + isPressed();
    }

    @Override
    public String getDescription() {
        return getName() + " " + getDirection(getThreshold());
    }

    private String getDirection(float reading) {
        if (reading == 0)
            return "";
        if (reading > 0) {
            return "+";
        }
        return "-";
    }

    @Override
    public boolean isValid() {
        return povNumber == 0 || povNumber == 1;
    }

}
