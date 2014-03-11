package com.shiny.joypadmod;

// Common code

import java.io.File;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Keyboard;

import com.shiny.joypadmod.helpers.ConfigFile;
import com.shiny.joypadmod.helpers.LogHelper;
import com.shiny.joypadmod.helpers.McObfuscationHelper;
import com.shiny.joypadmod.helpers.ModVersionHelper;
import com.shiny.joypadmod.inputevent.AxisInputEvent;
import com.shiny.joypadmod.inputevent.ButtonInputEvent;
import com.shiny.joypadmod.inputevent.ControllerBinding;
import com.shiny.joypadmod.inputevent.ControllerBinding.BindingOptions;
import com.shiny.joypadmod.inputevent.ControllerInputEvent;
import com.shiny.joypadmod.inputevent.ControllerUtils;

public class ControllerSettings
{

	public static final float defaultAxisDeadZone = 0.20f;
	public static final float defaultAxisThreshhold = 0.7f;
	public static final float defaultPovThreshhold = 0.9f;

	public enum JoyBindingEnum
	{
		joyBindAttack,
		joyBindUseItem,
		joyMovementYminus,
		joyMovementYplus,
		joyMovementXminus,
		joyMovementXplus,
		joyCameraYminus,
		joyCameraYplus,
		joyCameraXminus,
		joyCameraXplus,
		joyBindInteract,
		joyBindJump,
		joyBindSneak,
		joyBindRun,
		joyBindDrop,
		joyBindInventory,
		joyBindShiftClick,
		joyBindPrevItem,
		joyBindNextItem,
		joyBindMenu,
		joyBindGuiLeftClick,
		joyBindGuiRightClick,
		joyGuiYminus,
		joyGuiYplus,
		joyGuiXminus,
		joyGuiXplus,
		joyGuiCloseInventory,
		joyGuiScrollUp,
		joyGuiScrollDown
	}

	private static ControllerBinding joyBindings[] = null;
	private static List<ControllerBinding> userDefinedBindings = null;

	public static boolean useConstantCameraMovement = false;
	public static boolean displayHints = false;
	public static Controller joystick;
	public static int joyNo = -1;
	public static int inGameSensitivity = 20;
	public static int inMenuSensitivity = 20;
	public static int scrollDelay = 50;

	// used for some preliminary safe checks
	private static int requiredMinButtonCount = 4;
	private static int requiredButtonCount = 12;
	private static int requiredAxisCount = 4;

	public static Map<String, List<Integer>> validControllers;
	public static Map<String, List<Integer>> inValidControllers;
	public static ControllerUtils controllerUtils;

	// modDisabled will not set up the event handlers and will therefore render
	// the mod inoperable
	public static boolean modDisabled = false;

	// inputEnabled will control whether the mod will continually poll the
	// selected joystick for data
	private boolean inputEnabled = false;

	// suspending the controller will tell the main controller loop to stop
	// polling.
	// this is used during the controller setup screen when listening for
	// controller events to map to an action
	private static boolean suspendControllerInput = false;

	private static boolean invertYAxis = false;
	private static boolean toggleSneak = false;

	private static ConfigFile config = null;

	public ControllerSettings(File configFile)
	{
		config = new ConfigFile(configFile);
		controllerUtils = new ControllerUtils();
		validControllers = new HashMap<String, List<Integer>>();
		inValidControllers = new HashMap<String, List<Integer>>();
		userDefinedBindings = new ArrayList<ControllerBinding>();
	}

	public static ControllerBinding[] getDefaultJoyBindings()
	{
		GameSettings settings = Minecraft.getMinecraft().gameSettings;
		LogHelper.Info("Setting default joy bindings");
		ControllerBinding[] bindings = new ControllerBinding[JoyBindingEnum.values().length];
		bindings[JoyBindingEnum.joyBindJump.ordinal()] = new ControllerBinding("joy.jump", "Jump",
				new ButtonInputEvent(joyNo, 0, 1), new int[] { McObfuscationHelper.keyCode(settings.keyBindJump) }, 0,
				EnumSet.of(BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD));

		bindings[JoyBindingEnum.joyBindInventory.ordinal()] = new ControllerBinding("joy.inventory", "Open inventory",
				new ButtonInputEvent(joyNo, 3, 1),
				new int[] { McObfuscationHelper.keyCode(settings.keyBindInventory) }, 100,
				EnumSet.of(BindingOptions.GAME_BINDING));

		bindings[JoyBindingEnum.joyBindDrop.ordinal()] = new ControllerBinding("joy.drop", "Drop",
				new ButtonInputEvent(joyNo, 6, 1), new int[] { McObfuscationHelper.keyCode(settings.keyBindDrop) }, 0,
				EnumSet.of(BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD));

		bindings[JoyBindingEnum.joyBindSneak.ordinal()] = new ControllerBinding("joy.sneak", "Sneak",
				new ButtonInputEvent(joyNo, 8, 1), new int[] { McObfuscationHelper.keyCode(settings.keyBindSneak) }, 0,
				EnumSet.of(BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD));

		bindings[JoyBindingEnum.joyBindAttack.ordinal()] = new ControllerBinding("joy.attack", "Attack",
				new AxisInputEvent(joyNo, 4, defaultAxisThreshhold * -1, defaultAxisDeadZone), new int[] { -100 }, 0,
				EnumSet.of(BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD));

		bindings[JoyBindingEnum.joyBindUseItem.ordinal()] = new ControllerBinding("joy.use", "Use", new AxisInputEvent(
				joyNo, 4, defaultAxisThreshhold, defaultAxisDeadZone), new int[] { -99 }, 0,
				EnumSet.of(BindingOptions.GAME_BINDING));

		bindings[JoyBindingEnum.joyBindInteract.ordinal()] = new ControllerBinding("joy.interact", "Interact",
				new ButtonInputEvent(joyNo, 2, 1), new int[] { -99 }, 0, EnumSet.of(BindingOptions.GAME_BINDING));

		bindings[JoyBindingEnum.joyBindGuiLeftClick.ordinal()] = new ControllerBinding("joy.guiLeftClick",
				"Left click", new ButtonInputEvent(joyNo, 0, 1), new int[] { -100 }, 0,
				EnumSet.of(BindingOptions.MENU_BINDING));

		bindings[JoyBindingEnum.joyBindGuiRightClick.ordinal()] = new ControllerBinding("joy.guiRightClick",
				"Right click", new ButtonInputEvent(joyNo, 2, 1), new int[] { -99 }, 0,
				EnumSet.of(BindingOptions.MENU_BINDING));

		bindings[JoyBindingEnum.joyBindPrevItem.ordinal()] = new ControllerBinding("joy.prevItem", "Previous item",
				new ButtonInputEvent(joyNo, 4, 1), new int[] { -199 }, 0, EnumSet.of(BindingOptions.GAME_BINDING));

		bindings[JoyBindingEnum.joyBindNextItem.ordinal()] = new ControllerBinding("joy.nextItem", "Next item",
				new ButtonInputEvent(joyNo, 5, 1), new int[] { -201 }, 0, EnumSet.of(BindingOptions.GAME_BINDING));

		bindings[JoyBindingEnum.joyBindRun.ordinal()] = new ControllerBinding("joy.run", "Sprint",
				new ButtonInputEvent(joyNo, 9, 1), new int[] { Keyboard.KEY_LCONTROL }, 0, EnumSet.of(
						BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD));

		bindings[JoyBindingEnum.joyBindMenu.ordinal()] = new ControllerBinding("joy.menu", "Open menu",
				new ButtonInputEvent(joyNo, 7, 1), new int[] { Keyboard.KEY_ESCAPE }, 0, EnumSet.of(
						BindingOptions.GAME_BINDING, BindingOptions.MENU_BINDING));

		bindings[JoyBindingEnum.joyBindShiftClick.ordinal()] = new ControllerBinding("joy.shiftClick", "Shift-click",
				new ButtonInputEvent(joyNo, 1, 1), new int[] { Keyboard.KEY_LSHIFT, -100 }, 0, EnumSet.of(
						BindingOptions.MENU_BINDING, BindingOptions.REPEAT_IF_HELD));

		bindings[JoyBindingEnum.joyCameraXplus.ordinal()] = new ControllerBinding("joy.cameraX+", "Look right",
				new AxisInputEvent(joyNo, 3, defaultAxisThreshhold, defaultAxisDeadZone), null, 0, EnumSet.of(
						BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD));

		bindings[JoyBindingEnum.joyCameraXminus.ordinal()] = new ControllerBinding("joy.cameraX-", "Look left",
				new AxisInputEvent(joyNo, 3, defaultAxisThreshhold * -1, defaultAxisDeadZone), null, 0, EnumSet.of(
						BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD));

		bindings[JoyBindingEnum.joyCameraYminus.ordinal()] = new ControllerBinding("joy.cameraY-", "Look up",
				new AxisInputEvent(joyNo, 2, defaultAxisThreshhold * -1, defaultAxisDeadZone), null, 0, EnumSet.of(
						BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD));

		bindings[JoyBindingEnum.joyCameraYplus.ordinal()] = new ControllerBinding("joy.cameraY+", "Look down",
				new AxisInputEvent(joyNo, 2, defaultAxisThreshhold, defaultAxisDeadZone), null, 0, EnumSet.of(
						BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD));

		bindings[JoyBindingEnum.joyMovementXplus.ordinal()] = new ControllerBinding("joy.movementX+", "Strafe right",
				new AxisInputEvent(joyNo, 1, defaultAxisThreshhold, defaultAxisDeadZone),
				new int[] { McObfuscationHelper.keyCode(settings.keyBindRight) }, 0, EnumSet.of(
						BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD));

		bindings[JoyBindingEnum.joyMovementXminus.ordinal()] = new ControllerBinding("joy.movementX-", "Strafe left",
				new AxisInputEvent(joyNo, 1, defaultAxisThreshhold * -1, defaultAxisDeadZone),
				new int[] { McObfuscationHelper.keyCode(settings.keyBindLeft) }, 0, EnumSet.of(
						BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD));

		bindings[JoyBindingEnum.joyMovementYplus.ordinal()] = new ControllerBinding("joy.movementY+", "Move backward",
				new AxisInputEvent(joyNo, 0, defaultAxisThreshhold, defaultAxisDeadZone),
				new int[] { McObfuscationHelper.keyCode(settings.keyBindBack) }, 0, EnumSet.of(
						BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD));

		bindings[JoyBindingEnum.joyMovementYminus.ordinal()] = new ControllerBinding("joy.movementY-", "Move forward",
				new AxisInputEvent(joyNo, 0, defaultAxisThreshhold * -1, defaultAxisDeadZone),
				new int[] { McObfuscationHelper.keyCode(settings.keyBindForward) }, 0, EnumSet.of(
						BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD));

		bindings[JoyBindingEnum.joyGuiXplus.ordinal()] = new ControllerBinding("joy.guiX+", "GUI right",
				new AxisInputEvent(joyNo, 1, defaultAxisThreshhold, defaultAxisDeadZone), null, 0, EnumSet.of(
						BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD));

		bindings[JoyBindingEnum.joyGuiXminus.ordinal()] = new ControllerBinding("joy.guiX-", "GUI left",
				new AxisInputEvent(joyNo, 1, defaultAxisThreshhold * -1, defaultAxisDeadZone), null, 0, EnumSet.of(
						BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD));

		bindings[JoyBindingEnum.joyGuiYplus.ordinal()] = new ControllerBinding("joy.guiY+", "GUI down",
				new AxisInputEvent(joyNo, 0, defaultAxisThreshhold, defaultAxisDeadZone), null, 0, EnumSet.of(
						BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD));

		bindings[JoyBindingEnum.joyGuiYminus.ordinal()] = new ControllerBinding("joy.guiY-", "GUI up",
				new AxisInputEvent(joyNo, 0, defaultAxisThreshhold * -1, defaultAxisDeadZone), null, 0, EnumSet.of(
						BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD));

		bindings[JoyBindingEnum.joyGuiCloseInventory.ordinal()] = new ControllerBinding("joy.closeInventory",
				"Close container", new ButtonInputEvent(joyNo, 3, 1),
				new int[] { McObfuscationHelper.keyCode(settings.keyBindInventory) }, 100,
				EnumSet.of(BindingOptions.MENU_BINDING));

		bindings[JoyBindingEnum.joyGuiScrollDown.ordinal()] = new ControllerBinding("joy.scrollDown", "Scroll down",
				new ButtonInputEvent(joyNo, 5, 1), new int[] { -201 }, scrollDelay, EnumSet.of(
						BindingOptions.MENU_BINDING, BindingOptions.REPEAT_IF_HELD));

		bindings[JoyBindingEnum.joyGuiScrollUp.ordinal()] = new ControllerBinding("joy.scrollUp", "Scroll up",
				new ButtonInputEvent(joyNo, 4, 1), new int[] { -199 }, scrollDelay, EnumSet.of(
						BindingOptions.MENU_BINDING, BindingOptions.REPEAT_IF_HELD));

		return bindings;
	}

	public static ControllerBinding get(JoyBindingEnum joyBinding)
	{
		return get(joyBinding.ordinal());
	}

	public static ControllerBinding get(int joyBindingIndex)
	{
		if (joyBindings != null)
			return joyBindings[joyBindingIndex];

		return null;
	}

	public static int bindingListSize()
	{
		if (joyBindings == null)
			return 0;
		return joyBindings.length;
	}

	public void init()
	{
		LogHelper.Info("Minecraft Joypad (Controller) Mod v" + ModVersionHelper.VERSION
				+ " by Ljubomir Simin & Andrew Hickey\n---");

		config.init();

		if (config.preferedJoyName == "disabled")
		{
			LogHelper.Warn("Controller input disabled due to joypad value set to preferedJoyName set to disabled");
			inputEnabled = false;
			ControllerSettings.modDisabled = true;
			return;
		}

		invertYAxis = config.invertYAxis;
		toggleSneak = config.toggleSneak;
		inMenuSensitivity = config.inMenuSensitivity;// * (ModVersionHelper.getVersion() == 172 ? 2 : 1);
		inGameSensitivity = config.inGameSensitivity;// * (ModVersionHelper.getVersion() == 172 ? 2 : 1);

		LogHelper.Info("Initializing Controllers");

		// only set a controller as in use on init if they have previously gone
		// into controls to set it up
		// and it is detected as present

		int nControllers = detectControllers();
		int selectedController = -1;
		if (nControllers > 0 && config.preferedJoyNo >= 0)
		{
			selectedController = checkForControllerAtIndex(config.preferedJoyName, config.preferedJoyNo);
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

		if (selectedController < 0)
		{
			LogHelper.Warn("No joypad set up for this session."
					+ (nControllers > 0 ? " Must enter controller menu to enable." : ""));
			inputEnabled = false;
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
				LogHelper.Info("Found " + Controllers.getControllerCount() + " controller(s) in total.");
				for (int joyNo = 0; joyNo < Controllers.getControllerCount(); joyNo++)
				{
					Controller thisController = Controllers.getController(joyNo);

					logControllerInfo(thisController);

					if (controllerUtils.meetsInputRequirements(thisController, requiredButtonCount,
							requiredMinButtonCount, requiredAxisCount))
					{
						LogHelper.Info("Controller #" + joyNo + " ( " + thisController.getName()
								+ ") meets the input requirements");
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

			if (controllerNo < 0 || controllerNo >= Controllers.getControllerCount())
			{
				LogHelper.Error("Attempting to set controller index " + controllerNo + " there are currently "
						+ Controllers.getControllerCount() + " controllers detected.");
				return false;
			}

			joystick = Controllers.getController(controllerNo);
			joyNo = controllerNo;
			controllerUtils.printDeadZones(joystick);

			joyBindings = config.getControllerBindings(controllerNo, joystick.getName());
			setToggleSneak(toggleSneak);

			inputEnabled = true;
			Minecraft.getMinecraft().gameSettings.pauseOnLostFocus = false;
			ControllerSettings.resetGameAutoHandleBindings();
			ControllerSettings.resetMenuAutoHandleBindings();
		}
		catch (Exception e)
		{
			LogHelper.Error("Couldn't initialize Controllers: " + e.toString());
			joystick = null;
			inputEnabled = false;
		}

		return joystick != null;
	}

	public void setDefaultBindings()
	{
		if (joyNo < 0 || joystick == null || joyBindings == null)
			return;

		ControllerBinding[] bindings = getDefaultJoyBindings();
		for (int i = 0; i < bindings.length; i++)
		{
			if (!bindings[i].equals(joyBindings[i]))
			{
				setControllerBinding(i, bindings[i].inputEvent);
			}
		}
		setToggleSneak(toggleSneak);
	}

	public boolean isInputEnabled()
	{
		return inputEnabled;
	}

	public void setInputEnabled(boolean b)
	{
		inputEnabled = b;
		if (!b)
		{
			config.updatePreferedJoy(-1, null);
		}
		else
		{
			config.updatePreferedJoy(joyNo, null);
		}

	}

	private static long suspendMax;
	private static long suspendStart;

	public static void suspendControllerInput(boolean suspend, long maxTicksToSuspend)
	{
		if (suspend)
		{
			suspendStart = Minecraft.getSystemTime();
			suspendMax = maxTicksToSuspend;
		}
		ControllerSettings.suspendControllerInput = suspend;
		JoypadMouse.UnpressButtons();
	}

	public static boolean isSuspended()
	{
		if (ControllerSettings.suspendControllerInput)
		{
			if (Minecraft.getSystemTime() - suspendStart > suspendMax)
			{
				ControllerSettings.suspendControllerInput = false;
			}
		}
		return ControllerSettings.suspendControllerInput;
	}

	public static void setControllerBinding(int bindingIndex, ControllerInputEvent inputEvent)
	{
		ControllerSettings.joyBindings[bindingIndex].inputEvent = inputEvent;
		config.saveControllerBinding(joystick.getName(), joyBindings[bindingIndex]);
		if (joyBindings[bindingIndex].bindingOptions.contains(BindingOptions.GAME_BINDING))
			ControllerSettings.resetGameAutoHandleBindings();

		if (joyBindings[bindingIndex].bindingOptions.contains(BindingOptions.MENU_BINDING))
			ControllerSettings.resetMenuAutoHandleBindings();
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
		if (controllerName != null && validControllers.containsKey(controllerName))
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

	public static boolean getInvertYAxis()
	{
		return invertYAxis;
	}

	public static boolean getToggleSneak()
	{
		return toggleSneak;
	}

	public static void setInvertYAxis(boolean b)
	{
		if (invertYAxis != b)
		{
			invertYAxis = b;
			config.updateInvertJoypad(b);
		}
	}

	public static void setToggleSneak(boolean b)
	{
		if (toggleSneak != b)
		{
			toggleSneak = b;
			config.updateToggleSneak(b);
			if (joyBindings != null)
			{
				if (b)
				{
					joyBindings[JoyBindingEnum.joyBindSneak.ordinal()].bindingOptions.add(BindingOptions.IS_TOGGLE);
				}
				else
				{
					joyBindings[JoyBindingEnum.joyBindSneak.ordinal()].bindingOptions.remove(BindingOptions.IS_TOGGLE);
				}
			}
		}
		LogHelper.Info("Togglesneak set to " + b);
		resetGameAutoHandleBindings();
	}

	private static List<ControllerBinding> gameAutoHandleBindings = null;
	private static List<ControllerBinding> menuAutoHandleBindings = null;

	public static List<ControllerBinding> getGameAutoHandleBindings()
	{
		if (gameAutoHandleBindings == null)
		{
			gameAutoHandleBindings = new ArrayList<ControllerBinding>();
			for (ControllerBinding binding : joyBindings)
			{
				if (binding.keyCodes != null && binding.keyCodes.length != 0
						&& binding.bindingOptions.contains(BindingOptions.GAME_BINDING))
					gameAutoHandleBindings.add(binding);
			}
		}

		return gameAutoHandleBindings;
	}

	public static List<ControllerBinding> getMenuAutoHandleBindings()
	{
		if (menuAutoHandleBindings == null)
		{
			menuAutoHandleBindings = new ArrayList<ControllerBinding>();
			for (ControllerBinding binding : joyBindings)
			{
				if (binding.keyCodes != null && binding.keyCodes.length != 0
						&& binding.bindingOptions.contains(BindingOptions.MENU_BINDING))
					menuAutoHandleBindings.add(binding);
			}
		}

		return menuAutoHandleBindings;
	}

	public static void resetGameAutoHandleBindings()
	{
		if (gameAutoHandleBindings != null)
		{
			gameAutoHandleBindings.clear();
			gameAutoHandleBindings = null;
		}
	}

	public static void resetMenuAutoHandleBindings()
	{
		if (menuAutoHandleBindings != null)
		{
			menuAutoHandleBindings.clear();
			menuAutoHandleBindings = null;
		}
	}

	public static void unpressAll()
	{
		for (ControllerBinding binding : joyBindings)
		{
			if (binding.bindingOptions.contains(BindingOptions.IS_TOGGLE))
				binding.toggleState = false;
		}
		KeyBinding.unPressAllKeys();
	}
}
