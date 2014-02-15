package com.shiny.joypadmod.helpers;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.Configuration;

import com.shiny.joypadmod.ControllerSettings;
import com.shiny.joypadmod.inputevent.ControllerBinding;

public class ConfigFile
{
	public int preferedJoyNo;
	public String preferedJoyName;

	private Configuration config;
	private String userName;
	private String defaultCategory;

	public ConfigFile(File configFile)
	{
		config = new Configuration(configFile);

		config.load();

		userName = "unknown";
		defaultCategory = "Joypad-" + userName;

		if (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().getSession() != null)
		{
			userName = Minecraft.getMinecraft().getSession().getUsername();
		}

		int joyNo = config.get(defaultCategory, "JoyNo", -1).getInt();
		System.out.println(userName + "'s JoyNo == " + joyNo);

		// user can forcibly disable the joypad code from registering by setting
		// joyNo to anything less than -99
		if (joyNo <= -100)
		{
			LogHelper.Warn("Warning, joypad is disabled for user " + userName + " to re-enable, open config file and change joyNo to -1");
			preferedJoyNo = -100;
		}
		else
		{
			preferedJoyNo = joyNo;
		}

		config.save();
	}

	/*
	 * private String createConfigFileSettingsString(String joyName) { return
	 * defaultCategory + "." + joyName + ".Settings"; }
	 */

	private String getLastName(String s)
	{
		return s.substring(s.indexOf('.') + 1);
	}

	private String createConfigSettingString(String joyName, String controlString)
	{
		return joyName + "." + getLastName(controlString);
	}

	public ControllerBinding[] getControllerBindings(String joyName, int joyNum)
	{
		// joyNum not currently used
		ControllerBinding[] controlBindings = ControllerSettings.getDefaultJoyBindings();
		String category = defaultCategory + "." + joyName;
		int i = 0;
		LogHelper.Info("Attempting to get joy info for " + category);

		try
		{
			for (; i < controlBindings.length; i++)
			{
				// ConfigCategory configCategory = new
				// ConfigCategory(createConfigSettingString(joyName,
				// controlBindings[i].inputString));

				String bindSettings = config.get(createConfigSettingString(joyName, controlBindings[i].inputString), getLastName(controlBindings[i].inputString),
						controlBindings[i].toConfigFileString()).getString();
				System.out.println("Received bindSettings: " + controlBindings[i].inputString + " " + bindSettings);
			}
			config.save();
		}
		catch (Exception ex)
		{
			LogHelper.Error("Failed trying to get controller bindings in config file:" + category + "-" + controlBindings[i].inputString + " Exception: " + ex.toString());
		}

		return controlBindings;
	}

	public void saveControllerBinding(String joyName, ControllerBinding binding)
	{
		config.load();

		String catToDelete = null;

		for (String s : config.getCategoryNames())
		{
			if (s.contains(getLastName(binding.inputString)))
			{
				catToDelete = s;
				break;
			}
			System.out.println(s);
		}

		String category = createConfigSettingString(joyName, binding.inputString);
		LogHelper.Info("Attempting to save " + binding.inputString + " " + binding.toConfigFileString() + " for " + category);
		try
		{
			String key = getLastName(binding.inputString);
			if (config.hasKey(catToDelete, key))
			{
				config.getCategory(catToDelete).remove(key);

				// config.save();
				// config.load();
			}
			config.get(catToDelete, key, binding.toConfigFileString());
			config.save();
		}
		catch (Exception ex)
		{
			LogHelper.Error("Failed trying to save controller binding category " + category + " value " + binding.inputString + ":" + binding.toConfigFileString() + ". Exception: " + ex.toString());
		}

	}
}
