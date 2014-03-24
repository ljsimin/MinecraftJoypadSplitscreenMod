package com.shiny.joypadmod.lwjglVirtualInput;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import org.lwjgl.input.Keyboard;

import com.shiny.joypadmod.ControllerSettings;
import com.shiny.joypadmod.helpers.LogHelper;

public class VirtualKeyboard
{
	private static Field keyDownField;
	private static Field keyBufferField;
	private static Byte[] keyState;
	private static boolean created = false;

	/**
	 * VirtualKeyboard cannot be constructed.
	 */
	private VirtualKeyboard()
	{}

	public static void create() throws NoSuchFieldException, SecurityException
	{
		if (created)
			return;

		LogHelper.Info("Creating VirtualKeyboard");
		keyBufferField = Keyboard.class.getDeclaredField("readBuffer");
		keyDownField = Keyboard.class.getDeclaredField("keyDownBuffer");
		keyDownField.setAccessible(true);
		keyBufferField.setAccessible(true);
		keyState = new Byte[Keyboard.KEYBOARD_SIZE];
		for (int i = 0; i < Keyboard.KEYBOARD_SIZE; i++)
			keyState[i] = 0;
		created = true;
	}

	public static boolean isCreated()
	{
		return created;
	}

	// send a press key event to the keyboard buffer
	public static void pressKey(int keycode)
	{
		if (!checkCreated())
		{
			return;
		}

		if (keyHelper(keycode, 1))
		{
			if (ControllerSettings.loggingLevel > 1)
				LogHelper.Info("Pressing key " + Keyboard.getKeyName(keycode));
			keyState[keycode] = 1;
			holdKey(keycode, true);
		}
	}

	// send a release key event to the keyboard buffer
	// give option to only send the event if a pressKey event was recorded prior
	public static void releaseKey(int keycode, boolean onlyIfPressed)
	{
		if (!checkCreated())
		{
			return;
		}

		if (isValidKey(keycode, true) && (!onlyIfPressed || keyState[keycode] == 1))
		{
			if (ControllerSettings.loggingLevel > 1)
				LogHelper.Info("Releasing key " + Keyboard.getKeyName(keycode));
			keyHelper(keycode, 0);
			keyState[keycode] = 0;
			holdKey(keycode, false);
		}
	}

	public static void holdKey(int keycode, boolean down)
	{
		if (!checkCreated())
		{
			return;
		}

		if (!isValidKey(keycode, true))
		{
			return;
		}

		if (ControllerSettings.loggingLevel > 2)
			LogHelper.Info("Holding key " + Keyboard.getKeyName(keycode));
		if (keyDownField != null)
		{
			try
			{
				byte b = (byte) (down ? 1 : 0);
				((ByteBuffer) keyDownField.get(null)).put(keycode, b);
			}
			catch (Exception ex)
			{
				LogHelper.Error("Failed putting value in key buffer" + ex.toString());
			}
		}
	}

	private static boolean checkCreated()
	{
		if (!created)
		{
			LogHelper.Error("Virtual Keyboard has not been created or failed to initialize and cannot be used");
			return false;
		}
		return true;
	}

	private static boolean isValidKey(int keycode, boolean logError)
	{
		if (keycode < 0 || keycode > Keyboard.KEYBOARD_SIZE)
		{
			if (logError)
			{
				LogHelper.Error("Invalid keyboard keycode requested: " + keycode);
			}
			return false;
		}

		return true;
	}

	private static boolean keyHelper(int keycode, int state)
	{
		if (!checkCreated())
		{
			return false;
		}
		if (!isValidKey(keycode, true))
		{
			return false;
		}

		if (keyBufferField != null)
		{
			if (ControllerSettings.loggingLevel > 1)
				LogHelper.Info("Hacking key " + Keyboard.getKeyName(keycode) + " state: " + state);
			try
			{
				((ByteBuffer) keyBufferField.get(null)).compact();
				((ByteBuffer) keyBufferField.get(null)).putInt(keycode); // key
				((ByteBuffer) keyBufferField.get(null)).put((byte) state); // state
				((ByteBuffer) keyBufferField.get(null)).putInt(keycode); // character
				((ByteBuffer) keyBufferField.get(null)).putLong(500); // nanos
				((ByteBuffer) keyBufferField.get(null)).put((byte) 0); // repeat
				((ByteBuffer) keyBufferField.get(null)).flip();
				return true;

			}
			catch (Exception ex)
			{
				LogHelper.Error("Failed putting value in keyBufferField " + ex.toString());
			}
		}
		return false;
	}
}
