package com.shiny.joypadmod;

import java.util.logging.Level;

import cpw.mods.fml.common.FMLLog;

public class LogHelper
{
	public static void Debug(String Message)
	{
		FMLLog.log(Level.FINE, Message);
	}

	public static void Error(String Message)
	{
		FMLLog.log(Level.SEVERE, Message);
	}

	public static void Fatal(String Message)
	{
		FMLLog.log(Level.SEVERE, Message);
	}

	public static void Info(String Message)
	{
		FMLLog.log(Level.INFO, Message);
	}

	public static void Trace(String Message)
	{
		FMLLog.log(Level.FINER, Message);
	}

	public static void Warn(String Message)
	{
		FMLLog.log(Level.WARNING, Message);
	}
}