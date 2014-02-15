package com.shiny.joypadmod.inputevent;

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

}
