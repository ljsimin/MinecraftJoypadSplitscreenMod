package com.shiny.joypadmod.minecraftExtensions;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.input.Controllers;

import com.shiny.joypadmod.ControllerSettings;
import com.shiny.joypadmod.inputevent.ControllerInputEvent;

import cpw.mods.fml.client.GuiScrollingList;

public class JoypadControlList extends GuiScrollingList
{
	private FontRenderer fontRenderer;
	private int controllerInputTimeout = 5000;
	private long controllerTickStart = 0;

	private int selectedIndex = -1;
	private boolean doubleClicked = false;

	public JoypadControlList(JoypadConfigMenu parent, FontRenderer fontRenderer)
	{
		super(parent.mc, parent.controlListWidth, // width
				parent.height, // height
				parent.controlListYStart, // top start
				parent.controlListYStart + parent.controlListHeight, // bottom end
				parent.controlListXStart, // left start
				20); // entryHeight
		this.fontRenderer = fontRenderer;
	}

	@Override
	protected int getSize()
	{
		if (ControllerSettings.joyBindings == null)
			return 0;

		return ControllerSettings.joyBindings.length;
	}

	@Override
	protected void elementClicked(int index, boolean doubleClick)
	{
		selectedIndex = index;
		System.out.println("Element " + index + " clicked! Double: " + doubleClick);
		doubleClicked = doubleClick;
		if (doubleClick)
		{
			controllerTickStart = Minecraft.getSystemTime();
			ControllerSettings.suspendControllerInput(true, 10000);
		}
	}

	@Override
	protected boolean isSelected(int index)
	{
		return (selectedIndex == index);
	}

	@Override
	protected void drawBackground()
	{}

	public boolean getControllerInput()
	{
		if (selectedIndex < 0)
			return false;

		ControllerInputEvent inputEvent = null;
		try
		{
			while (Controllers.next())
			{
				System.out.println("Controllers.next triggered");

				if (Minecraft.getSystemTime() - controllerTickStart < 200)
				{
					System.out.println("Discarding events that occured too soon after last button click");
				}
				else
				{
					inputEvent = ControllerSettings.controllerUtils.getLastEvent(ControllerSettings.joystick,
							Controllers.getEventControlIndex());
					if (inputEvent != null)
					{
						System.out.println("Received from controller: " + inputEvent.getName());
						ControllerSettings.setControllerBinding(selectedIndex, inputEvent);
						return true;
					}
				}
			}

			// System.out.println("No controller event available");
		}
		catch (Exception ex)
		{
			System.out.println("Caught exception while trying to set controller button! " + ex.toString());
		}
		return false;
	};

	@Override
	protected void drawSlot(int var1, int var2, int var3, int var4, Tessellator var5)
	{
		String mcActionName = ControllerSettings.joyBindings[var1].menuString;

		String mcActionButton;

		if (doubleClicked && var1 == selectedIndex)
		{
			mcActionButton = "> ?? <";
		}
		else
		{
			mcActionButton = ControllerSettings.controllerUtils.getHumanReadableInputName(ControllerSettings.joystick,
					ControllerSettings.joyBindings[var1].inputEvent);
		}
		this.fontRenderer.drawString(this.fontRenderer.trimStringToWidth(mcActionName, 100), this.left + 3, var3 + 2,
				0xFF2222);
		this.fontRenderer.drawString(this.fontRenderer.trimStringToWidth(mcActionButton, listWidth - 100),
				this.left + 100, var3 + 2, -1);

		if (doubleClicked && var1 == selectedIndex)
		{
			if (getControllerInput() || Minecraft.getSystemTime() - controllerTickStart > controllerInputTimeout)
			{
				doubleClicked = false;
				ControllerSettings.suspendControllerInput(false, 0);
			}
		}
	}

}