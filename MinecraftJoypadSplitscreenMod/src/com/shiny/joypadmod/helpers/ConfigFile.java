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
	}

	public void init()
	{
		config.load();

		userName = "unknown";
		defaultCategory = "Joypad-" + userName;

		if (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().getSession() != null)
		{
			userName = Minecraft.getMinecraft().getSession().getUsername();
		}

		preferedJoyNo = config.get(defaultCategory, "JoyNo", -1).getInt();
		preferedJoyName = config.get(defaultCategory, "JoyName", "").getString();

		System.out.println(userName + "'s JoyNo == " + preferedJoyNo + " (" + preferedJoyName + ")");

		config.save();
	}

	public void updatePreferedJoy(int joyNo, String joyName)
	{
		String category = defaultCategory.toLowerCase();
		String[] keys = { "JoyNo", "JoyName" };
		int numKeys = joyName != null ? keys.length : 1;
		for (int i = 0; i < numKeys; i++)
		{
			if (config.hasKey(category, keys[i]))
			{
				config.getCategory(category).remove(keys[i]);
			}

			if (i == 0)
				config.get(category, keys[i], joyNo);
			else
				config.get(category, keys[i], joyName);
		}
		config.save();
	}

	private String createConfigSettingString(String joyName, String controlString)
	{
		return defaultCategory + "." + joyName + "." + controlString;
	}

	public ControllerBinding[] getControllerBindings(int joyNo, String joyName)
	{
		ControllerBinding[] controlBindings = ControllerSettings.getDefaultJoyBindings();
		String category = defaultCategory + "." + joyName;
		int i = 0;
		LogHelper.Info("Attempting to get joy info for " + category);

		try
		{
			for (; i < controlBindings.length; i++)
			{
				String bindSettings = config.get(createConfigSettingString(joyName, controlBindings[i].inputString), controlBindings[i].inputString, controlBindings[i].toConfigFileString())
						.getString();
				System.out.println("Received bindSettings: " + controlBindings[i].inputString + " " + bindSettings);
			}
			updatePreferedJoy(joyNo, joyName);
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
		String catToDelete = createConfigSettingString(joyName, binding.inputString).toLowerCase();

		LogHelper.Info("Attempting to save " + binding.inputString + " " + binding.toConfigFileString() + " for " + catToDelete);
		try
		{
			String key = binding.inputString;
			if (config.hasKey(catToDelete, key))
			{
				config.getCategory(catToDelete).remove(key);
			}
			config.get(catToDelete, key, binding.toConfigFileString());
			config.save();
		}
		catch (Exception ex)
		{
			LogHelper
					.Error("Failed trying to save controller binding category " + catToDelete + " value " + binding.inputString + ":" + binding.toConfigFileString() + ". Exception: " + ex.toString());
		}

	}
}
