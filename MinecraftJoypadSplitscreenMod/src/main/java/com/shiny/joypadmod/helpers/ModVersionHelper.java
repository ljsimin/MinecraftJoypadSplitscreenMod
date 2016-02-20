package com.shiny.joypadmod.helpers;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.RenderTickEvent;

import net.minecraftforge.client.event.GuiScreenEvent.InitGuiEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;


import com.shiny.joypadmod.ButtonScreenTips;
import com.shiny.joypadmod.ControllerSettings;
import com.shiny.joypadmod.GameRenderHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class ModVersionHelper
{
	public static final String VERSION = "1.7.10";
	public static final int MC_VERSION = 180;

	public void gameInit()
	{
		if (ControllerSettings.modDisabled)
		{
			LogHelper.Warn("Mod game initialization ignored due to mod disabled.  No in game options will appear to change this unless config file updated");
			return;
		}

		MinecraftForge.EVENT_BUS.register(this);
		// 1.7.2
		FMLCommonHandler.instance().bus().register(this);
		// 1.6.4
		// TickRegistry.registerTickHandler(new RenderTickHandler(),
		// Side.CLIENT);
		
		Customizations.init();
	}

	public static int getVersion()
	{
		return MC_VERSION;
	}

	// 1.7.2
	@SubscribeEvent(priority = EventPriority.HIGH)
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

	@SubscribeEvent(priority = EventPriority.HIGH)
	public void tickRenderClient(ClientTickEvent event)
	{
		if (event.phase == TickEvent.Phase.START)
		{
			GameRenderHandler.HandleClientStartTick();
		}
		else if (event.phase == TickEvent.Phase.END)
		{
			GameRenderHandler.HandleClientEndTick();
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void buttonMapDisplay(RenderGameOverlayEvent.Post event)
	{
		if(event.isCancelable() || event.type != ElementType.EXPERIENCE)
	    {      
	        return;
	    }
		new ButtonScreenTips();
	}
	
	public static ScaledResolution GetScaledResolution()
	{
		Minecraft mc = Minecraft.getMinecraft();
		// 1.8.8
		//return new ScaledResolution(mc);
		// 1.7.10 - 1.8.2 
		return new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
		// 1.7.2
		//return new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
	}
}
