package com.shiny.joypadmod.utils;

import java.util.ArrayList;
import java.util.List;

import com.shiny.joypadmod.gui.ButtonScreenTips;
import com.shiny.joypadmod.ControllerSettings;
import com.shiny.joypadmod.JoypadMod;
import com.shiny.joypadmod.devices.JoypadMouse;
import org.lwjgl.input.Mouse;

import com.shiny.joypadmod.gui.McGuiHelper;
import com.shiny.joypadmod.event.ControllerBinding;
import com.shiny.joypadmod.event.ControllerBinding.BindingOptions;
import com.shiny.joypadmod.event.ControllerInputEvent;
import com.shiny.joypadmod.devices.VirtualMouse;
import com.shiny.joypadmod.gui.joypad.JoypadConfigMenu;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

public class GameRenderHandler {
    private static Minecraft mc = Minecraft.getMinecraft();
    public static int reticalColor = 0xFFFFFFFF;
    // boolean to allow the original controls menu.
    // normally we override the controls menu when seen
    public static boolean allowOrigControlsMenu = false;
    private static long lastInGuiTick = 0;
    private static long lastInGameTick = 0;

    public static List<String> preRenderGuiBucket = new ArrayList<>();
    public static List<String> preRenderGameBucket = new ArrayList<>();

    public static boolean mouseDetected = false;

    public static void HandlePreRender() {
        try {
            if (mc.currentScreen != null && !ControllerSettings.isSuspended()) {
                if (mc.currentScreen instanceof GuiControls) {
                    if (!allowOrigControlsMenu) {
                        ReplaceControlScreen((GuiControls) mc.currentScreen);
                    }
                } else if (!(mc.currentScreen instanceof JoypadConfigMenu)) {
                    allowOrigControlsMenu = false;
                }

                if (InGuiCheckNeeded()) {
                    ControllerSettings.JoypadModInputLibrary.poll();
                    if (Mouse.isInsideWindow()
                            && Minecraft.getSystemTime() - JoypadMouse.AxisReader.lastNon0Reading > 1000) {
                        if (Mouse.getDX() != 0 || Mouse.getDY() != 0) {
                            if (ControllerSettings.loggingLevel > 2 && !mouseDetected) {
                                JoypadMod.logger.info("Mouse sharing of screen detected");
                            }
                            mouseDetected = true;
                        }
                    } else {
                        mouseDetected = false;
                    }
                    // This call here re-points the mouse position that Minecraft picks
                    // up to determine if it should do the Hover over button effect.
                    if (!mouseDetected)
                        VirtualMouse.setXY(JoypadMouse.getmcX(), JoypadMouse.getmcY());
                    if (preRenderGuiBucket.size() > 0) {
                        for (String mapKey : preRenderGuiBucket) {
                            ControllerSettings.get(mapKey).wasPressed(true, true);
                        }
                        preRenderGuiBucket.clear();
                    }
                    HandleDragAndScrolling();
                }
            }

            if (InGameCheckNeeded()) {
                ControllerSettings.JoypadModInputLibrary.poll();
                for (ControllerBinding binding = ControllerSettings.startGameBindIteration(); binding != null; binding = ControllerSettings.getNextGameAutoBinding()) {
                    if (binding.bindingOptions.contains(BindingOptions.RENDER_TICK))
                        binding.isPressed();
                }

                if (preRenderGameBucket.size() > 0) {
                    for (String mapKey : preRenderGameBucket) {
                        ControllerSettings.get(mapKey).wasPressed(true, true);
                    }
                    preRenderGameBucket.clear();
                }
            }
        } catch (Exception ex) {
            JoypadMod.logger.fatal("Joypad mod unhandled exception caught! " + ex.toString());
        }
    }

    public static int displayCountDown = 0;
    public static String displayMessage = "";

    public static void HandlePostRender() {
        if (ControllerSettings.isSuspended())
            return;

        ControllerSettings.JoypadModInputLibrary.poll();

        if (ControllerSettings.JoypadModInputLibrary.wasDisconnected()) {
            displayCountDown = 300;
            displayMessage = "Joypad disconnected";
        }

        if (ControllerSettings.JoypadModInputLibrary.wasConnected()) {
            displayCountDown = 300;
            displayMessage = "Joypad connected";
        }

        if (displayCountDown > 0) {
            Minecraft.getMinecraft().fontRenderer.drawStringWithShadow(displayMessage, 0, 0, 0xFFFF55);
            displayCountDown--;
        }

        try {
            if (InGuiCheckNeeded()) {
                // fixes issue with transitioning from inGui to game movement continuing
                if (Minecraft.getSystemTime() - lastInGameTick < 50) {
                    ControllerSettings.unpressAll();
                    Minecraft.getMinecraft().gameSettings.pauseOnLostFocus = false;
                }

                DrawRetical();
                new ButtonScreenTips();
            }

            if (InGameCheckNeeded()) {
                // fixes issue with transitioning from inGame to Gui movement continuing
                if (Minecraft.getSystemTime() - lastInGuiTick < 50) {
                    ControllerSettings.unpressAll();
                    Minecraft.getMinecraft().gameSettings.pauseOnLostFocus = false;
                }

                for (ControllerBinding binding = ControllerSettings.startGameBindIteration(); binding != null; binding = ControllerSettings.getNextGameAutoBinding()) {
                    binding.isPressed();
                }

                UpdateInGameCamera();
            }
        } catch (Exception ex) {
            JoypadMod.logger.fatal("Joypad mod unhandled exception caught! " + ex.toString());
        }

    }

    public static void HandleClientStartTick() {
        if (ControllerSettings.isSuspended())
            return;

        if (InGuiCheckNeeded()) {
            HandleJoystickInGui();
            lastInGuiTick = Minecraft.getSystemTime();
            HandleDragAndScrolling();
        }

        if (InGameCheckNeeded()) {
            HandleJoystickInGame();
            lastInGameTick = Minecraft.getSystemTime();
        }
    }

    public static void HandleClientEndTick() {
        // does nothing currently
    }

    private static void DrawRetical() {

        if (mc.currentScreen == null || !ControllerSettings.isInputEnabled())
            return;

        JoypadMouse.updateXY();
        int x = JoypadMouse.getX();
        int y = JoypadMouse.getY();
        Customizations.Reticle.Draw(x, y);
    }

    private static void UpdateInGameCamera() {
        if (mc.player != null) {
            if (JoypadMouse.pollAxis(false)) {

                mc.player.turn(JoypadMouse.AxisReader.deltaX, JoypadMouse.AxisReader.deltaY
                        * (ControllerSettings.getInvertYAxis() ? 1.0f : -1.0f));
            }
        }
    }

    private static void HandleDragAndScrolling() {

        if (VirtualMouse.isButtonDown(0) || VirtualMouse.isButtonDown(1)) {
            // VirtualMouse.moveMouse(JoypadMouse.getmcX(), JoypadMouse.getmcY());
            McGuiHelper.guiMouseDrag(JoypadMouse.getX(), JoypadMouse.getY());
            VirtualMouse.setMouseButton(JoypadMouse.isLeftButtonDown() ? 0 : 1, true);
        }

        if (mc.currentScreen instanceof GuiContainer) {
            if (Minecraft.getSystemTime() - ControllerSettings.get("joy.scrollDown").lastTick < 100
                    || Minecraft.getSystemTime() - ControllerSettings.get("joy.scrollUp").lastTick < 100)
                return;
        }

        ControllerSettings.get("joy.scrollDown").isPressed();
        ControllerSettings.get("joy.scrollUp").isPressed();
    }

    private static void HandleJoystickInGui() {
        // update mouse coordinates
        // JoypadMouse.updateXY();
        if (!mouseDetected)
            VirtualMouse.setXY(JoypadMouse.getmcX(), JoypadMouse.getmcY());

        for (ControllerBinding binding = ControllerSettings.startMenuBindIteration(); binding != null; binding = ControllerSettings.getNextMenuAutoBinding()) {
            if (!binding.bindingOptions.contains(BindingOptions.RENDER_TICK))
                binding.isPressed();
        }

        while (ControllerSettings.JoypadModInputLibrary.next() && mc.currentScreen != null) {
            // ignore controller events in the milliseconds following in GAME
            // controlling
            if (Minecraft.getSystemTime() - lastInGameTick < 200)
                continue;

            if (ControllerSettings.loggingLevel > 3) {
                try {
                    ControllerInputEvent inputEvent = ControllerSettings.controllerUtils.getLastEvent(
                            ControllerSettings.JoypadModInputLibrary.getController(ControllerSettings.joyNo), ControllerSettings.JoypadModInputLibrary.getEventControlIndex());
                    if (inputEvent != null) {
                        JoypadMod.logger.info("Input event " + inputEvent.toString()
                                + " triggered.  Finding associated binding");
                    }
                } catch (Exception ex) {
                    JoypadMod.logger.error("Exception caught debugging controller input events: " + ex.toString());
                }
            }

            for (ControllerBinding binding = ControllerSettings.startMenuBindIteration(); binding != null; binding = ControllerSettings.getNextMenuAutoBinding()) {
                if (binding.bindingOptions.contains(BindingOptions.RENDER_TICK)) {
                    if (binding.wasPressed(false)) {
                        preRenderGuiBucket.add(binding.inputString);
                    }
                } else {
                    binding.wasPressed();
                }
            }
        }
    }

    private static void HandleJoystickInGame() {
        for (ControllerBinding binding = ControllerSettings.startGameBindIteration(); binding != null; binding = ControllerSettings.getNextGameAutoBinding()) {
            binding.isPressed();
        }

        while (ControllerSettings.JoypadModInputLibrary.next() && (mc.currentScreen == null)) {
            // ignore controller events in the milliseconds following in GUI
            // controlling
            if (Minecraft.getSystemTime() - lastInGuiTick < 100)
                continue;

            if (ControllerSettings.loggingLevel > 3) {
                try {
                    ControllerInputEvent inputEvent = ControllerSettings.controllerUtils.getLastEvent(
                            ControllerSettings.JoypadModInputLibrary.getController(ControllerSettings.joyNo), ControllerSettings.JoypadModInputLibrary.getEventControlIndex());
                    if (inputEvent != null) {
                        JoypadMod.logger.info("Input event " + inputEvent.toString()
                                + " triggered.  Finding associated binding");
                    }
                } catch (Exception ex) {
                    JoypadMod.logger.error("Exception caught debugging controller input events: " + ex.toString());
                }
            }

            mc.inGameHasFocus = true;

            for (ControllerBinding binding = ControllerSettings.startGameBindIteration(); binding != null; binding = ControllerSettings.getNextGameAutoBinding()) {
                if (binding.bindingOptions.contains(BindingOptions.RENDER_TICK)) {
                    if (binding.wasPressed(false)) {
                        preRenderGameBucket.add(binding.inputString);
                    }
                } else {
                    binding.wasPressed();
                }
            }

        }
    }

    private static void ReplaceControlScreen(GuiControls gui) {
        if (!(mc.currentScreen instanceof JoypadConfigMenu)) {
            try {
                JoypadMod.logger.debug("Replacing control screen");
                GuiScreen parent = ObfuscationReflectionHelper.getPrivateValue(GuiControls.class, gui, "parentScreen", "field_146496_h");
                mc.displayGuiScreen(new JoypadConfigMenu(parent));
            } catch (Exception ex) {
                JoypadMod.logger.error("Failed to get parent of options gui.  aborting. Exception " + ex.toString());
            }
        }
    }

    public static boolean InGameCheckNeeded() {
        return CheckIfModEnabled() && mc.player != null && mc.currentScreen == null;
    }

    public static boolean InGuiCheckNeeded() {
        return CheckIfModEnabled() && mc.currentScreen != null;
    }

    public static boolean CheckIfModEnabled() {
        return mc != null && ControllerSettings.isInputEnabled() && ControllerSettings.joyNo != -1;
    }
}
