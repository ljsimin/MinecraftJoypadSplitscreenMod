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
import com.shiny.joypadmod.JoypadMod;

public class JoypadConfigMenu extends GuiScreen
{

	public boolean nextClicked = false;
	private int currentJoyIndex = 0;

	private int labelYStart = 5;

	private int buttonYStart_top = labelYStart + 40; // Y start of the buttons
														// at the top of the
														// screen
	public int buttonYEnd_top; // Y end of the buttons at the top of the screen
	public int buttonXStart_top; // X start of the buttons at the top of the
									// screen

	public int controllerButtonWidth; // determined by size of screen

	private int buttonYSpacing = 21; // spacing between the buttons

	public int buttonYStart_bottom;
	private int bottomButtonWidth = 60;

	GuiScreen parentScr;
	Minecraft mc = Minecraft.getMinecraft();

	private JoypadControlList optionList;
	private List<Integer> controllers;

	public JoypadConfigMenu(GuiScreen parent, GuiControls originalControlScreen)
	{
		super();
		parentScr = parent;
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
		addButton(new GuiButton(100, buttonXStart_top, buttonYStart_top, controllerButtonWidth, 20, getJoystickInfo(currentJoyIndex, JoyInfoEnum.name)));
		addButton(new GuiButton(101, buttonXStart_top, buttonYStart_top + buttonYSpacing, controllerButtonWidth / 2, 20, "PREV"));
		addButton(new GuiButton(102, buttonXStart_top + controllerButtonWidth / 2, buttonYStart_top + buttonYSpacing, controllerButtonWidth / 2, 20, "NEXT"));

		// the middle section will be populated with the controller settings so
		// record where we left off with the top
		buttonYEnd_top = buttonYStart_top + (buttonYSpacing * 2);

		// add bottom buttons
		addButton(new GuiButton(500, width / 2 - 92, buttonYStart_bottom, bottomButtonWidth, 20, "Calibrate"));
		addButton(new GuiButton(501, width / 2 - 30, buttonYStart_bottom, bottomButtonWidth, 20, "Exit"));
		addButton(new GuiButton(502, width / 2 + 32, buttonYStart_bottom, bottomButtonWidth, 20, "Reset"));

		this.optionList = new JoypadControlList(this, getFontRenderer());
	}

	@Override
	public void onGuiClosed()
	{
		System.out.println("JoypadConfigMenu OnGuiClosed");
		ControllerSettings.suspendControllerInput(false);
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
			ControllerSettings.inputEnabled = false;
			currentJoyIndex = getJoypadIndex(-1);
			updateControllerButton();
			break;
		case 102: // NEXT
			// disable for safety
			ControllerSettings.inputEnabled = false;
			currentJoyIndex = getJoypadIndex(1);
			updateControllerButton();
			break;
		case 501:
			// TODO replace minecraft game settings with copy
			JoypadMod.obfuscationHelper.DisplayGuiScreen(this.parentScr);
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
					ret += ControllerSettings.inputEnabled ? "on" : "off";
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
		this.drawCenteredString(getFontRenderer(), "Controller Settings", width / 2, heightOffset, -1);
		this.drawCenteredString(getFontRenderer(), "Press SPACE at any time to toggle controller on/off", width / 2, heightOffset + 11, -1);
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
	 * Fired when a key is typed. This is the equivalent of
	 * KeyListener.keyTyped(KeyEvent e).
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
		ControllerSettings.inputEnabled = !ControllerSettings.inputEnabled;
		updateControllerButton();
	}

	private void updateControllerButton()
	{
		if (ControllerSettings.inputEnabled && ControllerSettings.joyNo != controllers.get(currentJoyIndex))
			JoypadMod.controllerSettings.setController(controllers.get(currentJoyIndex));
		GuiButton button = (GuiButton) buttonList.get(0);
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
		return this.fontRendererObj;
	}
}
