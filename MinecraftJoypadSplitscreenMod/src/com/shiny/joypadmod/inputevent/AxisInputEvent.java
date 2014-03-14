package com.shiny.joypadmod.inputevent;

import org.lwjgl.input.Controllers;

import com.shiny.joypadmod.helpers.LogHelper;

public class AxisInputEvent extends ControllerInputEvent
{
	int axisNumber;

	boolean pressed = false; // used for input limiting

	public AxisInputEvent(int controllerId, int axisNumber, float threshold, float deadzone)
	{
		// check if valid axis
		super(EventType.AXIS, controllerId, axisNumber, threshold, deadzone);
		this.axisNumber = axisNumber;
		if (isValid())
		{
			this.setDeadZone(deadzone);
		}
		else
		{
			LogHelper.Error("Attempted to create a binding with invalid axis number. Axis index requested: "
					+ axisNumber + " axis available: " + Controllers.getController(controllerNumber).getAxisCount());
			LogHelper.Warn("Processing will continue with invalid axis " + axisNumber
					+ ".  Binding will not respond until rebound and may cause instability in mod.");
		}

	}

	@Override
	protected boolean isTargetEvent()
	{
		return Controllers.isEventAxis();
	}

	@Override
	public float getAnalogReading()
	{
		if (!isValid())
			return 0;
		return Controllers.getController(controllerNumber).getAxisValue(axisNumber);
	}

	@Override
	public float getDeadZone()
	{
		if (!isValid())
			return 0;
		return Controllers.getController(controllerNumber).getDeadZone(axisNumber);
	}

	@Override
	public String getName()
	{
		if (!isValid())
			return "Invalid";
		return Controllers.getController(controllerNumber).getAxisName(axisNumber);
	}

	@Override
	public String toString()
	{
		if (!isValid())
			return "Invalid";
		return new StringBuilder().append("Event: ").append(getName()).append(" Type: ").append(getEventType()).append(
				" Threshold: ").append(threshold).append(" Current value: ").append(getAnalogReading()).append(
				" Is pressed: ").append(isPressed()).toString();
	}

	@Override
	public void setDeadZone(float deadzone)
	{
		if (!isValid())
			return;
		LogHelper.Info("Setting deadzone on controller " + controllerNumber + " axis " + this.axisNumber + " value "
				+ deadzone);
		Controllers.getController(controllerNumber).setDeadZone(this.axisNumber, deadzone);
		this.deadzone = deadzone;
	}

	@Override
	public String getDescription()
	{
		if (!isValid())
			return "Not set";

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
		return axisNumber >= 0 && axisNumber < Controllers.getController(controllerNumber).getAxisCount();
	}
}
