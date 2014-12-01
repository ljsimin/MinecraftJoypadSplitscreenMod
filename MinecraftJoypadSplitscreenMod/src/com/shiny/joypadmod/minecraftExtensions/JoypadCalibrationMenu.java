package com.shiny.joypadmod.minecraftExtensions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;

import com.shiny.joypadmod.ControllerSettings;
import com.shiny.joypadmod.helpers.LogHelper;
import com.shiny.joypadmod.helpers.McObfuscationHelper;

public class JoypadCalibrationMenu extends GuiScreen
{
	public GuiScreen parent;

	// bottom button parameters
	private int buttonYStart_bottom;
	private int bottomButtonWidth = 70;

	private int joypadIndex;
	private int yStart = 5;
	private int buttonBoxWidth = 132;
	public int axisBoxWidth = 275;
	private int povBoxWidth;
	private int instructionBoxWidth;

	private int boxSpacing = 5;
	private String lastControllerEvent = "";

	private JoypadCalibrationList calibrationList = null;

	FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;

	String[] instructions = new String[] { "calibrationMenu.instructions1", "calibrationMenu.instructions2",
			"calibrationMenu.save" };

	public JoypadCalibrationMenu(GuiScreen parent, int joypadIndex)
	{
		super();
		this.joypadIndex = joypadIndex;
		this.parent = parent;
	}

	@Override
	public void onGuiClosed()
	{
		ControllerSettings.applySavedDeadZones(joypadIndex);
		super.onGuiClosed();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{

		povBoxWidth = fr.getStringWidth("PovX: -10.00");
		instructionBoxWidth = 0;
		for (int i = 0; i < instructions.length; i++)
		{
			int newWidth = fr.getStringWidth(McObfuscationHelper.lookupString(instructions[i]));
			if (newWidth > instructionBoxWidth)
				instructionBoxWidth = newWidth;
		}
		instructionBoxWidth += fr.getStringWidth("1: ") + 10;

		Math.max(5, width / 2 - ((axisBoxWidth + instructionBoxWidth + boxSpacing) / 2));

		buttonYStart_bottom = height - 20;

		int xPos = width / 2 - bottomButtonWidth / 2;

		GuiButton doneButton = new GuiButton(500, xPos, buttonYStart_bottom, bottomButtonWidth, 20, "gui.done");

		// these buttons will be moved if we display axis values
		if (joypadIndex != -1 && Controllers.getController(joypadIndex).getAxisCount() > 0)
		{
			int listStartY = (instructions.length + 3) * fr.FONT_HEIGHT + 20;
			int entryHeight = 32;

			calibrationList = new JoypadCalibrationList(Minecraft.getMinecraft(), width, height, listStartY,
					height - 25, 0, entryHeight, joypadIndex, this);

			xPos -= bottomButtonWidth / 2;
			buttonList.add(new GuiButton(400, xPos, buttonYStart_bottom, bottomButtonWidth, 20,
					McObfuscationHelper.lookupString("calibrationMenu.save")));
			xPos += bottomButtonWidth;
			doneButton.xPosition += bottomButtonWidth / 2;
			doneButton.displayString = McObfuscationHelper.lookupString("gui.cancel");
		}

		buttonList.add(doneButton);

	}

	@Override
	protected void mouseClicked(int par1, int par2, int par3)
	{
		try{
		super.mouseClicked(par1, par2, par3);
		}
		catch(java.io.IOException e){}
		if (calibrationList == null)
			return;

		for (GuiButton guiButton : calibrationList.buttonList)
		{
			if (guiButton.mousePressed(Minecraft.getMinecraft(), par1, par2))
			{
				calibrationList.actionPerformed(guiButton);
				break;
			}
		}
	}

	@Override
	protected void actionPerformed(GuiButton guiButton)
	{
		LogHelper.Info("Action performed on buttonID " + guiButton.id);

		switch (guiButton.id)
		{
		case 400: // Save
			ControllerSettings.saveDeadZones(joypadIndex);
			((GuiButton) buttonList.get(1)).displayString = McObfuscationHelper.lookupString("gui.done");
			break;
		case 500: // Done
			mc.displayGuiScreen(this.parent);
			break;
		}
	}

	@Override
	public void drawScreen(int par1, int par2, float par3)
	{
		drawDefaultBackground();

		if (calibrationList != null)
			calibrationList.drawScreen(par1, par2, par3);

		int ySpace = fr.FONT_HEIGHT;
		String title = McObfuscationHelper.lookupString("controlMenu.calibrate");
		Controller controller = null;
		if (joypadIndex != -1)
		{
			controller = Controllers.getController(joypadIndex);
			title += " - " + controller.getName();
		}

		write(yStart, fr.trimStringToWidth(title, width - 2));

		if (joypadIndex == -1)
		{
			write(yStart + ySpace * 2, McObfuscationHelper.lookupString("controlMenu.noControllers"));
		}
		else if (Controllers.getController(joypadIndex).getAxisCount() <= 0)
		{
			write(yStart + ySpace * 2, McObfuscationHelper.lookupString("controlMenu.axis") + "# 0!");
		}
		else
		{
			// readLastControllerEvent();
			// write(yStart + (int) (ySpace * 1.5), "Last Controller Event: " + lastControllerEvent, 0xAAAAAA);

			int yPos = yStart + ySpace * 2 + 2;
			drawInstructions(width / 2 - instructionBoxWidth / 2, yPos, ySpace, instructionBoxWidth);

			// int xyEndLeftDown[] = drawButtons(xStart, xyEndLeft[1] + boxSpacing + 2, ySpace);

		}

		super.drawScreen(par1, par2, par3);
	}

	public void drawBoxWithText(int xStart, int yStart, int xEnd, int yEnd, String title, int boxColor, int textColor)
	{
		boxColor = -1; // can't seem to get any boxes other than white
		textColor = 0xFFAA00;
		int stringWidth = fr.getStringWidth(title);
		int xPos = xStart + ((xEnd - xStart) / 2) - (stringWidth / 2);

		this.drawHorizontalLine(xStart, xPos, yStart, boxColor);
		int yTitleOffset = (fr.FONT_HEIGHT / 2) + (fr.getUnicodeFlag() ? 1 : -1);
		this.write(xPos + 2, yStart - yTitleOffset, title, textColor);
		xPos += stringWidth + 2;
		this.drawHorizontalLine(xPos, xEnd, yStart, boxColor);
		this.drawVerticalLine(xStart, yStart, yEnd, boxColor);
		this.drawHorizontalLine(xStart, xEnd, yEnd, boxColor);
		this.drawVerticalLine(xEnd, yStart, yEnd, boxColor);
	}

	private int[] drawInstructions(int xStart, int yStart, int ySpace, int totalWidth)
	{
		int yPos = yStart;

		String title = McObfuscationHelper.lookupString("calibrationMenu.instructionsTitle");
		int yEnd = yStart + ((instructions.length + 1) * ySpace);
		drawBoxWithText(xStart, yStart, xStart + totalWidth, yEnd, title, 0xAA0000, 0x0000AA);

		yPos += 7;
		xStart += 5;

		for (int i = 0; i < instructions.length; i++, yPos += ySpace)
		{
			String text = String.format("%s: %s", i + 1, McObfuscationHelper.lookupString(instructions[i]));
			int strWidth = fr.getStringWidth(text);
			write((totalWidth - 5) / 2 + xStart - strWidth / 2, yPos, text);
		}

		return new int[] { xStart + totalWidth, yEnd };
	}

	private void write(int yPos, String text)
	{
		write(yPos, text, -1);
	}

	private void write(int yPos, String text, int fontColor)
	{
		this.drawCenteredString(fr, text, width / 2, yPos, fontColor);
	}

	public void write(int xPos, int yPos, String text)
	{
		write(xPos, yPos, text, -1);
	}

	public void write(int xPos, int yPos, String text, int fontColor)
	{
		this.drawString(fr, text, xPos, yPos, fontColor);
	}

	public void drawHorizontalLine(int par1, int par2, int par3, int par4)
	{
		super.drawHorizontalLine(par1, par2, par3, par4);
	}

	public void drawVerticalLine(int par1, int par2, int par3, int par4)
	{
		super.drawVerticalLine(par1, par2, par3, par4);
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
			int maxSize = fr.getStringWidth(title + " 10");
			String stringOut = fr.trimStringToWidth(controller.getButtonName(i), maxSize);
			String output = stringOut + ": " + (controller.isButtonPressed(i) ? "Pressed" : "Not pressed");
			write(xStart, yPos, output);
		}
		drawPov(xStart, yPos, ySpace);
		return new int[] { xStart + butWidth, yEnd };
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
}
