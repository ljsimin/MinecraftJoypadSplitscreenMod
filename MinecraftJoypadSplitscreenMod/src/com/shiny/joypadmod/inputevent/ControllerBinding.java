package com.shiny.joypadmod.inputevent;

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
		String s = menuString + "," + inputEvent.toConfigFileString();
		return s;
	}

	// returns boolean - whether the input string was accepted and bound
	public boolean setToConfigFileString(String s, int joyNo)
	{
		if (s == null)
			return false;

		if (this.toConfigFileString().equalsIgnoreCase(s))
			return true;

		LogHelper.Info("setToConfigFileString called with following values: " + s);

		// TODO, verify using regex
		String[] settings = s.split(",");
		int minToProcess = 5;
		if (settings.length < minToProcess)
		{
			LogHelper.Error("Expected " + minToProcess + " arguments when parsing config setting: \"" + s
					+ "\" Received " + settings.length);
			return false;
		}

		ControllerInputEvent.EventType event;
		int eventIndex;
		float threshold;
		float deadzone;

		try
		{
			event = ControllerInputEvent.EventType.valueOf(settings[1]);
			eventIndex = Integer.parseInt(settings[2]);
			threshold = Float.parseFloat(settings[3]);
			deadzone = Float.parseFloat(settings[4]);
		}
		catch (Exception ex)
		{
			LogHelper.Error("Failed parsing string: " + s + " Exception: " + ex.toString());
			return false;
		}

		try
		{
			this.menuString = settings[0];

			if (event == EventType.BUTTON)
			{
				this.inputEvent = new ButtonInputEvent(joyNo, eventIndex);
			}
			else if (event == EventType.POV)
			{
				this.inputEvent = new PovInputEvent(joyNo, eventIndex, threshold);
			}
			else if (event == EventType.AXIS)
			{
				this.inputEvent = new AxisInputEvent(joyNo, eventIndex, threshold, deadzone);
			}
		}
		catch (Exception ex)
		{
			LogHelper.Error("Failed setting bindings using config string: " + s + ". Exception: " + ex.toString());
			return false;
		}

		return true;

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
