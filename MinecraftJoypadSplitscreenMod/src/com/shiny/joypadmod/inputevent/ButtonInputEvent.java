package com.shiny.joypadmod.inputevent;

import org.lwjgl.input.Controllers;

public class ButtonInputEvent extends ControllerInputEvent
{
	public ButtonInputEvent(int controllerNumber, int buttonNumber, float threshold)
	{
		super(EventType.BUTTON, controllerNumber, buttonNumber, threshold, 0);
	}

	@Override
	protected boolean isTargetEvent()
	{
		return Controllers.isEventButton();
	}

	@Override
	public float getAnalogReading()
	{
		if (Controllers.getController(controllerNumber).isButtonPressed(buttonNumber))
		{
			return 1.0f;
		}

		return 0f;
	}

	@Override
	public String getName()
	{
		if (!isValid())
			return "Invalid";
		return Controllers.getController(controllerNumber).getButtonName(buttonNumber);
	}

	@Override
	public String toString()
	{
		return new StringBuilder().append("Event: ").append(getName()).append(" Type: ").append(getEventType()).append(
				" Current value: ").append(getAnalogReading()).append(" Is pressed: ").append(isPressed()).toString();
	}

	@Override
	public String getDescription()
	{
		return getName();
	}

	@Override
	protected boolean isValid()
	{
		return buttonNumber < Controllers.getController(controllerNumber).getButtonCount();
	}
}
