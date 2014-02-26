package com.shiny.joypadmod;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import org.lwjgl.input.Keyboard;

import com.shiny.joypadmod.helpers.LogHelper;

public class VirtualKeyboard
{
	public boolean enabled;

	private Field keyDownField;
	private Field keyBufferField;
	private Byte[] keyState;

	public VirtualKeyboard()
	{
		try
		{
			keyBufferField = Keyboard.class.getDeclaredField("readBuffer");
			keyDownField = Keyboard.class.getDeclaredField("keyDownBuffer");
			keyDownField.setAccessible(true);
			keyBufferField.setAccessible(true);
			keyState = new Byte[Keyboard.KEYBOARD_SIZE];
			for (int i = 0; i < Keyboard.KEYBOARD_SIZE; i++)
				keyState[i] = 0;
			enabled = true;
		}
		catch (Exception ex)
		{
			enabled = false;
			LogHelper.Fatal("Unable to hack keyboard events. " + ex.toString());
		}
	}

	// send a press key event to the keyboard buffer
	public void pressKey(int keycode)
	{
		if (keyHelper(keycode, 1))
		{
			keyState[keycode] = 1;
		}
	}

	// send a release key event to the keyboard buffer
	// give option to only send the event if a pressKey event was recorded prior
	public void releaseKey(int keycode, boolean onlyIfPressed)
	{
		if (!enabled)
		{
			LogHelper.Error("Virtual Keyboard failed to initialize and cannot be used");
			return;
		}

		if (isValidKey(keycode, true) && (!onlyIfPressed || keyState[keycode] == 1))
		{
			keyHelper(keycode, 0);
			keyState[keycode] = 0;
		}
	}

	public void holdKey(int keycode, boolean down)
	{
		if (!isValidKey(keycode, true))
		{
			return;
		}

		LogHelper.Debug("Holding key " + keycode);
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

	private boolean isValidKey(int keycode, boolean logError)
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

	private boolean keyHelper(int keycode, int state)
	{
		if (!enabled)
		{
			LogHelper.Error("Virtual Keyboard failed to initialize and cannot be used");
			return false;
		}
		if (!isValidKey(keycode, true))
		{
			return false;
		}

		if (keyBufferField != null)
		{
			LogHelper.Info("Hacking key " + keycode + " state: " + state);
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
