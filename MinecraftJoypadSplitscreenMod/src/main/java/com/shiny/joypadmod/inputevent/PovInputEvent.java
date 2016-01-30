package com.shiny.joypadmod.inputevent;

import org.lwjgl.input.Controllers;

public class PovInputEvent extends ControllerInputEvent
{
	int povNumber;

	public PovInputEvent(int controllerId, int povNumber, float threshold)
	{
		// check if valid povNumber
		super(EventType.POV, controllerId, povNumber, threshold, 0);
		this.povNumber = povNumber;
	}

	@Override
	protected boolean isTargetEvent()
	{
		if (povNumber == 0)
			return Controllers.isEventPovX();
		return Controllers.isEventPovY();
	}

	@Override
	public float getAnalogReading()
	{
		return povNumber == 0 ? Controllers.getController(controllerNumber).getPovX() : Controllers.getController(
				controllerNumber).getPovY();
	}

	@Override
	public String getName()
	{
		return povNumber == 0 ? "POV X" : "POV Y";
	}

	@Override
	public String toString()
	{
		return new StringBuilder().append("Event: ").append(getName()).append(" Type: ").append(getEventType()).append(
				" Max Value: ").append(threshold).append(" Current value: ").append(getAnalogReading()).append(
				" Is pressed: ").append(isPressed()).toString();
	}

	@Override
	public String getDescription()
	{
		return new StringBuilder().append(getName()).append(" ").append(getDirection(getThreshold())).toString();
	}

	private String getDirection(float reading)
	{
		if (reading == 0)
			return "";
		if (reading > 0)
		{
			return "+";
		}
		return "-";
	}

	@Override
	public boolean isValid()
	{
		return povNumber == 0 || povNumber == 1;
	}

}
