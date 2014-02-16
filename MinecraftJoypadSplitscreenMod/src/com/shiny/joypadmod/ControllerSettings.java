package com.shiny.joypadmod;

// Common code

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;

import com.shiny.joypadmod.helpers.ConfigFile;
import com.shiny.joypadmod.helpers.LogHelper;
import com.shiny.joypadmod.helpers.ModVersionHelper;
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

	public static ControllerBinding joyBindings[];

	public static boolean useConstantCameraMovement = false;
	public static boolean displayHints = false;
	public static boolean toggleSneak = true;
	public static Controller joystick;
	public static int joyNo = -1;
	public static int joyCameraSensitivity = 20;

	// used for some preliminary safe checks
	private static int requiredMinButtonCount = 4;
	private static int requiredButtonCount = 12;
	private static int requiredAxisCount = 4;

	public static Map<String, List<Integer>> validControllers;
	public static Map<String, List<Integer>> inValidControllers;
	public static ControllerUtils controllerUtils;

	// inputEnabled will control whether the mod will continually poll the
	// selected joystick for data
	public static boolean inputEnabled = false;
	// modDisabled will not set up the event handlers and will therefore render
	// the mod inoperable
	public static boolean modDisabled = false;
	// suspending the controller will tell the main controller loop to stop
	// polling.
	// this is used during the controller setup screen when listening for
	// controller events to map to an action
	private static boolean suspendControllerInput = false;

	private static ConfigFile config = null;

	public ControllerSettings(File configFile)
	{
		config = new ConfigFile(configFile);
		controllerUtils = new ControllerUtils();
		validControllers = new HashMap<String, List<Integer>>();
		inValidControllers = new HashMap<String, List<Integer>>();
	}

	public static ControllerBinding[] getDefaultJoyBindings()
	{
		LogHelper.Info("Setting default joy bindings");
		joyBindJump = new ControllerBinding("joy.jump", "Jump", new ButtonInputEvent(joyNo, 0));
		joyBindInventory = new ControllerBinding("joy.inventory", "Open inventory", new ButtonInputEvent(joyNo, 3));
		joyBindDrop = new ControllerBinding("joy.drop", "Drop", new ButtonInputEvent(joyNo, 6));
		joyBindSneak = new ControllerBinding("joy.sneak", "Sneak", new ButtonInputEvent(joyNo, 8));
		joyBindAttack = new ControllerBinding("joy.attack", "Attack", new AxisInputEvent(joyNo, 4, defaultAxisThreshhold * -1, defaultAxisDeadZone));
		joyBindUseItem = new ControllerBinding("joy.use", "Use", new AxisInputEvent(joyNo, 4, defaultAxisThreshhold, defaultAxisDeadZone));
		joyBindInteract = new ControllerBinding("joy.interact", "Interact", new ButtonInputEvent(joyNo, 2));
		joyBindGuiLeftClick = new ControllerBinding("joy.guiLeftClick", "Left click", new ButtonInputEvent(joyNo, 0));
		joyBindGuiRightClick = new ControllerBinding("joy.guiRightClick", "Right click", new ButtonInputEvent(joyNo, 2));
		joyBindPrevItem = new ControllerBinding("joy.prevItem", "Previous item", new ButtonInputEvent(joyNo, 4));
		joyBindNextItem = new ControllerBinding("joy.nextItem", "Next item", new ButtonInputEvent(joyNo, 5));
		joyBindRun = new ControllerBinding("joy.run", "Sprint", new ButtonInputEvent(joyNo, 9));
		joyBindMenu = new ControllerBinding("joy.menu", "Open menu", new ButtonInputEvent(joyNo, 7));
		joyBindShiftClick = new ControllerBinding("joy.shiftClick", "Shift-click", new ButtonInputEvent(joyNo, 1));
		joyCameraXplus = new ControllerBinding("joy.cameraX+", "Look right", new AxisInputEvent(joyNo, 3, defaultAxisThreshhold, defaultAxisDeadZone));
		joyCameraXminus = new ControllerBinding("joy.cameraX-", "Look left", new AxisInputEvent(joyNo, 3, defaultAxisThreshhold * -1, defaultAxisDeadZone));
		joyCameraYplus = new ControllerBinding("joy.cameraY+", "Look down", new AxisInputEvent(joyNo, 2, defaultAxisThreshhold, defaultAxisDeadZone));
		joyCameraYminus = new ControllerBinding("joy.cameraY-", "Look up", new AxisInputEvent(joyNo, 2, defaultAxisThreshhold * -1, defaultAxisDeadZone));
		joyMovementXplus = new ControllerBinding("joy.movementX+", "Strafe right", new AxisInputEvent(joyNo, 1, defaultAxisThreshhold, defaultAxisDeadZone));
		joyMovementXminus = new ControllerBinding("joy.movementX-", "Strafe left", new AxisInputEvent(joyNo, 1, defaultAxisThreshhold * -1, defaultAxisDeadZone));
		joyMovementYplus = new ControllerBinding("joy.movementY+", "Move forward", new AxisInputEvent(joyNo, 0, defaultAxisThreshhold, defaultAxisDeadZone));
		joyMovementYminus = new ControllerBinding("joy.movementY-", "Move backward", new AxisInputEvent(joyNo, 0, defaultAxisThreshhold * -1, defaultAxisDeadZone));
		joyGuiXplus = new ControllerBinding("joy.guiX+", "GUI right", new PovInputEvent(joyNo, 0, defaultPovThreshhold));
		joyGuiXminus = new ControllerBinding("joy.guiX-", "GUI left", new PovInputEvent(joyNo, 0, defaultPovThreshhold * -1));
		joyGuiYplus = new ControllerBinding("joy.guiY+", "GUI down", new PovInputEvent(joyNo, 1, defaultPovThreshhold));
		joyGuiYminus = new ControllerBinding("joy.guiY-", "GUI up", new PovInputEvent(joyNo, 1, defaultPovThreshhold * -1));

		return (new ControllerBinding[] { joyBindAttack, joyBindUseItem, joyBindJump, joyBindSneak, joyBindDrop, joyBindInventory, joyBindInteract, joyBindGuiLeftClick, joyBindGuiRightClick,
				joyBindPrevItem, joyBindNextItem, joyBindRun, joyBindMenu, joyBindShiftClick, joyCameraXplus, joyCameraXminus, joyCameraYplus, joyCameraYminus, joyMovementXplus, joyMovementXminus,
				joyMovementYplus, joyMovementYminus, joyGuiXplus, joyGuiXminus, joyGuiYplus, joyGuiYminus });
	}

	public void init()
	{
		config.init();

		if (config.preferedJoyName == "disabled")
		{
			LogHelper.Warn("Controller input disabled due to joypad value set to preferedJoyName set to disabled");
			ControllerSettings.inputEnabled = false;
			ControllerSettings.modDisabled = true;
			return;
		}

		LogHelper.Info("Initializing Controllers");

		// only set a controller as in use on init if they have previously gone
		// into controls to set it up
		// and it is detected as present

		int nControllers = detectControllers();
		if (nControllers > 0)
		{
			int selectedController = checkForControllerAtIndex(config.preferedJoyName, config.preferedJoyNo);
			if (selectedController >= 0)
			{
				setController(selectedController);
				Controllers.clearEvents();
			}
			else
			{
				LogHelper.Info("No joypad set up for this session.  Must enter controller menu to enable");
			}

		}
		else
		{
			LogHelper.Warn("No controllers detected!");
			ControllerSettings.inputEnabled = false;
		}
	}

	public int detectControllers()
	{
		validControllers.clear();
		inValidControllers.clear();

		try
		{
			if (!Controllers.isCreated())
				Controllers.create();

			if (Controllers.getControllerCount() > 0)
			{
				LogHelper.Info("Minecraft Joypad (Controller) Mod v" + ModVersionHelper.VERSION + " by Ljubomir Simin & Andrew Hickey\n---");
				LogHelper.Info("Found " + Controllers.getControllerCount() + " controller(s) in total.");
				for (int joyNo = 0; joyNo < Controllers.getControllerCount(); joyNo++)
				{
					Controller thisController = Controllers.getController(joyNo);

					logControllerInfo(thisController);

					if (controllerUtils.meetsInputRequirements(thisController, requiredButtonCount, requiredMinButtonCount, requiredAxisCount))
					{
						LogHelper.Info("Controller #" + joyNo + " ( " + thisController.getName() + ") meets the input requirements");
						addControllerToList(validControllers, thisController.getName(), joyNo);
					}
					else
					{
						LogHelper.Info("This controller does not meet the input requirements");
						addControllerToList(inValidControllers, thisController.getName(), joyNo);
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

	public boolean setController(int controllerNo)
	{
		LogHelper.Info("Attempting to use controller " + controllerNo);
		try
		{
			if (!Controllers.isCreated())
				Controllers.create();

			LogHelper.Info("Controllers.getControllerCount == " + Controllers.getControllerCount());

			if (controllerNo < 0 || controllerNo > Controllers.getControllerCount())
			{
				LogHelper.Error("Attempting to set controller index " + controllerNo + " there are currently " + Controllers.getControllerCount() + " controllers detected.");
				return false;
			}

			joystick = Controllers.getController(controllerNo);
			joyNo = controllerNo;
			controllerUtils.printDeadZones(joystick);

			joyBindings = config.getControllerBindings(controllerNo, joystick.getName());

			for (ControllerBinding binding : joyBindings)
			{
				binding.inputEvent.setDeadZone();
			}

			inputEnabled = true;
		}
		catch (Exception e)
		{
			System.err.println("Couldn't initialize Controllers: " + e.toString());
			joystick = null;
			inputEnabled = false;
		}

		// Test code
		// if (joystick != null)
		// {
		// // note this is an instance of the private JInputController
		// //
		// http://grepcode.com/file/repo1.maven.org/maven2/org.lwjgl.lwjgl/lwjgl/2.8.2/org/lwjgl/input/JInputController.java
		// try
		// {
		// Class c = joystick.getClass();
		// Field buttonArray = c.getDeclaredField("buttonState");
		//
		// buttonArray.setAccessible(true);
		// boolean[] b = (boolean[]) buttonArray.get(joystick);
		// if (b == null)
		// System.out.println("Boolean array null!");
		// else
		// {
		// System.out.println("Found " + b.length + " buttons");
		// for (boolean bool : b)
		// {
		// System.out.println(bool);
		// }
		// }
		//
		// System.out.println("!!Succeeded getting buttonState!!");
		// }
		// catch (Exception ex)
		// {
		// System.out.println("!!Failed getting buttonState!! " +
		// ex.toString());
		// }
		//
		// }

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
		config.saveControllerBinding(joystick.getName(), joyBindings[inputId]);
	}

	private static void addControllerToList(Map<String, List<Integer>> listToUse, String name, int id)
	{
		List<Integer> ids = null;
		if (listToUse.containsKey(name))
		{
			ids = listToUse.get(name);
		}
		else
		{
			ids = new ArrayList<Integer>();
		}
		ids.add(id);

		listToUse.put(name, ids);
	}

	// look for controllername in valid controllers
	// if not found return -1 indicating the controller wasn't found at all
	// if found:
	// if controller at selected index, then return that index
	// else return the first index it is found at
	private int checkForControllerAtIndex(String controllerName, int joyIndex)
	{
		if (validControllers.containsKey(controllerName))
		{
			List<Integer> ids = validControllers.get(controllerName);
			if (ids.contains(joyIndex))
				return joyIndex;

			return ids.get(0);
		}

		return -1;
	}

	private void logControllerInfo(Controller controller)
	{
		LogHelper.Info("Found controller " + controller.getName() + " (" + joyNo + ")");
		LogHelper.Info("It has  " + controller.getButtonCount() + " buttons.");
		LogHelper.Info("It has  " + controller.getAxisCount() + " axes.");
	}

	public List<Integer> flattenMap(Map<String, List<Integer>> listToFlatten)
	{
		List<Integer> values = new ArrayList<Integer>();
		Iterator<Entry<String, List<Integer>>> it = listToFlatten.entrySet().iterator();
		while (it.hasNext())
		{
			List<Integer> ids = it.next().getValue();
			for (int i = 0; i < ids.size(); i++)
			{
				values.add(ids.get(i));
			}
		}
		java.util.Collections.sort(values);

		return values;
	}

}
