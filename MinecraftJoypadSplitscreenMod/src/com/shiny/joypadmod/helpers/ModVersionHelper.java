package com.shiny.joypadmod.helpers;

import com.shiny.joypadmod.ControllerSettings;
import com.shiny.joypadmod.TickHandler.ClientTickHandler;
import com.shiny.joypadmod.TickHandler.RenderTickHandler;

import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class ModVersionHelper
{
	public static final String VERSION = "1.6.4";
	public static final int MC_VERSION = 164;

	public void gameInit()
	{
		if (ControllerSettings.modDisabled)
		{
			LogHelper
					.Warn("Mod game initialization ignored due to mod disabled.  No in game options will appear to change this unless config file updated");
			return;
		}

		// 1.7.2
		// FMLCommonHandler.instance().bus().register(this);
		// 1.6.4
		TickRegistry.registerTickHandler(new RenderTickHandler(), Side.CLIENT);
		TickRegistry.registerTickHandler(new ClientTickHandler(), Side.CLIENT);
	}

	public static int getVersion()
	{
		return MC_VERSION;
	}

	// 1.7.2
	/*
	 * @SubscribeEvent public void tickRender(RenderTickEvent event) { if (event.phase == TickEvent.Phase.START) { GameRenderHandler.HandlePreRender(); } else if (event.phase == TickEvent.Phase.END) {
	 * GameRenderHandler.HandlePostRender(); } }
	 * 
	 * @SubscribeEvent public void tickRenderClient(ClientTickEvent event) { if (event.phase == TickEvent.Phase.START) { GameRenderHandler.HandleClientStartTick(); } else if (event.phase ==
	 * TickEvent.Phase.END) { GameRenderHandler.HandleClientEndTick(); } }
	 */
}
