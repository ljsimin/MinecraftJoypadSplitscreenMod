package com.shiny.joypadmod.helpers;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.settings.KeyBinding;

// the point of this class is to have a central location for calls to all the
// Minecraft functions that are currently obfuscated
// due to Forge not being up to date with MCP or MCP not having the latest
// variable name de-obfuscated
// This is particularly useful in early releases of Forge and less useful as the
// version stabilizes
// This may help in keeping older Minecraft joypad releases up to date with the
// latest additions

// you will note that it is primarily only useful for the functions that are
// currently obfuscated in Forge 1.7.2 but a central location
// for all Minecraft calls may be a good practice going forward
public class McObfuscationHelper
{
	// format of Map
	// key = de-obfuscated function or field Name
	// values = str1=164Name, str2=1.7.2,str3=nextVersionName etc

	// currently supported versions 1.6.4, 1.7.2
	public static Map<String, String[]> versionNameMap;

	static
	{
		versionNameMap = new HashMap<String, String[]>();
		versionNameMap.put("mouseClicked", new String[] { "func_73864_a", "func_73864_a" });
		versionNameMap.put("leftClick", new String[] { "doesn't exist!", "func_147116_af" });
		versionNameMap.put("clickMouse", new String[] { "func_71402_c", "doesn't exist!" });
		versionNameMap.put("sendClickBlockToController", new String[] { "func_71399_a", "func_147115_a" });

		versionNameMap.put("mouseMovedOrUp", new String[] { "func_73879_b", "func_146286_b" });
		versionNameMap.put("mouseClickMove", new String[] { "func_85041_a", "func_146273_a" });

		versionNameMap.put("eventButton", new String[] { "field_85042_b", "field_146287_f" });
		versionNameMap.put("lastMouseEvent", new String[] { "field_85043_c", "field_146288_g" });
		versionNameMap.put("parentScreen", new String[] { "field_73909_b", "field_146496_h" });
	}

	public static int keyCode(KeyBinding key)
	{

		// de-obfuscated
		return key.keyCode;

		// obfuscated 1.7.2
		// return key.func_151463_i();
	}

	// returns de-obfuscated and obfuscated names for that version
	public static String[] getMcVarNames(String fieldOrFunctionName)
	{
		String[] candidates = versionNameMap.get(fieldOrFunctionName);

		if (candidates == null)
			return new String[] { fieldOrFunctionName, "unknown" };

		switch (ModVersionHelper.MC_VERSION)
		{
		case 164:
			return new String[] { fieldOrFunctionName, candidates[0] };
		case 172:
			return new String[] { fieldOrFunctionName, candidates[1] };
		default:
			return new String[] { fieldOrFunctionName, "unknown" };
		}
	}

}
