package com.shiny.joypadmod.inputevent;

public class ControllerBinding
{

	/**
	 * Used as a key for the save file
	 */
	public String inputString;

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

	public ControllerBinding(String inputString, ControllerInputEvent inputEvent)
	{
		this.inputString = inputString;
		this.inputEvent = inputEvent;
	}

}
