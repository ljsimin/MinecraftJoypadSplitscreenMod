package com.shiny.joypadmod.helpers;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.Configuration;

import com.shiny.joypadmod.ControllerSettings;
import com.shiny.joypadmod.JoypadMod;
import com.shiny.joypadmod.inputevent.ControllerBinding;
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
			// userName = Minecraft.getMinecraft().getSession().getUsername();
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
		inGameSensitivity = config.get(defaultCategory, "GameSensitivity", 20).getInt();
		inMenuSensitivity = config.get(defaultCategory, "GuiSensitivity", 10).getInt();
		double lastConfigFileVersion = config.get(defaultCategory, "ConfigVersion", 0.07).getDouble(0.07);

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

	public ControllerBinding[] getControllerBindings(int joyNo, String joyName)
	{
		ControllerBinding[] controlBindingsDefault = ControllerSettings.getDefaultJoyBindings();
		String category = defaultCategory + "." + joyName;
		String bindingComment = "S:<actionID>=<Menu String>,<AXIS/BUTTON/POV>,<INDEX>,<THRESHOLD>,<DEADZONE>";
		int i = 0;
		LogHelper.Info("Attempting to get joy info for " + category);
		try
		{
			// this section of code needs comments or refactoring
			String versionCategory = defaultCategory + "." + joyName;
			double lastJoyConfigVersion = config.get(versionCategory, "ConfigVersion", 0.07).getDouble(0.07);
			for (; i < controlBindingsDefault.length; i++)
			{
				String bindingCategory = createConfigSettingString(joyName, controlBindingsDefault[i].inputString);
				String bindSettings = config.get(bindingCategory, controlBindingsDefault[i].inputString,
						controlBindingsDefault[i].toConfigFileString()).getString();
				config.addCustomCategoryComment(bindingCategory, bindingComment);
				LogHelper.Info("Received bindSettings: " + controlBindingsDefault[i].inputString + " " + bindSettings);
				if (!controlBindingsDefault[i].setToConfigFileString(bindSettings, joyNo, lastJoyConfigVersion))
				{
					LogHelper.Warn("Config file binding not accepted.  Resetting to default.  Config setting: "
							+ bindSettings);
					// reset to default in the file
					saveControllerBinding(joyName, controlBindingsDefault[i]);
				}
				// last time the config file changed how it interprets values
				else if (lastJoyConfigVersion < 0.08)
				{
					if (controlBindingsDefault[i].inputEvent.getEventType() == ControllerInputEvent.EventType.BUTTON)
						saveControllerBinding(joyName, controlBindingsDefault[i]);
				}
			}

			updatePreferedJoy(joyNo, joyName);
			updateKey(versionCategory, "ConfigVersion", String.valueOf(JoypadMod.MINVERSION));
			updateKey(defaultCategory, "ConfigVersion", String.valueOf(JoypadMod.MINVERSION));
			config.save();
		}
		catch (Exception ex)
		{
			LogHelper.Error("Failed trying to get controller bindings in config file:" + category + "-"
					+ controlBindingsDefault[i].inputString + " Exception: " + ex.toString());
		}

		// note: these may not actually be the default bindings depending on what was found in the config file
		return controlBindingsDefault;
	}

	public void saveControllerBinding(String joyName, ControllerBinding binding)
	{
		String catToUpdate = createConfigSettingString(joyName, binding.inputString);

		LogHelper.Info("Attempting to save " + binding.inputString + " " + binding.toConfigFileString() + " for "
				+ catToUpdate);
		updateKey(catToUpdate, binding.inputString, binding.toConfigFileString());
	}

	private String createConfigSettingString(String joyName, String controlString)
	{
		return defaultCategory + "." + joyName + "." + controlString;
	}

	private void updateKey(String category, String key, String value)
	{
		try
		{
			if (config.hasKey(category, key))
			{
				config.getCategory(category).remove(key);
			}
			config.get(category, key, value);
			config.save();
		}
		catch (Exception ex)
		{
			LogHelper.Error("Failed trying to save key " + category + " value " + key + ":" + value + ". Exception: "
					+ ex.toString());
		}
	}
}
