package com.shiny.joypadmod.inputevent;

import scala.NotImplementedError;

import com.shiny.joypadmod.helpers.LogHelper;
import com.shiny.joypadmod.inputevent.ControllerInputEvent.EventType;

public class ControllerBinding
{

	/**
	 * Used as a key for the save file
	 */
	public String inputString;
	public String menuString;

	public ControllerInputEvent inputEvent;

	public boolean isPressed()
	{
		return inputEvent.isPressed();
	}

	public boolean wasPressed()
	{
		return inputEvent.wasPressed();
	}

	public float getAnalogReading()
	{
		return inputEvent.getAnalogReading();
	}

	public ControllerBinding(String inputString, String menuString, ControllerInputEvent inputEvent)
	{
		this.inputString = inputString;
		this.menuString = menuString;
		this.inputEvent = inputEvent;
	}

	public String toConfigFileString()
	{
		String s = menuString + "," + inputEvent.getEventType() + "," + inputEvent.getEventIndex();
		// TODO add axisThreshold, axisDeadzone
		return s;
	}

	public void setToConfigFileString(String s, int joyNo)
	{
		if (s == null || this.toConfigFileString().equalsIgnoreCase(s))
			return;

		String[] settings = s.split(",");
		int numToProcess = 3;
		if (settings.length != numToProcess)
		{
			LogHelper.Error("Expected 3 arguments when parsing config setting: \"" + s + "\" Received "
					+ settings.length);
			return;
		}

		ControllerInputEvent.EventType event;
		int eventIndex;

		try
		{
			event = ControllerInputEvent.EventType.valueOf(settings[1]);
			eventIndex = Integer.parseInt(settings[2]);
		}
		catch (Exception ex)
		{
			LogHelper.Error("Failed parsing string: " + s + " Exception: " + ex.toString());
			return;
		}

		if (event == EventType.BUTTON)
		{
			this.menuString = settings[0];
			this.inputEvent = new ButtonInputEvent(this.inputEvent.getControllerIndex(), eventIndex);
		}
		else
		{
			throw new NotImplementedError();
		}

	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass())
		{
			return false;
		}

		ControllerBinding bind = (ControllerBinding) obj;

		if (this.inputString != bind.inputString
				|| this.inputEvent.getControllerIndex() != bind.inputEvent.getControllerIndex()
				|| !this.toConfigFileString().equals(bind.toConfigFileString()))
		{
			return false;
		}

		return true;
	}

}
