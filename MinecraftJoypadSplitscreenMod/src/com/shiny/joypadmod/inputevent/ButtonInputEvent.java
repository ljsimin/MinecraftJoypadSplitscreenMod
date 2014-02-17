package com.shiny.joypadmod.inputevent;

import org.lwjgl.input.Controllers;

public class ButtonInputEvent implements ControllerInputEvent
{

	private final int controllerNumber;

	private final int buttonNumber;

	public ButtonInputEvent(int controllerNumber, int buttonNumber)
	{
		this.controllerNumber = controllerNumber;
		this.buttonNumber = buttonNumber;
	}

	@Override
	public EventType getEventType()
	{
		return EventType.BUTTON;
	}

	@Override
	public boolean isPressed()
	{
		if (buttonNumber < Controllers.getController(controllerNumber).getButtonCount())
		{
			Controllers.getController(controllerNumber).poll();
			return (Controllers.getController(controllerNumber).isButtonPressed(buttonNumber));
		}

		return false;
	}

	@Override
	public boolean wasPressed()
	{
		boolean isValidEventType = true;
		try
		{
			isValidEventType = Controllers.isEventButton();
		}
		catch (NullPointerException e)
		{
			// cant determine event type
		}
		if (isValidEventType)
		{
			return (Controllers.getController(controllerNumber).isButtonPressed(buttonNumber));
		}
		return false;
	}

	@Override
	public float getAnalogReading()
	{
		return isPressed() ? 1.0f : 0.0f;
	}

	@Override
	public float getDeadZone()
	{
		return 0f;
	}

	@Override
	public String getName()
	{
		if (buttonNumber < Controllers.getController(controllerNumber).getButtonCount())
		{
			return Controllers.getController(controllerNumber).getButtonName(buttonNumber);
		}
		return "Unknown";
	}

	@Override
	public String toString()
	{
		return new StringBuilder().append("Event: ").append(getName()).append(" Type: ").append(getEventType())
				.append(" Current value: ").append(getAnalogReading()).append(" Is pressed: ").append(isPressed())
				.toString();
	}

	@Override
	public int getEventIndex()
	{
		return buttonNumber;
	}

	@Override
	public float getThreshold()
	{
		return 0;
	}

	@Override
	public void setDeadZone()
	{
		// nop
	}

	@Override
	public String getDescription()
	{
		return getName();
	}

	@Override
	public int getControllerIndex()
	{
		return controllerNumber;
	}
}
