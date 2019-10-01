package com.shiny.joypadmod.utils;

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
public class McObfuscationHelper {
    // format of Map
    // key = de-obfuscated function or field Name
    // values = str1=164Name, str2=1.7.2,str3=nextVersionName etc

    public static int keyCode(KeyBinding key) {
        return key.getKeyCode();
    }

    public static String getKeyDescription(KeyBinding key) {
        return key.getKeyDescription();
    }

    public static String getKeyCategory(KeyBinding key) {
        return key.getKeyCategory();
    }

    public static String lookupString(String input) {
        String ret = "";
        if (input.contains("joy.")) {
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

            if (!ret.equals("")) {
                if (input.contains("camera"))
                    ret = doTranslate("controlMenu.look") + " " + ret;
                else if (input.contains("gui"))
                    ret = doTranslate("controlMenu.mouse") + " " + ret;
                else if (input.contains("scroll"))
                    ret = doTranslate("controlMenu.scroll") + " " + ret;
                else if (input.contains("Item"))
                    ret = doTranslate("key.inventory") + " " + ret;

                return ret;
            }
        }

        if (input.contains("-Global-.GrabMouse")) {
            return symGet(JSyms.warning) + " " + doTranslate(input);
        }

        return doTranslate(input);
    }

    public enum JSyms {
        lArrow, rArrow, uArrow, dArrow, eCircle, fCircle, unbind, remove, warning
    }

    public static char symGet(JSyms sym) {
        switch (sym) {
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

    private static String doTranslate(String input) {
        String translation = I18n.format(input);
        if (translation.compareTo(input) == 0) {
            String keyString = input.replace("joy.", "key.");
            // translation failed so try to lookup with the key name
            translation = I18n.format(keyString);
            if (translation.compareTo(keyString) == 0)
                return input;
        }

        return translation;
    }

}
