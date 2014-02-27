package com.shiny.joypadmod.inputevent;

import org.lwjgl.input.Controllers;

import com.shiny.joypadmod.helpers.LogHelper;

/**
 * Input event that encapsulates the button press, pov and axis movement
 * 
 * @author shiny
 * 
 */
public abstract class ControllerInputEvent
{
	protected final EventType type;
	protected final int controllerNumber;
	protected final int buttonNumber;
	protected float threshold;
	protected float deadzone;
	// some controllers have buttons/axis lit up until they are moved/pressed.
	// we want to limit some of these from returning ispressed() until an actual event arrives
	protected boolean pressEventReceived;

	public enum EventType
	{
		BUTTON, AXIS, POV
	}

	public ControllerInputEvent(EventType type, int controllerNumber, int buttonNumber, float threshold, float deadzone)
	{
		this.type = type;
		this.controllerNumber = controllerNumber;
		this.buttonNumber = buttonNumber;
		this.threshold = threshold;
		this.deadzone = deadzone;
		pressEventReceived = false;
	}

	// subclasses will check the controller event to see if it matches their subclass type
	protected abstract boolean isTargetEvent();

	// check if axis or button number is valid
	protected abstract boolean isValid();

	public abstract float getAnalogReading();

	public abstract String getName();

	public abstract String getDescription();

	public EventType getEventType()
	{
		return type;
	}

	public boolean isPressed()
	{
		// this block will go straight to meetsThreshold after a single event at least has been received from Controllers
		if (!pressEventReceived && !wasPressedRaw())
			return false;

		return meetsThreshold();
	}

	public boolean wasPressed()
	{
		if (!isValid())
			return false;

		return wasPressedRaw() && meetsThreshold();
	}

	// just checks the event to see if it matches, not using threshold values
	public boolean wasPressedRaw()
	{
		try
		{
			if (Controllers.getEventSource().getIndex() == controllerNumber
					&& Controllers.getEventControlIndex() == buttonNumber && isTargetEvent())
			{
				pressEventReceived = true;
				return true;
			}
		}
		catch (Exception ex)
		{
			LogHelper.Info("Trying to call wasPressed on controllerInput that hasn't been triggered yet. Ignoring.");
		}

		return false;
	}

	protected boolean meetsThreshold()
	{
		return threshold > 0 ? getAnalogReading() >= threshold : getAnalogReading() <= threshold;
	}

	public int getControllerIndex()
	{
		return controllerNumber;
	}

	public int getEventIndex()
	{
		return buttonNumber;
	}

	public float getThreshold()
	{
		return threshold;
	}

	public void setThreshold(float f)
	{
		threshold = f;
	}

	public float getDeadZone()
	{
		return deadzone;
	}

	public void setDeadZone(float f)
	{}

	public String toConfigFileString()
	{
		String s = getEventType().toString() + "," + getEventIndex() + "," + getThreshold() + "," + getDeadZone();
		return s;
	}
}
