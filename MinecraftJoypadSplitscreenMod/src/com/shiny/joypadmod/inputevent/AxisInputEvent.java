package com.shiny.joypadmod.inputevent;

import org.lwjgl.input.Controllers;

public class AxisInputEvent extends ControllerInputEvent
{
	int axisNumber;

	boolean pressed = false; // used for input limiting

	public AxisInputEvent(int controllerId, int axisNumber, float threshold, float deadzone)
	{
		// check if valid axis
		super(EventType.AXIS, controllerId, axisNumber, threshold, deadzone);
		this.axisNumber = axisNumber;
		this.setDeadZone(deadzone);
	}

	@Override
	protected boolean isTargetEvent()
	{
		return Controllers.isEventAxis();
	}

	@Override
	public float getAnalogReading()
	{
		return Controllers.getController(controllerNumber).getAxisValue(axisNumber);
	}

	@Override
	public float getDeadZone()
	{
		return Controllers.getController(controllerNumber).getDeadZone(axisNumber);
	}

	@Override
	public String getName()
	{
		return Controllers.getController(controllerNumber).getAxisName(axisNumber);
	}

	@Override
	public String toString()
	{
		return new StringBuilder().append("Event: ").append(getName()).append(" Type: ").append(getEventType()).append(
				" Threshold: ").append(threshold).append(" Current value: ").append(getAnalogReading()).append(
				" Is pressed: ").append(isPressed()).toString();
	}

	@Override
	public void setDeadZone(float deadzone)
	{
		Controllers.getController(controllerNumber).setDeadZone(this.axisNumber, deadzone);
		this.deadzone = deadzone;
	}

	@Override
	public String getDescription()
	{
		return new StringBuilder().append(getName()).append(" ").append(getDirection(getThreshold())).toString();
	}

	private String getDirection(float reading)
	{
		if (reading > 0)
		{
			return "+";
		}
		return "-";
	}

	@Override
	protected boolean isValid()
	{
		return axisNumber < Controllers.getController(controllerNumber).getAxisCount();
	}
}
