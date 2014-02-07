package com.shiny.joypadmod.minecraftExtensions;


import java.util.Iterator;
import java.util.Map;

import com.shiny.joypadmod.ControllerSettings;
import com.shiny.joypadmod.JoypadMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.GameSettings;

public class JoypadConfigMenu extends GuiScreen {//GuiControls{
	
	boolean nextClicked = false;
	long lastClick = 0;
	int currentJoyNo = 0;
	GuiScreen parentScr;
	Minecraft mc = Minecraft.getMinecraft();

	public JoypadConfigMenu(GuiScreen parent, GameSettings gameSettings) 
	{
		parentScr = parent;
	}
	

	@Override
	public void initGui()
	{
		System.out.println("initGui MINE");
									
		AddButton(new GuiButton(0, 5, 5, 40, 20, "Enable"));
		AddButton(new GuiButton(1, 5, 50, 40, 20, "Next"));
		AddButton(new GuiButton(2, 5, 75, 40, 20, "Exit"));			
	}
		
	@Override
	protected void actionPerformed(GuiButton guiButton)
	{		
		System.out.println("Action performed on buttonID " + GetButtonId(guiButton));
		
		switch (GetButtonId(guiButton)) {
			case 0:
				String newText;
				if (GetDisplayString(guiButton) == "Enable")
				{
					ControllerSettings.inputEnabled = false;
					newText = "Disable";
				}
				else
				{
					ControllerSettings.inputEnabled = true;
					newText = "Enable";
				}
				SetDisplayString(guiButton, newText);
				break;
			case 1:
				if (Minecraft.getSystemTime() - lastClick > 1000)
				{				
					nextClicked = true;
					lastClick = Minecraft.getSystemTime();
				}
				break;
			case 2:
				JoypadMod.obfuscationHelper.DisplayGuiScreen(this.parentScr);
				break;
			
		}		
	}
	
	@Override
	public void drawScreen(int par1, int par2, float par3)
	{
		DrawDefaultBackground();		
		this.drawString(GetFontRenderer(), "Current Controller:", 6, 30, -1);
		String output = "mouse";
		if (ControllerSettings.inputEnabled)
		{
			int joyNo = ControllerSettings.joyNo;			
			if (nextClicked)
			{				
				Iterator<?> it =  ControllerSettings.validControllers.entrySet().iterator();
				Map.Entry pairs = null;
				while (it.hasNext())
				{
					pairs = (Map.Entry)it.next();
					if ((Integer)pairs.getKey() == joyNo)
					{
						if (!it.hasNext())
						{
							it = ControllerSettings.validControllers.entrySet().iterator();						
						}
						pairs = (Map.Entry)it.next();
						break;
					}					
				}
				
				if (pairs == null)
				{
					System.out.println("Impossible pairs is null!!");
				}
				else
				{
					ControllerSettings.SetController((Integer)pairs.getKey());					
				}	
				nextClicked = false;				
			}
			output = ControllerSettings.validControllers.get(joyNo);			
		}
		this.drawString(GetFontRenderer(), output, 6, 40, -1);
		super.drawScreen(par1, par2, par3);
		
	}
	
	// Obfuscation & back porting helpers -- here and not in ObfuscationHelper because accessing protected methods
	// TODO think about extending the GuiButton class for this functionality
	
	private void AddButton(GuiButton guiButton)
	{
		//field_146292_n.add(guiButton);
		buttonList.add(guiButton);
	}
	
	private void DrawDefaultBackground()
	{
		//this.func_146276_q_();
		this.drawDefaultBackground();
	}
	
	private int GetButtonId(GuiButton guiButton)
	{
		//return guiButton.field_146127_k;
		return guiButton.id;
	}
	
	private String GetDisplayString(GuiButton guiButton)
	{
		//return guiButton.field_146126_j;
		return guiButton.displayString;
	}
	
	private void SetDisplayString(GuiButton guiButton, String displayString)
	{
		//guiButton.field_146126_j = displayString;
		guiButton.displayString = displayString;
	}
	
	private FontRenderer GetFontRenderer()
	{
		//return this.field_146289_q;
		return this.fontRendererObj;
	}
}
