package com.shiny.joypadmod;

/*
 * Main class for Joypad mod. This initializes everything.
 */

// Common imports
import org.lwjgl.input.Controllers;
import java.io.File;
import net.minecraft.client.Minecraft;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

// 1.6.4
// import cpw.mods.fml.common.network.NetworkMod; 
// import cpw.mods.fml.common.registry.TickRegistry; 
// import cpw.mods.fml.relauncher.Side; 
// import net.minecraftforge.common.Configuration;
// import net.minecraftforge.common.Property;
 
//1.7.4
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.RenderTickEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;

@Mod(modid = JoypadMod.MODID, name = JoypadMod.NAME, version = JoypadMod.VERSION + JoypadMod.MINVERSION)
// 1.6.4
// @NetworkMod(serverSideRequired = false)
public class JoypadMod
{
	public static final String MODID = "joypadmod";
	public static final String VERSION = "1.7.2";
	public static final String MINVERSION = "-0.01pre";
	public static final String NAME = "Joypad / SplitScreen Mod";
	public static MinecraftObfuscationHelper obfuscationHelper;
	public static final int MC_VERSION = 172;

	public static ControllerSettings controllerSettings;
	public int JoyNo = -1;

	@EventHandler
	public void preInit(FMLPreInitializationEvent event)
	{
		readConfigFile(event.getSuggestedConfigurationFile());
	}

	@EventHandler
	public void init(FMLInitializationEvent event)
	{
		initialize();
		obfuscationHelper = new MinecraftObfuscationHelper();
		if (ControllerSettings.inputEnabled)
		{
			// 1.7.4
			FMLCommonHandler.instance().bus().register(this);
			// 1.6.4
			// TickRegistry.registerTickHandler(new RenderTickHandler(),
			// Side.CLIENT);
		}

	}

	public void initialize()
	{
		LogHelper.Info("Initializing " + NAME);
		controllerSettings = new ControllerSettings();
		if (JoyNo < -1)
		{
			LogHelper.Info("Controller disabled");
			ControllerSettings.inputEnabled = false;
		}
		else
		{
			int nControllers = ControllerSettings.DetectControllers();
			if (nControllers > 0)
			{
				int selectedController = 0;
				if (JoyNo >= 0 && JoyNo < ControllerSettings.DetectControllers())
					selectedController = JoyNo;
				ControllerSettings.SetController(selectedController);
				Controllers.clearEvents();
			}
		}
		Minecraft.getMinecraft().gameSettings.pauseOnLostFocus = false;
	}

	public void readConfigFile(File configFile)
	{
		Configuration config = new Configuration(configFile);

		config.load();

		String userName = "unknown";

		if (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().getSession() != null)
		{
			userName = Minecraft.getMinecraft().getSession().getUsername();
		}

		Property joyNo = config.get("Joypad-" + userName, "JoyNo", -1);
		System.out.println(userName + "'s JoyNo == " + joyNo.getInt());
		if (joyNo.getInt() != -1)
			JoyNo = joyNo.getInt();
		if (joyNo.getInt() == -2)
			JoyNo = -2;

		config.save();
	}

	// 1.7.2
	@SubscribeEvent
	public void tickRender(RenderTickEvent event)
	{
		if (event.phase == TickEvent.Phase.START)
		{
			GameRenderHandler.HandlePreRender();
		}
		else if (event.phase == TickEvent.Phase.END)
		{
			GameRenderHandler.HandlePostRender();
		}
	}

}
