package com.shiny.joypadmod.helpers;

import java.io.File;
import java.util.ArrayList;
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
	private File _configFile;
	private String userName;
	private String userCategory;
	private String bindingComment = "S:<actionID>=<Menu String>,{ <keycode> },<AXIS/BUTTON/POV>,<INDEX>,<THRESHOLD>,<DEADZONE>,<BINDING_OPTIONS1>,<BINDING_OPTIONS2>...";
	private List<ControllerBinding> controlBindingsFromConfigFile = new ArrayList<ControllerBinding>();

	private String globalCat = "-Global-";

	private double lastConfigFileVersion;

	public enum UserJoypadSettings
	{
		JoyNo, JoyName, InvertY, GameSensitivity, GuiSensitivity
	}

	public ConfigFile(File configFile)
	{
		_configFile = configFile;
		reload();
	}

	private void reload()
	{
		config = new Configuration(_configFile, true);
	}

	public void init()
	{
		config.load();

		ControllerSettings.loggingLevel = config.get(globalCat, "LoggingLevel", 1).getInt();

		userName = "unknown";

		if (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().getSession() != null)
		{
			userName = Minecraft.getMinecraft().getSession().getUsername();
		}

		setSharedProfile(config.get(globalCat, "SharedProfile", false).getBoolean(false));

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
		lastConfigFileVersion = config.get(getDefaultCategory(), "ConfigVersion", 0.07).getDouble(0.07);

		if (lastConfigFileVersion < 0.0953)
		{
			// delete outdated NumBindings tag
			this.deleteKey("-UserBindings-", "NumBindings");
		}

		LogHelper.Info(userName + "'s JoyNo == " + preferedJoyNo + " (" + preferedJoyName + "). SharedProfile = "
				+ getConfigFileSetting("-Global-.SharedProfile") + ". GrabMouse = "
				+ getConfigFileSetting("-Global-.GrabMouse") + ".  invertYAxis = " + ControllerSettings.invertYAxis
				+ ". ConfigVersion " + lastConfigFileVersion + ". Game Sensitivity multiplier: "
				+ ControllerSettings.inGameSensitivity + ". Menu Sensitivity multiplier: "
				+ ControllerSettings.inMenuSensitivity);

		addBindingOptionsComment();
		addGlobalOptionsComment();

		updateKey(getDefaultCategory(), "ConfigVersion", String.valueOf(JoypadMod.MINVERSION), true);
	}

	public String getDefaultCategory()
	{
		if (config.get(globalCat, "SharedProfile", false).getString().equals("true"))
		{
			return "Joypad--Shared-";
		}
		return "Joypad-" + userName;
	}

	public void setSharedProfile(boolean shared)
	{
		LogHelper.Info("Setting shared profile to " + shared);

		updateKey(globalCat, "SharedProfile", "" + shared, true);
		// individual or global
		ControllerSettings.inGameSensitivity = config.get(getDefaultCategory(), "GameSensitivity", 40).getInt();
		ControllerSettings.inMenuSensitivity = config.get(getDefaultCategory(), "GuiSensitivity", 10).getInt();
	}

	public void addComment(String category, String comment)
	{
		config.addCustomCategoryComment(category, comment);
		config.save();
	}

	public void updatePreferedJoy(int joyNo, String joyName)
	{
		String category = userCategory;
		updateKey(category, "JoyNo", "" + joyNo, false);
		updateKey(category, "JoyName", joyName, true);
	}

	public void updateConfigFileSetting(UserJoypadSettings setting, String value)
	{
		String category;
		switch (setting)
		{
		case JoyNo:
		case JoyName:
		case InvertY:
			category = userCategory;
			break;
		default:
			category = getDefaultCategory();
			break;
		}

		updateKey(category, setting.toString(), value, true);
	}

	public String getConfigFileSetting(String categoryKey)
	{
		int lastIndex = categoryKey.lastIndexOf('.');
		String category = categoryKey.substring(0, lastIndex);
		String key = categoryKey.substring(lastIndex + 1);
		return config.get(category, key, false).getString();
	}

	public void setConfigFileSetting(String categoryKey, String value)
	{
		int lastIndex = categoryKey.lastIndexOf('.');
		String category = categoryKey.substring(0, lastIndex);
		String key = categoryKey.substring(lastIndex + 1);
		setConfigFileSetting(category, key, value);
	}

	public void setConfigFileSetting(String category, String key, String value)
	{
		updateKey(category, key, value, true);
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

	public void deleteUserBinding(ControllerBinding binding)
	{
		String catToUpdate = "-UserBindings-";

		LogHelper.Info("Attempting to delete " + binding.inputString + " " + binding.toConfigFileString() + " for "
				+ catToUpdate);

		deleteKey(catToUpdate, binding.inputString);
	}

	public void saveControllerBinding(String joyName, ControllerBinding binding)
	{
		saveControllerBindingInternal(joyName, binding, true);
	}

	public List<ControllerBinding> getUserDefinedBindings(int joyNo)
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

	public void getJoypadSavedBindings(int joyNo, String joyName)
	{
		String category = getDefaultCategory() + "." + joyName;

		if (!config.hasCategory(category))
			return;

		double lastJoyConfigVersion = config.get(category, "ConfigVersion", 0.07).getDouble(0.07);

		boolean cleanupBindings = lastJoyConfigVersion < 0.0953;

		int numBindingsFound = populateBindingList(joyNo, joyName, lastJoyConfigVersion, cleanupBindings);
		if (numBindingsFound <= 0)
			return;

		try
		{
			if (cleanupBindings)
			{
				config.save();
				this.reload();
			}

			for (ControllerBinding binding : controlBindingsFromConfigFile)
			{
				try
				{
					saveControllerBindingInternal(joyName, binding, false);
				}
				catch (Exception ex)
				{
					LogHelper.Error("Failed trying to save controller binding: " + binding.toConfigFileString()
							+ " Exception: " + ex.toString());
				}
			}

			updateKey(category, "ConfigVersion", String.valueOf(JoypadMod.MINVERSION), true);
		}
		catch (Exception ex)
		{
			LogHelper.Error("Failed calling updateControllerBindings from config file. Exception: " + ex.toString());
		}
	}

	private int populateBindingList(int joyNo, String joyName, double lastJoyConfigVersion, boolean cleanupCategories)
	{
		if (joyNo < 0)
		{
			LogHelper.Info("Not processing joyNo " + joyNo);
			return 0;
		}

		controlBindingsFromConfigFile.clear();

		String category = getDefaultCategory() + "." + joyName;

		processJoyCategory(config.getCategory(category), joyNo, lastJoyConfigVersion, cleanupCategories);

		return controlBindingsFromConfigFile.size();

	}

	private void processJoyCategory(ConfigCategory joyCategory, int joyNo, double lastVersion, boolean cleanupCategories)
	{
		if (!joyCategory.getChildren().isEmpty())
		{
			for (ConfigCategory child : joyCategory.getChildren())
			{
				processJoyCategory(child, joyNo, lastVersion, cleanupCategories);
				if (cleanupCategories)
				{
					joyCategory.removeChild(child);
				}
			}
			return;
		}

		for (String key : joyCategory.keySet())
		{
			if (!key.contains("joy."))
			{
				// check to see if Minecraft is currently using this binding
				if (McKeyBindHelper.getMinecraftKeyBind(key) == null)
				{
					LogHelper.Info("Skipping binding " + key
							+ " from config file as Minecraft isn't using it this session");
					continue;
				}
			}
			String bindSettings = joyCategory.get(key).getString();
			if (!bindSettings.isEmpty())
			{
				try
				{
					ControllerBinding b = new ControllerBinding(key + "," + bindSettings, joyNo, lastVersion);
					if (b.inputString != null)
					{
						if (cleanupCategories)
						{
							ControllerBinding targetBinding = ControllerSettings.get(b.inputString);
							if (targetBinding != null)
							{
								targetBinding.inputEvent = b.inputEvent;
								if (b.bindingOptions.contains(BindingOptions.IS_TOGGLE))
									targetBinding.bindingOptions.add(BindingOptions.IS_TOGGLE);
								if (b.bindingOptions.contains(BindingOptions.RENDER_TICK))
									targetBinding.bindingOptions.add(BindingOptions.RENDER_TICK);
								b = targetBinding;
							}
						}
						controlBindingsFromConfigFile.add(b);
						ControllerSettings.joyBindingsMap.put(b.inputString, b);
					}

				}
				catch (Exception ex)
				{
					LogHelper.Error("Failed parsing config string " + bindSettings);
				}
			}
			if (cleanupCategories)
			{
				joyCategory.remove(key);
			}
		}
	}

	private void saveControllerBindingInternal(String joyName, ControllerBinding binding, boolean save)
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

		updateKey(catToUpdate, binding.inputString, binding.toConfigFileString(), save);
		ConfigCategory cc = config.getCategory(catToUpdate);
		cc.setComment(bindingComment);

		if (save)
		{
			config.save();
		}
	}

	private String createConfigSettingString(String joyName, String controlString)
	{
		return getDefaultCategory() + "." + joyName + "." + controlString;
	}

	private boolean deleteKey(String category, String key)
	{
		if (!config.hasCategory(category))
			return false;

		if (null != config.getCategory(category).remove(key))
		{
			config.save();
			LogHelper.Info("Deleted category " + category + " key " + key);
			return true;
		}

		return false;
	}

	// boolean true returns if a new key was created
	// false means key was updated
	private boolean updateKey(String category, String key, String value, boolean save)
	{
		boolean bRet = false;
		try
		{
			bRet = !deleteKey(category, key);
			config.get(category, key, value);

			LogHelper.Info(String.format("updateKey %s %s:%s with %s", bRet ? "created" : "updated", category, key,
					value));

			if (save)
				config.save();
		}
		catch (Exception ex)
		{
			LogHelper.Error("Failed trying to save key " + category + " value " + key + ":" + value + ". Exception: "
					+ ex.toString());
		}
		return bRet;
	}

	private void addBindingOptionsComment()
	{
		BindingOptions[] bos = ControllerBinding.BindingOptions.values();
		for (BindingOptions bo : bos)
		{
			config.get("-BindingOptions-", bo.toString(), " " + ControllerBinding.BindingOptionsComment[bo.ordinal()]);
		}
		config.addCustomCategoryComment("-BindingOptions-",
				"List of valid binding options that can be combined with Controller events");
	}

	private void addGlobalOptionsComment()
	{
		config.addCustomCategoryComment(
				globalCat,
				"GrabMouse = will grab mouse when in game (generally not good for splitscreen)\r\n"
						+ "LoggingLevel = 0-4 levels of logging ranging from next to none to very verbose. 1 recommended unless debugging.\r\n"
						+ "SharedProfile = Will share joypad settings across all users except for invert");
	}
}
