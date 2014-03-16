package com.shiny.joypadmod.helpers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Configuration;

import com.shiny.joypadmod.ControllerSettings;
import com.shiny.joypadmod.JoypadMod;
import com.shiny.joypadmod.inputevent.ControllerBinding;
import com.shiny.joypadmod.inputevent.ControllerBinding.BindingOptions;
import com.shiny.joypadmod.inputevent.ControllerInputEvent;

public class ConfigFile
{
	public int preferedJoyNo;
	public String preferedJoyName;
	public boolean invertYAxis;
	public boolean toggleSneak;
	public int inGameSensitivity;
	public int inMenuSensitivity;

	private Configuration config;
	private String userName;
	private String defaultCategory;

	private double lastConfigFileVersion;

	public ConfigFile(File configFile)
	{
		config = new Configuration(configFile, true);
	}

	public void init()
	{
		config.load();

		userName = "unknown";

		if (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().getSession() != null)
		{
			userName = Minecraft.getMinecraft().getSession().getUsername();
		}

		defaultCategory = "Joypad-" + userName;

		if (config.hasCategory(defaultCategory.toLowerCase()))
		{
			// using older case insensitive version
			config.removeCategory(config.getCategory(defaultCategory.toLowerCase()));
		}

		preferedJoyNo = config.get(defaultCategory, "JoyNo", -1).getInt();
		preferedJoyName = config.get(defaultCategory, "JoyName", "").getString();
		invertYAxis = config.get(defaultCategory, "InvertY", false).getBoolean(false);
		toggleSneak = config.get(defaultCategory, "ToggleSneak", false).getBoolean(false);
		inGameSensitivity = config.get(defaultCategory, "GameSensitivity", 40).getInt();
		inMenuSensitivity = config.get(defaultCategory, "GuiSensitivity", 10).getInt();
		lastConfigFileVersion = config.get(defaultCategory, "ConfigVersion", 0.07).getDouble(0.07);

		LogHelper.Info(userName + "'s JoyNo == " + preferedJoyNo + " (" + preferedJoyName + "). ToggleSneak = "
				+ toggleSneak + ". invertYAxis = " + invertYAxis + ". ConfigVersion " + lastConfigFileVersion
				+ ". Game Sensitivity multiplier: " + inGameSensitivity + ". Menu Sensitivity multiplier: "
				+ inMenuSensitivity);

		config.save();
	}

	public void updatePreferedJoy(int joyNo, String joyName)
	{
		String category = defaultCategory;
		String[] keys = { "JoyNo", "JoyName" };
		int numKeys = joyName != null ? keys.length : 1;
		for (int i = 0; i < numKeys; i++)
		{
			updateKey(category, keys[i], (i == 0 ? String.valueOf(joyNo) : joyName));
		}
	}

	public void updateInvertJoypad(boolean invert)
	{
		updateKey(defaultCategory, "InvertY", String.valueOf(invert));
	}

	public void updateToggleSneak(boolean toggleSneak)
	{
		updateKey(defaultCategory, "ToggleSneak", String.valueOf(toggleSneak));
	}

	public List<ControllerBinding> getUserBindings(int joyNo)
	{
		String category = "-UserBindings-";
		int numBindings = config.get(category, "NumBindings", 0).getInt();
		List<ControllerBinding> bindings = new ArrayList<ControllerBinding>();

		for (int i = 0; i < numBindings; i++)
		{
			String inputString = "user." + i;
			// String bindingComment = "S:" + inputString
			// + "=<Menu String>,<AXIS/BUTTON/POV>,<INDEX>,<THRESHOLD>,<DEADZONE>";
			String bindSettings = config.get(category, inputString, "Trigger1,{R},POV,1,1,0").getString();
			ControllerBinding binding = new ControllerBinding(inputString, "menu", null, null, 0,
					EnumSet.of(BindingOptions.GAME_BINDING));
			binding.setToConfigFileString(inputString + "," + bindSettings, joyNo, lastConfigFileVersion);
			bindings.add(binding);
		}
		return bindings;
	}

	public List<ControllerBinding> getControllerBindings(int joyNo, String joyName)
	{
		List<ControllerBinding> controlBindingsDefault = new ArrayList<ControllerBinding>(
				Arrays.asList(ControllerSettings.getDefaultJoyBindings()));
		String category = defaultCategory + "." + joyName;
		String bindingComment = "S:<actionID>=<Menu String>,<AXIS/BUTTON/POV>,<INDEX>,<THRESHOLD>,<DEADZONE>";
		int i = 0;
		LogHelper.Info("Attempting to get joy info for " + category);
		try
		{
			// getUserBindings();
			// this section of code needs comments or refactoring
			String versionCategory = defaultCategory + "." + joyName;
			double lastJoyConfigVersion = config.get(versionCategory, "ConfigVersion", 0.07).getDouble(0.07);
			for (; i < controlBindingsDefault.size(); i++)
			{
				String bindingCategory = createConfigSettingString(joyName, controlBindingsDefault.get(i).inputString);
				String bindSettings = config.get(bindingCategory, controlBindingsDefault.get(i).inputString,
						controlBindingsDefault.get(i).toConfigFileString()).getString();
				config.addCustomCategoryComment(bindingCategory, bindingComment);
				LogHelper.Info("Received bindSettings: " + controlBindingsDefault.get(i).inputString + " "
						+ bindSettings);
				if (!controlBindingsDefault.get(i).setToConfigFileString(bindSettings, joyNo, lastJoyConfigVersion))
				{
					LogHelper.Warn("Config file binding not accepted.  Resetting to default.  Config setting: "
							+ bindSettings);
					// reset to default in the file
					saveControllerBinding(joyName, controlBindingsDefault.get(i));
				}
				// last time the config file changed how it interprets values
				else if (lastJoyConfigVersion < 0.08)
				{
					if (controlBindingsDefault.get(i).inputEvent.getEventType() == ControllerInputEvent.EventType.BUTTON)
						saveControllerBinding(joyName, controlBindingsDefault.get(i));
				}
			}

			List<ControllerBinding> bindings = getUserBindings(joyNo);
			for (int j = 0; j < bindings.size(); j++)
				controlBindingsDefault.add(bindings.get(j));

			updatePreferedJoy(joyNo, joyName);
			updateKey(versionCategory, "ConfigVersion", String.valueOf(JoypadMod.MINVERSION));
			updateKey(defaultCategory, "ConfigVersion", String.valueOf(JoypadMod.MINVERSION));
			config.save();
		}
		catch (Exception ex)
		{
			LogHelper.Error("Failed trying to get controller bindings in config file:" + category + "-"
					+ controlBindingsDefault.get(i).inputString + " Exception: " + ex.toString());
		}

		// note: these may not actually be the default bindings depending on what was found in the config file
		return controlBindingsDefault;
	}

	public void saveControllerBinding(String joyName, ControllerBinding binding)
	{
		String catToUpdate;
		boolean userBinding = binding.inputString.toLowerCase().contains("user");
		if (userBinding)
		{
			catToUpdate = "-UserBindings-";
		}
		else
		{
			catToUpdate = createConfigSettingString(joyName, binding.inputString);
		}

		LogHelper.Info("Attempting to save " + binding.inputString + " " + binding.toConfigFileString() + " for "
				+ catToUpdate);

		if (updateKey(catToUpdate, binding.inputString, binding.toConfigFileString()) && userBinding)
		{
			int numBindings = config.get(catToUpdate, "NumBindings", 0).getInt();
			updateKey(catToUpdate, "NumBindings", "" + (numBindings + 1));
		}

	}

	private String createConfigSettingString(String joyName, String controlString)
	{
		return defaultCategory + "." + joyName + "." + controlString;
	}

	// boolean true returns if a new key was created.
	// false means key was updated
	private boolean updateKey(String category, String key, String value)
	{
		boolean bRet = true;
		try
		{
			if (config.hasKey(category, key))
			{
				config.getCategory(category).remove(key);
				bRet = false;
			}
			config.get(category, key, value);
			config.save();
		}
		catch (Exception ex)
		{
			LogHelper.Error("Failed trying to save key " + category + " value " + key + ":" + value + ". Exception: "
					+ ex.toString());
		}
		return bRet;
	}
}
