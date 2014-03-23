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
	public boolean invertYAxis;
	public int inGameSensitivity;
	public int inMenuSensitivity;

	private Configuration config;
	private String userName;
	private String defaultCategory;

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
		inGameSensitivity = config.get(defaultCategory, "GameSensitivity", 40).getInt();
		inMenuSensitivity = config.get(defaultCategory, "GuiSensitivity", 10).getInt();
		lastConfigFileVersion = config.get(defaultCategory, "ConfigVersion", 0.07).getDouble(0.07);

		LogHelper.Info(userName + "'s JoyNo == " + preferedJoyNo + " (" + preferedJoyName + "). invertYAxis = "
				+ invertYAxis + ". ConfigVersion " + lastConfigFileVersion + ". Game Sensitivity multiplier: "
				+ inGameSensitivity + ". Menu Sensitivity multiplier: " + inMenuSensitivity);

		addBindingOptionsComment();
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

	public void addComment(String category, String comment)
	{
		config.addCustomCategoryComment(category, comment);
		config.save();
	}

	public void updatePreferedJoy(int joyNo, String joyName)
	{
		String category = defaultCategory;
		updateKey(category, "JoyNo", "" + joyNo);
		updateKey(category, "JoyName", joyName);
	}

	public void updateConfigFileSetting(UserJoypadSettings setting, String value)
	{
		updateKey(defaultCategory, setting.toString(), value);
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
