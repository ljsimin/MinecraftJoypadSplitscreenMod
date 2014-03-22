package com.shiny.joypadmod.minecraftExtensions;

import java.text.DecimalFormat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;

import com.shiny.joypadmod.ControllerSettings;
import com.shiny.joypadmod.helpers.LogHelper;

public class JoypadCalibrationMenu extends GuiScreen
{
	private JoypadConfigMenu parent;
	private int sensitivity_menuStart;
	private int sensitivity_gameStart;

	// bottom button parameters
	private int buttonYStart_bottom;
	private int bottomButtonWidth = 70;

	private int joypadIndex;
	private int yStart = 5;
	private int xBoxStart;

	private int buttonBoxWidth = 132;
	private int axisBoxWidth = 240;
	private int povBoxWidth;
	private int instructionBoxWidth;

	private int boxSpacing = 5;
	private String lastControllerEvent = "";

	public JoypadCalibrationMenu(JoypadConfigMenu parent, int joypadIndex)
	{
		super();
		this.joypadIndex = joypadIndex;
		this.parent = parent;
		sensitivity_menuStart = ControllerSettings.inMenuSensitivity;
		sensitivity_gameStart = ControllerSettings.inGameSensitivity;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		povBoxWidth = parent.getFontRenderer().getStringWidth("PovX: -10.00");
		instructionBoxWidth = parent.getFontRenderer().getStringWidth("A balanced joystick will show 0") + 10;

		xBoxStart = Math.max(5, width / 2 - ((axisBoxWidth + instructionBoxWidth + boxSpacing) / 2));

		buttonYStart_bottom = height - 20;

		int xPos = width / 2 - bottomButtonWidth / 2;

		GuiButton doneButton = new GuiButton(500, xPos, buttonYStart_bottom, bottomButtonWidth, 20, "Exit");

		// these buttons will be moved if we display axis values
		if (joypadIndex != -1)
		{
			xPos -= bottomButtonWidth / 2;
			buttonList.add(new GuiButton(400, xPos, buttonYStart_bottom, bottomButtonWidth, 20, "Save"));
			xPos += bottomButtonWidth;
			doneButton.displayString = "Cancel";
		}

		buttonList.add(doneButton);

	}

	@Override
	public void onGuiClosed()
	{
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
		int axisId = guiButton.id;
		LogHelper.Info("Action performed on buttonID " + axisId);
		Controller controller = this.joypadIndex != -1 ? Controllers.getController(this.joypadIndex) : null;

		if (guiButton.id < 100)
		{
			// auto set this deadzone
			this.autoCalibrateAxis(this.joypadIndex, axisId);
		}
		else if (guiButton.id >= 100 && guiButton.id < 200)
		{
			// request to lower the deadzone of this axis
			axisId -= 100;
			controller.setDeadZone(axisId, controller.getDeadZone(axisId) - 0.01f);
		}
		else if (guiButton.id >= 200 && guiButton.id < 300)
		{
			// clear deadzone of this axis
			axisId -= 200;
			controller.setDeadZone(axisId, 0.0f);
		}
		else if (guiButton.id >= 300 && guiButton.id < 400)
		{
			// request to raise the deadzone of this axis
			axisId -= 300;
			controller.setDeadZone(axisId, controller.getDeadZone(axisId) + 0.01f);
		}
		else
		{
			switch (guiButton.id)
			{
			case 400: // Save
				ControllerSettings.saveDeadZones(joypadIndex);
				((GuiButton) buttonList.get(1)).displayString = "Done";
				break;
			case 500: // Done
				mc.displayGuiScreen(this.parent);
				break;
			}
		}
	}

	@Override
	public void drawScreen(int par1, int par2, float par3)
	{
		drawDefaultBackground();

		if (joypadIndex != -1)
			ControllerSettings.suspendControllerInput(true, 10000);

		int ySpace = parent.getFontRenderer().FONT_HEIGHT;
		String title = "Calibration menu";
		Controller controller = null;
		if (joypadIndex != -1)
		{
			controller = Controllers.getController(joypadIndex);
			title += " - " + controller.getName();
		}

		write(yStart, title);

		if (joypadIndex == -1)
		{
			write(yStart + ySpace * 2, "Please go back to previous menu and select a controller.");
		}
		else
		{
			readLastControllerEvent();
			write(yStart + (int) (ySpace * 1.5), "Last Controller Event: " + lastControllerEvent, 0xAAAAAA);
			int xStart = xBoxStart;
			int yPos = yStart + ySpace * 3 + 2;
			int xyEndLeft[] = drawInstructions(xStart, yPos, ySpace);
			int xyEndRight[] = drawAxis(xyEndLeft[0] + boxSpacing, yPos, 21, par1, par2);
			// move the save button
			((GuiButton) buttonList.get(0)).xPosition = xyEndLeft[0] + boxSpacing;
			((GuiButton) buttonList.get(0)).yPosition = xyEndRight[1];
			// move the Exit button
			((GuiButton) buttonList.get(1)).xPosition = xyEndLeft[0] + boxSpacing + bottomButtonWidth;
			((GuiButton) buttonList.get(1)).yPosition = xyEndRight[1];

			int xyEndLeftDown[] = drawButtons(xStart, xyEndLeft[1] + boxSpacing + 2, ySpace);
		}

		super.drawScreen(par1, par2, par3);
	}

	private void drawBoxWithText(int xStart, int yStart, int xEnd, int yEnd, String title, int boxColor, int textColor)
	{
		boxColor = -1; // can't seem to get any boxes other than white
		textColor = 0xFFAA00;
		int stringWidth = parent.getFontRenderer().getStringWidth(title);
		int xPos = xStart + ((xEnd - xStart) / 2) - (stringWidth / 2);

		this.drawHorizontalLine(xStart, xPos, yStart, boxColor);
		this.write(xPos + 2, yStart - (parent.getFontRenderer().FONT_HEIGHT / 2) + 1, title, textColor);
		xPos += stringWidth + 2;
		this.drawHorizontalLine(xPos, xEnd, yStart, boxColor);
		this.drawVerticalLine(xStart, yStart, yEnd, boxColor);
		this.drawHorizontalLine(xStart, xEnd, yEnd, boxColor);
		this.drawVerticalLine(xEnd, yStart, yEnd, boxColor);
	}

	private int[] drawButtons(int xStart, int yStart, int ySpace)
	{
		Controller controller = Controllers.getController(joypadIndex);
		int yPos = yStart;
		int maxButtons = 13;
		int butWidth = buttonBoxWidth;
		int numStrings = Math.min(controller.getButtonCount(), maxButtons);

		String title = "Buttons";
		int yEnd = yStart + ((numStrings + 3) * ySpace);
		drawBoxWithText(xStart, yStart, xStart + butWidth, yEnd, title, 0xAA0000, 0x0000AA);
		yPos += 7;
		xStart += 7;

		for (int i = 0; i < numStrings; i++, yPos += ySpace)
		{
			int maxSize = parent.getFontRenderer().getStringWidth(title + " 10");
			String stringOut = parent.getFontRenderer().trimStringToWidth(controller.getButtonName(i), maxSize);
			String output = stringOut + ": " + (controller.isButtonPressed(i) ? "Pressed" : "Not pressed");
			write(xStart, yPos, output);
		}
		drawPov(xStart, yPos, ySpace);
		return new int[] { xStart + butWidth, yEnd };
	}

	@SuppressWarnings("unchecked")
	private int[] drawAxis(int xStart, int yStart, int ySpace, int par1, int par2)
	{
		Controller controller = Controllers.getController(joypadIndex);
		int yPos = yStart;
		int maxStrings = 20;
		int butWidth = axisBoxWidth;
		int numStrings = Math.min(controller.getAxisCount(), maxStrings);
		DecimalFormat df1 = new DecimalFormat("#0.00");
		DecimalFormat df2 = new DecimalFormat("#0.00");
		int controlButWidth = 32;
		int directionButWidth = 15;

		for (int i = 0; i < numStrings; i++, yPos += ySpace)
		{
			int maxSize = parent.getFontRenderer().getStringWidth("X Axis:");
			String stringOut = parent.getFontRenderer().trimStringToWidth(controller.getAxisName(i), maxSize);

			String title = stringOut;
			drawBoxWithText(xStart, yPos, xStart + butWidth, yPos + 25, title, 0xAA0000, 0x0000AA);
			yPos += 10;
			int xPos = xStart + 5;

			String output = stringOut + ": " + df1.format(controller.getAxisValue(i));
			write(xPos, yPos, output);
			xPos += maxSize + parent.getFontRenderer().getStringWidth(" -1.00") + 4;
			output = "Deadzone: " + df2.format(controller.getDeadZone(i));
			write(xPos, yPos, output);
			xPos += parent.getFontRenderer().getStringWidth(output) + 5;

			int yOffset = -7;
			int xOffset = -2;
			if (this.buttonList.size() <= 2 + 4 * i)
			{
				buttonList.add(new GuiButton(i, xPos, yPos + yOffset, controlButWidth, 20, "Auto"));
				buttonList.add(new GuiButton(i + 100, xPos + controlButWidth + xOffset, yPos + yOffset,
						directionButWidth, 20, "<"));
				buttonList.add(new GuiButton(i + 200, xPos + controlButWidth + directionButWidth + xOffset * 2, yPos
						+ yOffset, controlButWidth, 20, "Clear"));
				buttonList.add(new GuiButton(i + 300, xPos + controlButWidth * 2 + directionButWidth + xOffset * 3,
						yPos + yOffset, directionButWidth, 20, ">"));
			}

			for (int k = 2 + 4 * i; k < 4 * i + 4; k++)
			{
				((GuiButton) buttonList.get(k)).drawButton(Minecraft.getMinecraft(), par1, par2);
			}
		}
		return new int[] { xStart + butWidth, yPos };
	}

	private int[] drawPov(int xStart, int yStart, int ySpace)
	{
		Controller controller = Controllers.getController(joypadIndex);
		int yPos = yStart;
		int butWidth = povBoxWidth;
		int numStrings = 2;
		// String title = "Pov";
		int yEnd = yStart + ((numStrings + 1) * ySpace);
		// drawBoxWithText(xStart, yStart, xStart + butWidth, yEnd, title, 0xAA0000, 0x0000AA);
		// yPos += 7;
		// xStart += 7;

		for (int i = 0; i < numStrings; i++, yPos += ySpace)
		{
			String output = i == 0 ? "PovX: " + controller.getPovX() : "PovY: " + controller.getPovY();
			write(xStart, yPos, output);
		}

		return new int[] { xStart + butWidth, yEnd };
	}

	private int[] drawInstructions(int xStart, int yStart, int ySpace)
	{
		int yPos = yStart;
		int butWidth = instructionBoxWidth;

		String title = "Instructions";
		String[] instructions = new String[] { "1. Move joystick axis around", "2. Let go of axis",
				"3. Press auto to find deadzone", "4. Save deadzone", "", "A balanced joystick will show 0",
				"when not pressed" };
		int yEnd = yStart + ((instructions.length + 1) * ySpace);
		drawBoxWithText(xStart, yStart, xStart + butWidth, yEnd, title, 0xAA0000, 0x0000AA);

		yPos += 7;
		xStart += 5;

		for (int i = 0; i < instructions.length; i++, yPos += ySpace)
		{
			write(xStart, yPos, instructions[i]);
		}

		return new int[] { xStart + butWidth, yEnd };
	}

	private void write(int yPos, String text)
	{
		write(yPos, text, -1);
	}

	private void write(int yPos, String text, int fontColor)
	{
		this.drawCenteredString(parent.getFontRenderer(), text, width / 2, yPos, fontColor);
	}

	private void write(int xPos, int yPos, String text)
	{
		write(xPos, yPos, text, -1);
	}

	private void write(int xPos, int yPos, String text, int fontColor)
	{
		this.drawString(parent.getFontRenderer(), text, xPos, yPos, fontColor);
	}

	private void readLastControllerEvent()
	{
		try
		{
			Controller controller = Controllers.getController(this.joypadIndex);
			while (Controllers.next() && Controllers.getEventSource() == controller)
			{
				if (Controllers.isEventAxis())
				{
					lastControllerEvent = controller.getAxisName(Controllers.getEventControlIndex());
				}
				else if (Controllers.isEventButton())
				{
					lastControllerEvent = controller.getButtonName(Controllers.getEventControlIndex());
				}
				else if (Controllers.isEventPovX())
				{
					lastControllerEvent = "PovX";
				}
				else if (Controllers.isEventPovY())
				{
					lastControllerEvent = "PovY";
				}
				else
				{
					lastControllerEvent = "Unknown controller event with index: " + Controllers.getEventControlIndex();
				}
			}
		}
		catch (Exception ex)
		{
			lastControllerEvent = ex.toString();
		}
	}

	private void autoCalibrateAxis(int joyId, int axisId)
	{
		Controller controller = Controllers.getController(joyId);
		controller.setDeadZone(axisId, 0);
		float currentValue = Math.abs(controller.getAxisValue(axisId));
		LogHelper.Info("Axis: " + axisId + " currently has a value of: " + currentValue);
		float newValue = currentValue + 0.15f;
		controller.setDeadZone(axisId, newValue);
		LogHelper.Info("Auto set axis " + axisId + " deadzone to " + newValue);
	}

}
