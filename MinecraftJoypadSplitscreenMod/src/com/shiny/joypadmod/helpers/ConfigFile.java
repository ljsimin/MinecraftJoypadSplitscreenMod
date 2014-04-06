package com.shiny.joypadmod.helpers;

import java.io.File;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Configuration;

import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;

import com.google.common.collect.Iterables;
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
	private String bindingComment = "S:<actionID>=<Menu String>,{ <keycode> },<AXIS/BUTTON/POV>,<INDEX>,<THRESHOLD>,<DEADZONE>,<BINDING_OPTIONS1>,<BINDING_OPTIONS2>...";

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
			try
			{
				ControllerBinding binding = new ControllerBinding(key + "," + bindSettings, joyNo,
						lastConfigFileVersion);
				if (!binding.hasCategory())
					binding.bindingOptions.add(BindingOptions.CATEGORY_MISC);
				ControllerSettings.userDefinedBindings.add(binding);
			}
			catch (Exception ex)
			{
				LogHelper.Error("Exception caught trying to process user binding " + bindSettings + " Exception "
						+ ex.toString());
			}
		}
		return ControllerSettings.userDefinedBindings;
	}

	private boolean setControllerBinding(int joyNo, double lastJoyConfigVersion, ConfigCategory joyCategory)
	{
		LogHelper.Info("Parsing user settings in " + joyCategory.getQualifiedName());
		String bindSettings = "Not Set";
		try
		{
			if (!joyCategory.getChildren().isEmpty())
			{
				boolean bRet = true;
				for (ConfigCategory child : joyCategory.getChildren())
				{
					if (!setControllerBinding(joyNo, lastJoyConfigVersion, child))
						bRet = false;
				}
				return bRet;
			}
			String key = Iterables.getFirst(joyCategory.keySet(), "");
			// get binding string from the configuration file
			bindSettings = joyCategory.get(key).getString();
			if (!bindSettings.isEmpty())
			{
				// create binding based on this string from config file
				ControllerBinding tempBinding = new ControllerBinding(key + "," + bindSettings, joyNo,
						lastJoyConfigVersion);
				// check if this binding string was valid
				if (tempBinding.inputString == null)
				{
					LogHelper.Error("Found invalid entry in the config file: " + bindSettings + " ignoring");
					return false;
				}
				// get corresponding default binding if it exists
				ControllerBinding defaultBinding = ControllerSettings.joyBindingsMap.get(tempBinding.inputString);
				if (defaultBinding != null && lastJoyConfigVersion < 0.0953)
				{
					// update the default binding with the values from the config file
					defaultBinding.inputEvent = tempBinding.inputEvent;
					if (tempBinding.bindingOptions.contains(BindingOptions.IS_TOGGLE))
						defaultBinding.bindingOptions.add(BindingOptions.IS_TOGGLE);
				}
				else
				{
					LogHelper.Info("Found a non default binding in the config file. Will add it to the list of bindings."
							+ bindSettings);
					ControllerSettings.joyBindingsMap.put(tempBinding.inputString, tempBinding);
				}
				joyCategory.setComment(bindingComment);
				if (lastJoyConfigVersion < 0.0953)
				{
					saveControllerBinding(Controllers.getController(joyNo).getName(),
							ControllerSettings.joyBindingsMap.get(tempBinding.inputString));
				}
			}
		}
		catch (Exception ex)
		{
			LogHelper.Error("Failed parsing " + joyCategory.getQualifiedName() + " string value: " + bindSettings + " "
					+ ex.toString());
			return false;
		}
		return true;
	}

	public void updateControllerBindings(int joyNo, String joyName)
	{
		String category = defaultCategory + "." + joyName;
		LogHelper.Info("Attempting to get joy info for " + category);
		try
		{
			double lastJoyConfigVersion = config.get(category, "ConfigVersion", 0.07).getDouble(0.07);

			if (lastJoyConfigVersion < 0.0953)
			{
				// changed run to sprint so delete the old run. it will need to be rebound later
				if (this.updateKey(category + ".joy", "run", "", true))
					LogHelper.Info("Removed outdated binding \"run\". Please rebind \"sprint\" if necessary");
			}

			if (config.hasCategory(category + ".joy"))
			{
				for (ConfigCategory joySettings : config.getCategory(category + ".joy").getChildren())
				{
					setControllerBinding(joyNo, lastJoyConfigVersion, joySettings);
				}
			}

			updatePreferedJoy(joyNo, joyName);
			updateKey(category, "ConfigVersion", String.valueOf(JoypadMod.MINVERSION));
			updateKey(defaultCategory, "ConfigVersion", String.valueOf(JoypadMod.MINVERSION));
			config.save();
		}
		catch (Exception ex)
		{
			LogHelper.Error("Failed calling updateControllerBindings from config file. Exception: " + ex.toString());
		}
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

	public void deleteUserBinding(ControllerBinding binding)
	{
		String catToUpdate = "-UserBindings-";

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
