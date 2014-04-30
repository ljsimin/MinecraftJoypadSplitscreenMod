package com.shiny.joypadmod;

// Common code

import java.io.File;
import java.text.DecimalFormat;
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
import com.shiny.joypadmod.helpers.ConfigFile.UserJoypadSettings;
import com.shiny.joypadmod.helpers.LogHelper;
import com.shiny.joypadmod.helpers.McKeyBindHelper;
import com.shiny.joypadmod.helpers.McObfuscationHelper;
import com.shiny.joypadmod.helpers.ModVersionHelper;
import com.shiny.joypadmod.inputevent.AxisInputEvent;
import com.shiny.joypadmod.inputevent.ButtonInputEvent;
import com.shiny.joypadmod.inputevent.ControllerBinding;
import com.shiny.joypadmod.inputevent.ControllerBinding.BindingOptions;
import com.shiny.joypadmod.inputevent.ControllerUtils;
import com.shiny.joypadmod.lwjglVirtualInput.VirtualMouse;

public class ControllerSettings
{

	public static final float defaultAxisDeadZone = 0.20f;
	public static final float defaultAxisThreshhold = 0.7f;
	public static final float defaultPovThreshhold = 0.9f;

	public static List<ControllerBinding> userDefinedBindings;

	public static Map<String, ControllerBinding> joyBindingsMap = null;

	public static boolean useConstantCameraMovement = false;
	public static boolean displayHints = false;
	// public static Controller joystick;
	public static int joyNo = -1;

	public static int inGameSensitivity = 25;
	public static int inMenuSensitivity = 10;
	public static int scrollDelay = 50;

	public static int loggingLevel = 1;

	// used for some preliminary safe checks
	private static int requiredMinButtonCount = 4;
	private static int requiredButtonCount = 12;
	private static int requiredAxisCount = 4;

	private static Map<String, List<Integer>> validControllers;
	private static Map<String, List<Integer>> inValidControllers;
	public static ControllerUtils controllerUtils;

	// modDisabled will not set up the event handlers and will therefore render
	// the mod inoperable
	public static boolean modDisabled = false;

	// inputEnabled will control whether the mod will continually poll the
	// selected joystick for data
	private static boolean inputEnabled = false;

	// suspending the controller will tell the main controller loop to stop
	// polling.
	// this is used during the controller setup screen when listening for
	// controller events to map to an action
	private static boolean suspendControllerInput = false;

	public static boolean invertYAxis = false;
	public static boolean grabMouse = false;

	private static ConfigFile config = null;

	public ControllerSettings(File configFile)
	{
		config = new ConfigFile(configFile);
		config.init();
		controllerUtils = new ControllerUtils();
		validControllers = new HashMap<String, List<Integer>>();
		inValidControllers = new HashMap<String, List<Integer>>();
		joyBindingsMap = new HashMap<String, ControllerBinding>();
		userDefinedBindings = new ArrayList<ControllerBinding>();
		grabMouse = ControllerSettings.getGameOption("-Global-.GrabMouse").equals("true");
		try
		{
			Controllers.create();
		}
		catch (Exception ex)
		{
			LogHelper.Error("Failed creating controller object. " + ex.toString());
		}
	}

	private static int currentDisplayedMap = -1;

	public static void setDefaultJoyBindingMap(int joyIndex, boolean updateWithConfigFile)
	{
		if (currentDisplayedMap == joyIndex)
		{
			LogHelper.Info("Skipping setting up the joybinding map as it is already set up for this joypad");
			return;
		}

		currentDisplayedMap = joyIndex;

		GameSettings settings = Minecraft.getMinecraft().gameSettings;
		LogHelper.Info("Setting default joy binding map");

		joyBindingsMap.clear();

		int yAxisIndex = ControllerUtils.findYAxisIndex(joyIndex);
		int xAxisIndex = ControllerUtils.findXAxisIndex(joyIndex);

		joyBindingsMap.put(
				"joy.jump",
				new ControllerBinding("joy.jump", "Jump", new ButtonInputEvent(joyIndex, 0, 1),
						new int[] { McObfuscationHelper.keyCode(settings.keyBindJump) }, 0, EnumSet.of(
								BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD,
								BindingOptions.CATEGORY_MOVEMENT)));

		joyBindingsMap.put(
				"joy.inventory",
				new ControllerBinding("joy.inventory", "Open inventory", new ButtonInputEvent(joyIndex, 3, 1),
						new int[] { McObfuscationHelper.keyCode(settings.keyBindInventory) }, 100, EnumSet.of(
								BindingOptions.GAME_BINDING, BindingOptions.CATEGORY_INVENTORY)));

		joyBindingsMap.put(
				"joy.drop",
				new ControllerBinding("joy.drop", "Drop", new ButtonInputEvent(joyIndex, 6, 1),
						new int[] { McObfuscationHelper.keyCode(settings.keyBindDrop) }, 0, EnumSet.of(
								BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD,
								BindingOptions.CATEGORY_GAMEPLAY)));

		joyBindingsMap.put(
				"joy.sneak",
				new ControllerBinding("joy.sneak", "Sneak", new ButtonInputEvent(joyIndex, 8, 1),
						new int[] { McObfuscationHelper.keyCode(settings.keyBindSneak) }, 0, EnumSet.of(
								BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD,
								BindingOptions.CATEGORY_MOVEMENT)));

		joyBindingsMap.put(
				"joy.attack",
				new ControllerBinding("joy.attack", "Attack", new AxisInputEvent(joyIndex, 4, defaultAxisThreshhold
						* -1, defaultAxisDeadZone), new int[] { -100 }, 0, EnumSet.of(BindingOptions.GAME_BINDING,
						BindingOptions.REPEAT_IF_HELD, BindingOptions.CATEGORY_GAMEPLAY)));

		joyBindingsMap.put(
				"joy.use",
				new ControllerBinding("joy.use", "Use", new AxisInputEvent(joyIndex, 4, defaultAxisThreshhold,
						defaultAxisDeadZone), new int[] { -99 }, 0, EnumSet.of(BindingOptions.GAME_BINDING,
						BindingOptions.REPEAT_IF_HELD, BindingOptions.CATEGORY_GAMEPLAY)));

		joyBindingsMap.put(
				"joy.interact",
				new ControllerBinding("joy.interact", "Interact", new ButtonInputEvent(joyIndex, 2, 1),
						new int[] { -99 }, 0, EnumSet.of(BindingOptions.GAME_BINDING, BindingOptions.CATEGORY_GAMEPLAY)));

		joyBindingsMap.put("joy.guiLeftClick",
				new ControllerBinding("joy.guiLeftClick", "Left click", new ButtonInputEvent(joyIndex, 0, 1),
						new int[] { -100 }, 0, EnumSet.of(BindingOptions.MENU_BINDING, BindingOptions.CATEGORY_UI)));

		joyBindingsMap.put("joy.guiRightClick",
				new ControllerBinding("joy.guiRightClick", "Right click", new ButtonInputEvent(joyIndex, 2, 1),
						new int[] { -99 }, 0, EnumSet.of(BindingOptions.MENU_BINDING, BindingOptions.CATEGORY_UI)));

		joyBindingsMap.put(
				"joy.prevItem",
				new ControllerBinding("joy.prevItem", "Previous item", new ButtonInputEvent(joyIndex, 4, 1),
						new int[] { -199 }, 0,
						EnumSet.of(BindingOptions.GAME_BINDING, BindingOptions.CATEGORY_GAMEPLAY)));

		joyBindingsMap.put(
				"joy.nextItem",
				new ControllerBinding("joy.nextItem", "Next item", new ButtonInputEvent(joyIndex, 5, 1),
						new int[] { -201 }, 0,
						EnumSet.of(BindingOptions.GAME_BINDING, BindingOptions.CATEGORY_GAMEPLAY)));

		joyBindingsMap.put(
				"joy.sprint",
				new ControllerBinding("joy.sprint", "Sprint", new ButtonInputEvent(joyIndex, 9, 1),
						new int[] { Keyboard.KEY_LCONTROL }, 0, EnumSet.of(BindingOptions.GAME_BINDING,
								BindingOptions.REPEAT_IF_HELD, BindingOptions.CATEGORY_GAMEPLAY)));

		joyBindingsMap.put(
				"joy.menu",
				new ControllerBinding("joy.menu", "Open menu", new ButtonInputEvent(joyIndex, 7, 1),
						new int[] { Keyboard.KEY_ESCAPE }, 0, EnumSet.of(BindingOptions.GAME_BINDING,
								BindingOptions.MENU_BINDING, BindingOptions.CATEGORY_MISC)));

		joyBindingsMap.put(
				"joy.shiftClick",
				new ControllerBinding("joy.shiftClick", "Shift-click", new ButtonInputEvent(joyIndex, 1, 1), new int[] {
						Keyboard.KEY_LSHIFT, -100 }, 0, EnumSet.of(BindingOptions.MENU_BINDING,
						BindingOptions.REPEAT_IF_HELD, BindingOptions.CATEGORY_INVENTORY)));

		joyBindingsMap.put(
				"joy.cameraX+",
				new ControllerBinding("joy.cameraX+", "Look right", new AxisInputEvent(joyIndex, xAxisIndex + 2,
						defaultAxisThreshhold, defaultAxisDeadZone), null, 0, EnumSet.of(BindingOptions.GAME_BINDING,
						BindingOptions.REPEAT_IF_HELD, BindingOptions.CATEGORY_GAMEPLAY)));

		joyBindingsMap.put(
				"joy.cameraX-",
				new ControllerBinding("joy.cameraX-", "Look left", new AxisInputEvent(joyIndex, xAxisIndex + 2,
						defaultAxisThreshhold * -1, defaultAxisDeadZone), null, 0, EnumSet.of(
						BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD, BindingOptions.CATEGORY_GAMEPLAY)));

		joyBindingsMap.put(
				"joy.cameraY-",
				new ControllerBinding("joy.cameraY-", "Look up", new AxisInputEvent(joyIndex, xAxisIndex + 1,
						defaultAxisThreshhold * -1, defaultAxisDeadZone), null, 0, EnumSet.of(
						BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD, BindingOptions.CATEGORY_GAMEPLAY)));

		joyBindingsMap.put(
				"joy.cameraY+",
				new ControllerBinding("joy.cameraY+", "Look down", new AxisInputEvent(joyIndex, xAxisIndex + 1,
						defaultAxisThreshhold, defaultAxisDeadZone), null, 0, EnumSet.of(BindingOptions.GAME_BINDING,
						BindingOptions.REPEAT_IF_HELD, BindingOptions.CATEGORY_GAMEPLAY)));

		joyBindingsMap.put(
				"joy.right",
				new ControllerBinding("joy.right", "Strafe right", new AxisInputEvent(joyIndex, xAxisIndex,
						defaultAxisThreshhold, defaultAxisDeadZone),
						new int[] { McObfuscationHelper.keyCode(settings.keyBindRight) }, 0, EnumSet.of(
								BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD,
								BindingOptions.CATEGORY_MOVEMENT)));

		joyBindingsMap.put(
				"joy.left",
				new ControllerBinding("joy.left", "Strafe left", new AxisInputEvent(joyIndex, xAxisIndex,
						defaultAxisThreshhold * -1, defaultAxisDeadZone),
						new int[] { McObfuscationHelper.keyCode(settings.keyBindLeft) }, 0, EnumSet.of(
								BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD,
								BindingOptions.CATEGORY_MOVEMENT)));

		joyBindingsMap.put(
				"joy.back",
				new ControllerBinding("joy.back", "Move backward", new AxisInputEvent(joyIndex, yAxisIndex,
						defaultAxisThreshhold, defaultAxisDeadZone),
						new int[] { McObfuscationHelper.keyCode(settings.keyBindBack) }, yAxisIndex, EnumSet.of(
								BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD,
								BindingOptions.CATEGORY_MOVEMENT)));

		joyBindingsMap.put(
				"joy.forward",
				new ControllerBinding("joy.forward", "Move forward", new AxisInputEvent(joyIndex, yAxisIndex,
						defaultAxisThreshhold * -1, defaultAxisDeadZone),
						new int[] { McObfuscationHelper.keyCode(settings.keyBindForward) }, 0, EnumSet.of(
								BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD,
								BindingOptions.CATEGORY_MOVEMENT)));

		joyBindingsMap.put(
				"joy.guiX+",
				new ControllerBinding("joy.guiX+", "GUI right", new AxisInputEvent(joyIndex, xAxisIndex,
						defaultAxisThreshhold, defaultAxisDeadZone), null, 0, EnumSet.of(BindingOptions.MENU_BINDING,
						BindingOptions.REPEAT_IF_HELD, BindingOptions.CATEGORY_UI)));

		joyBindingsMap.put(
				"joy.guiX-",
				new ControllerBinding("joy.guiX-", "GUI left", new AxisInputEvent(joyIndex, xAxisIndex,
						defaultAxisThreshhold * -1, defaultAxisDeadZone), null, 0, EnumSet.of(
						BindingOptions.MENU_BINDING, BindingOptions.REPEAT_IF_HELD, BindingOptions.CATEGORY_UI)));

		joyBindingsMap.put(
				"joy.guiY+",
				new ControllerBinding("joy.guiY+", "GUI down", new AxisInputEvent(joyIndex, yAxisIndex,
						defaultAxisThreshhold, defaultAxisDeadZone), null, 0, EnumSet.of(BindingOptions.MENU_BINDING,
						BindingOptions.REPEAT_IF_HELD, BindingOptions.CATEGORY_UI)));

		joyBindingsMap.put(
				"joy.guiY-",
				new ControllerBinding("joy.guiY-", "GUI up", new AxisInputEvent(joyIndex, yAxisIndex,
						defaultAxisThreshhold * -1, defaultAxisDeadZone), null, 0, EnumSet.of(
						BindingOptions.MENU_BINDING, BindingOptions.REPEAT_IF_HELD, BindingOptions.CATEGORY_UI)));

		joyBindingsMap.put(
				"joy.closeInventory",
				new ControllerBinding("joy.closeInventory", "Close container", new ButtonInputEvent(joyIndex, 3, 1),
						new int[] { McObfuscationHelper.keyCode(settings.keyBindInventory) }, 100, EnumSet.of(
								BindingOptions.MENU_BINDING, BindingOptions.CATEGORY_INVENTORY)));

		joyBindingsMap.put(
				"joy.scrollDown",
				new ControllerBinding("joy.scrollDown", "Scroll down", new ButtonInputEvent(joyIndex, 5, 1),
						new int[] { -201 }, scrollDelay, EnumSet.of(BindingOptions.MENU_BINDING,
								BindingOptions.REPEAT_IF_HELD, BindingOptions.RENDER_TICK, BindingOptions.CATEGORY_UI)));

		joyBindingsMap.put(
				"joy.scrollUp",
				new ControllerBinding("joy.scrollUp", "Scroll up", new ButtonInputEvent(joyIndex, 4, 1),
						new int[] { -199 }, scrollDelay, EnumSet.of(BindingOptions.MENU_BINDING,
								BindingOptions.REPEAT_IF_HELD, BindingOptions.RENDER_TICK, BindingOptions.CATEGORY_UI)));

		if (updateWithConfigFile)
			config.getJoypadSavedBindings(joyIndex, Controllers.getController(joyIndex).getName());

		List<ControllerBinding> userBindings = config.getUserDefinedBindings(joyIndex);

		for (ControllerBinding b : userBindings)
		{
			joyBindingsMap.put(b.inputString, b);
		}
	}

	public static ControllerBinding get(String key)
	{
		return joyBindingsMap.get(key);
	}

	public static List<String> getBindingsWithCategory(String categoryString)
	{
		List<String> cList = new ArrayList<String>();
		for (Map.Entry<String, ControllerBinding> entry : joyBindingsMap.entrySet())
		{
			if (entry.getValue().getCategoryString().compareTo(categoryString) == 0)
				cList.add(entry.getValue().inputString);
		}
		return cList;
	}

	public static void delete(String key)
	{
		ControllerBinding binding = joyBindingsMap.get(key);
		if (binding != null)
		{
			if (binding.inputString.contains("user."))
			{
				userDefinedBindings.remove(binding);
			}
			joyBindingsMap.remove(key);
			config.deleteUserBinding(binding);
		}

	}

	public static int bindingListSize()
	{
		return joyBindingsMap.size();
	}

	public void init()
	{
		LogHelper.Info("Minecraft Joypad (Controller) Mod v" + ModVersionHelper.VERSION
				+ " by Ljubomir Simin & Andrew Hickey\n---");

		if (config.preferedJoyName == "disabled")
		{
			LogHelper.Warn("Controller input disabled due to joypad value 'preferedJoyName' set to disabled");
			inputEnabled = false;
			ControllerSettings.modDisabled = true;
			return;
		}

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

	private int detectControllers()
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
				for (int joyIndex = 0; joyIndex < Controllers.getControllerCount(); joyIndex++)
				{
					Controller thisController = Controllers.getController(joyIndex);

					logControllerInfo(thisController);

					if (controllerUtils.meetsInputRequirements(thisController, requiredButtonCount,
							requiredMinButtonCount, requiredAxisCount))
					{
						LogHelper.Info("Controller #" + joyIndex + " ( " + thisController.getName()
								+ ") meets the input requirements");
						addControllerToList(validControllers, thisController.getName(), joyIndex);
					}
					else
					{
						LogHelper.Info("This controller does not meet the input requirements");
						addControllerToList(inValidControllers, thisController.getName(), joyIndex);
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

	public static boolean setController(int controllerNo)
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

			ControllerSettings.setDefaultJoyBindingMap(controllerNo, true);
			joyNo = controllerNo;
			controllerUtils.printDeadZones(Controllers.getController(controllerNo));
			inputEnabled = true;

			applySavedDeadZones(joyNo);

			config.updatePreferedJoy(controllerNo, Controllers.getController(controllerNo).getName());

			Minecraft.getMinecraft().gameSettings.pauseOnLostFocus = false;
			JoypadMouse.AxisReader.centerCrosshairs();
			checkIfBindingsNeedUpdating();
			unpressAll();
			return true;
		}
		catch (Exception e)
		{
			LogHelper.Error("Couldn't initialize Controllers: " + e.toString());
			inputEnabled = false;
		}
		return false;
	}

	public static void resetBindings(int joyIndex)
	{
		if (joyIndex >= 0 && joyIndex < Controllers.getControllerCount())
		{
			currentDisplayedMap = -1;
			setDefaultJoyBindingMap(joyIndex, false);
			for (Map.Entry<String, ControllerBinding> entry : joyBindingsMap.entrySet())
			{
				if (!entry.getKey().contains("user."))
					config.saveControllerBinding(Controllers.getController(joyIndex).getName(), entry.getValue());
			}
		}

		unpressAll();
	}

	public static boolean isInputEnabled()
	{
		return inputEnabled;
	}

	public static void setInputEnabled(int joyIndex, boolean b)
	{
		unpressAll();
		if (!b)
		{
			JoypadMouse.AxisReader.setXY(0, 0);
			VirtualMouse.setXY(0, 0);
			inputEnabled = false;
			config.updatePreferedJoy(-1, null);
			return;
		}

		if (joyNo != joyIndex)
		{
			setController(joyIndex);
			return;
		}

		inputEnabled = true;
		config.updatePreferedJoy(joyIndex, Controllers.getController(joyIndex).getName());
		JoypadMouse.AxisReader.centerCrosshairs();
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

	public static void setControllerBinding(int joyIndex, String bindingKey, ControllerBinding binding)
	{
		ControllerSettings.joyBindingsMap.put(bindingKey, binding);
		config.saveControllerBinding(Controllers.getController(joyIndex).getName(), binding);
	}

	public static void unsetControllerBinding(int joyIndex, String key)
	{
		ControllerBinding binding = joyBindingsMap.get(key);
		if (binding != null)
		{
			binding.inputEvent = new ButtonInputEvent(0, -1, 1);
			config.saveControllerBinding(Controllers.getController(joyIndex).getName(), binding);
			unpressAll();
		}
	}

	public static void addUserBinding(ControllerBinding binding)
	{
		joyBindingsMap.put(binding.inputString, binding);
		userDefinedBindings.add(binding);
		config.saveControllerBinding(null, binding);
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
		LogHelper.Info("Found controller " + controller.getName() + " (" + controller.getIndex() + ")");
		LogHelper.Info("It has  " + controller.getButtonCount() + " buttons.");
		LogHelper.Info("It has  " + controller.getAxisCount() + " axes.");
	}

	private static List<Integer> flattenMap(Map<String, List<Integer>> listToFlatten)
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

	public static List<Integer> getJoypadList(boolean includeInvalid)
	{
		List<Integer> joypadList = ControllerSettings.flattenMap(ControllerSettings.validControllers);
		if (includeInvalid)
		{
			joypadList.addAll(ControllerSettings.flattenMap(ControllerSettings.inValidControllers));
		}
		return joypadList;
	}

	public static boolean getInvertYAxis()
	{
		return invertYAxis;
	}

	public static void setInvertYAxis(boolean b)
	{
		if (invertYAxis != b)
		{
			invertYAxis = b;
			config.updateConfigFileSetting(UserJoypadSettings.InvertY, "" + b);
		}
	}

	public static void setToggle(int joyIndex, String bindingKey, boolean b)
	{

		ControllerBinding binding = joyBindingsMap.get(bindingKey);
		boolean changed = false;
		if (b)
		{
			changed = binding.bindingOptions.add(BindingOptions.IS_TOGGLE);
		}
		else
		{
			changed = binding.bindingOptions.remove(BindingOptions.IS_TOGGLE);
		}

		if (changed)
		{
			setControllerBinding(joyIndex, bindingKey, binding);
		}
	}

	private static Iterator<Entry<String, ControllerBinding>> gameBindIterator;
	private static Iterator<Entry<String, ControllerBinding>> menuBindIterator;

	private static ControllerBinding getNextBinding(Iterator<Entry<String, ControllerBinding>> current,
			BindingOptions target)
	{
		while (current.hasNext())
		{
			Map.Entry<String, ControllerBinding> entry = current.next();
			if (entry.getValue().bindingOptions.contains(target) && entry.getValue().inputEvent.isValid())
			{
				return entry.getValue();
			}
		}

		return null;
	}

	public static ControllerBinding getNextGameAutoBinding()
	{
		return getNextBinding(gameBindIterator, BindingOptions.GAME_BINDING);
	}

	public static ControllerBinding getNextMenuAutoBinding()
	{
		return getNextBinding(menuBindIterator, BindingOptions.MENU_BINDING);
	}

	public static ControllerBinding startGameBindIteration()
	{
		gameBindIterator = joyBindingsMap.entrySet().iterator();
		return getNextGameAutoBinding();
	}

	public static ControllerBinding startMenuBindIteration()
	{
		menuBindIterator = joyBindingsMap.entrySet().iterator();
		return getNextMenuAutoBinding();
	}

	public static void unpressAll()
	{
		for (Map.Entry<String, ControllerBinding> entry : joyBindingsMap.entrySet())
		{
			if (entry.getValue().bindingOptions.contains(BindingOptions.IS_TOGGLE))
				entry.getValue().toggleState = false;
		}

		KeyBinding.unPressAllKeys();
		VirtualMouse.unpressAllButtons();
	}

	public static void saveSensitivityValues()
	{
		LogHelper.Info("Saving game sensitivity value: " + ControllerSettings.inGameSensitivity);
		config.updateConfigFileSetting(ConfigFile.UserJoypadSettings.GameSensitivity, ""
				+ ControllerSettings.inGameSensitivity);
		LogHelper.Info("Saving menu sensitivity value: " + ControllerSettings.inMenuSensitivity);
		config.updateConfigFileSetting(ConfigFile.UserJoypadSettings.GuiSensitivity, ""
				+ ControllerSettings.inMenuSensitivity);
	}

	public static void saveDeadZones(int joyId)
	{
		Controller controller = Controllers.getController(joyId);
		DecimalFormat df = new DecimalFormat("#0.00");

		for (int i = 0; i < controller.getAxisCount(); i++)
		{
			config.setConfigFileSetting("-Deadzones-." + controller.getName(), controller.getAxisName(i),
					df.format(controller.getDeadZone(i)));
		}
		config.addComment("-Deadzones-", "Deadzone values here will override values in individual bindings");
		LogHelper.Info("Saved deadzones for " + controller.getName());
	}

	private static void saveCurrentJoyBindings()
	{
		String joyName = Controllers.getController(currentDisplayedMap).getName();
		for (Map.Entry<String, ControllerBinding> entry : joyBindingsMap.entrySet())
		{
			config.saveControllerBinding(joyName, entry.getValue());
		}
	}

	public static void applySavedDeadZones(int joyId)
	{
		if (joyId < 0)
			return;

		LogHelper.Info("Applying configurated deadzones");

		config.applySavedDeadZones(Controllers.getController(joyId));

	}

	public static ControllerBinding findControllerBindingWithKey(int keyCode, BindingOptions option)
	{
		for (Map.Entry<String, ControllerBinding> entry : joyBindingsMap.entrySet())
		{
			if (entry.getValue().inputEvent.isValid() && entry.getValue().keyCodes != null
					&& entry.getValue().bindingOptions != null && entry.getValue().bindingOptions.contains(option))
			{
				for (int bindKeyCode : entry.getValue().keyCodes)
				{
					if (bindKeyCode == keyCode)
					{
						return entry.getValue();
					}
				}
			}
		}

		return null;
	}

	// call this when there is a possibility of a key change
	public static void checkIfBindingsNeedUpdating()
	{
		if (joyNo < 0)
			return;

		for (Map.Entry<String, ControllerBinding> entry : joyBindingsMap.entrySet())
		{
			if (entry.getValue().inputEvent.isValid() && entry.getValue().keyCodes != null
					&& entry.getValue().keyCodes.length >= 1 && !entry.getKey().contains("user."))
			{
				KeyBinding kb = McKeyBindHelper.getMinecraftKeyBind(entry.getKey());
				if (kb == null && entry.getKey().contains("joy."))
					kb = McKeyBindHelper.getMinecraftKeyBind(entry.getKey().replace("joy.", "key."));
				if (kb != null)
				{
					int keyCode = McObfuscationHelper.keyCode(kb);
					if (entry.getValue().keyCodes[0] != keyCode)
					{
						entry.getValue().keyCodes = new int[] { keyCode };
						setControllerBinding(joyNo, entry.getKey(), entry.getValue());
					}
				}
			}
		}
	}

	public static boolean checkIfDuplicateBinding(String bindingKey)
	{
		ControllerBinding b = get(bindingKey);
		if (b == null || !b.inputEvent.isValid())
			return false;

		for (Map.Entry<String, ControllerBinding> entry : joyBindingsMap.entrySet())
		{
			if (entry.getValue().inputEvent.isValid() && !entry.getKey().equals(bindingKey)
					&& entry.getValue().inputEvent.equals(b.inputEvent))
			{
				if ((b.bindingOptions.contains(BindingOptions.GAME_BINDING) && entry.getValue().bindingOptions.contains(BindingOptions.GAME_BINDING))
						|| (b.bindingOptions.contains(BindingOptions.MENU_BINDING) && entry.getValue().bindingOptions.contains(BindingOptions.MENU_BINDING)))
					return true;
			}
		}

		return false;
	}

	public static String getGameOption(String optionKey)
	{
		return config.getConfigFileSetting(optionKey);
	}

	public static void setGameOption(String optionKey, String value)
	{
		config.setConfigFileSetting(optionKey, value);
		if (optionKey.contains("SharedProfile") && currentDisplayedMap != -1)
		{
			saveCurrentJoyBindings();
		}
		else if (optionKey.contains("GrabMouse"))
		{
			grabMouse = Boolean.parseBoolean(value);
		}
	}
}
