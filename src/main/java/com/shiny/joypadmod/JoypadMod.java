package com.shiny.joypadmod;

import com.shiny.joypadmod.helpers.McGuiHelper;
import com.shiny.joypadmod.helpers.ModVersionHelper;
import com.shiny.joypadmod.lwjglVirtualInput.VirtualKeyboard;
import com.shiny.joypadmod.lwjglVirtualInput.VirtualMouse;
import com.shiny.joypadmod.minecraftExtensions.JoypadMouseHelper;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = JoypadMod.MODID, name = JoypadMod.NAME, version = JoypadMod.VERSION, clientSideOnly = true, acceptedMinecraftVersions = "[1.12]")
public class JoypadMod {
    public static Logger logger = LogManager.getLogger("Joypad Mod");
    public static final String MODID = "joypadmod";
    public static final String NAME = "Joypad Mod";
    public static final String VERSION = "1.12.0-0.22";

    private static ControllerSettings controllerSettings;

    private ModVersionHelper modHelper;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        logger.info("preInit");
        controllerSettings = new ControllerSettings(event.getSuggestedConfigurationFile());
    }

    @EventHandler
    public void init(FMLInitializationEvent event) {
        logger.info("init");
        try {
            if (Minecraft.getMinecraft().mouseHelper == null) {
                JoypadMod.logger.warn("Replacing Mousehelper that may have already been replaced by another mod!");
            }
            Minecraft.getMinecraft().mouseHelper = new JoypadMouseHelper();
            JoypadMod.logger.info("Replaced mousehelper in Minecraft with JoypadMouseHelper");
        } catch (Exception ex) {
            JoypadMod.logger.warn("Unable to exchange mousehelper. Game may grab mouse from keyboard players!");
        }
    }

    @EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        logger.info("postInit");
        controllerSettings.init();
        try {
            VirtualKeyboard.create();
        } catch (Exception ex) {
            logger.fatal("Unable to initialize VirtualKeyboard.  Limited compatibility with some mods likely. " + ex.toString());
        }

        try {
            VirtualMouse.create();
            McGuiHelper.create();
        } catch (Exception e) {
            logger.fatal("A fatal error occurred, disabling mod. " + e.toString());
            ControllerSettings.modDisabled = true;
        }

        modHelper = new ModVersionHelper();
        modHelper.gameInit();
    }

}
