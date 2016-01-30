package com.shiny.joypadmod.inputevent;

import org.lwjgl.input.Controllers;

import com.shiny.joypadmod.ControllerSettings;
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
	protected boolean isActive;
	protected boolean wasReleased;
	// switch whether this has ever been pressed in its lifetime
	protected boolean pressedOnce;

	public enum EventType
	{
		BUTTON, AXIS, POV
	}

	public ControllerInputEvent(EventType type, int controllerNumber, int buttonNumber, float threshold, float deadzone)
	{
		LogHelper.Info("ControllerInputEvent constructor params:( type: " + type + ", controllerNumber: "
				+ controllerNumber + ", buttonNumber: " + buttonNumber + ", threshhold: " + threshold + ", deadzone: "
				+ deadzone + ")");
		this.type = type;
		this.controllerNumber = controllerNumber;
		this.buttonNumber = buttonNumber;
		this.threshold = threshold;
		this.deadzone = deadzone;
		isActive = false;
		wasReleased = false;
		pressedOnce = false;
	}

	// subclasses will check the controller event to see if it matches their subclass type
	protected abstract boolean isTargetEvent();

	// check if axis or button number is valid
	public abstract boolean isValid();

	public abstract float getAnalogReading();

	public abstract String getName();

	public abstract String getDescription();

	public EventType getEventType()
	{
		return type;
	}

	public boolean isActive()
	{
		return isActive;
	}

	public boolean pressedOnce()
	{
		return pressedOnce;
	}

	public boolean isPressed()
	{
		if (!isActive || !isValid())
			return false;

		boolean ret = meetsThreshold();

		if (!ret)
		{
			wasReleased = true;
			isActive = false;
		}

		return ret;
	}

	// this should only be called when a Controllers.next call has returned true
	public boolean wasPressed()
	{
		if (!isValid())
			return false;

		boolean bRet = wasPressedRaw() && meetsThreshold();

		if (bRet)
		{
			isActive = true;
		}

		if (isActive && wasReleased)
		{
			LogHelper.Warn("wasPressed returning true prior to the wasReleased being consumed");
			wasReleased = false;
		}

		return bRet;
	}

	// just checks the event to see if it matches, not using threshold values
	public boolean wasPressedRaw()
	{
		if (!isValid())
			return false;

		if (Controllers.getEventSource().getIndex() == controllerNumber && isTargetEvent())
		{
			pressedOnce = true;
			return true;
		}

		return false;
	}

	// signify to the caller that this was just released and needs a release event to be sent
	public boolean wasReleased()
	{
		boolean bRet = false;
		if (wasReleased)
		{
			if (ControllerSettings.loggingLevel > 1)
				LogHelper.Debug("wasReleased returning true for " + getName());
			bRet = true;
			wasReleased = false;
		}
		return bRet;
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
		ControllerInputEvent inputEvent = (ControllerInputEvent) obj;
		if (inputEvent.type == this.type && inputEvent.buttonNumber == this.buttonNumber
				&& inputEvent.threshold == this.threshold)
			return true;

		return false;
	}
}
