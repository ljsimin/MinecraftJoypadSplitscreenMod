package com.shiny.joypadmod.minecraftExtensions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

import com.shiny.joypadmod.JoypadMod;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.shiny.joypadmod.ControllerSettings;
import com.shiny.joypadmod.helpers.McKeyBindHelper;
import com.shiny.joypadmod.helpers.McObfuscationHelper;
import com.shiny.joypadmod.helpers.McObfuscationHelper.JSyms;
import com.shiny.joypadmod.event.ButtonInputEvent;
import com.shiny.joypadmod.event.ControllerBinding;
import com.shiny.joypadmod.event.ControllerBinding.BindingOptions;
import com.shiny.joypadmod.event.ControllerInputEvent;
import com.shiny.joypadmod.event.ControllerInputEvent.EventType;
import com.shiny.joypadmod.lwjglVirtualInput.VirtualMouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.client.GuiScrollingList;

public class JoypadControlList extends GuiScrollingList {
    private FontRenderer fontRenderer;
    private int controllerInputTimeout = 5000;
    public long controllerTickStart = 0;

    private static final int buttonHeight = 20;

    public static int lastXClick = 0;
    public static int lastYClick = 0;
    public static int lastMouseButton = 0;

    private int selectedIndex = -1;
    public int bindingIndexToUpdate = -1;
    public int inputIndexToUpdate = -1;
    private JoypadConfigMenu parent;
    private int lastListSize = 0;
    private int descriptionStartX;
    private int controlButtonCenterOffset = 20;

    public static GuiTextField textInputName = null;

    private static Minecraft mc = Minecraft.getMinecraft();

    public List<String> joyBindKeys;

    public JoypadControlList(JoypadConfigMenu parent, FontRenderer fontRenderer) {

        super(mc, parent.controlListWidth, // width
                parent.height, // height
                parent.controlListYStart, // top start
                parent.controlListYStart + parent.controlListHeight, // bottom end
                parent.controlListXStart, // left start
                buttonHeight); // entryHeight

        JoypadMod.logger.info("width:" + parent.controlListWidth + " height:" + parent.height + " yStart:"
                + parent.controlListYStart + " bottomEnd:" + (parent.controlListYStart + parent.controlListHeight)
                + "xStart: " + parent.controlListXStart);

        this.parent = parent;
        this.fontRenderer = fontRenderer;
        this.textInputName = new GuiTextField(20001, this.fontRenderer, 0, 0, 0, 0);
        this.textInputName.setVisible(false);
        joyBindKeys = new ArrayList<>();
        descriptionStartX = parent.buttonXStart_top;
        if (this.parent.getCurrentControllerId() != -1) {
            updatejoyBindKeys();
        }
    }

    public void updatejoyBindKeys() {
        joyBindKeys.clear();

        KeyBinding[] akeybinding = (KeyBinding[]) ArrayUtils.clone(mc.gameSettings.keyBindings);

        Arrays.sort(akeybinding);

        ControllerSettings.setDefaultJoyBindingMap(parent.getCurrentControllerId(), true);

        String category = "joy.categories.ui";
        joyBindKeys.add(category);

        String thisCategory = "";
        for (KeyBinding key : akeybinding) {
            String joyTarget = McObfuscationHelper.getKeyDescription(key).replace("key.", "joy.");
            String joyKey = "";

            if (ControllerSettings.joyBindingsMap.containsKey(joyTarget)) {
                joyKey = joyTarget;
                thisCategory = ControllerSettings.get(joyKey).getCategoryString();
            } else {
                joyKey = McObfuscationHelper.getKeyDescription(key);
                thisCategory = McObfuscationHelper.getKeyCategory(key).replace("key.", "joy.");
            }
            if (thisCategory.compareTo(category) != 0) {
                // get any other bindings that are of the outgoing category but not originating from Minecraft
                getBindingsWithCategory(category);
                joyBindKeys.add(thisCategory);
                category = thisCategory;
            }
            joyBindKeys.add(joyKey);
        }
        // get any leftover bindings that may have been missed
        getBindingsWithCategory(thisCategory);

        sortBindKeys();
    }

    private void getBindingsWithCategory(String category) {
        List<String> otherBindings = ControllerSettings.getBindingsWithCategory(category);
        for (String bindingKeys : otherBindings) {
            if (!joyBindKeys.contains(bindingKeys))
                joyBindKeys.add(bindingKeys);
        }
    }

    private void sortBindKeys() {
        int lastCategoryStart = -1;
        String[] list = joyBindKeys.toArray(new String[joyBindKeys.size()]);
        for (int i = 0; i < list.length; i++) {
            if (list[i].contains("categories") || (i + 1 == list.length && i++ > 0)) {
                if (lastCategoryStart != -1) {
                    // found current category end
                    if (lastCategoryStart < i - 2) {
                        Arrays.sort(list, lastCategoryStart + 1, i, Comparator.comparing(s -> parent.sGet(s)));
                    }
                }
                lastCategoryStart = i;
            }
        }
        joyBindKeys.clear();
        int longestWidthFound = 0;
        for (String s : list) {
            joyBindKeys.add(s);
            int thisWidth = parent.getFontRenderer().getStringWidth(parent.sGet(s));
            if (thisWidth > longestWidthFound)
                longestWidthFound = thisWidth;
        }
        if (longestWidthFound > parent.controllerButtonWidth / 2 - controlButtonCenterOffset - 3) {
            descriptionStartX = parent.buttonXStart_top - (parent.controllerButtonWidth / 2 - longestWidthFound)
                    - controlButtonCenterOffset - 5;
            if (descriptionStartX < 0)
                descriptionStartX = 0;
        }

    }

    @Override
    protected int getSize() {
        return joyBindKeys.size();
    }

    @Override
    protected void elementClicked(int index, boolean doubleClick) {
        selectedIndex = index;
    }

    @Override
    protected boolean isSelected(int index) {
        return false;
    }

    @Override
    protected void drawBackground() {
        parent.drawBackground(0);
    }

    int wheelDown = 0;

    @Override
    protected void drawSlot(int var1, int var2, int var3, int var4, Tessellator var5) {
        if (var1 >= joyBindKeys.size())
            return;

        if (wheelDown-- > 0)
            VirtualMouse.scrollWheel(-1);
        if (lastListSize != getSize()) {
            if (lastListSize > 0 && getSize() > lastListSize) {
                wheelDown = 350;
                selectedIndex = getSize() - 1;
            } else {
                selectedIndex = Math.max(-1, selectedIndex - 1);
            }
            lastListSize = getSize();
        }

        int centerStart = parent.buttonXStart_top + parent.controllerButtonWidth / 2;

        if (joyBindKeys.get(var1).contains("categories.")) {
            // this is a new category
            String category = parent.sGet(joyBindKeys.get(var1));

            this.fontRenderer.drawString(category, centerStart - this.fontRenderer.getStringWidth(category) / 2,
                    var3 + 5, -1);
            return;
        }

        String controlDescription = parent.sGet(joyBindKeys.get(var1));
        boolean duplicate = ControllerSettings.checkIfDuplicateBinding(joyBindKeys.get(var1));

        this.fontRenderer.drawString(controlDescription, descriptionStartX, var3 + buttonHeight / 2
                - this.fontRenderer.FONT_HEIGHT / 2, duplicate ? 0xFF5555 : -1);

        drawControlButtons(var1, centerStart - controlButtonCenterOffset, var3, joyBindKeys.get(var1),
                var1 == selectedIndex);

        if (bindingIndexToUpdate != -1) {
            if (getControllerInput() || Minecraft.getSystemTime() - controllerTickStart > controllerInputTimeout) {
                bindingIndexToUpdate = -1;
                ControllerSettings.suspendControllerInput(false, 0);
            }
        }
    }

    private int controlButtonWidth = 70;
    private int smallButtonWidth = 15;

    private void drawControlButtons(int id, int x, int y, String bindingKey, boolean slotSelected) {
        if (bindingKey == null)
            return;

        Minecraft mc = Minecraft.getMinecraft();
        final ScaledResolution scaledResolution = JoypadMod.GetScaledResolution();

        final int k = Mouse.getX() * scaledResolution.getScaledWidth() / mc.displayWidth;
        final int i1 = scaledResolution.getScaledHeight() - Mouse.getY() * scaledResolution.getScaledHeight()
                / mc.displayHeight - 1;

        // check if any buttons need updating
        checkButtonPressAction(id, x, y, bindingKey);

        ControllerBinding binding = ControllerSettings.get(bindingKey);

        String controlButtonStr = "NONE";
        if (this.parent.getCurrentControllerId() != -1) {
            if (bindingIndexToUpdate == id)
                controlButtonStr = "> ?? <";
            else if (binding != null) {
                controlButtonStr = ControllerSettings.controllerUtils.getHumanReadableInputName(
                        ControllerSettings.JoypadModInputLibrary.getController(this.parent.getCurrentControllerId()), binding.inputEvent);
            }
        }
        if (controlButtonStr.equals("NONE")) {
            if (binding != null) {
                if (binding.keyCodes != null && binding.keyCodes.length == 1 && binding.bindingOptions != null
                        && binding.bindingOptions.contains(BindingOptions.GAME_BINDING)) {
                    controlButtonStr = ControllerSettings.checkKeyCodeBound(this.parent.getCurrentControllerId(), binding.keyCodes[0], controlButtonStr);
                }
            } else {
                controlButtonStr = this.checkKeyCodeBound(bindingKey, controlButtonStr);
            }

            if (ControllerSettings.loggingLevel > 1 && !controlButtonStr.equals("NONE")) {
                JoypadMod.logger.info(String.format(
                        "Found that binding %s has a ControllerBinding (%s) that activates same code. ", bindingKey,
                        controlButtonStr));
            }
            // don't change controller input name if we still haven't found a controller input
            if (id == inputIndexToUpdate && controlButtonStr.equals("NONE"))
                inputIndexToUpdate = -1;
        }

        if (inputIndexToUpdate == id) {
            int key = parent.lastKeyCode;
            if (key == Keyboard.KEY_ESCAPE || key == 28) {
                if (key == 28) {
                    String theKey = binding.inputEvent.getDescription();
                    if (theKey != "NONE") {
                        String newMapping =
                                this.textInputName.getText().length() > 0 ?
                                        this.textInputName.getText() :
                                        binding.inputEvent.getDescription();
                        // update the mapping
                        ControllerSettings.controllerUtils.updateCurrentJoypadMap(theKey, newMapping);
                    }
                }
                this.textInputName.setVisible(false);
                inputIndexToUpdate = -1;
            } else {
                if (!this.textInputName.getVisible()) {
                    textInputName.width = controlButtonWidth;
                    textInputName.height = buttonHeight;
                    textInputName.setText(controlButtonStr);
                    this.textInputName.setVisible(true);
                }
                textInputName.x = x;
                textInputName.y = y;
                textInputName.drawTextBox();
                this.textInputName.setFocused(true);
            }
            parent.lastKeyCode = -1;
        } else {
            controlButtonStr = this.fontRenderer.trimStringToWidth(controlButtonStr, controlButtonWidth - 2);

            GuiButton b = new GuiButton(10001, x, y, controlButtonWidth, buttonHeight, controlButtonStr);
            b.drawButton(mc, k, i1, 0);

            if (binding == null)
                return;

            // - or x
            // draw a minus if the button is currently valid
            char optionRemove = McObfuscationHelper.symGet(JSyms.unbind);
            boolean enable = true;
            if (!binding.inputEvent.isValid()) {
                // else draw an X if the button is currently invalid and its a user binding
                if (binding.inputString.contains("user."))
                    optionRemove = McObfuscationHelper.symGet(JSyms.remove);
                else
                    // disable the button if the input is currently invalid
                    enable = false;
            }

            if (enable) {
                // draw the remove/unbind option for this binding
                b = new GuiButton(10002, x + controlButtonWidth, y, smallButtonWidth, buttonHeight, "" + optionRemove);
                b.drawButton(mc, k, i1, 0);

                // draw the toggle option button
                if (binding.inputEvent.getEventType() != EventType.AXIS
                        && !binding.bindingOptions.contains(BindingOptions.MENU_BINDING)) {
                    char toggle = McObfuscationHelper.symGet(McObfuscationHelper.JSyms.eCircle);
                    if (binding.bindingOptions.contains(BindingOptions.IS_TOGGLE))
                        toggle = McObfuscationHelper.symGet(McObfuscationHelper.JSyms.fCircle);
                    b = new GuiButton(10003, x + controlButtonWidth + smallButtonWidth, y, smallButtonWidth, buttonHeight,
                            "" + toggle);
                    b.drawButton(mc, k, i1, 0);
                }
            }
        }
    }

    private String checkKeyCodeBound(String bindingKey, String defaultStr) {
        KeyBinding kb = McKeyBindHelper.getMinecraftKeyBind(bindingKey);
        if (kb != null) {
            int keyCode = McObfuscationHelper.keyCode(kb);
            if (keyCode != Keyboard.KEY_NONE) {
                return ControllerSettings.checkKeyCodeBound(this.parent.getCurrentControllerId(), keyCode, defaultStr);
            }
        }
        return defaultStr;
    }

    private boolean checkButtonPressAction(int id, int x, int y, String bindingKey) {
        boolean checkCancelInputWait = lastYClick > 0;
        if (lastYClick >= y && lastYClick <= y + buttonHeight && lastXClick >= x) {
            lastYClick = 0;
            // remove any stale keycodes
            parent.lastKeyCode = -1;
            // check if we are in range of the buttons
            if (this.lastMouseButton == 1 && lastXClick <= x + controlButtonWidth) {
                if (bindingIndexToUpdate == -1) {
                    this.inputIndexToUpdate = id;
                    checkCancelInputWait = false;
                }
            } else if (this.lastMouseButton == 0 && !this.textInputName.getVisible()) {
                ControllerBinding binding = ControllerSettings.get(bindingKey);
                if (lastXClick <= x + controlButtonWidth) {
                    bindingIndexToUpdate = id;
                    controllerTickStart = Minecraft.getSystemTime();
                    ControllerSettings.suspendControllerInput(true, 10000);
                    checkCancelInputWait = false;
                } else if (binding != null) {
                    if (lastXClick <= x + controlButtonWidth + smallButtonWidth) {
                        if (binding.inputEvent.isValid()) {
                            ControllerSettings.unsetControllerBinding(parent.getCurrentControllerId(), binding.inputString);
                        } else if (binding.inputString.contains("user")) {
                            // delete this user binding
                            ControllerSettings.delete(binding.inputString);
                            joyBindKeys.remove(id);
                            return false;
                        }

                    } else if (lastXClick <= x + controlButtonWidth + smallButtonWidth * 2) {
                        if (binding.inputEvent.getEventType() != EventType.AXIS
                                && !binding.bindingOptions.contains(BindingOptions.MENU_BINDING)) {
                            ControllerSettings.setToggle(parent.getCurrentControllerId(), binding.inputString,
                                    !binding.bindingOptions.contains(BindingOptions.IS_TOGGLE));
                        }
                    }
                }
            }
        }

        if (checkCancelInputWait) {
            if (bindingIndexToUpdate != -1)
                // click was outside so timeout the controller input wait
                controllerTickStart = controllerInputTimeout + 1;
            inputIndexToUpdate = -1;
            this.textInputName.setVisible(false);
        }
        return true;
    }

    private boolean getControllerInput() {
        try {
            ControllerSettings.JoypadModInputLibrary.poll();
            while (ControllerSettings.JoypadModInputLibrary.next()) {
                if (Minecraft.getSystemTime() - controllerTickStart < 200) {
                    JoypadMod.logger.info("Discarding events that occured too soon after last button click");
                } else {
                    if (ControllerSettings.JoypadModInputLibrary.getEventSource().getIndex() != parent.getCurrentControllerId())
                        continue;

                    ControllerInputEvent inputEvent = ControllerSettings.controllerUtils.getLastEvent(
                            ControllerSettings.JoypadModInputLibrary.getController(parent.getCurrentControllerId()),
                            ControllerSettings.JoypadModInputLibrary.getEventControlIndex());
                    if (inputEvent != null) {
                        float threshold = inputEvent.getThreshold();
                        JoypadMod.logger.info("Received from controller: " + inputEvent.getName() + " threshold: " + threshold);

                        if (inputEvent.getEventType() == EventType.AXIS) {
                            threshold = ControllerSettings.defaultAxisThreshhold * (threshold > 0 ? 1 : -1);
                        } else if (inputEvent.getEventType() == EventType.POV) {
                            threshold = ControllerSettings.defaultPovThreshhold * (threshold > 0 ? 1 : -1);
                        } else {
                            threshold = 1;
                        }
                        inputEvent.setThreshold(threshold);
                        String bindingKey = joyBindKeys.get(bindingIndexToUpdate);
                        ControllerBinding binding = this.findOrCreateBinding(bindingKey);
                        binding.inputEvent = inputEvent;
                        ControllerSettings.setControllerBinding(parent.getCurrentControllerId(), binding.inputString,
                                binding);
                        return true;
                    }
                }
            }
        } catch (Exception ex) {
            JoypadMod.logger.error("Caught exception while trying to set controller button! " + ex.toString());
        }
        return false;
    }

    private ControllerBinding findOrCreateBinding(String bindingKey) {
        ControllerBinding b = ControllerSettings.get(bindingKey);

        if (b == null) {
            KeyBinding kb = McKeyBindHelper.getMinecraftKeyBind(bindingKey);
            if (kb != null) {
                b = new ControllerBinding(bindingKey, bindingKey, new ButtonInputEvent(parent.getCurrentControllerId(),
                        -1, 1), new int[]{McObfuscationHelper.keyCode(kb)}, 0, EnumSet.of(
                        BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD, BindingOptions.RENDER_TICK,
                        ControllerBinding.mapMinecraftCategory(McObfuscationHelper.getKeyCategory(kb))));
            }
        }

        return b;
    }

}
