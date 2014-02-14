package com.shiny.joypadmod.helpers;

import java.io.File;

import net.minecraft.client.Minecraft;
import net.minecraftforge.common.config.Configuration;

public class ConfigFile
{
	public int preferedJoyNo;
	public String preferedJoyName;

	private Configuration config;
	private String userName;

	public ConfigFile(File configFile)
	{
		config = new Configuration(configFile);

		config.load();

		userName = "unknown";

		if (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().getSession() != null)
		{
			userName = Minecraft.getMinecraft().getSession().getUsername();
		}

		int joyNo = config.get("Joypad-" + userName, "JoyNo", -1).getInt();
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

}
