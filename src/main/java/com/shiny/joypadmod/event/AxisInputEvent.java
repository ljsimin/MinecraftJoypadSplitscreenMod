package com.shiny.joypadmod.event;

import com.shiny.joypadmod.ControllerSettings;
import com.shiny.joypadmod.JoypadMod;

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
			if (controllerId < 0)
			{
				JoypadMod.logger.error("Tried to create an axis with invalid controller number");
			}
			else
			{
				JoypadMod.logger.error("Attempted to create a binding with invalid axis number. Axis index requested: "
						+ axisNumber + " axis available: " + ControllerSettings.JoypadModInputLibrary.getController(controllerNumber).getAxisCount());
			}

			JoypadMod.logger.warn("Processing will continue with invalid axis " + axisNumber
					+ ".  Binding will not respond until rebound and may cause instability in mod.");
		}

	}

	@Override
	protected boolean isTargetEvent()
	{
		return ControllerSettings.JoypadModInputLibrary.isEventAxis() && ControllerSettings.JoypadModInputLibrary.getEventControlIndex() == axisNumber;
	}

	@Override
	public float getAnalogReading()
	{
		if (!isValid())
			return 0;
		return ControllerUtils.getAxisValue(ControllerSettings.JoypadModInputLibrary.getController(controllerNumber), axisNumber);
	}

	@Override
	public float getDeadZone()
	{
		if (!isValid())
			return 0;
		return ControllerSettings.JoypadModInputLibrary.getController(controllerNumber).getDeadZone(axisNumber);
	}

	@Override
	public String getName()
	{
		if (!isValid())
			return "Not Set";
		return ControllerSettings.JoypadModInputLibrary.getController(controllerNumber).getAxisName(axisNumber);
	}

	@Override
	public String toString()
	{
		if (!isValid())
			return "Not Set";
		return new StringBuilder().append("Event: ").append(getName()).append(" Type: ").append(getEventType()).append(
				" Threshold: ").append(threshold).append(" Current value: ").append(getAnalogReading()).append(
				" Is pressed: ").append(isPressed()).toString();
	}

	@Override
	public void setDeadZone(float deadzone)
	{
		if (!isValid())
			return;
		JoypadMod.logger.info("Setting deadzone on controller " + controllerNumber + " axis " + this.axisNumber + " value "
				+ deadzone);
		ControllerSettings.JoypadModInputLibrary.getController(controllerNumber).setDeadZone(this.axisNumber, deadzone);
		this.deadzone = deadzone;
	}

	@Override
	public String getDescription()
	{
		if (!isValid())
			return "NONE";

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
	public boolean isValid()
	{
		return controllerNumber >= 0 && axisNumber >= 0
				&& axisNumber < ControllerSettings.JoypadModInputLibrary.getController(controllerNumber).getAxisCount();
	}
}
