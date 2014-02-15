package com.shiny.joypadmod;

/*
 * Main class for Joypad mod. This initializes everything.
 */

import com.shiny.joypadmod.helpers.LogHelper;
import com.shiny.joypadmod.helpers.MinecraftObfuscationHelper;
import com.shiny.joypadmod.helpers.ModVersionHelper;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = JoypadMod.MODID, name = JoypadMod.NAME, version = ModVersionHelper.VERSION + ModVersionHelper.MINVERSION)
// 1.6.4
// @NetworkMod(serverSideRequired = false)
public class JoypadMod
{
	public static final String MODID = "joypadmod";
	public static final String NAME = "Joypad / SplitScreen Mod";

	public static MinecraftObfuscationHelper obfuscationHelper;

	public static ControllerSettings controllerSettings;

	private ModVersionHelper modHelper;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		LogHelper.Info("preInit");
		controllerSettings = new ControllerSettings(event.getSuggestedConfigurationFile());
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		LogHelper.Info("init");
		obfuscationHelper = new MinecraftObfuscationHelper();
		controllerSettings.init();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		LogHelper.Info("postInit");
		modHelper = new ModVersionHelper();
		modHelper.gameInit();
	}

}
