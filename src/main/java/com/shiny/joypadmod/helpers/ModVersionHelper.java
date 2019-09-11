package com.shiny.joypadmod.helpers;

import com.shiny.joypadmod.JoypadMod;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;

import com.shiny.joypadmod.ButtonScreenTips;
import com.shiny.joypadmod.ControllerSettings;
import com.shiny.joypadmod.GameRenderHandler;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class ModVersionHelper {

    public void gameInit() {
        if (ControllerSettings.modDisabled) {
            JoypadMod.logger.warn("Mod game initialization ignored due to mod disabled.  No in game options will appear to change this unless config file updated");
            return;
        }

        MinecraftForge.EVENT_BUS.register(this);

        Customizations.init();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void tickRender(RenderTickEvent event) {
        if (event.phase == Phase.START) {
            GameRenderHandler.HandlePreRender();
        } else if (event.phase == Phase.END) {
            GameRenderHandler.HandlePostRender();
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void tickRenderClient(ClientTickEvent event) {
        if (event.phase == Phase.START) {
            GameRenderHandler.HandleClientStartTick();
        } else if (event.phase == Phase.END) {
            GameRenderHandler.HandleClientEndTick();
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void buttonMapDisplay(RenderGameOverlayEvent.Post event) {

        if (event.isCancelable() || event.getType() != ElementType.EXPERIENCE) {
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
