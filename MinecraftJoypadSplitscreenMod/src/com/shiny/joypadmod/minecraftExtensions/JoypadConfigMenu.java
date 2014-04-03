package com.shiny.joypadmod.minecraftExtensions;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Keyboard;

import com.shiny.joypadmod.ControllerSettings;
import com.shiny.joypadmod.GameRenderHandler;
import com.shiny.joypadmod.JoypadMod;
import com.shiny.joypadmod.helpers.LogHelper;
import com.shiny.joypadmod.inputevent.ButtonInputEvent;
import com.shiny.joypadmod.inputevent.ControllerBinding;
import com.shiny.joypadmod.inputevent.ControllerBinding.BindingOptions;

public class JoypadConfigMenu extends GuiScreen
{

	public boolean nextClicked = false;
	public int currentJoyIndex = 0;
	public int lastJoyIndex = 0;
	// start of text at top
	private int labelYStart = 2;

	// top button parameters
	// Y start of the buttons at the top of the screen
	private int buttonYStart_top = labelYStart + 12;
	// Y end of the buttons at the top of the screen
	public int buttonYEnd_top;
	// X start of the buttons at the top of the screen
	public int buttonXStart_top;

	public int sensitivityXStart;
	public int sensitivityYStart;
	public int menuSensitivityStringXStart;
	public int gameSensitivityStringXStart;

	public int controllerStringY;

	// determined by size of screen
	public int controllerButtonWidth;

	// spacing between the buttons
	private int buttonYSpacing = 21;

	// control list parameters
	public int controlListYStart;
	public int controlListXStart;
	public int controlListWidth;
	public int controlListHeight;

	// bottom button parameters
	private int buttonYStart_bottom;
	private int bottomButtonWidth = 70;

	private GuiScreen parentScr;
	public Minecraft mc = Minecraft.getMinecraft();

	private long customBindingTickStart = 0;
	private int customBindingKeyIndex = -1;

	private int sensitivity_menuStart;
	private int sensitivity_gameStart;

	private JoypadControlList optionList;
	private List<Integer> controllers;

	private enum ButtonsEnum
	{
		control,
		prev,
		next,
		showInvalid,
		invert,
		menuSensitivityDown,
		menuSensitivityDownSmall,
		menuSensitivityupSmall,
		menuSensitivityup,
		gameSensitivityDown,
		gameSensitivityDownSmall,
		gameSensitivityupSmall,
		gameSensitivityup,
		calibrate,
		addCustom,
		reset,
		done,
		mouseMenu
	}

	public JoypadConfigMenu(GuiScreen parent)
	{
		super();
		parentScr = parent;
		getControllers(true);
		sensitivity_menuStart = ControllerSettings.inMenuSensitivity;
		sensitivity_gameStart = ControllerSettings.inGameSensitivity;
	}

	public void getControllers(boolean valid)
	{
		controllers = JoypadMod.controllerSettings.flattenMap(valid ? ControllerSettings.validControllers
				: ControllerSettings.inValidControllers);

		if (controllers.size() <= 0)
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
		controllerButtonWidth = width - width / 5;
		if (controllerButtonWidth > 395)
			controllerButtonWidth = 395;
		buttonXStart_top = (width - controllerButtonWidth) / 2;
		buttonYStart_bottom = height - 20;

		controllerStringY = buttonYStart_top;

		int buttonYOffset = 10;
		// controller button
		addButton(new GuiButton(100, buttonXStart_top, buttonYStart_top + buttonYOffset, controllerButtonWidth, 20,
				getJoystickInfo(currentJoyIndex, JoyInfoEnum.name)), controllers.size() > 0);

		buttonYOffset += 20;

		// prev controller button
		addButton(new GuiButton(101, buttonXStart_top, buttonYStart_top + buttonYOffset, controllerButtonWidth / 3, 20,
				"PREV"));
		// next controller button
		addButton(new GuiButton(102, buttonXStart_top + controllerButtonWidth / 3, buttonYStart_top + buttonYOffset,
				controllerButtonWidth / 3, 20, "NEXT"));

		// other controllers
		addButton(new GuiButton(503, buttonXStart_top + (controllerButtonWidth / 3 * 2), buttonYStart_top
				+ buttonYOffset, controllerButtonWidth / 3 + 1, 20, "Other controls"));

		buttonYOffset += 22;

		int invertButtonWidth = 66;

		// invert axis toggle
		addButton(new GuiButton(401, buttonXStart_top, buttonYStart_top + buttonYOffset, invertButtonWidth, 20,
				"Invert : " + (ControllerSettings.getInvertYAxis() ? "on" : "off")));

		sensitivityXStart = buttonXStart_top + invertButtonWidth + 1;
		sensitivityYStart = buttonYStart_top + buttonYOffset;
		int topRowButtonXOffset = sensitivityXStart;

		int sensitivityButtonWidth = 15;

		// menu sensitivity down
		addButton(new GuiButton(420, topRowButtonXOffset, buttonYStart_top + buttonYOffset, sensitivityButtonWidth, 20,
				"<<"));
		topRowButtonXOffset += sensitivityButtonWidth - 2;
		addButton(new GuiButton(421, topRowButtonXOffset, buttonYStart_top + buttonYOffset, sensitivityButtonWidth, 20,
				"<"));

		menuSensitivityStringXStart = topRowButtonXOffset + sensitivityButtonWidth + 1;
		topRowButtonXOffset += 27 + sensitivityButtonWidth;

		// menu sensitivity up
		addButton(new GuiButton(422, topRowButtonXOffset, buttonYStart_top + buttonYOffset, sensitivityButtonWidth, 20,
				">"));
		topRowButtonXOffset += sensitivityButtonWidth - 2;
		addButton(new GuiButton(423, topRowButtonXOffset, buttonYStart_top + buttonYOffset, sensitivityButtonWidth, 20,
				">>"));

		topRowButtonXOffset += sensitivityButtonWidth + 2;

		// gui sensitivity down
		addButton(new GuiButton(425, topRowButtonXOffset, buttonYStart_top + buttonYOffset, sensitivityButtonWidth, 20,
				"<<"));
		topRowButtonXOffset += sensitivityButtonWidth - 2;
		addButton(new GuiButton(426, topRowButtonXOffset, buttonYStart_top + buttonYOffset, sensitivityButtonWidth, 20,
				"<"));

		gameSensitivityStringXStart = topRowButtonXOffset + sensitivityButtonWidth + 1;
		topRowButtonXOffset += 27 + sensitivityButtonWidth;

		// game sensitivity up
		addButton(new GuiButton(427, topRowButtonXOffset, buttonYStart_top + buttonYOffset, sensitivityButtonWidth, 20,
				">"));
		topRowButtonXOffset += sensitivityButtonWidth - 2;
		addButton(new GuiButton(428, topRowButtonXOffset, buttonYStart_top + buttonYOffset, sensitivityButtonWidth, 20,
				">>"));
		topRowButtonXOffset += sensitivityButtonWidth - 1;

		int calibrateButtonWidth = controllerButtonWidth + buttonXStart_top - topRowButtonXOffset;

		addButton(new GuiButton(500, topRowButtonXOffset, buttonYStart_top + buttonYOffset, controllerButtonWidth
				+ buttonXStart_top - topRowButtonXOffset, 20, this.getFontRenderer().trimStringToWidth("Calibrate",
				calibrateButtonWidth)));

		buttonYOffset += 20;
		// the middle section will be populated with the controller settings so
		// record where we left off with the top
		buttonYEnd_top = buttonYStart_top + buttonYOffset;

		controlListYStart = buttonYEnd_top + 2;
		controlListXStart = buttonXStart_top;
		controlListWidth = (int) (controllerButtonWidth / 1.3);
		controlListHeight = buttonYStart_bottom - buttonYEnd_top - 2;

		int rightButtonsXStart = controlListXStart + controlListWidth + 5;

		// add buttons to right of control list box
		int buttonNum = 0;
		int rightButtonWidth = controllerButtonWidth + buttonXStart_top - rightButtonsXStart;

		addButton(new GuiButton(610, rightButtonsXStart, controlListYStart + (buttonYSpacing * buttonNum++),
				rightButtonWidth, 20, "Add Key"));

		// add bottom buttons
		buttonNum = 0;

		addButton(new GuiButton(400, controlListXStart + bottomButtonWidth * buttonNum++, buttonYStart_bottom,
				bottomButtonWidth, 20, "Reset"));

		addButton(new GuiButton(501, controlListXStart + bottomButtonWidth * buttonNum++, buttonYStart_bottom,
				bottomButtonWidth, 20, "Done"));
		addButton(new GuiButton(502, controlListXStart + bottomButtonWidth * buttonNum++, buttonYStart_bottom,
				bottomButtonWidth, 20, "Mouse menu"));

		this.optionList = new JoypadControlList(this, getFontRenderer());
	}

	@Override
	public void onGuiClosed()
	{
		LogHelper.Info("JoypadConfigMenu OnGuiClosed");
		ControllerSettings.suspendControllerInput(false, 0);
		if (sensitivity_menuStart != ControllerSettings.inMenuSensitivity
				|| sensitivity_gameStart != ControllerSettings.inGameSensitivity)
		{
			ControllerSettings.saveSensitivityValues();
		}
	}

	@Override
	protected void actionPerformed(GuiButton guiButton)
	{
		LogHelper.Info("Action performed on buttonID " + getButtonId(guiButton));

		if (controllers.size() <= 0 && getButtonId(guiButton) <= 500)
			return;

		switch (getButtonId(guiButton))
		{
		case 100: // Controller button
			toggleController();
			break;
		case 101: // PREV
			// disable for safety
			JoypadMod.controllerSettings.setInputEnabled(false);
			currentJoyIndex = getJoypadIndex(-1);
			updateControllerButton();
			break;
		case 102: // NEXT
			// disable for safety
			JoypadMod.controllerSettings.setInputEnabled(false);
			currentJoyIndex = getJoypadIndex(1);
			updateControllerButton();
			break;
		case 400: // Reset
			if (currentJoyIndex != -1)
				JoypadMod.controllerSettings.setDefaultBindings(currentJoyIndex);
			break;
		case 401: // invert
			ControllerSettings.setInvertYAxis(!ControllerSettings.getInvertYAxis());
			toggleOnOffButton(ControllerSettings.getInvertYAxis(), ButtonsEnum.invert.ordinal());
			break;
		case 420:
		case 421:
		case 422:
		case 423:
		case 425:
		case 426:
		case 427:
		case 428:
			int bigChange = 5;
			int smallChange = 1;
			switch (getButtonId(guiButton))
			{
			case 420: // menu sensitivity down big
				ControllerSettings.inMenuSensitivity -= bigChange;
				break;
			case 421: // menu sensitivity down small
				ControllerSettings.inMenuSensitivity -= smallChange;
				break;
			case 422: // menu sensitivity up small
				ControllerSettings.inMenuSensitivity += smallChange;
				break;
			case 423: // menu sensitivity up big
				ControllerSettings.inMenuSensitivity += bigChange;
				break;
			case 425: // gui sensitivity down big
				ControllerSettings.inGameSensitivity -= bigChange;
				break;
			case 426: // gui sensitivity down small
				ControllerSettings.inGameSensitivity -= smallChange;
				break;
			case 427: // gui sensitivity up small
				ControllerSettings.inGameSensitivity += smallChange;
				break;
			case 428: // gui sensitivity up big
				ControllerSettings.inGameSensitivity += bigChange;
				break;
			}
			break;
		case 500: // Calibrate
			int realJoyIndex = currentJoyIndex != -1 ? this.controllers.get(currentJoyIndex) : -1;
			mc.displayGuiScreen(new JoypadCalibrationMenu(this, realJoyIndex));
			break;
		case 501: // Done
			mc.displayGuiScreen(this.parentScr);
			break;
		case 502: // Mouse menu
			GameRenderHandler.allowOrigControlsMenu = true;
			mc.displayGuiScreen(new GuiControls(this, mc.gameSettings));
			break;
		case 503: // unhide controllers
			JoypadMod.controllerSettings.setInputEnabled(false);
			if (guiButton.displayString.contains("Other"))
			{
				getControllers(false);
				guiButton.displayString = guiButton.displayString.replace("Other", "Valid");
			}
			else
			{
				getControllers(true);
				guiButton.displayString = guiButton.displayString.replace("Valid", "Other");
			}
			enableDisableButton(ButtonsEnum.control.ordinal(), controllers.size() > 0);
			if (controllers.size() > 0)
			{
				updateControllerButton();
			}
			break;
		case 610: // custom binding
			customBindingKeyIndex = ButtonsEnum.addCustom.ordinal();
			customBindingTickStart = Minecraft.getSystemTime();
			break;
		}
	}

	enum JoyInfoEnum
	{
		name, buttonAxisInfo
	};

	private String getJoystickInfo(int joyIndex, JoyInfoEnum joyInfo)
	{
		String ret = "";

		if (controllers.size() == 0)
			return "No controllers found!";

		try
		{
			if (joyIndex >= controllers.size())
				ret = "Code Error: Invalid controller # selected";
			else
			{
				int joyNo = controllers.get(joyIndex);
				Controller control = Controllers.getController(joyNo);
				if (joyInfo == JoyInfoEnum.buttonAxisInfo)
				{
					ret += "Controller " + (joyIndex + 1) + " of " + controllers.size();
					ret += "-Buttons: " + control.getButtonCount();
					ret += " Axis: " + control.getAxisCount();
				}
				else if (joyInfo == JoyInfoEnum.name)
				{
					ret += control.getName() + ": ";

					ret += JoypadMod.controllerSettings.isInputEnabled() ? "on" : "off";
				}
			}
		}
		catch (Exception ex)
		{
			ret += " Exception caught getting controller info! " + ex.getClass();
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

		if (customBindingTickStart > 0)
		{
			if (Minecraft.getSystemTime() - customBindingTickStart > 5000)
				customBindingTickStart = 0;
			changeButtonText(customBindingKeyIndex, "Press Key");
			for (int i = 0; i < Keyboard.KEYBOARD_SIZE; i++)
			{
				if (Keyboard.isKeyDown(i))
				{
					String key = Keyboard.getKeyName(i);
					LogHelper.Info("Received " + key);
					customBindingTickStart = 0;
					ControllerBinding binding;
					binding = new ControllerBinding("user." + key, key, new ButtonInputEvent(
							this.getCurrentControllerId(), -1, 1), new int[] { Keyboard.getKeyIndex(key) }, 0,
							EnumSet.of(BindingOptions.GAME_BINDING, BindingOptions.REPEAT_IF_HELD,
									BindingOptions.RENDER_TICK, BindingOptions.CATEGORY_MISC));
					if (this.optionList != null && !this.optionList.joyBindings.contains(binding))
					{
						ControllerSettings.addUserBinding(binding);
						this.optionList.joyBindings.add(binding);
					}

					break;
				}
			}
		}
		else
		{
			if (customBindingKeyIndex != -1)
			{
				changeButtonText(customBindingKeyIndex,
						customBindingKeyIndex == ButtonsEnum.addCustom.ordinal() ? "Add key" : "Change key");
			}
		}

		if (this.currentJoyIndex != lastJoyIndex)
		{
			lastJoyIndex = currentJoyIndex;
			if (this.optionList != null)
				this.optionList.updateJoyBindings();
		}

		this.drawCenteredString(getFontRenderer(), "Joypad Mod Controls - Press space to toggle controller", width / 2,
				labelYStart, -1);

		// output TEXT buttons Axis, POV count here
		String joyStickInfoText = getJoystickInfo(currentJoyIndex, JoyInfoEnum.buttonAxisInfo);
		this.drawCenteredString(getFontRenderer(), joyStickInfoText, width / 2, controllerStringY, 0xAAAAAA);

		int sensitivityColor = 0xFFAA00;

		this.drawString(getFontRenderer(), "Menu", menuSensitivityStringXStart, sensitivityYStart + 1, sensitivityColor);

		this.drawString(getFontRenderer(), "Game", gameSensitivityStringXStart, sensitivityYStart + 1, sensitivityColor);

		int heightOffset = getFontRenderer().FONT_HEIGHT + 1;

		int menuSens = ControllerSettings.inMenuSensitivity;
		int gameSens = ControllerSettings.inGameSensitivity;
		int menuValueOffset = 5;
		if (menuSens < -99)
			menuValueOffset = 1;
		else if (menuSens < -9 || menuSens > 99)
			menuValueOffset = 3;
		else if (menuSens >= 0 && menuSens <= 9)
			menuValueOffset = 8;

		int gameValueOffset = 5;
		if (gameSens < -99)
			gameValueOffset = 1;
		else if (gameSens < -9 || gameSens > 99)
			gameValueOffset = 3;
		else if (gameSens >= 0 && gameSens <= 9)
			gameValueOffset = 8;

		this.drawString(getFontRenderer(), "" + menuSens, menuSensitivityStringXStart + menuValueOffset,
				sensitivityYStart + heightOffset, sensitivityColor);
		this.drawString(getFontRenderer(), "" + gameSens, gameSensitivityStringXStart + gameValueOffset,
				sensitivityYStart + heightOffset, sensitivityColor);

		// CONTROLLER NAME BUTTON
		// PREV NEXT
		// CALIBRATE

		super.drawScreen(par1, par2, par3);
	}

	/**
	 * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
	 */
	protected void keyTyped(char c, int code)
	{
		if (c == ' ' && controllers.size() > 0)
		{
			toggleController();
		}
		else
		{
			super.keyTyped(c, code);
		}
	}

	@Override
	protected void mouseClicked(int par1, int par2, int par3)
	{
		if (this.optionList != null)
		{
			JoypadControlList.lastXClick = par1;
			JoypadControlList.lastYClick = par2;
		}
		super.mouseClicked(par1, par2, par3);
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

	private void enableDisableButton(int buttonIndex, boolean enable)
	{
		((GuiButton) buttonList.get(buttonIndex)).enabled = enable;
	}

	private void toggleOnOffButton(boolean b, int index)
	{
		String s1 = b ? "off" : "on";
		String s2 = b ? "on" : "off";
		String newString = ((GuiButton) buttonList.get(index)).displayString.replace(s1, s2);

		changeButtonText(index, newString);
	}

	private void toggleController()
	{
		LogHelper.Info("Enable/disable input");
		JoypadMod.controllerSettings.setInputEnabled(!JoypadMod.controllerSettings.isInputEnabled());
		updateControllerButton();
	}

	private void updateControllerButton()
	{
		if (JoypadMod.controllerSettings.isInputEnabled()
				&& ControllerSettings.joyNo != controllers.get(currentJoyIndex))
		{
			JoypadMod.controllerSettings.setController(controllers.get(currentJoyIndex));
		}

		changeButtonText(ButtonsEnum.control.ordinal(), getJoystickInfo(currentJoyIndex, JoyInfoEnum.name));
	}

	// Obfuscation & back porting helpers -- here and not in ObfuscationHelper
	// because accessing protected methods
	// TODO think about extending the GuiButton class for this functionality

	@SuppressWarnings("unchecked")
	private void addButton(GuiButton guiButton, boolean enabled)
	{
		if (!enabled)
			guiButton.enabled = false;
		// field_146292_n.add(guiButton);
		buttonList.add(guiButton);
	}

	@SuppressWarnings("unchecked")
	private void addButton(GuiButton guiButton)
	{
		buttonList.add(guiButton);
	}

	public int getButtonId(GuiButton guiButton)
	{
		// return guiButton.field_146127_k;
		return guiButton.id;
	}

	public int getCurrentControllerId()
	{
		return controllers.get(currentJoyIndex);
	}

	public FontRenderer getFontRenderer()
	{
		// return this.field_146289_q;
		return this.fontRenderer;
		// return this.fontRendererObj;
	}
}
