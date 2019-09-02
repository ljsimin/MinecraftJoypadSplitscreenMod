package com.shiny.joypadmod.helpers;

import net.minecraftforge.fml.common.FMLLog;

import org.apache.logging.log4j.Level;

import com.shiny.joypadmod.ControllerSettings;


public class LogHelper
{
	public static void Debug(String Message)
	{
		FMLLog.log(Level.DEBUG, Message);
	}

	public static void Error(String Message)
	{
		FMLLog.log(Level.ERROR, Message);
	}

	public static void Fatal(String Message)
	{
		FMLLog.log(Level.FATAL, Message);
	}

	public static void Info(String Message)
	{
		if (ControllerSettings.loggingLevel > 0)
			FMLLog.log(Level.INFO, Message);
	}

	public static void Trace(String Message)
	{
		FMLLog.log(Level.TRACE, Message);
	}

	public static void Warn(String Message)
	{
		FMLLog.log(Level.WARN, Message);
	}
}