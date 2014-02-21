package com.shiny.joypadmod;

/*
 * Main class for Joypad mod. This initializes everything.
 */

import net.minecraft.client.Minecraft;

import com.shiny.joypadmod.helpers.LogHelper;
import com.shiny.joypadmod.helpers.MinecraftObfuscationHelper;
import com.shiny.joypadmod.helpers.ModVersionHelper;
import com.shiny.joypadmod.minecraftExtensions.JoypadMouseHelper;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;

@Mod(modid = JoypadMod.MODID, name = JoypadMod.NAME, version = ModVersionHelper.VERSION + JoypadMod.MINVERSION)
// 1.6.4
@NetworkMod(serverSideRequired = false)
public class JoypadMod
{
	public static final String MODID = "JoypadSplitscreenMod";
	public static final String NAME = "Joypad / SplitScreen Mod";
	public static final String MINVERSION = "-0.075pre";

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
		try
		{
			if (!(Minecraft.getMinecraft().mouseHelper instanceof net.minecraft.util.MouseHelper))
			{
				LogHelper.Warn("Replacing Mousehelper that may have already been replaced by another mod!");
			}
			Minecraft.getMinecraft().mouseHelper = new JoypadMouseHelper();
			LogHelper.Info("Replaced mousehelper in Minecraft with JoypadMouseHelper");
		}
		catch (Exception ex)
		{
			LogHelper.Warn("Unable to exchange mousehelper. Game may grab mouse from keyboard players!");
		}
		obfuscationHelper = new MinecraftObfuscationHelper();
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event)
	{
		LogHelper.Info("postInit");
		controllerSettings.init();
		modHelper = new ModVersionHelper();
		modHelper.gameInit();
	}
}
