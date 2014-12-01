package com.shiny.joypadmod.minecraftExtensions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraftforge.fml.client.GuiScrollingList;

import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Mouse;

import com.shiny.joypadmod.helpers.LogHelper;
import com.shiny.joypadmod.helpers.McObfuscationHelper;
import com.shiny.joypadmod.inputevent.ControllerUtils;


public class JoypadCalibrationList extends GuiScrollingList
{
	private Minecraft mc;
	private int width;
	private int entryHeight;
	private int joypadIndex;
	private JoypadCalibrationMenu parent;

	public JoypadCalibrationList(Minecraft client, int width, int height, int top, int bottom, int left,
			int entryHeight, int joypadIndex, JoypadCalibrationMenu parent)
	{
		super(client, width, height, top, bottom, left, entryHeight);
		mc = Minecraft.getMinecraft();
		this.width = width;
		this.entryHeight = entryHeight;
		this.joypadIndex = joypadIndex;
		this.parent = parent;
	}

	@Override
	protected int getSize()
	{
		int ret = Controllers.getController(joypadIndex).getAxisCount();
		if (ret > 0)
		{
			int theHeight = this.bottom - this.top;
			// make sure all items will appear at the top
			if (ret * entryHeight < theHeight)
				ret = (int) Math.floor(theHeight / entryHeight);
		}

		return Math.max(Controllers.getController(joypadIndex).getAxisCount(), ret);
	}

	@Override
	protected void elementClicked(int index, boolean doubleClick)
	{}

	@Override
	protected boolean isSelected(int index)
	{
		return false;
	}

	@Override
	protected void drawBackground()
	{
		// TODO Auto-generated method stub

	}

	public void actionPerformed(GuiButton guiButton)
	{
		int axisId = guiButton.id;
		LogHelper.Info("Action performed on buttonID " + axisId);
		Controller controller = this.joypadIndex != -1 ? Controllers.getController(this.joypadIndex) : null;

		if (guiButton.id < 100)
		{
			// auto set this deadzone
			ControllerUtils.autoCalibrateAxis(this.joypadIndex, axisId);
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
	}

	public List<GuiButton> buttonList = new ArrayList<GuiButton>();

	@Override
	protected void drawSlot(int var1, int var2, int var3, int var4, Tessellator var5)
	{
		final ScaledResolution scaledResolution = new ScaledResolution(mc, mc.displayWidth,
				mc.displayHeight);

		final int k = Mouse.getX() * scaledResolution.getScaledWidth() / mc.displayWidth;
		final int i1 = scaledResolution.getScaledHeight() - Mouse.getY() * scaledResolution.getScaledHeight()
				/ mc.displayHeight - 1;

		if (var1 < Controllers.getController(joypadIndex).getAxisCount())
		{
			int totalWidth = parent.axisBoxWidth;
			drawAxis(var1, this.width / 2 - totalWidth / 2, var3 + 2, 21, k, i1, totalWidth);

			for (int i = 4 * var1; i < 4 * var1 + 4; i++)
			{
				if (buttonList.size() > i)
				{
					buttonList.get(i).yPosition = var3 + 5;
					buttonList.get(i).drawButton(Minecraft.getMinecraft(), k, i1);
				}
			}
		}
	}

	private int[] drawAxis(int axisNum, int xStart, int yStart, int ySpace, int par1, int par2, int totalWidth)
	{
		Controller controller = Controllers.getController(joypadIndex);
		int yPos = yStart;
		DecimalFormat df = new DecimalFormat("#0.00");
		int autoButtonWidth = mc.fontRendererObj.getStringWidth(McObfuscationHelper.lookupString("calibrationMenu.auto")) + 10;
		int resetButtonWidth = mc.fontRendererObj.getStringWidth(McObfuscationHelper.lookupString("controls.reset")) + 10;
		int directionButWidth = 15;

		int maxSize = parent.fr.getStringWidth("X Axis:");
		String title = parent.fr.trimStringToWidth(controller.getAxisName(axisNum), maxSize);

		parent.drawBoxWithText(xStart, yPos, xStart + totalWidth, yPos + 25, title, 0xAA0000, 0x0000AA);
		yPos += 10;
		int xPos = xStart + 5;

		String output = title + ": " + df.format(controller.getAxisValue(axisNum));
		parent.write(xPos, yPos, output);
		xPos += maxSize + parent.fr.getStringWidth(" -1.00") + 4;
		output = McObfuscationHelper.lookupString("calibrationMenu.deadzone") + ": "
				+ df.format(controller.getDeadZone(axisNum));
		parent.write(xPos, yPos, output);
		xPos += parent.fr.getStringWidth(output) + 5;

		int xPos2 = xStart + totalWidth - directionButWidth;

		int yOffset = -7;
		int xOffset = 2;
		if (this.buttonList.size() <= 4 * axisNum)
		{
			buttonList.add(new GuiButton(axisNum + 300, xPos2, yPos + yOffset, directionButWidth, 20, ">"));
			xPos2 -= resetButtonWidth - xOffset;
			buttonList.add(new GuiButton(axisNum + 200, xPos2, yPos + yOffset, resetButtonWidth, 20,
					McObfuscationHelper.lookupString("controls.reset")));
			xPos2 -= directionButWidth - xOffset;
			buttonList.add(new GuiButton(axisNum + 100, xPos2, yPos + yOffset, directionButWidth, 20, "<"));
			xPos2 -= autoButtonWidth - xOffset;
			buttonList.add(new GuiButton(axisNum, xPos2, yPos + yOffset, autoButtonWidth, 20,
					McObfuscationHelper.lookupString("calibrationMenu.auto")));

			/*
			 * buttonList.add(new GuiButton(axisNum, xPos, yPos + yOffset, autoButtonWidth, 20, McObfuscationHelper.lookupString("calibrationMenu.auto"))); buttonList.add(new GuiButton(axisNum + 100,
			 * xPos + autoButtonWidth + xOffset, yPos + yOffset, directionButWidth, 20, "<")); buttonList.add(new GuiButton(axisNum + 200, xPos + autoButtonWidth + directionButWidth + xOffset * 2,
			 * yPos + yOffset, resetButtonWidth, 20, McObfuscationHelper.lookupString("controls.reset"))); buttonList.add(new GuiButton(axisNum + 300, xPos + autoButtonWidth + resetButtonWidth +
			 * directionButWidth + xOffset * 3, yPos + yOffset, directionButWidth, 20, ">"));
			 */
		}

		return new int[] { xStart + totalWidth, yPos };
	}
}
