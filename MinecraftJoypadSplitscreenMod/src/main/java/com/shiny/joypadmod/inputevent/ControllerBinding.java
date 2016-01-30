package com.shiny.joypadmod.inputevent;

import java.util.EnumSet;
import java.util.Locale;

import net.minecraft.client.Minecraft;

import org.lwjgl.input.Keyboard;

import com.shiny.joypadmod.helpers.LogHelper;
import com.shiny.joypadmod.inputevent.ControllerInputEvent.EventType;
import com.shiny.joypadmod.lwjglVirtualInput.VirtualKeyboard;
import com.shiny.joypadmod.lwjglVirtualInput.VirtualMouse;
import com.shiny.joypadmod.minecraftExtensions.JoypadCalibrationMenu;

public class ControllerBinding
{

	public enum BindingOptions
	{
		MENU_BINDING,
		GAME_BINDING,
		IS_TOGGLE,
		REPEAT_IF_HELD,
		CLIENT_TICK,
		RENDER_TICK,
		CATEGORY_MOVEMENT,
		CATEGORY_UI,
		CATEGORY_INVENTORY,
		CATEGORY_GAMEPLAY,
		CATEGORY_MULTIPLAYER,
		CATEGORY_MISC,
	};

	public static String[] BindingOptionsComment = { "Will trigger in menu screens", "Will trigger during game play",
			"Pressing button once will toggle on / off", "Continues to trigger if held down",
			"Send the trigger during client tick", "Send the trigger during render tick",
			"Shows up in Movement category in menu", "Shows up in UI category in menu", "Inventory category in menu",
			"Gameplay category in menu", "Multiplayer category in menu", "Misc category in menu" };

	/**
	 * Used as a key for the save file
	 */
	public String inputString;
	public String menuString;
	public int[] keyCodes;
	public boolean toggleState = false;
	public long delay;
	public long lastTick = 0;

	public EnumSet<BindingOptions> bindingOptions;

	public ControllerInputEvent inputEvent;

	public ControllerBinding(String inputString, String menuString, ControllerInputEvent inputEvent, int[] keyCodes,
			long delayBetweenPresses, EnumSet<BindingOptions> options)
	{
		this.inputString = inputString;
		this.menuString = menuString;
		this.inputEvent = inputEvent;
		this.keyCodes = keyCodes;
		this.delay = delayBetweenPresses;
		this.bindingOptions = options;
	}

	public ControllerBinding(String bindingString, int joyNo, double lastConfigFileVersion)
	{
		this.setToConfigFileString(bindingString, joyNo, lastConfigFileVersion);
	}

	public void setKeybinding(int[] keyCodes)
	{
		this.keyCodes = keyCodes;
	}

	public boolean hasCategory()
	{
		if ((bindingOptions.contains(BindingOptions.CATEGORY_GAMEPLAY))
				|| (bindingOptions.contains(BindingOptions.CATEGORY_UI))
				|| (bindingOptions.contains(BindingOptions.CATEGORY_INVENTORY))
				|| (bindingOptions.contains(BindingOptions.CATEGORY_MOVEMENT))
				|| (bindingOptions.contains(BindingOptions.CATEGORY_MULTIPLAYER))
				|| (bindingOptions.contains(BindingOptions.CATEGORY_MISC)))
			return true;

		return false;
	}

	public BindingOptions getCategory()
	{
		for (BindingOptions option : bindingOptions)
		{
			if (option.toString().contains("CATEGORY_"))
				return option;
		}
		return BindingOptions.CATEGORY_MISC;

	}

	public String getCategoryString()
	{
		BindingOptions category = getCategory();
		String target = category.toString().split("_")[1].toLowerCase(Locale.ENGLISH);
		return "joy.categories." + target;
	}

	public static BindingOptions mapMinecraftCategory(String category)
	{
		if (category.contains(".gameplay"))
			return BindingOptions.CATEGORY_GAMEPLAY;
		if (category.contains(".ui"))
			return BindingOptions.CATEGORY_UI;
		if (category.contains(".inventory"))
			return BindingOptions.CATEGORY_INVENTORY;
		if (category.contains(".movement"))
			return BindingOptions.CATEGORY_MOVEMENT;
		if (category.contains(".multiplayer"))
			return BindingOptions.CATEGORY_MULTIPLAYER;

		return BindingOptions.CATEGORY_MISC;
	}

	private void handleMouse(boolean pressed, int code, boolean firstPress)
	{
		if (code >= 0)
		{
			LogHelper.Warn("Someone is calling handle mouse for a non-mouse code!");
			return;
		}
		// this code is a little weird but the idea was taken from Mojang
		// a mouse button has a keycode of -100 for button 0 and -99 for button 1
		// i've reused this idea here and added the scrolling index of -201 which will signify -1 and -199 to signify +1
		// which are the values the mouse uses when sending a scroll event
		boolean isWheel = false;
		if (code <= -199)
		{
			isWheel = true;
			code += 200;
		}
		else
		{
			// mouse press requested
			code += 100;
		}

		if (pressed)
		{
			if (isWheel)
			{
				if (firstPress || bindingOptions.contains(BindingOptions.REPEAT_IF_HELD))
					VirtualMouse.scrollWheel(code);
			}
			else
			{
				if (firstPress)
				{
					VirtualMouse.holdMouseButton(code, true);
				}
				else if (bindingOptions.contains(BindingOptions.REPEAT_IF_HELD))
				{
					VirtualMouse.setMouseButton(code, true);
				}
			}
		}
		else if (!isWheel)
		{
			// scroll wheels are discreet events and have no held state
			VirtualMouse.releaseMouseButton(code, true);
		}

	}

	public boolean isPressed()
	{
		return isPressed(this.keyCodes != null);
	}

	public boolean isPressed(boolean autoHandle)
	{
		boolean bRet = inputEvent.isPressed();
		// consume the wasReleasedEvent if it was just released
		boolean wasReleased = !bRet ? inputEvent.wasReleased() : false;

		// override to set to true if it has been toggled on
		if (bindingOptions.contains(BindingOptions.IS_TOGGLE) && toggleState)
		{
			bRet = true;
		}

		// only proceed if this has been set to active through a wasPressed result
		// or if it was just released
		if ((autoHandle) && (bRet && (Minecraft.getSystemTime() - lastTick >= delay))
				|| (wasReleased && this.keyCodes != null))
		{
			for (int i : keyCodes)
			{
				if (i < 0)
				{
					handleMouse(bRet, i, false);
					continue;
				}

				if (!bRet)
				{
					VirtualKeyboard.releaseKey(i, true);
				}
				else if (bindingOptions.contains(BindingOptions.REPEAT_IF_HELD))
				{
					VirtualKeyboard.holdKey(i, true);
				}
			}
			if (bRet)
				lastTick = Minecraft.getSystemTime();
		}

		return bRet;
	}

	public boolean wasPressed()
	{
		return wasPressed(keyCodes != null && keyCodes.length != 0);
	}

	public boolean wasPressed(boolean autoHandle)
	{
		return wasPressed(autoHandle, false);
	}

	public boolean wasPressed(boolean autoHandle, boolean forceHandle)
	{
		boolean bRet = forceHandle ? true : inputEvent.wasPressed();
		if (bRet)
		{
			boolean sendPressKey = true;

			if (bindingOptions.contains(BindingOptions.IS_TOGGLE))
			{
				toggleState = !toggleState;
				sendPressKey = toggleState;
			}

			if (autoHandle && Minecraft.getSystemTime() - lastTick >= delay)
			{
				for (int i : keyCodes)
				{
					if (i < 0)
					{
						handleMouse(bRet, i, true);
						continue;
					}

					// ignore escape key bindings from calibration menu
					if (i == Keyboard.KEY_ESCAPE && Minecraft.getMinecraft().currentScreen != null
							&& Minecraft.getMinecraft().currentScreen instanceof JoypadCalibrationMenu)
						continue;
					if (sendPressKey)
					{
						VirtualKeyboard.pressKey(i);
					}
					else
					{
						VirtualKeyboard.releaseKey(i, true);
					}
				}
				lastTick = Minecraft.getSystemTime();
			}
		}
		return bRet;
	}

	public float getAnalogReading()
	{
		if (inputEvent.isValid())
			return inputEvent.getAnalogReading();

		return 0;
	}

	public String toConfigFileString()
	{
		String s = menuString + ",";

		if (keyCodes != null)
		{
			s += "{";
			for (int i = 0; i < keyCodes.length; i++)
			{
				s += keyCodes[i];

				if (i + 1 < keyCodes.length)
					s += " ";
			}
			s += "},";
		}

		if (inputEvent != null)
			s += inputEvent.toConfigFileString();

		if (bindingOptions != null)
		{
			Object[] options = bindingOptions.toArray();
			for (Object bo : options)
			{
				s += "," + bo.toString();
			}
		}

		return s;
	}

	// returns boolean - whether the input string was accepted and bound
	public boolean setToConfigFileString(String s, int joyNo, double lastConfigFileVersion)
	{
		if (s == null)
			return false;

		LogHelper.Info("setToConfigFileString called with following values: " + s);

		String[] settings = s.split(",");
		int minToProcess = 6;
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
			int i = 0;
			this.inputString = settings[i++];
			if (lastConfigFileVersion < 0.0953)
			{
				// remap old names
				if (this.inputString.contains("run"))
				{
					this.inputString = "joy.sprint";
				}
				else if (this.inputString.contains("movement"))
				{
					if (this.inputString.contains("Y-"))
						this.inputString = "joy.forward";
					else if (this.inputString.contains("Y+"))
						this.inputString = "joy.back";
					else if (this.inputString.contains("X-"))
						this.inputString = "joy.left";
					else if (this.inputString.contains("X+"))
						this.inputString = "joy.right";
				}

			}
			this.menuString = settings[i++];

			if (settings[i].contains("{") && settings[i].contains("}"))
			{
				String[] keyCodesS = settings[i].replaceAll("\\{", "").replaceAll("\\}", "").split(" ");
				keyCodes = new int[keyCodesS.length];
				for (int j = 0; j < keyCodesS.length; j++)
				{
					try
					{
						keyCodes[j] = Integer.parseInt(keyCodesS[j]);
					}
					catch (NumberFormatException nfe)
					{
						keyCodes[j] = Keyboard.getKeyIndex(keyCodesS[j]);
					}
				}
				i++;
			}

			event = ControllerInputEvent.EventType.valueOf(settings[i++]);
			eventIndex = Integer.parseInt(settings[i++]);
			threshold = Float.parseFloat(settings[i++]);
			deadzone = Float.parseFloat(settings[i++]);

			if (bindingOptions == null)
			{
				bindingOptions = EnumSet.noneOf(BindingOptions.class);
			}
			else
			{
				bindingOptions.clear();
			}

			while (i < settings.length)
			{
				try
				{
					bindingOptions.add(BindingOptions.valueOf(settings[i]));
				}
				catch (Exception ex)
				{
					LogHelper.Error("Failed trying to parse " + settings[i] + " to a binding option. " + ex.toString());
				}
				i++;
			}

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
