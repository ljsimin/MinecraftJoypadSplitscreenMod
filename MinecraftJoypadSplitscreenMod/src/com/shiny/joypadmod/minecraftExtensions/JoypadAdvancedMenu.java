package com.shiny.joypadmod.minecraftExtensions;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class JoypadAdvancedMenu extends GuiScreen
{

	private JoypadConfigMenu parent;

	public JoypadAdvancedMenu(JoypadConfigMenu parent)
	{
		super();
		this.parent = parent;

	}

	@SuppressWarnings("unchecked")
	@Override
	public void initGui()
	{
		buttonList.add(new GuiButton(500, width / 2 - parent.bottomButtonWidth / 2, height - 20,
				parent.bottomButtonWidth, 20, parent.sGet("gui.done")));
	}

	@Override
	protected void actionPerformed(GuiButton guiButton)
	{
		switch (guiButton.id)
		{

		case 500: // Done
			mc.displayGuiScreen(this.parent);
			break;
		}

	}

	@Override
	public void drawScreen(int par1, int par2, float par3)
	{
		drawDefaultBackground();

		int labelYStart = 5;

		String titleText = String.format("Joypad Mod %s", parent.sGet("controlMenu.advanced"));
		this.drawCenteredString(parent.getFontRenderer(), titleText, width / 2, labelYStart, -1);

		super.drawScreen(par1, par2, par3);
	}

}
