package com.shiny.joypadmod.minecraftExtensions;

import java.util.EnumSet;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.shiny.joypadmod.ControllerSettings;
import com.shiny.joypadmod.GameRenderHandler;
import com.shiny.joypadmod.devices.InputDevice;
import com.shiny.joypadmod.helpers.LogHelper;
import com.shiny.joypadmod.helpers.McObfuscationHelper;
import com.shiny.joypadmod.inputevent.ButtonInputEvent;
import com.shiny.joypadmod.inputevent.ControllerBinding;
import com.shiny.joypadmod.inputevent.ControllerBinding.BindingOptions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiScreen;

public class JoypadConfigMenu extends GuiScreen
{
	private int currentJoyIndex = 0;

	// control list parameters
	public int controlListYStart;
	public int controlListXStart;
	public int controlListWidth;
	public int controlListHeight;

	// start of text at top
	private int labelYStart = 2;

	// top button parameters
	// Y start of the buttons at the top of the screen
	private int buttonYStart_top = labelYStart + 12;
	// Y end of the buttons at the top of the screen
	private int buttonYEnd_top;
	// X start of the buttons at the top of the screen
	public int buttonXStart_top;

	// private int sensitivityXStart;
	private int sensitivityYStart;
	private int controllerStringY;

	// determined by size of screen
	public int controllerButtonWidth;

	// bottom button parameters
	private int buttonYStart_bottom;
	public int bottomButtonWidth = 70;
	
	public int lastKeyCode = -1;

	private GuiScreen parentScr;

	private long customBindingTickStart = 0;
	private int customBindingKeyIndex = -1;

	private JoypadControlList optionList;
	private List<Integer> controllers;

	private int sensitivity_menuStart;
	private int sensitivity_gameStart;

	private enum ButtonsEnum
	{
		control, prev, next, menuSensitivity, gameSensitivity, addKey, reset, done, advanced, mouseMenu
	}

	public JoypadConfigMenu(GuiScreen parent)
	{
		super();
		parentScr = parent;
		sensitivity_menuStart = ControllerSettings.inMenuSensitivity;
		sensitivity_gameStart = ControllerSettings.inGameSensitivity;
	}

	public void getControllers()
	{
		controllers = ControllerSettings.getJoypadList(ControllerSettings.getGameOption("-Global-.displayAllControls").equals(
				"true"));

		if (!joyConfigMenuEnabled())
		{
			currentJoyIndex = -1;
			return;
		}

		currentJoyIndex = 0;

		if (ControllerSettings.joyNo >= 0)
		{
			for (int i = 0; i < controllers.size(); i++)
			{
				if (controllers.get(i) == ControllerSettings.joyNo)
				{
					currentJoyIndex = i;
					break;
				}
			}
		}
	}

	@Override
	public void initGui()
	{
		getControllers();
		controllerButtonWidth = width - width / 5;
		if (controllerButtonWidth > 310)
			controllerButtonWidth = 310;
		buttonXStart_top = (width - controllerButtonWidth) / 2;
		buttonYStart_bottom = height - 20;

		controllerStringY = buttonYStart_top;

		int buttonYOffset = 10;
		// controller button
		addButton(new GuiButton(100, buttonXStart_top, buttonYStart_top + buttonYOffset, controllerButtonWidth, 20,
				getJoystickInfo(JoyInfoEnum.name)), controllers != null && controllers.size() > 0);

		buttonYOffset += 20;

		// prev controller button
		addButton(new GuiButton(101, buttonXStart_top, buttonYStart_top + buttonYOffset, controllerButtonWidth / 2, 20,
				"<<"));
		// next controller button
		addButton(new GuiButton(102, buttonXStart_top + controllerButtonWidth / 2, buttonYStart_top + buttonYOffset,
				controllerButtonWidth / 2, 20, ">>"));

		// other controllers
		// addButton(new GuiButton(200, buttonXStart_top + (controllerButtonWidth / 3 * 2), buttonYStart_top
		// + buttonYOffset, controllerButtonWidth / 3 + 1, 20, sGet("controlMenu.otherControls")));

		buttonYOffset += 22;

		// sensitivityXStart = buttonXStart_top + invertButtonWidth + 1;
		sensitivityYStart = buttonYStart_top + buttonYOffset;
		// int topRowButtonXOffset = sensitivityXStart;

		// int sensitivitySliderWidth = (controllerButtonWidth + buttonXStart_top - sensitivityXStart) / 2;
		GuiSlider menuSensitivity = new GuiSlider(310, buttonXStart_top, sensitivityYStart, controllerButtonWidth / 2,
				20, "controlMenu.sensitivity.menu", (float) ControllerSettings.inMenuSensitivity / 100f);
		menuSensitivity.updateText();
		addButton(menuSensitivity);
		GuiSlider guiSensitivity = new GuiSlider(320, buttonXStart_top + controllerButtonWidth / 2, sensitivityYStart,
				controllerButtonWidth / 2, 20, "controlMenu.sensitivity.game",
				(float) ControllerSettings.inGameSensitivity / 100f);
		guiSensitivity.updateText();
		addButton(guiSensitivity);

		buttonYOffset += 20;
		// the middle section will be populated with the controller settings so
		// record where we left off with the top
		buttonYEnd_top = buttonYStart_top + buttonYOffset;

		controlListYStart = buttonYEnd_top + 2;
		controlListXStart = 0;// buttonXStart_top;
		controlListWidth = buttonXStart_top + controllerButtonWidth;
		controlListHeight = buttonYStart_bottom - buttonYEnd_top - 2;

		int rightButtonsXStart = controlListXStart + controlListWidth + 2;

		// add buttons to right of control list box
		int buttonNum = 0;
		int rightButtonWidth = Math.max(getFontRenderer().getStringWidth(sGet("controlMenu.addKey")),
				getFontRenderer().getStringWidth(sGet("controlMenu.pressKey"))) + 10;
		int buttonYSpacing = 20;

		addButton(new GuiButton(350, rightButtonsXStart, controlListYStart + (buttonYSpacing * buttonNum++),
				rightButtonWidth, 20, sGet("controlMenu.addKey")));

		// add bottom buttons
		buttonNum = 0;
		int numBottomButtons = 4;
		int bottomButtonStart = buttonXStart_top + controllerButtonWidth / 2 - (bottomButtonWidth / 2)
				* numBottomButtons;

		addButton(new GuiButton(400, bottomButtonStart + bottomButtonWidth * buttonNum++, buttonYStart_bottom,
				bottomButtonWidth, 20, sGet("controls.reset")));

		addButton(new GuiButton(500, bottomButtonStart + bottomButtonWidth * buttonNum++, buttonYStart_bottom,
				bottomButtonWidth, 20, sGet("gui.done")));

		addButton(new GuiButton(420, bottomButtonStart + bottomButtonWidth * buttonNum++, buttonYStart_bottom,
				bottomButtonWidth, 20, sGet("controlMenu.advanced")));

		addButton(new GuiButton(520, bottomButtonStart + bottomButtonWidth * buttonNum++, buttonYStart_bottom,
				bottomButtonWidth, 20, sGet("controlMenu.mouse") + " " + sGet("joy.menu")));

		this.optionList = new JoypadControlList(this, getFontRenderer());
	}

	@Override
	public void onGuiClosed()
	{
		LogHelper.Info("JoypadConfigMenu OnGuiClosed");
		if (ControllerSettings.isInputEnabled())
			ControllerSettings.controllerUtils.saveCurrentJoypadMap();
		ControllerSettings.suspendControllerInput(false, 0);
		if (sensitivity_menuStart != ControllerSettings.inMenuSensitivity
				|| sensitivity_gameStart != ControllerSettings.inGameSensitivity)
		{
			ControllerSettings.saveSensitivityValues();
		}
		ControllerSettings.checkIfBindingsNeedUpdating();
	}

	@Override
	protected void actionPerformed(GuiButton guiButton)
	{
		int id = getButtonId(guiButton);
		LogHelper.Info("Action performed on buttonID " + id);

		if (!joyConfigMenuEnabled() && id != 420 && id < 500)
			return;

		switch (id)
		{
		case 100: // Controller button
			toggleController();
			break;
		case 101: // PREV
			// disable for safety
			ControllerSettings.setInputEnabled(-1, false);
			currentJoyIndex = getJoypadIndex(-1);
			updateControllerButton();
			optionList.updatejoyBindKeys();
			break;
		case 102: // NEXT
			// disable for safety
			ControllerSettings.setInputEnabled(-1, false);
			currentJoyIndex = getJoypadIndex(1);
			updateControllerButton();
			optionList.updatejoyBindKeys();
			break;
			/*
			 * case 200: // unhide controllers ControllerSettings.setInputEnabled(-1, false); if (guiButton.displayString.equals(sGet("controlMenu.otherControls"))) { getControllers(false);
			 * guiButton.displayString = sGet("controlMenu.validControls"); } else { getControllers(true); guiButton.displayString = sGet("controlMenu.otherControls"); }
			 * enableDisableButton(ButtonsEnum.control.ordinal(), controllers.size() > 0); if (controllers.size() > 0) { updateControllerButton(); } break;
			 */
		case 310: // menu/game sensitivity
		case 320:
			break;
		case 350: // custom binding
			customBindingKeyIndex = ButtonsEnum.addKey.ordinal();
			customBindingTickStart = Minecraft.getSystemTime();
			break;
		case 400: // Reset
			if (currentJoyIndex != -1)
			{
				ControllerSettings.resetBindings(getCurrentControllerId());
			}
			break;
		case 420: // advanced
			mc.displayGuiScreen(new JoypadAdvancedMenu(this, getCurrentControllerId()));
			break;
		case 500: // Done
			mc.displayGuiScreen(this.parentScr);
			break;
		case 520: // Mouse menu
			GameRenderHandler.allowOrigControlsMenu = true;
			mc.displayGuiScreen(new GuiControls(this, mc.gameSettings));
			break;
		}
	}

	enum JoyInfoEnum
	{
		name, buttonAxisInfo
	};

	private String getJoystickInfo(JoyInfoEnum joyInfo)
	{
		String ret = "";

		if (!joyConfigMenuEnabled() || this.getCurrentControllerId() == -1)
			return sGet("controlMenu.noControllers");

		try
		{
			InputDevice control = ControllerSettings.JoypadModInputLibrary.getController(this.getCurrentControllerId());
			if (joyInfo == JoyInfoEnum.buttonAxisInfo)
			{
				ret += String.format("%s %d/%d - ", sGet("controlMenu.controller"), currentJoyIndex + 1,
						controllers.size());
				ret += String.format("%s: %d ", sGet("controlMenu.buttons"), control.getButtonCount());
				ret += String.format("%s: %d", sGet("controlMenu.axis"), control.getAxisCount());
			}
			else if (joyInfo == JoyInfoEnum.name)
			{
				ret += control.getName();
				if (!control.isConnected())
					ret += " [Disconnected]";
				ret += ": " + (ControllerSettings.isInputEnabled() ? sGet("options.on") : sGet("options.off"));
			}

		}
		catch (Exception ex)
		{
			ret += " Exception caught getting controller info! " + ex.toString();
		}
		return ret;
	}

	@Override
	public void drawScreen(int par1, int par2, float par3)
	{
		drawDefaultBackground();

		if (this.optionList != null)
		{
			this.optionList.drawScreen(par1, par2, par3);
		}

		checkCustomBindTrigger();
		checkSensitivitySliders();

		String titleText = String.format("Joypad Mod - %s - %s", sGet("controls.title"),
				sGet("controlMenu.toggleInstructions"));
		this.drawCenteredString(getFontRenderer(), titleText, width / 2, labelYStart, -1);

		// output TEXT buttons Axis, POV count here
		String joyStickInfoText = getJoystickInfo(JoyInfoEnum.buttonAxisInfo);
		this.drawCenteredString(getFontRenderer(), joyStickInfoText, width / 2, controllerStringY, 0xAAAAAA);

		// CONTROLLER NAME BUTTON
		// PREV NEXT OTHER

		super.drawScreen(par1, par2, par3);
	}

	private void checkSensitivitySliders()
	{
		float f = ((GuiSlider) buttonList.get(ButtonsEnum.gameSensitivity.ordinal())).getValue();
		ControllerSettings.inGameSensitivity = (int) (f * 100);
		f = ((GuiSlider) buttonList.get(ButtonsEnum.menuSensitivity.ordinal())).getValue();
		ControllerSettings.inMenuSensitivity = (int) (f * 100);
	}

	private void checkCustomBindTrigger()
	{
		if (customBindingTickStart > 0)
		{
			if (Minecraft.getSystemTime() - customBindingTickStart > 5000)
				customBindingTickStart = 0;
			changeButtonText(customBindingKeyIndex, sGet("controlMenu.pressKey"));
			if (this.lastKeyCode != -1)
			{
				String key = Keyboard.getKeyName(this.lastKeyCode);
				this.lastKeyCode = -1;
				LogHelper.Info("Received " + key);
				customBindingTickStart = 0;
				ControllerBinding binding;
				binding = new ControllerBinding("user." + key, key, new ButtonInputEvent(
						this.getCurrentControllerId(), -1, 1), new int[] { Keyboard.getKeyIndex(key) }, 0,
						EnumSet.of(BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD,
								BindingOptions.RENDER_TICK, BindingOptions.CATEGORY_MISC));
				if (this.optionList != null && !this.optionList.joyBindKeys.contains(binding.inputString))
				{
					ControllerSettings.addUserBinding(binding);
					this.optionList.joyBindKeys.add(binding.inputString);
				}
			}
		}
		else
		{
			if (customBindingKeyIndex != -1)
			{
				changeButtonText(customBindingKeyIndex, sGet("controlMenu.addKey"));
				customBindingKeyIndex = -1;
			}
		}
	}

	/**
	 * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
	 */
	protected void keyTyped(char c, int code)
	{
		if (customBindingTickStart > 0)
		{
			this.lastKeyCode = code;
			return;
		}
		
		if (JoypadControlList.textInputName != null 
				&& JoypadControlList.textInputName.getVisible())
		{
			this.lastKeyCode = code;
			JoypadControlList.textInputName.textboxKeyTyped(c, code);
			return;
		}
		
		if (c == ' ' && controllers.size() > 0)
		{
			toggleController();
		}
		else
		{
			try{
				super.keyTyped(c, code);}
			catch(java.io.IOException e){}
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
	{
		if (this.optionList != null)
		{
			JoypadControlList.lastXClick = mouseX;
			JoypadControlList.lastYClick = mouseY;
			JoypadControlList.lastMouseButton = mouseButton;
		}
		try{
			super.mouseClicked(mouseX, mouseY, mouseButton);
		}catch(java.io.IOException e){}
	}

	private int getJoypadIndex(int offset)
	{
		if (offset == 0 || controllers.size() == 1)
			return currentJoyIndex;

		int i = offset > 0 ? currentJoyIndex + 1 : currentJoyIndex - 1;

		if (i >= controllers.size())
			i = 0;
		else if (i < 0)
			i = controllers.size() - 1;

		return i;
	}

	private void changeButtonText(int buttonIndex, String text)
	{
		((GuiButton) buttonList.get(buttonIndex)).displayString = text;
	}

	/*
	 * private void enableDisableButton(int buttonIndex, boolean enable) { ((GuiButton) buttonList.get(buttonIndex)).enabled = enable; }
	 */

	private void toggleController()
	{
		LogHelper.Info("Enable/disable input");
		ControllerSettings.setInputEnabled(getCurrentControllerId(), !ControllerSettings.isInputEnabled());
		updateControllerButton();
	}

	private void updateControllerButton()
	{
		if (ControllerSettings.isInputEnabled() && ControllerSettings.joyNo != getCurrentControllerId())
		{
			ControllerSettings.setController(getCurrentControllerId());
		}

		changeButtonText(ButtonsEnum.control.ordinal(), getJoystickInfo(JoyInfoEnum.name));
	}

	// Obfuscation & back porting helpers -- here and not in ObfuscationHelper
	// because accessing protected methods

	@SuppressWarnings("unchecked")
	private void addButton(GuiButton guiButton, boolean enabled)
	{
		if (!enabled)
			guiButton.enabled = false;
		// field_146292_n.add(guiButton);
		buttonList.add(guiButton);
	}

	private int getButtonId(GuiButton guiButton)
	{
		// return guiButton.field_146127_k;
		return guiButton.id;
	}

	public int getCurrentControllerId()
	{
		if (currentJoyIndex == -1)
			return -1;
		return controllers.get(currentJoyIndex);
	}

	public FontRenderer getFontRenderer()
	{
		// return this.field_146289_q;
		// return this.fontRenderer;
		return this.fontRenderer;
	}

	public String sGet(String inputCode)
	{
		return McObfuscationHelper.lookupString(inputCode);
	}

	private boolean joyConfigMenuEnabled()
	{
		if (controllers == null || controllers.size() <= 0)
			return false;

		return true;
	}
}
