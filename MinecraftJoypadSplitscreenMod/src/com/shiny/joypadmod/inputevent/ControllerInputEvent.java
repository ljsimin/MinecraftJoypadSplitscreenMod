package com.shiny.joypadmod.inputevent;

/**
 * Input event that encapsulates the button press, pov and axis movement
 * 
 * @author shiny
 * 
 */
public interface ControllerInputEvent
{

	public enum EventType
	{
		BUTTON, AXIS, POV
	}

	public EventType getEventType();

	public boolean isPressed();

	boolean wasPressed();

	public float getAnalogReading();

	public float getDeadZone();

	public void setDeadZone();

	public String getName();

	public int getEventIndex();

	public int getControllerIndex();

	public float getThreshold();

	public String getDescription();

	public String toConfigFileString();
}
