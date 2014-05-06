package com.shiny.joypadmod.helpers;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.resources.I18n;
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
		return key.getKeyCode();

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

	public static String getKeyDescription(KeyBinding key)
	{
		return key.getKeyDescription();
	}

	public static String getKeyCategory(KeyBinding key)
	{
		return key.getKeyCategory();
	}

	public static String lookupString(String input)
	{
		String ret = "";
		if (input.contains("joy."))
		{
			if (input.contains("X-") || input.contains("prev"))
				ret += symGet(JSyms.lArrow);
			else if (input.contains("X+") || input.contains("next"))
				ret += symGet(JSyms.rArrow);
			else if (input.contains("Y-") || input.contains("Up"))
				ret += symGet(JSyms.uArrow);
			else if (input.contains("Y+") || input.contains("Down"))
				ret += symGet(JSyms.dArrow);
			if (input.equals("joy.closeInventory"))
				return doTranslate("key.inventory") + " " + symGet(JSyms.remove);

			if (ret != "")
			{
				if (input.contains("camera"))
					ret = doTranslate("controlMenu.look") + " " + ret;
				else if (input.contains("gui"))
					ret = doTranslate("controlMenu.mouse") + " " + ret;
				else if (input.contains("scroll"))
					ret = doTranslate("controlMenu.scroll") + " " + ret;
				else if (input.contains("Item") || (input.contains("Item")))
					ret = doTranslate("key.inventory") + " " + ret;

				return ret;
			}
		}

		if (input.contains("-Global-.GrabMouse"))
		{
			return symGet(JSyms.warning) + " " + doTranslate(input);
		}

		return doTranslate(input);
	}

	public enum JSyms
	{
		lArrow, rArrow, uArrow, dArrow, eCircle, fCircle, unbind, remove, warning
	};

	public static char symGet(JSyms sym)
	{
		switch (sym)
		{
		case lArrow:
			return 0x2B05;
		case rArrow:
			return 0x27A1;
		case uArrow:
			return 0x2B06;
		case dArrow:
			return 0x2B07;
		case unbind:
			return '-';
		case eCircle:
			return 9675;
		case fCircle:
			return 9679;
		case remove:
			return 0x2716;
		case warning:
			return 0x26A0;
		default:
			return '?';

		}
	}

	private static String doTranslate(String input)
	{
		String translation = I18n.format(input, new Object[0]);
		if (translation.compareTo(input) == 0)
		{
			String keyString = input.replace("joy.", "key.");
			// translation failed so try to lookup with the key name
			translation = I18n.format(keyString, new Object[0]);
			if (translation.compareTo(keyString) == 0)
				return input;
		}

		return translation;
	}

}
