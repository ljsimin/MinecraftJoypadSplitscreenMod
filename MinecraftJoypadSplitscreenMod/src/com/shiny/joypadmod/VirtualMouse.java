package com.shiny.joypadmod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import cpw.mods.fml.common.ObfuscationReflectionHelper;

public class VirtualMouse
{

	// last delta movement of axis
	public static float deltaX;
	public static float deltaY;

	// last virtual mouse position
	public int x = 0;
	public int y = 0;

	// values that Minecraft expects when reading the actual mouse
	public int mcX = 0;
	public int mcY = 0;

	public boolean leftButtonHeld = false;
	public boolean rightButtonHeld = false;
	public boolean ignoreFirstAttackPress = true; // some quirk with xbox
													// gamepads lights up
													// this button but
													// doesn't release it on
													// init

	public boolean usingAxisCoordinates = true;
	public static boolean debug = false;
	public float sensitivity = 0.07f;

	private Field keyDownField;

	private static Minecraft mc = Minecraft.getMinecraft();

	public VirtualMouse()
	{
		// TODO: Move other reflected fields/methods initialization here
		try
		{
			keyDownField = Keyboard.class.getDeclaredField("keyDownBuffer");
			keyDownField.setAccessible(true);
		}
		catch (Exception ex)
		{
			LogHelper.Fatal("Unable to hack keyboard events. " + ex.toString());
		}

	}

	public int getX()
	{
		if (this.usingAxisCoordinates)
			this.setMouseCoordinatesWithController();
		return this.x;
	}

	public int getY()
	{
		if (this.usingAxisCoordinates)
			this.setMouseCoordinatesWithController();
		return this.y;
	}

	public void leftButtonDown()
	{
		if (ignoreFirstAttackPress)
		{
			ignoreFirstAttackPress = false;
			return;
		}
		if (!this.leftButtonHeld)
		{
			this.gui_mouseDown(x, y, 0);
			VirtualMouse.hack_mouseButton(0);
			this.leftButtonHeld = true;
		}
	}

	public void leftButtonUp()
	{
		if (this.leftButtonHeld)
		{
			this.gui_mouseUp(x, y, 0);
			this.leftButtonHeld = false;
		}
	}

	public void rightButtonDown()
	{
		if (!this.rightButtonHeld)
		{
			this.gui_mouseDown(x, y, 1);
			VirtualMouse.hack_mouseButton(1);
			this.rightButtonHeld = true;
		}
	}

	public void rightButtonUp()
	{
		if (this.rightButtonHeld)
		{
			this.gui_mouseUp(x, y, 1);
			this.rightButtonHeld = false;
		}
	}

	public void UnpressButtons()
	{
		this.leftButtonHeld = false;
		this.rightButtonHeld = false;
	}

	// used for in game clicking
	public static void leftClick()
	{
		game_leftClick();
	}

	// this is the equivalent of moving the mouse around on your joypad
	public static void updateCameraAxisReading()
	{
		float var3 = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
		float var4 = var3 * var3 * var3 * 8.0F;
		float var8 = Math.abs(ControllerSettings.joyCameraXplus.getAnalogReading()) > Math.abs(ControllerSettings.joyCameraXminus.getAnalogReading()) ? ControllerSettings.joyCameraXplus
				.getAnalogReading() : ControllerSettings.joyCameraXminus.getAnalogReading();
		float var9 = Math.abs(ControllerSettings.joyCameraYplus.getAnalogReading()) > Math.abs(ControllerSettings.joyCameraYminus.getAnalogReading()) ? ControllerSettings.joyCameraYplus
				.getAnalogReading() : ControllerSettings.joyCameraYminus.getAnalogReading();
		deltaX = (float) (Math.round(var8 * (float) ControllerSettings.joyCameraSensitivity) * var4);
		deltaY = (float) (Math.round(var9 * (float) ControllerSettings.joyCameraSensitivity) * var4 * -1.0F);

		if (debug)
		{
			LogHelper.Debug("Camera deltaX: " + deltaX + " Camera deltaY: " + deltaY);
		}
	}

	private void setMouseCoordinatesWithController()
	{
		final ScaledResolution scaledResolution = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);

		updateCameraAxisReading();

		int dx = (int) (sensitivity * deltaX);
		int dy = (int) (sensitivity * deltaY) * (mc.gameSettings.invertMouse ? 1 : -1);
		x += dx;
		y += dy;

		if (x < 0)
			x = 0;
		if (y < 0)
			y = 0;
		if (x > scaledResolution.getScaledWidth())
			x = scaledResolution.getScaledWidth() - 5;
		if (y > scaledResolution.getScaledHeight())
			y = scaledResolution.getScaledHeight() - 5;

		if (debug)
			LogHelper.Debug("Virtual Mouse x: " + x + " y: " + y);

		mcY = mc.displayHeight - (int) (y * scaledResolution.getScaleFactor());
		mcX = x * scaledResolution.getScaleFactor();
	}

	String[] eventButtonNames = JoypadMod.obfuscationHelper.GetMinecraftVarNames("eventButton");
	String[] lastMouseEventNames = JoypadMod.obfuscationHelper.GetMinecraftVarNames("lastMouseEvent");

	// todo: look at this variable!!?!
	int gMdParam = 0;

	private void gui_mouseDown(int rawX, int rawY, int button)
	{
		// todo: this function is riddled with bad names

		if (gMdParam == -1)
		{
			System.out.println("gui_mouseDown disabled due to earlier error");
			return;
		}

		String[] names = JoypadMod.obfuscationHelper.GetMinecraftVarNames("mouseClicked");
		Method mouseClicked = null;

		@SuppressWarnings("rawtypes")
		Class[] params = new Class[] { int.class, int.class, int.class };

		LogHelper.Debug("Calling mouseClicked");
		try
		{
			ObfuscationReflectionHelper.setPrivateValue(GuiScreen.class, (GuiScreen) mc.currentScreen, button, eventButtonNames[0], eventButtonNames[1]);
			ObfuscationReflectionHelper.setPrivateValue(GuiScreen.class, (GuiScreen) mc.currentScreen, Minecraft.getSystemTime(), lastMouseEventNames[0], lastMouseEventNames[1]);

			try
			{
				mouseClicked = GuiScreen.class.getDeclaredMethod(names[gMdParam], params);
			}
			catch (Exception ex)
			{
				mouseClicked = GuiScreen.class.getDeclaredMethod(names[1], params);
				gMdParam = 1;
			}

			mouseClicked.setAccessible(true);
			mouseClicked.invoke((Object) mc.currentScreen, rawX, rawY, button);
		}
		catch (Exception ex)
		{
			LogHelper.Error("Failed calling " + names[0] + "(" + names[1] + ") : " + ex.toString());
			gMdParam = -1;
		}
	}

	int muParam = 0;

	private void gui_mouseUp(int rawX, int rawY, int button)
	{
		if (muParam == -1)
		{
			LogHelper.Error("gui_mouseUp disabled due to earlier error");
			return;
		}

		String[] names = JoypadMod.obfuscationHelper.GetMinecraftVarNames("mouseMovedOrUp");

		Method mouseMovedOrUp = null;
		@SuppressWarnings("rawtypes")
		Class[] params = new Class[] { int.class, int.class, int.class };
		LogHelper.Debug("Calling mouseUp");

		try
		{
			try
			{
				mouseMovedOrUp = GuiScreen.class.getDeclaredMethod(names[muParam], params);
			}
			catch (Exception ex)
			{
				mouseMovedOrUp = GuiScreen.class.getDeclaredMethod(names[1], params);
				muParam = 1;
			}
			mouseMovedOrUp.setAccessible(true);
			mouseMovedOrUp.invoke((Object) mc.currentScreen, rawX, rawY, button);
		}
		catch (Exception ex)
		{
			LogHelper.Error("Failed calling " + names[0] + "(" + names[1] + ") : " + ex.toString());
			muParam = -1;
		}
	}

	// todo: again!!
	int mdParam = 0;

	public void gui_mouseDrag(int rawX, int rawY)
	{
		if (mdParam == -1)
		{
			LogHelper.Error("gui_mouseDrag disabled due to earlier error");
			return;
		}

		long lastEvent = -1;
		int eventButton = -1;
		String[] names = JoypadMod.obfuscationHelper.GetMinecraftVarNames("mouseClickMove");
		Method mouseButtonMove = null;
		@SuppressWarnings("rawtypes")
		Class[] params = new Class[] { int.class, int.class, int.class, long.class };
		LogHelper.Debug("Calling mouseDrag");

		try
		{
			eventButton = ObfuscationReflectionHelper.getPrivateValue(GuiScreen.class, (GuiScreen) mc.currentScreen, eventButtonNames[0], eventButtonNames[1]);
			lastEvent = ObfuscationReflectionHelper.getPrivateValue(GuiScreen.class, (GuiScreen) mc.currentScreen, lastMouseEventNames[0], lastMouseEventNames[1]);
		}
		catch (Exception ex)
		{
			LogHelper.Error("Failed calling ObfuscationReflectionHelper" + ex.toString());
			if (lastEvent == -1)
				lastEvent = 100;
			eventButton = 0;
		}
		long var3 = Minecraft.getSystemTime() - lastEvent;

		try
		{
			try
			{
				mouseButtonMove = GuiScreen.class.getDeclaredMethod(names[mdParam], params);
			}
			catch (Exception ex)
			{
				mouseButtonMove = GuiScreen.class.getDeclaredMethod(names[1], params);
				mdParam = 1;
			}
			mouseButtonMove.setAccessible(true);
			mouseButtonMove.invoke((Object) mc.currentScreen, rawX, rawY, eventButton, var3);
		}
		catch (Exception ex)
		{
			LogHelper.Error("Failed calling " + names[0] + "(" + names[1] + ") : " + ex.toString());
			mdParam = -1;
		}

	}

	// oh minecraft why did you have to mess with the clicking function?
	private static int glcParam = 0;

	// note clicking functions updated in 1.7.2+, separate function calls for
	// left vs right click
	@SuppressWarnings("unused")
	private static void game_leftClick()
	{

		if (glcParam == -1)
		{
			LogHelper.Error("leftClick disabled due to earlier error");
			return;
		}

		String functionName = JoypadMod.VERSION == "1.6.4" ? "clickMouse" : "leftClick";
		String[] names = JoypadMod.obfuscationHelper.GetMinecraftVarNames(functionName);

		LogHelper.Debug("Calling " + names[0] + "(" + names[1] + ")");

		@SuppressWarnings({ "rawtypes" })
		Class[] params = JoypadMod.VERSION == "1.6.4" ? new Class[] { int.class } : null;
		Method clickLeftMouse;
		try
		{
			try
			{
				clickLeftMouse = Minecraft.class.getDeclaredMethod(names[glcParam], params);
			}
			catch (Exception ex)
			{
				clickLeftMouse = Minecraft.class.getDeclaredMethod(names[1], params);
				glcParam = 1;
			}
			clickLeftMouse.setAccessible(true);
			if (JoypadMod.VERSION == "1.6.4")
				clickLeftMouse.invoke((Object) mc, 0);
			else
				clickLeftMouse.invoke((Object) mc);
		}
		catch (Exception ex)
		{
			LogHelper.Error("Failed calling " + names[0] + "(" + names[1] + ") : " + ex.toString());
			glcParam = -1;
		}
	}

	public boolean hack_mouseXY(int x, int y)
	{
		LogHelper.Debug("Hacking mouse position to x:" + x + " y:" + y);
		try
		{
			Field xField = Mouse.class.getDeclaredField("x");
			Field yField = Mouse.class.getDeclaredField("y");

			xField.setAccessible(true);
			yField.setAccessible(true);
			xField.setInt(null, x);
			yField.setInt(null, y);
		}
		catch (Exception ex)
		{
			LogHelper.Error("Failed calling Mouse fields: " + ex.toString());
			return false;
		}

		return true;
	}

	public static boolean hack_mouseButton(int button)
	{
		LogHelper.Debug("Hacking mouse button: " + button);
		try
		{
			Field buttonField = Mouse.class.getDeclaredField("buttons");
			buttonField.setAccessible(true);

			// left Button
			if (button == 0)
				((ByteBuffer) buttonField.get(null)).put(0, (byte) 1);
			// right Button
			else if (button == 1)
				((ByteBuffer) buttonField.get(null)).put(1, (byte) 1);
		}
		catch (Exception ex)
		{
			LogHelper.Error("Failed calling Mouse fields: " + ex.toString());
			return false;
		}

		return true;
	}

	public void hack_shiftKey(boolean down)
	{
		LogHelper.Debug("Hacking shift key");
		if (keyDownField != null)
		{
			try
			{
				byte b = (byte) (down ? 1 : 0);
				((ByteBuffer) keyDownField.get(null)).put(Keyboard.KEY_LSHIFT, b);
			}
			catch (Exception ex)
			{
				LogHelper.Error("Failed putting value in shift key buffer" + ex.toString());
			}

		}

	}
}
