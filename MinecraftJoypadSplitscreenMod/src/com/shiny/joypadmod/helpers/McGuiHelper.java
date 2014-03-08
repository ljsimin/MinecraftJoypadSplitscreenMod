package com.shiny.joypadmod.helpers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.Timer;
import cpw.mods.fml.common.ObfuscationReflectionHelper;

public class McGuiHelper
{

	private static Method mouseClicked = null;
	private static Method mouseMovedOrUp = null;
	private static Method mouseButtonMove = null;
	private static Method clickLeftMouse = null;
	private static Method sendClickBlockToController = null;
	private static Minecraft mc = Minecraft.getMinecraft();

	private static final String[] eventButtonNames = McObfuscationHelper.getMcVarNames("eventButton");
	private static final String[] lastMouseEventNames = McObfuscationHelper.getMcVarNames("lastMouseEvent");

	private static boolean created = false;

	@SuppressWarnings("rawtypes")
	public static void create() throws Exception
	{
		LogHelper.Info("Creating McGuiHelper");
		String[] names = McObfuscationHelper.getMcVarNames("mouseClicked");
		String[] names2 = McObfuscationHelper.getMcVarNames("mouseMovedOrUp");
		String[] names3 = McObfuscationHelper.getMcVarNames("mouseClickMove");
		String[] names4 = McObfuscationHelper.getMcVarNames(ModVersionHelper.getVersion() == 164 ? "clickMouse"
				: "leftClick");
		String[] names5 = McObfuscationHelper.getMcVarNames("sendClickBlockToController");

		Class[] params = new Class[] { int.class, int.class, int.class };
		Class[] params2 = new Class[] { int.class, int.class, int.class };
		Class[] params3 = new Class[] { int.class, int.class, int.class, long.class };
		Class[] params4 = ModVersionHelper.getVersion() == 164 ? new Class[] { int.class } : null;
		Class[] params5 = ModVersionHelper.getVersion() == 164 ? new Class[] { int.class, boolean.class }
				: new Class[] { boolean.class };

		mouseClicked = tryGetMethod(GuiScreen.class, params, names);
		mouseMovedOrUp = tryGetMethod(GuiScreen.class, params2, names2);
		mouseButtonMove = tryGetMethod(GuiScreen.class, params3, names3);
		clickLeftMouse = tryGetMethod(Minecraft.class, params4, names4);
		sendClickBlockToController = tryGetMethod(Minecraft.class, params5, names5);

		created = true;
	}

	@SuppressWarnings("rawtypes")
	private static Method tryGetMethod(Class<?> inClass, Class[] params, String[] names) throws NoSuchMethodException,
			SecurityException
	{
		Method m;
		try
		{
			m = inClass.getDeclaredMethod(names[0], params);
		}
		catch (Exception ex)
		{
			m = inClass.getDeclaredMethod(names[1], params);
		}

		m.setAccessible(true);
		return m;
	}

	public static boolean isCreated()
	{
		return created;
	}

	public static void guiMouseDown(int rawX, int rawY, int button)
	{
		if (!checkCreated())
			return;

		LogHelper.Debug("Calling mouseClicked");
		try
		{
			ObfuscationReflectionHelper.setPrivateValue(GuiScreen.class, (GuiScreen) mc.currentScreen, button,
					eventButtonNames[0], eventButtonNames[1]);
			ObfuscationReflectionHelper.setPrivateValue(GuiScreen.class, (GuiScreen) mc.currentScreen,
					Minecraft.getSystemTime(), lastMouseEventNames[0], lastMouseEventNames[1]);

			mouseClicked.invoke((Object) mc.currentScreen, rawX, rawY, button);
		}
		catch (Exception ex)
		{}
	}

	public static void guiMouseUp(int rawX, int rawY, int button)
	{
		if (!checkCreated())
			return;

		LogHelper.Debug("Calling mouseUp");

		try
		{
			mouseMovedOrUp.invoke((Object) mc.currentScreen, rawX, rawY, button);
		}
		catch (Exception ex)
		{}
	}

	public static void guiMouseDrag(int rawX, int rawY)
	{
		if (!checkCreated())
			return;

		long lastEvent = -1;
		int eventButton = -1;
		LogHelper.Info("Calling mouseDrag");

		try
		{
			eventButton = ObfuscationReflectionHelper.getPrivateValue(GuiScreen.class, (GuiScreen) mc.currentScreen,
					eventButtonNames[0], eventButtonNames[1]);
			lastEvent = ObfuscationReflectionHelper.getPrivateValue(GuiScreen.class, (GuiScreen) mc.currentScreen,
					lastMouseEventNames[0], lastMouseEventNames[1]);
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
			mouseButtonMove.invoke((Object) mc.currentScreen, rawX, rawY, eventButton, var3);
		}
		catch (Exception ex)
		{}

	}

	public static void gameLeftClick()
	{
		if (!checkCreated())
			return;

		try
		{
			if (ModVersionHelper.getVersion() == 164)
				clickLeftMouse.invoke((Object) mc, 0);
			else
				clickLeftMouse.invoke((Object) mc);
		}
		catch (Exception ex)
		{}
	}

	public static void gameSendClickBlockToController(int i, boolean b)
	{
		if (!checkCreated())
			return;

		try
		{
			if (ModVersionHelper.getVersion() == 164)
				sendClickBlockToController.invoke((Object) mc, i, b);
			else
				sendClickBlockToController.invoke((Object) mc, b);
		}
		catch (Exception ex)
		{}
	}

	public static Timer getMcTimer()
	{
		try
		{
			Field timer = Minecraft.class.getDeclaredField("timer");
			timer.setAccessible(true);
			return (Timer) timer.get(mc);
		}
		catch (Exception ex)
		{
			LogHelper.Error("Failed getting timer" + ex.toString());
		}

		return null;
	}

	private static boolean checkCreated()
	{
		if (!created)
		{
			LogHelper.Error("Unable to use McGuiHelper because it failed to initialize");
		}

		return created;
	}
}
