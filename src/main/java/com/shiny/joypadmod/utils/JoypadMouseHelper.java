package com.shiny.joypadmod.utils;

import com.shiny.joypadmod.ControllerSettings;

import com.shiny.joypadmod.JoypadMod;
import net.minecraft.util.MouseHelper;

// warning: small but non zero chance of this causing incompatibility with other mods
public class JoypadMouseHelper extends MouseHelper {
    /**
     * Grabs the mouse cursor it doesn't move and isn't seen.
     */
    @Override
    public void grabMouseCursor() {
        if (ControllerSettings.isInputEnabled() && !ControllerSettings.grabMouse) {
            // VirtualMouse.setGrabbed(true);
            return;
        }

        super.grabMouseCursor();
    }

    /**
     * Ungrabs the mouse cursor so it can be moved and set it to the center of the screen
     */
    @Override
    public void ungrabMouseCursor() {
        if (ControllerSettings.isInputEnabled() && !ControllerSettings.grabMouse) {
            // VirtualMouse.setGrabbed(false);
            return;
        }

        super.ungrabMouseCursor();
    }

    /*
     * @Override public void mouseXYChange() { this.deltaX = Mouse.getDX(); this.deltaY = Mouse.getDY(); if (this.deltaX != 0 || this.deltaY != 0) { JoypadMod.logger.info("MouseHelper dx:" + deltaX + " dy:"
     * + deltaY); } }
     */

    @Override
    protected void finalize() throws Throwable {
        try {
            JoypadMod.logger.warn("JoypadMouseHelper being garbage collected. "
                    + "If Minecraft not shutting down, this means another mod may have replaced it.");
        } catch (Throwable t) {
            throw t;
        } finally {
            super.finalize();
        }
    }

    // side note, I had early visions of simply Overriding the mouseXYChange() method and this mod would be
    // half done, but alas not all Minecraft uses this method.

}
