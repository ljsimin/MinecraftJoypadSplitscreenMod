package com.shiny.joypadmod;

import com.shiny.joypadmod.helpers.Customizations;
import com.shiny.joypadmod.helpers.McGuiHelper;
import com.shiny.joypadmod.lwjglVirtualInput.VirtualKeyboard;
import com.shiny.joypadmod.lwjglVirtualInput.VirtualMouse;
import com.shiny.joypadmod.minecraftExtensions.JoypadMouseHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.crash.CrashReport;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = JoypadMod.MODID, name = JoypadMod.NAME, version = JoypadMod.VERSION, clientSideOnly = true, acceptedMinecraftVersions = "[1.12]")
public class JoypadMod {
    public static Logger logger = LogManager.getLogger("Joypad Mod");
    public static final String MODID = "joypadmod";
    public static final String NAME = "Joypad Mod";
    public static final String VERSION = "1.12.0-0.22";

    private static ControllerSettings controllerSettings;

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
            Minecraft.getMinecraft().crashed(new CrashReport("A fatal error occurred.", e));
        }

        MinecraftForge.EVENT_BUS.register(this);

        Customizations.init();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void tickRender(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            GameRenderHandler.HandlePreRender();
        } else if (event.phase == TickEvent.Phase.END) {
            GameRenderHandler.HandlePostRender();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void tickRenderClient(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            GameRenderHandler.HandleClientStartTick();
        } else if (event.phase == TickEvent.Phase.END) {
            GameRenderHandler.HandleClientEndTick();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void buttonMapDisplay(RenderGameOverlayEvent.Post event) {
        if (event.isCancelable() || event.getType() != RenderGameOverlayEvent.ElementType.EXPERIENCE) {
            return;
        }
        new ButtonScreenTips();
    }

    //TODO: Replace this (removed in 1.13+)
    public static ScaledResolution GetScaledResolution() {
        Minecraft mc = Minecraft.getMinecraft();
        return new ScaledResolution(mc);
    }
}
