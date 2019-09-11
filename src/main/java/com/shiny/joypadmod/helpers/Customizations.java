package com.shiny.joypadmod.helpers;

import com.shiny.joypadmod.JoypadMod;
import org.lwjgl.opengl.GL11;

import com.shiny.joypadmod.ControllerSettings;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;

public class Customizations {
	
	public static class Reticle
	{
		public static int width;
		public static int height;
		public static int imageWidth;
		public static int imageHeight;
		public static int reticleColor = 0xFFFFFFFF;
		private static String imageLocation = null;
		
		private static ResourceLocation resource = null;
		private static Minecraft mc;
		private static boolean glBlend = true;
		private static boolean glDepthTest = false;
		
		public static String getLocation()
		{
			return imageLocation;
		}
		
		public static void setLocation(String path, int inWidth, int inHeight, 
				int inImageWidth, int inImageHeight)
		{
			resource = new ResourceLocation(path);
			width = inWidth;
			height = inHeight;
			imageWidth = inImageWidth;
			imageHeight = inImageHeight;
			mc = Minecraft.getMinecraft();
		}
		
		public static void SetupGl()
		{
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			if(GL11.glIsEnabled(GL11.GL_DEPTH_TEST)){  // do this to ensure that we render on top of everything
				glDepthTest=true;
		        GL11.glDisable(GL11.GL_DEPTH_TEST);
		    } 
			if(resource != null && !GL11.glIsEnabled(GL11.GL_BLEND)){  // do this to ensure that transparency is on for our reticle texture
				glBlend=false;
		        GL11.glEnable(GL11.GL_BLEND);
		    } 
		}
		
		public static void RestoreGl()
		{
			if (glDepthTest)
			{
				GL11.glEnable(GL11.GL_DEPTH_TEST);
			}
			if (resource != null && !glBlend)
			{
				GL11.glDisable(GL11.GL_BLEND);
			}
		}
		
		public static Boolean parseSettings(String settings)
		{
			if (settings != null)
			{
				String[] tokens = settings.split(",");
				if (tokens.length == 5)
				{
					try
					{
						setLocation(tokens[0], Integer.parseInt(tokens[1]),
								 Integer.parseInt(tokens[2]),
								 Integer.parseInt(tokens[3]),
								 Integer.parseInt(tokens[4]));
						return true;
					} catch (Exception ex)
					{
						JoypadMod.logger.error("Failed parsing settings string for reticle: " + settings +
								". " + ex.toString());
					}
				}
			}
			JoypadMod.logger.error("Unexpected settings string: " + settings);
			return false;
		}
		
		public static void Draw(int x, int y)
		{
			SetupGl();
			if (resource != null)
			{
				try
				{
					mc.renderEngine.bindTexture(resource);
					
					Gui.drawModalRectWithCustomSizedTexture(x - width/2, y-height/2, 0, 0, 
							width, height, imageWidth, imageHeight);	
					
				} catch (Exception ex)
				{
					JoypadMod.logger.error("Caught exception when rendering reticle. Defaulting to basic."
							+ ex.getMessage());
					resource = null;
				}
			}
			else
			{
				Gui.drawRect(x - 3, y, x + 4, y + 1, reticleColor);
				Gui.drawRect(x, y - 3, x + 1, y + 4, reticleColor);
			}
			RestoreGl();
		}
		
	}
	
	public static void init()
	{
		String user = ControllerSettings.config.getDefaultCategory();
		String reticleSettings = ControllerSettings.config.getConfigFileSetting(user + ".CustomReticle");
		if (reticleSettings == "false")
		{
			reticleSettings = "joypadmod:textures/reticle.png,16,16,16,16";
			ControllerSettings.config.setConfigFileSetting(user + ".CustomReticle", reticleSettings);
		}
		Reticle.parseSettings(reticleSettings);
	}

}
