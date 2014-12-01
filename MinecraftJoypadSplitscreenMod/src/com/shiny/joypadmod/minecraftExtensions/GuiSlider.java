package com.shiny.joypadmod.minecraftExtensions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.StatCollector;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import com.shiny.joypadmod.helpers.McObfuscationHelper;

public class GuiSlider extends GuiButton
{
	/** The value of this slider control. */
	protected float sliderValue = 1.0F;
	public float minValue = 0.01F;
	public float maxValue = 1.0F;

	/** Is this slider control being dragged. */
	public boolean dragging;

	protected String baseDisplayString;

	public GuiSlider(int id, int posX, int posY, int width, int height, String displayString, float value)
	{
		super(id, posX, posY, width, height, displayString);
		this.sliderValue = value;
		this.baseDisplayString = displayString;
	}

	public void setValue(float value)
	{
		if (value < minValue)
		{
			value = minValue;
		}
		if (value > maxValue)
		{
			value = maxValue;
		}
		sliderValue = value;
	}

	public float getValue()
	{
		return sliderValue;
	}

	/**
	 * Returns 0 if the button is disabled, 1 if the mouse is NOT hovering over this button and 2 if it IS hovering over this button.
	 */
	@Override
	public int getHoverState(boolean mouseOver)
	{
		return 0;
	}

	/**
	 * Fired when the mouse button is dragged. Equivalent of MouseListener.mouseDragged(MouseEvent e).
	 */
	@Override
	protected void mouseDragged(Minecraft minecraft, int mouseX, int mouseY)
	{
		// something is awry with this receiving the mouse released event so check manually if button pressed
		if (!Mouse.isButtonDown(0))
			this.dragging = false;

		if (this.visible)
		{
			if (this.dragging)
			{
				setValue((float) (mouseX - (this.xPosition + 4)) / (float) (this.width - 8));
				this.updateText();
			}

			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.drawTexturedModalRect(this.xPosition + (int) (this.sliderValue * (this.width - 8)), this.yPosition, 0,
					66, 4, 20);
			this.drawTexturedModalRect(this.xPosition + (int) (this.sliderValue * (this.width - 8)) + 4,
					this.yPosition, 196, 66, 4, 20);
		}
	}

	/**
	 * Returns true if the mouse has been pressed on this control. Equivalent of MouseListener.mousePressed(MouseEvent e).
	 */
	@Override
	public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY)
	{
		if (super.mousePressed(minecraft, mouseX, mouseY))
		{
			setValue((float) (mouseX - (this.xPosition + 4)) / (float) (this.width - 8));

			this.dragging = true;
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Fired when the mouse button is released. Equivalent of MouseListener.mouseReleased(MouseEvent e).
	 */
	@Override
	public void mouseReleased(int mouseX, int mouseY)
	{
		this.dragging = false;
	}

	public void updateText()
	{
		String output = "";
		if (this.baseDisplayString.equals("controlMenu.sensitivity.game"))
		{
			output = String.format("(%s) %s", McObfuscationHelper.lookupString("key.categories.gameplay"),
					McObfuscationHelper.lookupString("options.sensitivity"));
		}
		else if (this.baseDisplayString.equals("controlMenu.sensitivity.menu"))
		{
			output = String.format("(%s) %s", McObfuscationHelper.lookupString("joy.menu"),
					McObfuscationHelper.lookupString("options.sensitivity"));
		}
		if (output != "")
		{
			FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
			String value = ": " + (int) (this.sliderValue * 100.0F);
			this.displayString = fr.trimStringToWidth(output, this.width - fr.getStringWidth(value)) + value;
		}
		else
		{
			this.displayString = StatCollector.translateToLocalFormatted(this.baseDisplayString,
					(int) (this.sliderValue * 100.0F));
		}
	}
}
