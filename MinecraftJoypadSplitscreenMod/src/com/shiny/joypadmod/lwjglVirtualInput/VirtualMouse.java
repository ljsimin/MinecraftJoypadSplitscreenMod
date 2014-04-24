package com.shiny.joypadmod.lwjglVirtualInput;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;

import org.lwjgl.input.Mouse;

import com.shiny.joypadmod.ControllerSettings;
import com.shiny.joypadmod.helpers.LogHelper;

public class VirtualMouse
{

	private static Field xField;
	private static Field yField;
	private static Field dxField;
	private static Field dyField;
	// private static Field eventXField;
	// private static Field eventYField;
	private static Field buttonField;
	private static Field mouseReadBuffer;
	private static Field isGrabbedField;
	private static int[] buttonsDown = { 0, 0, 0 };
	private static final int buttonsSupported = 3;

	private static int lastX = 0;
	private static int lastY = 0;

	private static boolean created = false;

	private VirtualMouse()
	{}

	public static void create() throws NoSuchFieldException, SecurityException
	{
		if (created)
			return;

		LogHelper.Info("Creating VirtualMouse");
		xField = getSetField("x");
		yField = getSetField("y");
		dxField = getSetField("dx");
		dyField = getSetField("dy");
		// eventXField = getSetField("event_x");
		// eventYField = getSetField("event_y");
		buttonField = getSetField("buttons");
		mouseReadBuffer = getSetField("readBuffer");
		isGrabbedField = getSetField("isGrabbed");

		created = true;
	}

	private static Field getSetField(String fieldName) throws NoSuchFieldException, SecurityException
	{
		Field f = Mouse.class.getDeclaredField(fieldName);
		f.setAccessible(true);
		return f;
	}

	public static boolean isCreated()
	{
		return created;
	}

	public static void unpressAllButtons()
	{
		for (int i = 0; i < buttonsSupported; i++)
		{
			releaseMouseButton(i, true);
		}
	}

	// note a Mouse.poll is used indirectly by Minecraft when it calls Display.update
	// Mouse.poll will set the dx/dy values based on what it reads from the mouse
	// therefore, we have to send the dx/dy values in addition to setting the
	// dx/dy variables directly
	// this function doesn't really work properly right now
	public static boolean moveMouse(int dx, int dy)
	{
		if (!checkCreated())
			return false;

		if (ControllerSettings.loggingLevel > 1)
			LogHelper.Info("Moving mouse: dx:" + dx + " dy:" + dy);
		addMouseEvent((byte) -1, (byte) 0, dx, dy, 0, 1000);
		try
		{
			dxField.setInt(null, dx);
			dyField.setInt(null, dy);
		}
		catch (Exception ex)
		{
			LogHelper.Error("Failed setting dx/dy value of mouse. " + ex.toString());
		}

		return true;
	}

	public static boolean holdMouseButton(int button, boolean onlyIfNotHeld)
	{
		if (!checkCreated() || !checkButtonSupported(button))
			return false;

		if (onlyIfNotHeld && buttonsDown[button] != 0)
		{
			if (ControllerSettings.loggingLevel > 1)
				LogHelper.Info("rejecting mouse button press as its already indicated as held");
			return true;
		}

		if (ControllerSettings.loggingLevel > 1)
			LogHelper.Info("Holding mouse button: " + button);
		setMouseButton(button, true);
		addMouseEvent((byte) button, (byte) 1, lastX, lastY, 0, 1000);
		buttonsDown[button] = 1;
		return true;
	}

	public static boolean releaseMouseButton(int button, boolean onlyIfHeld)
	{
		if (!checkCreated() || !checkButtonSupported(button))
			return false;

		if (onlyIfHeld && buttonsDown[button] != 1)
			return true;

		if (ControllerSettings.loggingLevel > 1)
			LogHelper.Info("Releasing mouse button: " + button);
		setMouseButton(button, false);
		addMouseEvent((byte) button, (byte) 0, lastX, lastY, 0, 1000);
		buttonsDown[button] = 0;
		return true;
	}

	public static boolean isButtonDown(int button)
	{
		return buttonsDown[button] == 1;
	}

	public static boolean scrollWheel(int event_dwheel)
	{
		if (!checkCreated())
			return false;

		if (ControllerSettings.loggingLevel > 1)
			LogHelper.Info("Setting scroll wheel: " + event_dwheel);
		addMouseEvent((byte) -1, (byte) 0, 0, 0, event_dwheel, 10000);
		return true;
	}

	public static boolean setXY(int x, int y)
	{
		if (!checkCreated())
			return false;

		if (ControllerSettings.loggingLevel > 3)
			LogHelper.Info("Setting mouse position to x:" + x + " y:" + y);

		try
		{
			xField.setInt(null, x);
			yField.setInt(null, y);
			// eventXField.setInt(null, x);
			// eventYField.setInt(null, y);
			lastX = x;
			lastY = y;
			return true;
		}
		catch (Exception ex)
		{
			LogHelper.Error("Failed setting x/y value of mouse. " + ex.toString());
		}
		return false;
	}

	public static boolean setMouseButton(int button, boolean down)
	{
		if (ControllerSettings.loggingLevel > 3)
			LogHelper.Info("Hacking mouse button: " + button);
		try
		{
			((ByteBuffer) buttonField.get(null)).put(button, (byte) (down ? 1 : 0));
		}
		catch (Exception ex)
		{
			LogHelper.Error("Failed setting mouse button field: " + ex.toString());
			return false;
		}

		return true;
	}

	private static boolean addMouseEvent(byte eventButton, byte eventState, int event_dx, int event_dy,
			int event_dwheel, long event_nanos)
	{
		if (!checkCreated())
			return false;

		if (ControllerSettings.loggingLevel > 1)
			LogHelper.Info("AddMouseEvent: eventButton: " + eventButton + " eventState: " + eventState + " event_dx: "
					+ event_dx + " event_dy: " + event_dy + " event_dwheel: " + event_dwheel);

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

	public static void setGrabbed(boolean grabbed)
	{
		if (ControllerSettings.loggingLevel > 3)
			LogHelper.Info("Setting Mouse.isGrabbed to return: " + grabbed);
		try
		{
			isGrabbedField.setBoolean(null, grabbed);
		}
		catch (Exception ex)
		{
			LogHelper.Error("Failed trying to set Mouse.isGrabbed. " + ex.toString());
		}
	}

	private static boolean checkButtonSupported(int button)
	{
		if (button < 0 || button >= buttonsSupported)
		{
			LogHelper.Error("Button number is not supported.  Max button index is " + (buttonsSupported - 1));
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
