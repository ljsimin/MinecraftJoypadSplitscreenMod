package com.shiny.joypadmod.minecraftExtensions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;

import com.shiny.joypadmod.ControllerSettings;
import com.shiny.joypadmod.JoypadMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiScreen;

public class JoypadConfigMenu extends GuiScreen
{

	public boolean nextClicked = false;
	private int currentJoyNo = 0;

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

	public Map<String, String> translation;

	private JoypadControlList optionList;

	public JoypadConfigMenu(GuiScreen parent, GuiControls originalControlScreen)
	{
		super();
		parentScr = parent;
		if (ControllerSettings.joyNo >= 0)
			currentJoyNo = ControllerSettings.joyNo;

		// TODO find somewhere to put this, most likely ControllerSettings or
		// ControllerUtils
		translation = new HashMap<String, String>();
		// used until I figure out how to pack .property files
		translation.put("joy.attack", "Attack");
		translation.put("joy.use", "Use");
		translation.put("joy.jump", "Jump");
		translation.put("joy.sneak", "Sneak");
		translation.put("joy.drop", "Drop");
		translation.put("joy.inventory", "Open inventory");
		translation.put("joy.interact", "Interact");
		translation.put("joy.guiLeftClick", "Left click");
		translation.put("joy.guiRightClick", "Right click");
		translation.put("joy.prevItem", "Previous item");
		translation.put("joy.nextItem", "Next item");
		translation.put("joy.run", "Sprint");
		translation.put("joy.menu", "Open menu");
		translation.put("joy.shiftClick", "Shift-click");
		translation.put("joy.cameraX+", "Look right");
		translation.put("joy.cameraX-", "Look left");
		translation.put("joy.cameraY+", "Look down");
		translation.put("joy.cameraY-", "Look up");
		translation.put("joy.movementX+", "Strafe right");
		translation.put("joy.movementX-", "Strafe left");
		translation.put("joy.movementY+", "Move forward");
		translation.put("joy.movementY-", "Move backwards");
		translation.put("joy.guiX+", "GUI right");
		translation.put("joy.guiX-", "GUI left");
		translation.put("joy.guiY+", "GUI down");
		translation.put("joy.guiY-", "GUI up");
	}

	@Override
	public void initGui()
	{
		controllerButtonWidth = width - width / 5;
		buttonXStart_top = width / 10;
		buttonYStart_bottom = height - 20;

		// add top buttons
		AddButton(new GuiButton(100, buttonXStart_top, buttonYStart_top, controllerButtonWidth, 20, getJoystickInfo(currentJoyNo, JoyInfoEnum.name)));
		AddButton(new GuiButton(101, buttonXStart_top, buttonYStart_top + buttonYSpacing, controllerButtonWidth / 2, 20, "PREV"));
		AddButton(new GuiButton(102, buttonXStart_top + controllerButtonWidth / 2, buttonYStart_top + buttonYSpacing, controllerButtonWidth / 2, 20, "NEXT"));

		// the middle section will be populated with the controller settings so
		// record where we left off with the top
		buttonYEnd_top = buttonYStart_top + (buttonYSpacing * 2);

		// add bottom buttons
		AddButton(new GuiButton(500, width / 2 - 92, buttonYStart_bottom, bottomButtonWidth, 20, "Calibrate"));
		AddButton(new GuiButton(501, width / 2 - 30, buttonYStart_bottom, bottomButtonWidth, 20, "Exit"));
		AddButton(new GuiButton(502, width / 2 + 32, buttonYStart_bottom, bottomButtonWidth, 20, "Reset"));

		this.optionList = new JoypadControlList(this, GetFontRenderer());
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
		System.out.println("Action performed on buttonID " + GetButtonId(guiButton));

		switch (GetButtonId(guiButton))
		{
		case 100: // Controller button
			ToggleController();
			break;
		case 101: // PREV
			// disable for safety
			ControllerSettings.inputEnabled = false;
			currentJoyNo = GetJoypadId(-1);
			UpdateControllerButton();
			break;
		case 102: // NEXT
			// disable for safety
			ControllerSettings.inputEnabled = false;
			currentJoyNo = GetJoypadId(1);
			UpdateControllerButton();
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

	private String getJoystickInfo(int joyNo, JoyInfoEnum joyInfo)
	{
		String ret = "";

		try
		{
			if (joyNo > Controllers.getControllerCount())
				ret = "Code Error: Invalid controller # selected";
			else
			{
				Controller control = Controllers.getController(joyNo);
				if (joyInfo == JoyInfoEnum.buttonAxisInfo)
				{
					ret += "Controller " + joyNo + " of " + (Controllers.getControllerCount() - 1);
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
		DrawDefaultBackground();
		this.optionList.drawScreen(par1, par2, par3);
		// this.keyBindingList.drawScreen(par1, par2, par3);
		int heightOffset = labelYStart;
		this.drawCenteredString(GetFontRenderer(), "Controller Settings", width / 2, heightOffset, -1);
		this.drawCenteredString(GetFontRenderer(), "Press SPACE at any time to toggle controller on/off", width / 2, heightOffset + 11, -1);
		heightOffset += 29;

		// output TEXT buttons Axis, POV count here
		String joyStickInfoText = getJoystickInfo(currentJoyNo, JoyInfoEnum.buttonAxisInfo);
		this.drawCenteredString(GetFontRenderer(), joyStickInfoText, width / 2, heightOffset, -1);

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
			ToggleController();
		}
		else
		{
			super.keyTyped(c, code);
		}
	}

	@SuppressWarnings("rawtypes")
	private int GetJoypadId(int offset)
	{
		if (offset == 0)
			return currentJoyNo;

		// need to iterate through the list of valid controllers
		Map.Entry previous = null;
		Map.Entry current = null;
		Iterator it = ControllerSettings.validControllers.entrySet().iterator();

		// find the current joyNo in the list of names
		while (it.hasNext())
		{
			previous = current;
			current = (Map.Entry) it.next();
			if ((Integer) current.getKey() == currentJoyNo)
			{
				if (offset < 0 && previous == null)
				{
					// at beginning of list and need to "wrap around" to end
					while (it.hasNext())
						previous = (Map.Entry) it.next();
				}

				if (offset > 0)
				{
					if (!it.hasNext())
					{
						// at end of list and need to "wrap around" to beginning
						it = ControllerSettings.validControllers.entrySet().iterator();
					}
					current = (Map.Entry) it.next();
				}

				break;
			}
		}

		if (current == null)
			return 0; // something went wrong!

		if (offset < 0)
		{
			return (Integer) previous.getKey();
		}

		return (Integer) current.getKey();
	}

	private void ToggleController()
	{
		System.out.println("Enable/disable input");
		ControllerSettings.inputEnabled = !ControllerSettings.inputEnabled;
		UpdateControllerButton();
	}

	private void UpdateControllerButton()
	{
		if (ControllerSettings.inputEnabled && ControllerSettings.joyNo != currentJoyNo)
			ControllerSettings.SetController(currentJoyNo);
		GuiButton button = (GuiButton) buttonList.get(0);
		button.displayString = getJoystickInfo(currentJoyNo, JoyInfoEnum.name);
	}

	// Obfuscation & back porting helpers -- here and not in ObfuscationHelper
	// because accessing protected methods
	// TODO think about extending the GuiButton class for this functionality

	@SuppressWarnings("unchecked")
	private void AddButton(GuiButton guiButton)
	{
		// field_146292_n.add(guiButton);
		buttonList.add(guiButton);
	}

	private void DrawDefaultBackground()
	{
		// this.func_146276_q_();
		this.drawDefaultBackground();
	}

	private int GetButtonId(GuiButton guiButton)
	{
		// return guiButton.field_146127_k;
		return guiButton.id;
	}

	private FontRenderer GetFontRenderer()
	{
		// return this.field_146289_q;
		return this.fontRendererObj;
	}
}
