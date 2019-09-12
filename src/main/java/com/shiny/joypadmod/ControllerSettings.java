package com.shiny.joypadmod;

// Common code

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.shiny.joypadmod.devices.*;
import com.shiny.joypadmod.gui.ButtonScreenTips;
import net.minecraft.crash.CrashReport;
import org.lwjgl.input.Keyboard;

import com.shiny.joypadmod.mappings.DefaultAxisMappings;
import com.shiny.joypadmod.mappings.DefaultButtonMappings;
import com.shiny.joypadmod.utils.ConfigFile;
import com.shiny.joypadmod.utils.ConfigFile.UserJoypadSettings;
import com.shiny.joypadmod.utils.McKeyBindHelper;
import com.shiny.joypadmod.utils.McObfuscationHelper;
import com.shiny.joypadmod.event.AxisInputEvent;
import com.shiny.joypadmod.event.ButtonInputEvent;
import com.shiny.joypadmod.event.ControllerBinding;
import com.shiny.joypadmod.event.ControllerBinding.BindingOptions;
import com.shiny.joypadmod.event.ControllerUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

public class ControllerSettings {

    public static final float defaultAxisDeadZone = 0.20f;
    public static final float defaultAxisThreshhold = 0.7f;
    public static final float defaultPovThreshhold = 0.9f;

    public static List<ControllerBinding> userDefinedBindings;

    public static Map<String, ControllerBinding> joyBindingsMap = null;

    public static boolean useConstantCameraMovement = false;
    public static boolean displayHints = false;
    public static boolean useLegacyInput = false;

    public static InputLibrary JoypadModInputLibrary = null;
    public static int joyNo = -1;

    public static int inGameSensitivity = 25;
    public static int inMenuSensitivity = 10;
    public static int scrollDelay = 50;

    public static int loggingLevel = 1;

    // used for some preliminary safe checks
    private static int requiredMinButtonCount = 4;
    private static int requiredButtonCount = 12;
    private static int requiredAxisCount = 4;

    private static Map<String, List<Integer>> validControllers;
    private static Map<String, List<Integer>> inValidControllers;
    private static Map<Integer, List<Integer>> singleDirectionAxis;
    private static DefaultButtonMappings bMap = new DefaultButtonMappings();
    private static DefaultAxisMappings aMap = new DefaultAxisMappings();
    public static ControllerUtils controllerUtils;

    public static List<Integer> xbox6Axis = new ArrayList<>();

    // inputEnabled will control whether the mod will continually poll the
    // selected joystick for data
    private static boolean inputEnabled = false;

    // suspending the controller will tell the main controller loop to stop
    // polling.
    // this is used during the controller setup screen when listening for
    // controller events to map to an action
    private static boolean suspendControllerInput = false;

    public static boolean invertYAxis = false;
    public static boolean grabMouse = false;

    public static ConfigFile config = null;

    public ControllerSettings(File configFile) {
        config = new ConfigFile(configFile);
        config.init();
        controllerUtils = new ControllerUtils();
        validControllers = new HashMap<>();
        inValidControllers = new HashMap<>();
        singleDirectionAxis = new HashMap<>();
        joyBindingsMap = new HashMap<>();
        userDefinedBindings = new ArrayList<>();
        grabMouse = ControllerSettings.getGameOption("-Global-.GrabMouse").equals("true");

        if (!useLegacyInput) {
            // try XInput only first
            try {
                JoypadModInputLibrary = new XInputLibrary();
                JoypadModInputLibrary.create();
                JoypadMod.logger.info("Using XInput library for Joypad Mod controls");
            } catch (UnsatisfiedLinkError e) {
                JoypadMod.logger.error("XInput: Controller object linking error. " + e.toString());
            } catch (Exception ex) {
                JoypadMod.logger.error("XInput: Failed creating controller object. " + ex.toString());
            }
        } else {
            JoypadMod.logger.info("XInput: LegacyInput is set to true.");
        }

        if (JoypadModInputLibrary == null || !JoypadModInputLibrary.isCreated()) {
            // if it failed fall back to LWJGL
            try {
                JoypadModInputLibrary = new LWJGLibrary();
                JoypadModInputLibrary.create();
                bMap = bMap.new LWJGLButtonMappings();
                aMap = aMap.new LWJGLAxisMappings();
                JoypadMod.logger.info("Using LWJGL for Joypad Mod controls");
            } catch (Exception ex) {
                Minecraft.getMinecraft().crashed(new CrashReport("Failed creating LWJGL controller object", ex));
            }
        }
    }

    private static int currentDisplayedMap = -1;

    public static void setDefaultJoyBindingMap(int joyIndex, boolean updateWithConfigFile) {
        if (currentDisplayedMap == joyIndex) {
            JoypadMod.logger.info("Skipping setting up the joybinding map as it is already set up for this joypad");
            return;
        }

        currentDisplayedMap = joyIndex;

        GameSettings settings = Minecraft.getMinecraft().gameSettings;
        JoypadMod.logger.info("Setting default joy binding map");

        joyBindingsMap.clear();

        int yAxisIndex = ControllerUtils.findYAxisIndex(joyIndex);
        int xAxisIndex = ControllerUtils.findXAxisIndex(joyIndex);

        // check for new Xbox one case
        InputDevice controller = JoypadModInputLibrary.getController(joyIndex);
        if (controller.getName().toLowerCase().contains("xbox one") && controller.getAxisCount() == 6) {
            JoypadMod.logger.info("XBox One 6 axis joypad detected.");
            if (!xbox6Axis.contains(joyIndex))
                xbox6Axis.add(joyIndex);
        }

        joyBindingsMap.put("joy.jump",
                new ControllerBinding("joy.jump", "Jump", new ButtonInputEvent(joyIndex, bMap.A(), 1),
                        new int[]{McObfuscationHelper.keyCode(settings.keyBindJump)}, 0,
                        EnumSet.of(BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD,
                                BindingOptions.CATEGORY_MOVEMENT)));

        joyBindingsMap.put("joy.inventory",
                new ControllerBinding("joy.inventory", "Open inventory", new ButtonInputEvent(joyIndex, bMap.Y(), 1),
                        new int[]{McObfuscationHelper.keyCode(settings.keyBindInventory)}, 100,
                        EnumSet.of(BindingOptions.GAME_BINDING, BindingOptions.CATEGORY_INVENTORY)));

        joyBindingsMap.put("joy.drop",
                new ControllerBinding("joy.drop", "Drop", new ButtonInputEvent(joyIndex, bMap.Back(), 1),
                        new int[]{McObfuscationHelper.keyCode(settings.keyBindDrop)}, 0,
                        EnumSet.of(BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD,
                                BindingOptions.CATEGORY_GAMEPLAY)));

        joyBindingsMap.put("joy.sneak",
                new ControllerBinding("joy.sneak", "Sneak", new ButtonInputEvent(joyIndex, bMap.LS(), 1),
                        new int[]{McObfuscationHelper.keyCode(settings.keyBindSneak)}, 0,
                        EnumSet.of(BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD,
                                BindingOptions.CATEGORY_MOVEMENT)));

        int axisIndexToUse = xbox6Axis.contains(joyIndex) ? 5 : 4;
        float thresholdToUse = xbox6Axis.contains(joyIndex) ? defaultAxisThreshhold : defaultAxisThreshhold * -1;

        joyBindingsMap.put("joy.attack",
                new ControllerBinding("joy.attack", "Attack",
                        new AxisInputEvent(joyIndex, aMap.RT(), thresholdToUse, defaultAxisDeadZone),
                        new int[]{-100}, 0, EnumSet.of(BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD,
                        BindingOptions.CATEGORY_GAMEPLAY)));

        joyBindingsMap.put("joy.use",
                new ControllerBinding("joy.use", "Use",
                        new AxisInputEvent(joyIndex, aMap.LT(), defaultAxisThreshhold, defaultAxisDeadZone),
                        new int[]{-99}, 0, EnumSet.of(BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD,
                        BindingOptions.CATEGORY_GAMEPLAY)));

        joyBindingsMap.put("joy.interact",
                new ControllerBinding("joy.interact", "Interact", new ButtonInputEvent(joyIndex, bMap.X(), 1),
                        new int[]{-99}, 0,
                        EnumSet.of(BindingOptions.GAME_BINDING, BindingOptions.CATEGORY_GAMEPLAY)));

        joyBindingsMap.put("joy.guiLeftClick",
                new ControllerBinding("joy.guiLeftClick", "Left click", new ButtonInputEvent(joyIndex, bMap.A(), 1),
                        new int[]{-100}, 0, EnumSet.of(BindingOptions.MENU_BINDING, BindingOptions.CATEGORY_UI)));

        joyBindingsMap.put("joy.guiRightClick",
                new ControllerBinding("joy.guiRightClick", "Right click", new ButtonInputEvent(joyIndex, bMap.X(), 1),
                        new int[]{-99}, 0, EnumSet.of(BindingOptions.MENU_BINDING, BindingOptions.CATEGORY_UI)));

        joyBindingsMap.put("joy.prevItem",
                new ControllerBinding("joy.prevItem", "Previous item", new ButtonInputEvent(joyIndex, bMap.LB(), 1),
                        new int[]{-199}, 0,
                        EnumSet.of(BindingOptions.GAME_BINDING, BindingOptions.CATEGORY_GAMEPLAY)));

        joyBindingsMap.put("joy.nextItem",
                new ControllerBinding("joy.nextItem", "Next item", new ButtonInputEvent(joyIndex, bMap.RB(), 1),
                        new int[]{-201}, 0,
                        EnumSet.of(BindingOptions.GAME_BINDING, BindingOptions.CATEGORY_GAMEPLAY)));

        joyBindingsMap.put("joy.sprint",
                new ControllerBinding("joy.sprint", "Sprint", new ButtonInputEvent(joyIndex, bMap.RS(), 1),
                        new int[]{Keyboard.KEY_LCONTROL}, 0, EnumSet.of(BindingOptions.GAME_BINDING,
                        BindingOptions.REPEAT_IF_HELD, BindingOptions.CATEGORY_GAMEPLAY)));

        joyBindingsMap.put("joy.menu", new ControllerBinding("joy.menu", "Open menu",
                new ButtonInputEvent(joyIndex, bMap.Start(), 1), new int[]{Keyboard.KEY_ESCAPE}, 0,
                EnumSet.of(BindingOptions.GAME_BINDING, BindingOptions.MENU_BINDING, BindingOptions.CATEGORY_MISC)));

        joyBindingsMap.put("joy.shiftClick",
                new ControllerBinding("joy.shiftClick", "Shift-click", new ButtonInputEvent(joyIndex, bMap.B(), 1),
                        new int[]{Keyboard.KEY_LSHIFT, -100}, 0, EnumSet.of(BindingOptions.MENU_BINDING,
                        BindingOptions.REPEAT_IF_HELD, BindingOptions.CATEGORY_INVENTORY)));

        joyBindingsMap.put("joy.cameraX+",
                new ControllerBinding("joy.cameraX+", "Look right",
                        new AxisInputEvent(joyIndex, aMap.RSx(), defaultAxisThreshhold, defaultAxisDeadZone), null, 0,
                        EnumSet.of(BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD,
                                BindingOptions.CATEGORY_GAMEPLAY)));

        joyBindingsMap.put("joy.cameraX-",
                new ControllerBinding("joy.cameraX-", "Look left",
                        new AxisInputEvent(joyIndex, aMap.RSx(), defaultAxisThreshhold * -1, defaultAxisDeadZone), null,
                        0, EnumSet.of(BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD,
                        BindingOptions.CATEGORY_GAMEPLAY)));

        joyBindingsMap.put("joy.cameraY-",
                new ControllerBinding("joy.cameraY-", "Look up",
                        new AxisInputEvent(joyIndex, aMap.RSy(), defaultAxisThreshhold * -1, defaultAxisDeadZone), null,
                        0, EnumSet.of(BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD,
                        BindingOptions.CATEGORY_GAMEPLAY)));

        joyBindingsMap.put("joy.cameraY+",
                new ControllerBinding("joy.cameraY+", "Look down",
                        new AxisInputEvent(joyIndex, aMap.RSy(), defaultAxisThreshhold, defaultAxisDeadZone), null, 0,
                        EnumSet.of(BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD,
                                BindingOptions.CATEGORY_GAMEPLAY)));

        joyBindingsMap.put("joy.right",
                new ControllerBinding("joy.right", "Strafe right",
                        new AxisInputEvent(joyIndex, aMap.LSx(), defaultAxisThreshhold, defaultAxisDeadZone),
                        new int[]{McObfuscationHelper.keyCode(settings.keyBindRight)}, 0,
                        EnumSet.of(BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD,
                                BindingOptions.CATEGORY_MOVEMENT)));

        joyBindingsMap.put("joy.left",
                new ControllerBinding("joy.left", "Strafe left",
                        new AxisInputEvent(joyIndex, aMap.LSx(), defaultAxisThreshhold * -1, defaultAxisDeadZone),
                        new int[]{McObfuscationHelper.keyCode(settings.keyBindLeft)}, 0,
                        EnumSet.of(BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD,
                                BindingOptions.CATEGORY_MOVEMENT)));

        joyBindingsMap.put("joy.back",
                new ControllerBinding("joy.back", "Move backward",
                        new AxisInputEvent(joyIndex, aMap.LSy(), defaultAxisThreshhold, defaultAxisDeadZone),
                        new int[]{McObfuscationHelper.keyCode(settings.keyBindBack)}, yAxisIndex,
                        EnumSet.of(BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD,
                                BindingOptions.CATEGORY_MOVEMENT)));

        joyBindingsMap.put("joy.forward",
                new ControllerBinding("joy.forward", "Move forward",
                        new AxisInputEvent(joyIndex, aMap.LSy(), defaultAxisThreshhold * -1, defaultAxisDeadZone),
                        new int[]{McObfuscationHelper.keyCode(settings.keyBindForward)}, 0,
                        EnumSet.of(BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD,
                                BindingOptions.CATEGORY_MOVEMENT)));

        joyBindingsMap.put("joy.guiX+", new ControllerBinding("joy.guiX+", "GUI right",
                new AxisInputEvent(joyIndex, aMap.LSx(), defaultAxisThreshhold, defaultAxisDeadZone), null, 0,
                EnumSet.of(BindingOptions.MENU_BINDING, BindingOptions.REPEAT_IF_HELD, BindingOptions.CATEGORY_UI)));

        joyBindingsMap.put("joy.guiX-", new ControllerBinding("joy.guiX-", "GUI left",
                new AxisInputEvent(joyIndex, aMap.LSx(), defaultAxisThreshhold * -1, defaultAxisDeadZone), null, 0,
                EnumSet.of(BindingOptions.MENU_BINDING, BindingOptions.REPEAT_IF_HELD, BindingOptions.CATEGORY_UI)));

        joyBindingsMap.put("joy.guiY+", new ControllerBinding("joy.guiY+", "GUI down",
                new AxisInputEvent(joyIndex, aMap.LSy(), defaultAxisThreshhold, defaultAxisDeadZone), null, 0,
                EnumSet.of(BindingOptions.MENU_BINDING, BindingOptions.REPEAT_IF_HELD, BindingOptions.CATEGORY_UI)));

        joyBindingsMap.put("joy.guiY-", new ControllerBinding("joy.guiY-", "GUI up",
                new AxisInputEvent(joyIndex, aMap.LSy(), defaultAxisThreshhold * -1, defaultAxisDeadZone), null, 0,
                EnumSet.of(BindingOptions.MENU_BINDING, BindingOptions.REPEAT_IF_HELD, BindingOptions.CATEGORY_UI)));

        joyBindingsMap.put("joy.closeInventory",
                new ControllerBinding("joy.closeInventory", "Close container",
                        new ButtonInputEvent(joyIndex, bMap.Y(), 1),
                        new int[]{McObfuscationHelper.keyCode(settings.keyBindInventory)}, 100,
                        EnumSet.of(BindingOptions.MENU_BINDING, BindingOptions.CATEGORY_INVENTORY)));

        joyBindingsMap.put("joy.scrollDown",
                new ControllerBinding("joy.scrollDown", "Scroll down", new ButtonInputEvent(joyIndex, bMap.RB(), 1),
                        new int[]{-201}, scrollDelay,
                        EnumSet.of(BindingOptions.MENU_BINDING, BindingOptions.REPEAT_IF_HELD,
                                BindingOptions.RENDER_TICK, BindingOptions.CATEGORY_UI)));

        joyBindingsMap.put("joy.scrollUp",
                new ControllerBinding("joy.scrollUp", "Scroll up", new ButtonInputEvent(joyIndex, bMap.LB(), 1),
                        new int[]{-199}, scrollDelay,
                        EnumSet.of(BindingOptions.MENU_BINDING, BindingOptions.REPEAT_IF_HELD,
                                BindingOptions.RENDER_TICK, BindingOptions.CATEGORY_UI)));

        if (updateWithConfigFile)
            config.getJoypadSavedBindings(joyIndex, controller.getName());

        List<ControllerBinding> userBindings = config.getUserDefinedBindings(joyIndex);

        for (ControllerBinding b : userBindings) {
            joyBindingsMap.put(b.inputString, b);
        }
    }

    public static ControllerBinding get(String key) {
        return joyBindingsMap.get(key);
    }

    public static List<String> getBindingsWithCategory(String categoryString) {
        List<String> cList = new ArrayList<>();
        for (Map.Entry<String, ControllerBinding> entry : joyBindingsMap.entrySet()) {
            if (entry.getValue().getCategoryString().compareTo(categoryString) == 0)
                cList.add(entry.getValue().inputString);
        }
        return cList;
    }

    public static void delete(String key) {
        ControllerBinding binding = joyBindingsMap.get(key);
        if (binding != null) {
            if (binding.inputString.contains("user.")) {
                userDefinedBindings.remove(binding);
            }
            joyBindingsMap.remove(key);
            config.deleteUserBinding(binding);
        }

    }

    public static int bindingListSize() {
        return joyBindingsMap.size();
    }

    public void init() {
        if (config.preferedJoyName == "disabled") {
            JoypadMod.logger.warn("Controller input disabled due to joypad value 'preferedJoyName' set to disabled");
            inputEnabled = false;
            //ControllerSettings.modDisabled = true; TODO: See if this ruins anything.
            return;
        }

        JoypadMod.logger.info("Initializing Controllers");

        // only set a controller as in use on init if they have previously gone
        // into controls to set it up
        // and it is detected as present

        int nControllers = detectControllers();
        int selectedController = -1;
        if (nControllers > 0 && config.preferedJoyNo >= 0) {
            selectedController = checkForControllerAtIndex(config.preferedJoyName, config.preferedJoyNo);
            if (selectedController >= 0) {
                setController(selectedController);
                JoypadModInputLibrary.clearEvents();
            } else {
                JoypadMod.logger.info("No joypad set up for this session.  Must enter controller menu to enable");
            }

        }

        if (selectedController < 0) {
            JoypadMod.logger.warn("No joypad set up for this session."
                    + (nControllers > 0 ? " Must enter controller menu to enable." : ""));
            inputEnabled = false;
        }
    }

    private int detectControllers() {
        validControllers.clear();
        inValidControllers.clear();
        {
            try {
                if (!JoypadModInputLibrary.isCreated())
                    JoypadModInputLibrary.create();

                if (JoypadModInputLibrary.getControllerCount() > 0) {
                    JoypadMod.logger.info("Found " + JoypadModInputLibrary.getControllerCount() + " controller(s) in total.");
                    for (int joyIndex = 0; joyIndex < JoypadModInputLibrary.getControllerCount(); joyIndex++) {
                        InputDevice thisController = JoypadModInputLibrary.getController(joyIndex);

                        JoypadMod.logger.info("Found controller " + thisController.getName() + " (" + thisController.getIndex() + ")");
                        JoypadMod.logger.info("It has  " + thisController.getButtonCount() + " buttons and " + thisController.getAxisCount() + " axes.");

                        if (controllerUtils.meetsInputRequirements(thisController, requiredButtonCount,
                                requiredMinButtonCount, requiredAxisCount)) {
                            JoypadMod.logger.info("Controller #" + joyIndex + " ( " + thisController.getName()
                                    + ") meets the input requirements");
                            addControllerToList(validControllers, thisController.getName(), joyIndex);
                        } else {
                            JoypadMod.logger.info("This controller does not meet the input requirements");
                            addControllerToList(inValidControllers, thisController.getName(), joyIndex);
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Couldn't initialize Controllers: " + e.getMessage());
            }
        }

        JoypadMod.logger.info("Found " + validControllers.size() + " valid controllers!");
        return validControllers.size();
    }

    private static String intListToString(List<Integer> intList) {
        StringBuilder sb = new StringBuilder();
        if (intList.size() > 0) {
            for (Integer theInt : intList) {
                sb.append(theInt);
                sb.append(',');
            }
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    private static List<Integer> stringToIntList(String intStr) {
        List<Integer> ret = new ArrayList<>();
        for (String theInt : intStr.split(",")) {
            try {
                ret.add(Integer.parseInt(theInt));
            } catch (NumberFormatException e) {
                JoypadMod.logger.info("[stringToIntList] Invalid Integer in list." + e.toString());
            }
        }
        return ret;
    }

    /**
     * @param controllerNo The index of the controller
     * @param axisNo       The index of the axis
     * @return true if the axis is single-directed, and false instead
     */
    public static boolean isSingleDirectionAxis(int controllerNo, int axisNo) {
        List<Integer> axis = singleDirectionAxis.get(controllerNo);
        if (axis != null)
            return axis.contains(axisNo);
        return false;
    }

    /**
     * @param controllerNo The index of the controller
     */
    private static void addSingleDirectionAxis(int controllerNo) {
        if (!singleDirectionAxis.containsKey(controllerNo)) {
            singleDirectionAxis.put(controllerNo, new ArrayList<>());
        }
    }

    private static void setSingleDirectionAxis(int controllerNo, List<Integer> axisList) {
        List<Integer> finalAxisList = new ArrayList<>();
        InputDevice c = JoypadModInputLibrary.getController(controllerNo);
        StringBuilder sbSDAMessage = new StringBuilder();
        sbSDAMessage.append("Setting the following as Single Direction Axes on " + c.getName());
        for (Integer i : axisList) {
            if (i >= 0 && i < c.getAxisCount()) {
                sbSDAMessage.append(" " + c.getAxisName(i));
                finalAxisList.add(i);
            } else {
                JoypadMod.logger.info("Rejecting invalid axis in Single Direction Axis list: " + i);
            }
        }
        JoypadMod.logger.info(sbSDAMessage.toString());
        singleDirectionAxis.put(controllerNo, finalAxisList);
    }

    /**
     * @param controllerNo The index of the controller
     * @param axisNo       The index of the axis
     */
    public static void toggleSingleDirectionAxis(int controllerNo, int axisNo) {
        addSingleDirectionAxis(controllerNo);
        List<Integer> axis = singleDirectionAxis.get(controllerNo);
        if (axis.contains(axisNo)) {
            axis.remove(Integer.valueOf(axisNo));
        } else {
            axis.add(axisNo);
        }
    }

    /**
     * @param controllerNo The index of the controller
     * @return List of single direction axis
     */
    public static List<Integer> getSingleDirectionAxis(int controllerNo) {
        if (singleDirectionAxis.containsKey(controllerNo))
            return singleDirectionAxis.get(controllerNo);
        return new ArrayList<>(); //return empty list
    }

    public static boolean setController(int controllerNo) {
        JoypadMod.logger.info("Attempting to use controller " + controllerNo);

        try {
            if (!JoypadModInputLibrary.isCreated())
                JoypadModInputLibrary.create();

            JoypadMod.logger.info("Controllers.getControllerCount == " + JoypadModInputLibrary.getControllerCount());

            if (controllerNo < 0 || controllerNo >= JoypadModInputLibrary.getControllerCount()) {
                JoypadMod.logger.error("Attempting to set controller index " + controllerNo + " there are currently "
                        + JoypadModInputLibrary.getControllerCount() + " controllers detected.");
                return false;
            }

            addSingleDirectionAxis(controllerNo);

            InputDevice controller = JoypadModInputLibrary.getController(controllerNo);
            ControllerSettings.setDefaultJoyBindingMap(controllerNo, true);
            joyNo = controllerNo;
            controllerUtils.printDeadZones(controller);
            inputEnabled = true;

            applySavedDeadZones(joyNo);

            String axisStr = config.getConfigFileSetting("-SingleDirectionAxis-." + controller.getName());
            if (axisStr != null) // should never be null (default to "false")
            // but just in case
            {
                // handle the case where a 6 axis xbox controller is detected
                // and they haven't manually applied any SDA settings
                if (axisStr.equals("false")) {
                    if (xbox6Axis.contains(joyNo)) {
                        setSingleDirectionAxis(joyNo, new ArrayList<>(Arrays.asList(4, 5)));
                        JoypadMod.logger.info(
                                "Auto setting XBox One single direction axis. If there are trigger problems after this this is why");
                    }
                } else if (!axisStr.equals("")) {
                    setSingleDirectionAxis(joyNo, stringToIntList(axisStr));
                    JoypadMod.logger.info("Retrieved informations about single-direction axis");
                }
            }

            config.updatePreferedJoy(controllerNo, controller.getName());

            Minecraft.getMinecraft().gameSettings.pauseOnLostFocus = false;
            JoypadMouse.AxisReader.centerCrosshairs();
            checkIfBindingsNeedUpdating();
            unpressAll();
            return true;
        } catch (Exception e) {
            JoypadMod.logger.error("Couldn't initialize Controllers: " + e.toString());
            inputEnabled = false;
        }

        return false;
    }

    public static void resetBindings(int joyIndex) {
        if (joyIndex >= 0 && joyIndex < JoypadModInputLibrary.getControllerCount()) {
            currentDisplayedMap = -1;
            setDefaultJoyBindingMap(joyIndex, false);
            for (Map.Entry<String, ControllerBinding> entry : joyBindingsMap.entrySet()) {
                if (!entry.getKey().contains("user."))
                    config.saveControllerBinding(JoypadModInputLibrary.getController(joyIndex).getName(),
                            entry.getValue());
            }
        }

        unpressAll();
    }

    public static boolean isInputEnabled() {
        return inputEnabled;
    }

    public static void setInputEnabled(int joyIndex, boolean b) {
        unpressAll();
        if (!b) {
            JoypadMouse.AxisReader.setXY(0, 0);
            VirtualMouse.setXY(0, 0);
            inputEnabled = false;
            config.updatePreferedJoy(-1, null);
            return;
        }

        if (joyNo != joyIndex) {
            setController(joyIndex);
            return;
        }

        inputEnabled = true;
        config.updatePreferedJoy(joyIndex, JoypadModInputLibrary.getController(joyIndex).getName());
        JoypadMouse.AxisReader.centerCrosshairs();
    }

    private static long suspendMax;
    private static long suspendStart;

    public static void suspendControllerInput(boolean suspend, long maxTicksToSuspend) {
        if (suspend) {
            suspendStart = Minecraft.getSystemTime();
            suspendMax = maxTicksToSuspend;
        }
        ControllerSettings.suspendControllerInput = suspend;
        JoypadMouse.UnpressButtons();
    }

    public static boolean isSuspended() {
        if (ControllerSettings.suspendControllerInput) {
            if (Minecraft.getSystemTime() - suspendStart > suspendMax) {
                ControllerSettings.suspendControllerInput = false;
            }
        }
        return ControllerSettings.suspendControllerInput;
    }

    public static void setControllerBinding(int joyIndex, String bindingKey, ControllerBinding binding) {
        ControllerSettings.joyBindingsMap.put(bindingKey, binding);
        config.saveControllerBinding(JoypadModInputLibrary.getController(joyIndex).getName(), binding);
    }

    public static void unsetControllerBinding(int joyIndex, String key) {
        ControllerBinding binding = joyBindingsMap.get(key);
        if (binding != null) {
            binding.inputEvent = new ButtonInputEvent(0, -1, 1);
            config.saveControllerBinding(JoypadModInputLibrary.getController(joyIndex).getName(), binding);
            unpressAll();
        }
    }

    public static void addUserBinding(ControllerBinding binding) {
        joyBindingsMap.put(binding.inputString, binding);
        userDefinedBindings.add(binding);
        config.saveControllerBinding(null, binding);
    }

    private static void addControllerToList(Map<String, List<Integer>> listToUse, String name, int id) {
        List<Integer> ids = null;
        if (listToUse.containsKey(name)) {
            ids = listToUse.get(name);
        } else {
            ids = new ArrayList<>();
        }
        ids.add(id);

        listToUse.put(name, ids);
    }

    // look for controllername in valid controllers
    // if not found return -1 indicating the controller wasn't found at all
    // if found:
    // if controller at selected index, then return that index
    // else return the first index it is found at
    private int checkForControllerAtIndex(String controllerName, int joyIndex) {
        if (controllerName != null && validControllers.containsKey(controllerName)) {
            List<Integer> ids = validControllers.get(controllerName);
            if (ids.contains(joyIndex))
                return joyIndex;

            return ids.get(0);
        }

        return -1;
    }

    private void logControllerInfo(InputDevice controller) {
        JoypadMod.logger.info("Found controller " + controller.getName() + " (" + controller.getIndex() + ")");
        JoypadMod.logger.info("It has  " + controller.getButtonCount() + " buttons.");
        JoypadMod.logger.info("It has  " + controller.getAxisCount() + " axes.");
    }

    private static List<Integer> flattenMap(Map<String, List<Integer>> listToFlatten) {
        List<Integer> values = new ArrayList<>();
        for (Entry<String, List<Integer>> stringListEntry : listToFlatten.entrySet()) {
            List<Integer> ids = stringListEntry.getValue();
            for (Integer id : ids) {
                values.add(id);
            }
        }
        java.util.Collections.sort(values);

        return values;
    }

    public static List<Integer> getJoypadList(boolean includeInvalid) {
        List<Integer> joypadList = ControllerSettings.flattenMap(ControllerSettings.validControllers);
        if (includeInvalid) {
            joypadList.addAll(ControllerSettings.flattenMap(ControllerSettings.inValidControllers));
        }
        return joypadList;
    }

    public static boolean getInvertYAxis() {
        return invertYAxis;
    }

    public static void setInvertYAxis(boolean b) {
        if (invertYAxis != b) {
            invertYAxis = b;
            config.updateConfigFileSetting(UserJoypadSettings.InvertY, "" + b);
        }
    }

    public static void setToggle(int joyIndex, String bindingKey, boolean b) {

        ControllerBinding binding = joyBindingsMap.get(bindingKey);
        boolean changed = false;
        if (b) {
            changed = binding.bindingOptions.add(BindingOptions.IS_TOGGLE);
        } else {
            changed = binding.bindingOptions.remove(BindingOptions.IS_TOGGLE);
        }

        if (changed) {
            setControllerBinding(joyIndex, bindingKey, binding);
        }
    }

    private static Iterator<Entry<String, ControllerBinding>> gameBindIterator;
    private static Iterator<Entry<String, ControllerBinding>> menuBindIterator;

    private static ControllerBinding getNextBinding(Iterator<Entry<String, ControllerBinding>> current,
                                                    BindingOptions target) {
        while (current.hasNext()) {
            Map.Entry<String, ControllerBinding> entry = current.next();
            if (entry.getValue().bindingOptions.contains(target) && entry.getValue().inputEvent.isValid()) {
                return entry.getValue();
            }
        }

        return null;
    }

    public static ControllerBinding getNextGameAutoBinding() {
        return getNextBinding(gameBindIterator, BindingOptions.GAME_BINDING);
    }

    public static ControllerBinding getNextMenuAutoBinding() {
        return getNextBinding(menuBindIterator, BindingOptions.MENU_BINDING);
    }

    public static ControllerBinding startGameBindIteration() {
        gameBindIterator = joyBindingsMap.entrySet().iterator();
        return getNextGameAutoBinding();
    }

    public static ControllerBinding startMenuBindIteration() {
        menuBindIterator = joyBindingsMap.entrySet().iterator();
        return getNextMenuAutoBinding();
    }

    public static void unpressAll() {
        for (Map.Entry<String, ControllerBinding> entry : joyBindingsMap.entrySet()) {
            if (entry.getValue().bindingOptions.contains(BindingOptions.IS_TOGGLE))
                entry.getValue().toggleState = false;
        }

        KeyBinding.unPressAllKeys();
        VirtualMouse.unpressAllButtons();
    }

    public static void saveSensitivityValues() {
        JoypadMod.logger.info("Saving game sensitivity value: " + ControllerSettings.inGameSensitivity);
        config.updateConfigFileSetting(ConfigFile.UserJoypadSettings.GameSensitivity,
                "" + ControllerSettings.inGameSensitivity);
        JoypadMod.logger.info("Saving menu sensitivity value: " + ControllerSettings.inMenuSensitivity);
        config.updateConfigFileSetting(ConfigFile.UserJoypadSettings.GuiSensitivity,
                "" + ControllerSettings.inMenuSensitivity);
    }

    public static void saveDeadZones(InputDevice controller) {
        DecimalFormat df = new DecimalFormat("#0.00");

        for (int i = 0; i < controller.getAxisCount(); i++) {
            config.setConfigFileSetting("-Deadzones-." + controller.getName(), controller.getAxisName(i),
                    df.format(controller.getDeadZone(i)));
        }
        config.addComment("-Deadzones-", "Deadzone values here will override values in individual bindings");
        JoypadMod.logger.info("Saved deadzones for " + controller.getName());
    }

    public static void saveSingleDirectionAxis(InputDevice controller) {
        String axisList = intListToString(getSingleDirectionAxis(controller.getIndex()));
        config.setConfigFileSetting("-SingleDirectionAxis-", controller.getName(), axisList);
        config.addComment("-SingleDirectionAxis-", "Set single-direction axis for this controller");
        JoypadMod.logger.info("Saved single-direction axis for " + controller.getName() + " values: '" + axisList + "'");
    }

    private static void saveCurrentJoyBindings() {
        String joyName = JoypadModInputLibrary.getController(currentDisplayedMap).getName();
        for (Map.Entry<String, ControllerBinding> entry : joyBindingsMap.entrySet()) {
            config.saveControllerBinding(joyName, entry.getValue());
        }
    }

    public static void applySavedDeadZones(int joyId) {
        if (joyId < 0)
            return;

        JoypadMod.logger.info("Applying configurated deadzones");

        config.applySavedDeadZones(JoypadModInputLibrary.getController(joyId));

    }

    public static ControllerBinding findControllerBindingWithKey(int keyCode, BindingOptions option) {
        for (Map.Entry<String, ControllerBinding> entry : joyBindingsMap.entrySet()) {
            if (entry.getValue().inputEvent.isValid() && entry.getValue().keyCodes != null
                    && entry.getValue().bindingOptions != null && entry.getValue().bindingOptions.contains(option)) {
                for (int bindKeyCode : entry.getValue().keyCodes) {
                    if (bindKeyCode == keyCode) {
                        return entry.getValue();
                    }
                }
            }
        }

        return null;
    }

    // call this when there is a possibility of a key change
    public static void checkIfBindingsNeedUpdating() {
        if (joyNo < 0)
            return;

        boolean updated = false;

        for (Map.Entry<String, ControllerBinding> entry : joyBindingsMap.entrySet()) {
            if (entry.getValue().inputEvent.isValid() && entry.getValue().keyCodes != null
                    && entry.getValue().keyCodes.length >= 1 && !entry.getKey().contains("user.")) {
                KeyBinding kb = McKeyBindHelper.getMinecraftKeyBind(entry.getKey());
                if (kb == null && entry.getKey().contains("joy."))
                    kb = McKeyBindHelper.getMinecraftKeyBind(entry.getKey().replace("joy.", "key."));
                if (kb != null) {
                    int keyCode = McObfuscationHelper.keyCode(kb);
                    if (entry.getValue().keyCodes[0] != keyCode) {
                        entry.getValue().keyCodes = new int[]{keyCode};
                        setControllerBinding(joyNo, entry.getKey(), entry.getValue());
                    }
                }
                updated = true;
            }
        }
        if (updated)
            ButtonScreenTips.UpdateHintString();
    }

    public static boolean checkIfDuplicateBinding(String bindingKey) {
        ControllerBinding b = get(bindingKey);
        if (b == null || !b.inputEvent.isValid())
            return false;

        for (Map.Entry<String, ControllerBinding> entry : joyBindingsMap.entrySet()) {
            if (entry.getValue().inputEvent.isValid() && !entry.getKey().equals(bindingKey)
                    && entry.getValue().inputEvent.equals(b.inputEvent)) {
                if ((b.bindingOptions.contains(BindingOptions.GAME_BINDING)
                        && entry.getValue().bindingOptions.contains(BindingOptions.GAME_BINDING))
                        || (b.bindingOptions.contains(BindingOptions.MENU_BINDING)
                        && entry.getValue().bindingOptions.contains(BindingOptions.MENU_BINDING)))
                    return true;
            }
        }

        return false;
    }

    public static String getGameOption(String optionKey) {
        return config.getConfigFileSetting(optionKey);
    }

    public static void setGameOption(String optionKey, String value) {
        config.setConfigFileSetting(optionKey, value);
        if (optionKey.contains("SharedProfile") && currentDisplayedMap != -1) {
            saveCurrentJoyBindings();
        } else if (optionKey.contains("GrabMouse")) {
            grabMouse = Boolean.parseBoolean(value);
        } else if (optionKey.contains("DisplayHints")) {
            displayHints = Boolean.parseBoolean(value);
        }
    }

    public static String checkKeyCodeBound(int joyNum, int keyCode, String defaultStr) {
        ControllerBinding b = ControllerSettings.findControllerBindingWithKey(keyCode, BindingOptions.GAME_BINDING);

        if (b != null) {
            return ControllerSettings.controllerUtils
                    .getHumanReadableInputName(JoypadModInputLibrary.getController(joyNum), b.inputEvent);
        }

        return defaultStr;
    }
}
