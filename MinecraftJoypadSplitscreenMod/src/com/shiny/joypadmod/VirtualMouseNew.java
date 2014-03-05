package com.shiny.joypadmod;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import org.lwjgl.input.Mouse;

import com.shiny.joypadmod.helpers.LogHelper;

public class VirtualMouseNew
{

	private static Field xField;
	private static Field yField;
	private static Field buttonField;
	private static Field mouseReadBuffer;

	private static boolean created = false;

	private VirtualMouseNew()
	{}

	public static void create() throws NoSuchFieldException, SecurityException
	{
		if (created)
			return;

		LogHelper.Info("Creating VirtualMouse");

		xField = Mouse.class.getDeclaredField("x");
		yField = Mouse.class.getDeclaredField("y");
		buttonField = Mouse.class.getDeclaredField("buttons");
		mouseReadBuffer = Mouse.class.getDeclaredField("readBuffer");
		xField.setAccessible(true);
		yField.setAccessible(true);
		buttonField.setAccessible(true);
		mouseReadBuffer.setAccessible(true);
		created = true;
	}

	public static boolean isCreated()
	{
		return created;
	}

	public static boolean scrollWheel(int event_dwheel)
	{
		if (!checkCreated())
			return false;

		LogHelper.Info("Setting scroll wheel: " + event_dwheel);
		addMouseEvent((byte) -1, (byte) 0, 0, 0, event_dwheel, 500);
		return true;
	}

	public static boolean releaseMouseButton(byte button)
	{
		if (!checkCreated())
			return false;

		LogHelper.Info("Releasing mouse button: " + button);
		addMouseEvent(button, (byte) 0, 0, 0, 0, 500);
		return true;
	}

	public static boolean holdMouseButton(byte button)
	{
		if (!checkCreated())
			return false;

		LogHelper.Info("Holding mouse button: " + button);
		addMouseEvent(button, (byte) 1, 0, 0, 0, 500);
		return true;
	}

	public static boolean setXY(int x, int y)
	{
		if (!checkCreated())
			return false;

		LogHelper.Info("Setting mouse position to x:" + x + " y:" + y);

		try
		{
			xField.setInt(null, x);
			yField.setInt(null, y);
			return true;
		}
		catch (Exception ex)
		{
			LogHelper.Error("Failed setting x/y value of mouse. " + ex.toString());
		}
		return false;
	}

	private static boolean addMouseEvent(byte eventButton, byte eventState, int event_dx, int event_dy,
			int event_dwheel, long event_nanos)
	{
		if (!checkCreated())
			return false;

		try
		{
			((ByteBuffer) mouseReadBuffer.get(null)).compact();
			((ByteBuffer) mouseReadBuffer.get(null)).put(eventButton); // eventButton
			((ByteBuffer) mouseReadBuffer.get(null)).put(eventState); // eventState
			((ByteBuffer) mouseReadBuffer.get(null)).putInt(event_dx); // event_dx
			((ByteBuffer) mouseReadBuffer.get(null)).putInt(event_dy); // event_dy
			((ByteBuffer) mouseReadBuffer.get(null)).putInt(event_dwheel); // event_dwheel
			((ByteBuffer) mouseReadBuffer.get(null)).putLong(event_nanos); // event_nanos
			((ByteBuffer) mouseReadBuffer.get(null)).flip();
		}
		catch (Exception ex)
		{
			LogHelper.Error("Failed putting value in mouseReadBuffer " + ex.toString());
			return false;
		}

		return true;
	}

	private static boolean checkCreated()
	{
		if (!created)
		{
			LogHelper.Error("Virtual mouse has not been created or failed to initialize and cannot be used");
			return false;
		}
		return true;
	}

}
