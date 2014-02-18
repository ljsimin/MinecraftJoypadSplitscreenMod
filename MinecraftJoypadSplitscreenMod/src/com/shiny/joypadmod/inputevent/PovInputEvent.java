package com.shiny.joypadmod.inputevent;

import org.lwjgl.input.Controllers;

public class PovInputEvent implements ControllerInputEvent
{

	int controllerNumber;
	int povNumber;
	float threshold;

	public PovInputEvent(int controllerId, int povNumber, float threshold)
	{
		this.controllerNumber = controllerId;
		this.povNumber = povNumber;
		this.threshold = threshold;
	}

	@Override
	public EventType getEventType()
	{
		return EventType.POV;
	}

	@Override
	public boolean isPressed()
	{
		if (threshold > 0)
		{
			return getAnalogReading() >= threshold;
		}
		return getAnalogReading() <= threshold;
	}

	@Override
	public boolean wasPressed()
	{
		boolean isValidEventType = false;
		try
		{
			isValidEventType = Controllers.isEventPovX() || Controllers.isEventPovY();
		}
		catch (NullPointerException e)
		{
			// Can't determine the event type
		}
		if (isValidEventType)
		{
			if (threshold > 0)
			{
				return getAnalogReading() >= threshold ? true : false;
			}
			return getAnalogReading() <= threshold ? true : false;
		}
		return false;
	}

	@Override
	public float getAnalogReading()
	{
		return povNumber == 0 ? Controllers.getController(controllerNumber).getPovX() : Controllers.getController(
				controllerNumber).getPovY();
	}

	@Override
	public float getDeadZone()
	{
		return 0;
	}

	@Override
	public String getName()
	{
		return povNumber == 0 ? "POV X" : "POV Y";
	}

	@Override
	public String toString()
	{
		return new StringBuilder().append("Event: ").append(getName()).append(" Type: ").append(getEventType())
				.append(" Max Value: ").append(threshold).append(" Current value: ").append(getAnalogReading())
				.append(" Is pressed: ").append(isPressed()).toString();
	}

	@Override
	public int getEventIndex()
	{
		return povNumber;
	}

	@Override
	public float getThreshold()
	{
		return threshold;
	}

	@Override
	public void setDeadZone()
	{
		// nop
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
	public int getControllerIndex()
	{
		return controllerNumber;
	}

	@Override
	public String toConfigFileString()
	{
		String s = getEventType().toString() + "," + getEventIndex() + "," + getThreshold() + ",0";
		return s;
	}

}
