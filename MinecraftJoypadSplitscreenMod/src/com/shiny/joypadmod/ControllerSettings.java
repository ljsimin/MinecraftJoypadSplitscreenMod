package com.shiny.joypadmod;

// Common code

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;

import com.shiny.joypadmod.inputevent.AxisInputEvent;
import com.shiny.joypadmod.inputevent.ButtonInputEvent;
import com.shiny.joypadmod.inputevent.ControllerBinding;
import com.shiny.joypadmod.inputevent.ControllerInputEvent;
import com.shiny.joypadmod.inputevent.ControllerUtils;
import com.shiny.joypadmod.inputevent.PovInputEvent;

public class ControllerSettings
{

	public static final float defaultAxisDeadZone = 0.25f;
	public static final float defaultAxisThreshhold = 0.75f;
	public static final float defaultPovThreshhold = 0.9f;

	public static ControllerBinding joyBindJump;
	public static ControllerBinding joyBindInventory;
	public static ControllerBinding joyBindDrop;
	public static ControllerBinding joyBindSneak;
	public static ControllerBinding joyBindAttack;
	public static ControllerBinding joyBindUseItem;
	public static ControllerBinding joyBindInteract;
	public static ControllerBinding joyBindGuiLeftClick;
	public static ControllerBinding joyBindGuiRightClick;
	public static ControllerBinding joyBindRun;
	public static ControllerBinding joyBindMenu;
	public static ControllerBinding joyBindShiftClick;
	public static ControllerBinding joyBindPrevItem;
	public static ControllerBinding joyBindNextItem;
	public static ControllerBinding joyBindings[];
	public static ControllerBinding joyCameraXplus;
	public static ControllerBinding joyCameraXminus;
	public static ControllerBinding joyCameraYplus;
	public static ControllerBinding joyCameraYminus;
	public static ControllerBinding joyMovementXplus;
	public static ControllerBinding joyMovementXminus;
	public static ControllerBinding joyMovementYplus;
	public static ControllerBinding joyMovementYminus;
	public static ControllerBinding joyGuiXplus;
	public static ControllerBinding joyGuiXminus;
	public static ControllerBinding joyGuiYplus;
	public static ControllerBinding joyGuiYminus;
	public static boolean useConstantCameraMovement = false;
	public static boolean displayHints = false;
	public static boolean toggleSneak = true;
	public static Controller joystick;
	public static int joyNo = 0;
	public static int joyCameraSensitivity = 20;
	public static boolean inputEnabled = false;
	// used for some preliminary safe checks
	private static int requiredMinButtonCount = 4;
	private static int requiredButtonCount = 12;
	private static int requiredAxisCount = 4;

	public static Map<Integer, String> validControllers;
	public static Map<Integer, String> inValidControllers;
	public static Controllers controllers;
	public static ControllerUtils controllerUtils;

	private static boolean suspendControllerInput = false;

	public ControllerSettings()
	{
		controllers = new Controllers();
		controllerUtils = new ControllerUtils();
		validControllers = new HashMap<Integer, String>();
		inValidControllers = new HashMap<Integer, String>();
	}

	public static void setDefaultJoyBindings()
	{
		LogHelper.Info("Setting default joy bindings");
		joyBindJump = new ControllerBinding("joy.jump", new ButtonInputEvent(joyNo, 0));
		joyBindInventory = new ControllerBinding("joy.inventory", new ButtonInputEvent(joyNo, 3));
		joyBindDrop = new ControllerBinding("joy.drop", new ButtonInputEvent(joyNo, 6));
		joyBindSneak = new ControllerBinding("joy.sneak", new ButtonInputEvent(joyNo, 8));
		joyBindAttack = new ControllerBinding("joy.attack", new AxisInputEvent(joyNo, 4, defaultAxisThreshhold * -1, defaultAxisDeadZone));
		joyBindUseItem = new ControllerBinding("joy.use", new AxisInputEvent(joyNo, 4, defaultAxisThreshhold, defaultAxisDeadZone));
		joyBindInteract = new ControllerBinding("joy.interact", new ButtonInputEvent(joyNo, 2));
		joyBindGuiLeftClick = new ControllerBinding("joy.guiLeftClick", new ButtonInputEvent(joyNo, 0));
		joyBindGuiRightClick = new ControllerBinding("joy.guiRightClick", new ButtonInputEvent(joyNo, 2));
		joyBindPrevItem = new ControllerBinding("joy.prevItem", new ButtonInputEvent(joyNo, 4));
		joyBindNextItem = new ControllerBinding("joy.nextItem", new ButtonInputEvent(joyNo, 5));
		joyBindRun = new ControllerBinding("joy.run", new ButtonInputEvent(joyNo, 9));
		joyBindMenu = new ControllerBinding("joy.menu", new ButtonInputEvent(joyNo, 7));
		joyBindShiftClick = new ControllerBinding("joy.shiftClick", new ButtonInputEvent(joyNo, 1));
		joyCameraXplus = new ControllerBinding("joy.cameraX+", new AxisInputEvent(joyNo, 3, defaultAxisThreshhold, defaultAxisDeadZone));
		joyCameraXminus = new ControllerBinding("joy.cameraX-", new AxisInputEvent(joyNo, 3, defaultAxisThreshhold * -1, defaultAxisDeadZone));
		joyCameraYplus = new ControllerBinding("joy.cameraY+", new AxisInputEvent(joyNo, 2, defaultAxisThreshhold, defaultAxisDeadZone));
		joyCameraYminus = new ControllerBinding("joy.cameraY-", new AxisInputEvent(joyNo, 2, defaultAxisThreshhold * -1, defaultAxisDeadZone));
		joyMovementXplus = new ControllerBinding("joy.movementX+", new AxisInputEvent(joyNo, 1, defaultAxisThreshhold, defaultAxisDeadZone));
		joyMovementXminus = new ControllerBinding("joy.movementX-", new AxisInputEvent(joyNo, 1, defaultAxisThreshhold * -1, defaultAxisDeadZone));
		joyMovementYplus = new ControllerBinding("joy.movementY+", new AxisInputEvent(joyNo, 0, defaultAxisThreshhold, defaultAxisDeadZone));
		joyMovementYminus = new ControllerBinding("joy.movementY-", new AxisInputEvent(joyNo, 0, defaultAxisThreshhold * -1, defaultAxisDeadZone));
		joyGuiXplus = new ControllerBinding("joy.guiX+", new PovInputEvent(joyNo, 0, defaultPovThreshhold));
		joyGuiXminus = new ControllerBinding("joy.guiX-", new PovInputEvent(joyNo, 0, defaultPovThreshhold * -1));
		joyGuiYplus = new ControllerBinding("joy.guiY+", new PovInputEvent(joyNo, 1, defaultPovThreshhold));
		joyGuiYminus = new ControllerBinding("joy.guiY-", new PovInputEvent(joyNo, 1, defaultPovThreshhold * -1));

		joyBindings = (new ControllerBinding[] { joyBindAttack, joyBindUseItem, joyBindJump, joyBindSneak, joyBindDrop, joyBindInventory, joyBindInteract, joyBindGuiLeftClick, joyBindGuiRightClick,
				joyBindPrevItem, joyBindNextItem, joyBindRun, joyBindMenu, joyBindShiftClick, joyCameraXplus, joyCameraXminus, joyCameraYplus, joyCameraYminus, joyMovementXplus, joyMovementXminus,
				joyMovementYplus, joyMovementYminus, joyGuiXplus, joyGuiXminus, joyGuiYplus, joyGuiYminus });
	}

	public static int DetectControllers()
	{
		validControllers.clear();
		inValidControllers.clear();

		try
		{
			if (!Controllers.isCreated())
				Controllers.create();
			if (Controllers.getControllerCount() > 0)
			{
				LogHelper.Info("Minecraft Joypad (Controller) Mod v" + JoypadMod.VERSION + " by Ljubomir Simin & Andrew Hickey\n---");
				LogHelper.Info("Found " + Controllers.getControllerCount() + " controller(s) in total.");
				for (int joyNo = 0; joyNo < Controllers.getControllerCount(); joyNo++)
				{
					Controller thisController = Controllers.getController(joyNo);

					LogHelper.Info("Found controller " + thisController.getName() + " (" + joyNo + ")");
					LogHelper.Info("It has  " + thisController.getButtonCount() + " buttons.");
					LogHelper.Info("It has  " + thisController.getAxisCount() + " axes.");
					if (controllerUtils.meetsInputRequirements(thisController, requiredButtonCount, requiredMinButtonCount, requiredAxisCount))
					{
						LogHelper.Info("Controller #" + joyNo + " ( " + thisController.getName() + ") meets the input requirements");
						validControllers.put(joyNo, thisController.getName());
					}
					else
					{
						inValidControllers.put(joyNo, thisController.getName());
						LogHelper.Info("This controller does not meet the input requirements");
					}
					LogHelper.Info("---");
				}
			}
		}
		catch (org.lwjgl.LWJGLException e)
		{
			System.err.println("Couldn't initialize Controllers: " + e.getMessage());
		}

		LogHelper.Info("Found " + validControllers.size() + " valid controllers!");
		return validControllers.size();
	}

	public static boolean SetController(int controllerNo)
	{
		LogHelper.Info("Attempting to use controller " + controllerNo);
		try
		{
			if (!Controllers.isCreated())
				Controllers.create();

			LogHelper.Info("Controllers.getControllerCount == " + Controllers.getControllerCount());
			joystick = Controllers.getController(controllerNo);
			joyNo = controllerNo;
			controllerUtils.printDeadZones(joystick);
			inputEnabled = true;

			setDefaultJoyBindings();

			for (ControllerBinding binding : joyBindings)
			{
				binding.inputEvent.setDeadZone();
			}
		}
		catch (Exception e)
		{
			System.err.println("Couldn't initialize Controllers: " + e.toString());
			joystick = null;
			inputEnabled = false;
		}
		if (joystick != null)
		{
			// note this is an instance of the private JInputController
			// http://grepcode.com/file/repo1.maven.org/maven2/org.lwjgl.lwjgl/lwjgl/2.8.2/org/lwjgl/input/JInputController.java
			try
			{
				Class c = joystick.getClass();
				Field buttonArray = c.getDeclaredField("buttonState");

				buttonArray.setAccessible(true);
				boolean[] b = (boolean[]) buttonArray.get(joystick);
				if (b == null)
					System.out.println("Boolean array null!");
				else
				{
					System.out.println("Found " + b.length + " buttons");
					for (boolean bool : b)
					{
						System.out.println(bool);
					}
				}

				System.out.println("!!Succeeded getting buttonState!!");
			}
			catch (Exception ex)
			{
				System.out.println("!!Failed getting buttonState!! " + ex.toString());
			}

		}
		return joystick != null;
	}

	public static void suspendControllerInput(boolean b)
	{
		ControllerSettings.suspendControllerInput = b;
		GameRenderHandler.joypadMouse.UnpressButtons();
	}

	public static boolean isSuspended()
	{
		return ControllerSettings.suspendControllerInput;
	}

	public static void setControllerBinding(int inputId, ControllerInputEvent inputEvent)
	{
		ControllerSettings.joyBindings[inputId].inputEvent = inputEvent;
	}
}
