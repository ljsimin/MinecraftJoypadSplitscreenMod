package com.shiny.joypadmod.gui;

import java.lang.reflect.Method;

import com.shiny.joypadmod.JoypadMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class McGuiHelper {

    private static Method mouseButtonMove = null;
    private static Minecraft mc = Minecraft.getMinecraft();

    private static boolean created = false;

    @SuppressWarnings("rawtypes")
    public static void create() throws Exception {
        JoypadMod.logger.info("Creating McGuiHelper");
        Class[] params3 = new Class[]{int.class, int.class, int.class, long.class};

        mouseButtonMove = tryGetMethod(GuiScreen.class, params3, new String[]{"mouseClickMove","func_146273_a"});

        created = true;
    }

    @SuppressWarnings("rawtypes")
    private static Method tryGetMethod(Class<?> inClass, Class[] params, String[] names) throws NoSuchMethodException,
            SecurityException {
        Method m;
        try {
            m = inClass.getDeclaredMethod(names[0], params);
        } catch (Exception ex) {
            m = inClass.getDeclaredMethod(names[1], params);
        }

        m.setAccessible(true);
        return m;
    }

    public static void guiMouseDrag(int rawX, int rawY) {
        if (!created){
            JoypadMod.logger.error("Unable to use McGuiHelper because it failed to initialize");
            return;
        }

        long lastEvent = -1;
        int eventButton = -1;
        // JoypadMod.logger.info("Calling mouseDrag");

        try {
            eventButton = ObfuscationReflectionHelper.getPrivateValue(GuiScreen.class, mc.currentScreen, "eventButton","field_146287_f");
            lastEvent = ObfuscationReflectionHelper.getPrivateValue(GuiScreen.class, mc.currentScreen, "lastMouseEvent","field_146288_g");
        } catch (Exception ex) {
            JoypadMod.logger.error("Failed calling ObfuscationReflectionHelper" + ex.toString());
            if (lastEvent == -1)
                lastEvent = 100;
            eventButton = 0;
        }
        long var3 = Minecraft.getSystemTime() - lastEvent;

        try {
            mouseButtonMove.invoke(mc.currentScreen, rawX, rawY, eventButton, var3);
        } catch (Exception ex) {
        }

    }
}
