package com.shiny.joypadmod;

import java.util.EnumSet;

// 1.6.4
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class TickHandler
{
	public static class RenderTickHandler implements ITickHandler
	{
		// start of rendering on screen
		@Override
		public void tickStart(EnumSet<TickType> type, Object... tickData)
		{
			GameRenderHandler.HandlePreRender();
		}

		// end of rendering on screen
		@Override
		public void tickEnd(EnumSet<TickType> type, Object... tickData)
		{
			GameRenderHandler.HandlePostRender();
		}

		@Override
		public EnumSet<TickType> ticks()
		{
			return EnumSet.of(TickType.RENDER);
		}

		@Override
		public String getLabel()
		{
			return "joyRenderTickHandler";
		}
	}

	public static class ClientTickHandler implements ITickHandler
	{

		@Override
		public void tickStart(EnumSet<TickType> type, Object... tickData)
		{
			GameRenderHandler.HandleClientStartTick();
		}

		@Override
		public void tickEnd(EnumSet<TickType> type, Object... tickData)
		{
			GameRenderHandler.HandleClientEndTick();
		}

		@Override
		public EnumSet<TickType> ticks()
		{
			return EnumSet.of(TickType.CLIENT);
		}

		@Override
		public String getLabel()
		{
			return "joyClientTickHandler";
		}

	}
}
