package com.shiny.joypadmod.helpers;

import java.lang.reflect.Method;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class McGuiHelper
{

	private static Method mouseButtonMove = null;
	private static Minecraft mc = Minecraft.getMinecraft();

	private static final String[] eventButtonNames = McObfuscationHelper.getMcVarNames("eventButton");
	private static final String[] lastMouseEventNames = McObfuscationHelper.getMcVarNames("lastMouseEvent");

	private static boolean created = false;

	@SuppressWarnings("rawtypes")
	public static void create() throws Exception
	{
		LogHelper.Info("Creating McGuiHelper");
		String[] names3 = McObfuscationHelper.getMcVarNames("mouseClickMove");

		Class[] params3 = new Class[] { int.class, int.class, int.class, long.class };

		mouseButtonMove = tryGetMethod(GuiScreen.class, params3, names3);

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

	public static void guiMouseDrag(int rawX, int rawY)
	{
		if (!checkCreated())
			return;

		long lastEvent = -1;
		int eventButton = -1;
		// LogHelper.Info("Calling mouseDrag");

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

	private static boolean checkCreated()
	{
		if (!created)
		{
			LogHelper.Error("Unable to use McGuiHelper because it failed to initialize");
		}

		return created;
	}
}
