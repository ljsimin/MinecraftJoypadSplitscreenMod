package com.shiny.joypadmod.inputevent;

import net.minecraft.client.settings.KeyBinding;

import com.shiny.joypadmod.VirtualKeyboard;
import com.shiny.joypadmod.helpers.LogHelper;
import com.shiny.joypadmod.inputevent.ControllerInputEvent.EventType;

public class ControllerBinding
{

	/**
	 * Used as a key for the save file
	 */
	public String inputString;
	public String menuString;
	public KeyBinding keybinding;

	public ControllerInputEvent inputEvent;

	public ControllerBinding(String inputString, String menuString, ControllerInputEvent inputEvent)
	{
		this.inputString = inputString;
		this.menuString = menuString;
		this.inputEvent = inputEvent;
		this.keybinding = null;
	}

	public void setKeybinding(KeyBinding keybinding)
	{
		this.keybinding = keybinding;
	}

	public boolean isPressed()
	{
		boolean bRet = inputEvent.isPressed();
		if (keybinding != null)
		{
			if (bRet)
			{
				VirtualKeyboard.holdKey(keybinding.getKeyCode(), true);
			}
			else
			{
				VirtualKeyboard.releaseKey(keybinding.getKeyCode(), true);
			}
		}
		return bRet;
	}

	public boolean wasPressed()
	{
		boolean bRet = inputEvent.wasPressed();
		if (bRet && keybinding != null)
		{
			VirtualKeyboard.pressKey(keybinding.getKeyCode());
		}
		return bRet;
	}

	public float getAnalogReading()
	{
		return inputEvent.getAnalogReading();
	}

	public String toConfigFileString()
	{
		String s = menuString + "," + inputEvent.toConfigFileString();
		return s;
	}

	// returns boolean - whether the input string was accepted and bound
	public boolean setToConfigFileString(String s, int joyNo, double lastConfigFileVersion)
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
				// thresholds for buttons were set at 0 prior to version .08, so set these to 1
				if (lastConfigFileVersion < 0.08)
					threshold = 1;
				this.inputEvent = new ButtonInputEvent(joyNo, eventIndex, threshold);
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
