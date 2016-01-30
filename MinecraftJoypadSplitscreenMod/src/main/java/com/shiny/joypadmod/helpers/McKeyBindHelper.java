package com.shiny.joypadmod.helpers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;

public class McKeyBindHelper
{
	public static KeyBinding getMinecraftKeyBind(String bindingKey)
	{
		for (KeyBinding kb : Minecraft.getMinecraft().gameSettings.keyBindings)
		{
			String keyInputString = McObfuscationHelper.getKeyDescription(kb);
			if (keyInputString.compareTo(bindingKey) == 0)
			{
				return kb;
			}
		}
		return null;
	}
}
