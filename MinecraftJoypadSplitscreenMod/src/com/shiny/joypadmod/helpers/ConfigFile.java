package com.shiny.joypadmod.helpers;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;

import org.lwjgl.input.Controller;

import com.shiny.joypadmod.ControllerSettings;
import com.shiny.joypadmod.JoypadMod;
import com.shiny.joypadmod.inputevent.ControllerBinding;
import com.shiny.joypadmod.inputevent.ControllerBinding.BindingOptions;

public class ConfigFile
{
	public int preferedJoyNo;
	public String preferedJoyName;

	private Configuration config;
	private String userName;
	private String defaultCategory;
	private String userCategory;

	private double lastConfigFileVersion;

	public enum UserJoypadSettings
	{
		JoyNo, JoyName, InvertY, GameSensitivity, GuiSensitivity
	}

	public ConfigFile(File configFile)
	{
		config = new Configuration(configFile, true);
	}

	public void init()
	{
		config.load();

		String globalCat = "-Global-";
		boolean sharedProfile = config.get(globalCat, "SharedProfile", false).getBoolean(false);

		ControllerSettings.grabMouse = config.get(globalCat, "GrabMouse", false).getBoolean(false);
		ControllerSettings.loggingLevel = config.get(globalCat, "LoggingLevel", 1).getInt();

		userName = "unknown";

		if (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().getSession() != null)
		{
			userName = Minecraft.getMinecraft().getSession().getUsername();
		}

		defaultCategory = "Joypad-" + (sharedProfile ? "-Shared-" : userName);
		userCategory = "Joypad-" + userName;

		if (config.hasCategory(userCategory.toLowerCase()))
		{
			// using older case insensitive version
			config.removeCategory(config.getCategory(userCategory.toLowerCase()));
		}

		// always individual
		preferedJoyNo = config.get(userCategory, "JoyNo", -1).getInt();
		preferedJoyName = config.get(userCategory, "JoyName", "").getString();
		ControllerSettings.invertYAxis = config.get(userCategory, "InvertY", false).getBoolean(false);

		// individual or global
		ControllerSettings.inGameSensitivity = config.get(defaultCategory, "GameSensitivity", 40).getInt();
		ControllerSettings.inMenuSensitivity = config.get(defaultCategory, "GuiSensitivity", 10).getInt();
		lastConfigFileVersion = config.get(defaultCategory, "ConfigVersion", 0.07).getDouble(0.07);

		LogHelper.Info(userName + "'s JoyNo == " + preferedJoyNo + " (" + preferedJoyName + "). SharedProfile = "
				+ sharedProfile + ". GrabMouse = " + ControllerSettings.grabMouse + ".  invertYAxis = "
				+ ControllerSettings.invertYAxis + ". ConfigVersion " + lastConfigFileVersion
				+ ". Game Sensitivity multiplier: " + ControllerSettings.inGameSensitivity
				+ ". Menu Sensitivity multiplier: " + ControllerSettings.inMenuSensitivity);

		addBindingOptionsComment();
		addGlobalOptionsComment();
		config.save();
	}

	public void addBindingOptionsComment()
	{
		BindingOptions[] bos = ControllerBinding.BindingOptions.values();
		for (BindingOptions bo : bos)
		{
			config.get("-BindingOptions-", bo.toString(), " " + ControllerBinding.BindingOptionsComment[bo.ordinal()]);
		}
		config.addCustomCategoryComment("-BindingOptions-",
				"List of valid binding options that can be combined with Controller events");
	}

	public void addGlobalOptionsComment()
	{
		config.addCustomCategoryComment(
				"-Global-",
				"GrabMouse = will grab mouse when in game (generally not good for splitscreen)\r\n"
						+ "LoggingLevel = 0-4 levels of logging ranging from next to none to very verbose. 1 recommended unless debugging.\r\n"
						+ "SharedProfile = Will share joypad settings across all users except for invert");
	}

	public void addComment(String category, String comment)
	{
		config.addCustomCategoryComment(category, comment);
		config.save();
	}

	public void updatePreferedJoy(int joyNo, String joyName)
	{
		String category = userCategory;
		updateKey(category, "JoyNo", "" + joyNo);
		updateKey(category, "JoyName", joyName);
	}

	public void updateConfigFileSetting(UserJoypadSettings setting, String value)
	{
		String category = defaultCategory;
		switch (setting)
		{
		case JoyNo:
		case JoyName:
		case InvertY:
			category = userCategory;
			break;
		default:
			category = defaultCategory;
			break;
		}

		updateKey(category, setting.toString(), value);
	}

	public void applySavedDeadZones(Controller c)
	{
		ConfigCategory cc = config.getCategory("-Deadzones-." + c.getName());

		for (int i = 0; i < c.getAxisCount(); i++)
		{
			String key = c.getAxisName(i);
			if (cc.containsKey(key))
			{
				try
				{
					String floatStr = cc.get(key).getString();
					LogHelper.Info("Applying deadzone value " + floatStr + " to " + c.getAxisName(i));
					c.setDeadZone(i, Float.parseFloat(floatStr));
				}
				catch (Exception ex)
				{
					LogHelper.Error("Failed trying to apply deadzone for " + c.getAxisName(i) + " using the value for "
							+ key + " from the config file");
				}
			}
		}
	}

	public void updateConfigFileSettingEx(String category, String key, String value)
	{
		updateKey(category, key, value);
	}

	public List<ControllerBinding> getUserBindings(int joyNo)
	{
		String category = "-UserBindings-";
		ControllerSettings.userDefinedBindings.clear();

		ConfigCategory cc = config.getCategory(category);
		for (String key : cc.keySet())
		{
			String bindSettings = cc.get(key).getString();
			ControllerBinding binding = new ControllerBinding(key, "Trigger-" + key, null, null, 0,
					EnumSet.of(BindingOptions.GAME_BINDING));
			if (binding.setToConfigFileString(key + "," + bindSettings, joyNo, JoypadMod.MINVERSION))
				ControllerSettings.userDefinedBindings.add(binding);
		}
		return ControllerSettings.userDefinedBindings;
	}

	public List<ControllerBinding> getControllerBindings(int joyNo, String joyName)
	{
		List<ControllerBinding> controlBindingsDefault = new ArrayList<ControllerBinding>(
				Arrays.asList(ControllerSettings.getDefaultJoyBindings()));
		String category = defaultCategory + "." + joyName;
		String bindingComment = "S:<actionID>=<Menu String>,<AXIS/BUTTON/POV>,<INDEX>,<THRESHOLD>,<DEADZONE>,<BINDING_OPTIONS1>,<BINDING_OPTIONS2>...";
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
				else if (lastJoyConfigVersion < 0.0951)
				{
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

		updateKey(catToUpdate, binding.inputString, binding.toConfigFileString());

	}

	public void deleteControllerBinding(String joyName, ControllerBinding binding)
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

		LogHelper.Info("Attempting to delete " + binding.inputString + " " + binding.toConfigFileString() + " for "
				+ catToUpdate);

		updateKey(catToUpdate, binding.inputString, binding.toConfigFileString(), true);
	}

	private String createConfigSettingString(String joyName, String controlString)
	{
		return defaultCategory + "." + joyName + "." + controlString;
	}

	private boolean updateKey(String category, String key, String value)
	{
		return updateKey(category, key, value, false);
	}

	// boolean true returns if a new key was created or deleted.
	// false means key was updated
	private boolean updateKey(String category, String key, String value, boolean delete)
	{
		boolean bRet = true;
		try
		{
			if (config.hasKey(category, key))
			{
				config.getCategory(category).remove(key);
				bRet = delete ? true : false;
			}
			if (!delete)
			{
				config.get(category, key, value);
			}
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
