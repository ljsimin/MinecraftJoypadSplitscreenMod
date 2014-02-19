package com.shiny.joypadmod.minecraftExtensions;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;

import com.shiny.joypadmod.ControllerSettings;
import com.shiny.joypadmod.GameRenderHandler;
import com.shiny.joypadmod.JoypadMod;

public class JoypadConfigMenu extends GuiScreen
{

	public boolean nextClicked = false;
	private int currentJoyIndex = 0;

	// start of text at top
	private int labelYStart = 3;

	// top button parameters
	// Y start of the buttons at the top of the screen
	private int buttonYStart_top = labelYStart + 40;
	// Y end of the buttons at the top of the screen
	public int buttonYEnd_top;
	// X start of the buttons at the top of the screen
	public int buttonXStart_top;

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
	public int buttonYStart_bottom;
	private int bottomButtonWidth = 70;

	private GuiScreen parentScr;
	private GuiControls mouseGui;
	public Minecraft mc = Minecraft.getMinecraft();

	private JoypadControlList optionList;
	private List<Integer> controllers;

	private enum ButtonsEnum
	{
		control, prev, next, reset, calibrate, done, mouseMenu
	}

	public JoypadConfigMenu(GuiScreen parent, GuiControls originalControlScreen)
	{
		super();
		parentScr = parent;
		mouseGui = originalControlScreen;
		controllers = JoypadMod.controllerSettings.flattenMap(ControllerSettings.validControllers);

		if (ControllerSettings.joyNo >= 0)
		{
			for (int i = 0; i < controllers.size(); i++)
				if (controllers.get(i) == ControllerSettings.joyNo)
				{
					currentJoyIndex = i;
					break;
				}
		}
	}

	@Override
	public void initGui()
	{
		controllerButtonWidth = width - width / 5;
		buttonXStart_top = width / 10;
		buttonYStart_bottom = height - 20;

		// add top buttons
		addButton(new GuiButton(100, buttonXStart_top, buttonYStart_top, controllerButtonWidth, 20, getJoystickInfo(
				currentJoyIndex, JoyInfoEnum.name)));
		addButton(new GuiButton(101, buttonXStart_top, buttonYStart_top + buttonYSpacing, controllerButtonWidth / 2,
				20, "PREV"));
		addButton(new GuiButton(102, buttonXStart_top + controllerButtonWidth / 2, buttonYStart_top + buttonYSpacing,
				controllerButtonWidth / 2, 20, "NEXT"));

		// the middle section will be populated with the controller settings so
		// record where we left off with the top
		buttonYEnd_top = buttonYStart_top + (buttonYSpacing * 2);

		controlListYStart = buttonYEnd_top + 2;
		controlListXStart = buttonXStart_top + controllerButtonWidth / 5;
		controlListWidth = (int) (controllerButtonWidth / 1.5);
		controlListHeight = buttonYStart_bottom - buttonYEnd_top - 2;

		// GameSettings.Options options = GameSettings.Options.SENSITIVITY;
		// addButton(new GuiOptionSlider(options.returnEnumOrdinal(), width / 2 + 23, labelYStart
		// + getFontRenderer().FONT_HEIGHT * 2 + 3, options));

		int resetXStart = controlListXStart + controlListWidth + 5;
		addButton(new GuiButton(400, resetXStart, controlListYStart, controllerButtonWidth + buttonXStart_top
				- resetXStart, 20, "Reset"));

		// add bottom buttons
		addButton(new GuiButton(500, width / 2 - (int) (bottomButtonWidth * 1.5), buttonYStart_bottom,
				bottomButtonWidth, 20, "Calibrate"));
		// TODO calibration
		((GuiButton) buttonList.get(ButtonsEnum.calibrate.ordinal())).enabled = false;
				
		addButton(new GuiButton(501, width / 2 - (bottomButtonWidth / 2), buttonYStart_bottom, bottomButtonWidth, 20,
				"Done"));		
		GuiButton mouseKeyboardMenuButton = new GuiButton(502, width / 2 + (bottomButtonWidth / 2),
				buttonYStart_bottom, bottomButtonWidth, 20, "Mouse menu");
		mouseKeyboardMenuButton.enabled = !JoypadMod.controllerSettings.isInputEnabled();
		addButton(mouseKeyboardMenuButton);

		this.optionList = new JoypadControlList(this, getFontRenderer());
	}

	@Override
	public void onGuiClosed()
	{
		System.out.println("JoypadConfigMenu OnGuiClosed");
		ControllerSettings.suspendControllerInput(false, 0);
	}

	@Override
	protected void actionPerformed(GuiButton guiButton)
	{
		System.out.println("Action performed on buttonID " + getButtonId(guiButton));

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
			JoypadMod.controllerSettings.setDefaultBindings();
			break;
		case 500: // Calibrate
			// TODO implement
			break;
		case 501: // Done
			JoypadMod.obfuscationHelper.DisplayGuiScreen(this.parentScr);
			break;
		case 502: // Mouse menu
			GameRenderHandler.allowMouseMenu = true;
			JoypadMod.obfuscationHelper.DisplayGuiScreen(mouseGui);
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

		try
		{
			if (joyIndex > controllers.size())
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
		this.optionList.drawScreen(par1, par2, par3);
		int heightOffset = labelYStart;
		this.drawCenteredString(getFontRenderer(), "Joypad Mod Controls", width / 2, heightOffset, -1);
		this.drawCenteredString(getFontRenderer(), "Press SPACE at any time to toggle controller on/off", width / 2,
				heightOffset + getFontRenderer().FONT_HEIGHT + 2, 0xAAAAAA);
		heightOffset += 29;

		// output TEXT buttons Axis, POV count here
		String joyStickInfoText = getJoystickInfo(currentJoyIndex, JoyInfoEnum.buttonAxisInfo);
		this.drawCenteredString(getFontRenderer(), joyStickInfoText, width / 2, heightOffset, -1);

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
		if (c == ' ')
		{
			toggleController();
		}
		else
		{
			super.keyTyped(c, code);
		}
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

	private void toggleController()
	{
		System.out.println("Enable/disable input");
		JoypadMod.controllerSettings.setInputEnabled(!JoypadMod.controllerSettings.isInputEnabled());
		updateControllerButton();
	}

	private void updateControllerButton()
	{
		GuiButton button = (GuiButton) buttonList.get(ButtonsEnum.mouseMenu.ordinal());
		if (JoypadMod.controllerSettings.isInputEnabled())
		{
			button.enabled = false;
			if (ControllerSettings.joyNo != controllers.get(currentJoyIndex))
				JoypadMod.controllerSettings.setController(controllers.get(currentJoyIndex));
		}
		else
			button.enabled = true;

		button = (GuiButton) buttonList.get(ButtonsEnum.control.ordinal());
		button.displayString = getJoystickInfo(currentJoyIndex, JoyInfoEnum.name);
	}

	// Obfuscation & back porting helpers -- here and not in ObfuscationHelper
	// because accessing protected methods
	// TODO think about extending the GuiButton class for this functionality

	@SuppressWarnings("unchecked")
	private void addButton(GuiButton guiButton)
	{
		// field_146292_n.add(guiButton);
		buttonList.add(guiButton);
	}

	private int getButtonId(GuiButton guiButton)
	{
		// return guiButton.field_146127_k;
		return guiButton.id;
	}

	private FontRenderer getFontRenderer()
	{
		// return this.field_146289_q;
		return this.fontRenderer;
	}
}
