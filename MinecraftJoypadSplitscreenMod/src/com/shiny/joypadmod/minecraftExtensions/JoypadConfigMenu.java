package com.shiny.joypadmod.minecraftExtensions;


import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.shiny.joypadmod.ControllerSettings;
import com.shiny.joypadmod.JoypadMod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlider;
import net.minecraft.client.settings.EnumOptions;
import net.minecraft.client.settings.GameSettings;

public class JoypadConfigMenu extends GuiControls{
	
	boolean nextClicked = false;
	long lastClick = 0;
	int currentJoyNo = 0;
	GuiScreen parentScr;
	//public GuiControlsJoypad(GuiScreen par1GuiScreen, GameSettings par2GameSettings)
	//{
	//	super(par1GuiScreen, par2GameSettings);
	//}

	public JoypadConfigMenu(GuiScreen parent, GameSettings gameSettings) {
		
		super(parent, gameSettings);
		System.out.println("CONSTRUCTOR!!!!!");
		parentScr = parent;
		
		// TODO Auto-generated constructor stub
	}

	@Override
	public void initGui()
	{
		System.out.println("initGui MINE");
		
		//AddTheButton("Enable");
		//this.field_146292_n.add(new GuiButton(10000, 5, 5, 40, 20, "Enable"));
		super.initGui();
				
		AddTheButton("Enable");
		buttonList.add(new GuiButton(10010, 5, 50, 40, 20, "Next"));
		buttonList.add(new GuiButton(10020, 5, 75, 40, 20, "Exit"));
		
		//this.drawString(this.fontRenderer, this.options.getKeyBindingDescription(l), k + l % 2 * 160 + 70 + 6, this.height / 6 + 24 * (l >> 1) + 7, -1);
		
		/*
		EnumOptions myEnumOptions = new EnumOptions();
		//public GuiSlider(int par1, int par2, int par3, EnumOptions par4EnumOptions, String par5Str, float par6)
		buttonList.add(new GuiSlider(10001, 5, 25, 150, 20, "MySlider!"));
			
		
		int i = 0;
		for (Map.Entry<Integer, String> entry : ControllerSettings.validControllers.entrySet())
		{
			this.buttonList.add(new GuiButton(10000 + entry.getKey(), 5, 20 + 20*i, 40, 40 + 20*i, "BLAH"));//entry.getValue()));
			i++;		
		}
		*/
		//this.field_146292_n.clear();
		//this.field_146292_n.add(new GuiButton(10000, 5, 5, 40, 20, "Enable"));
		// width this.field_146294_l, height this.field_146295_m
		//this.field_146293_o.add(new GuiOptionButton(6, 120, this.field_146294_l /* height */ - 29, 150, 20, "hello"));		
	}
		
	@Override
	protected void actionPerformed(GuiButton par1GuiButton) //func_146284_a
	{		
		System.out.println("Action performed on buttonID " + par1GuiButton.id);
		
		if (par1GuiButton.id /*.field_146127_k */== 10000)
		{
			String newText;
			if (par1GuiButton.displayString /*.field_146126_j*/ == "Enable")
			{
				ControllerSettings.inputEnabled = false;
				newText = "Disable";
			}
			else
			{
				ControllerSettings.inputEnabled = true;
				newText = "Enable";
			}
			par1GuiButton.displayString = newText;
			//this.field_146292_n.
			//AddTheButton(newText);
				
		}	
		else if (par1GuiButton.id /*.field_146127_k */== 10010)
		{
			if (Minecraft.getSystemTime() - lastClick > 1000)
			{				
				nextClicked = true;
				lastClick = Minecraft.getSystemTime();
			}
		}
		else if (par1GuiButton.id == 10020)
        {
            this.mc.displayGuiScreen(this.parentScr);
        }
	}
	
	@Override
	public void drawScreen(int par1, int par2, float par3)
	{
		super.drawScreen(par1, par2, par3);
		this.drawString(this.fontRenderer, "Current Controller:", 6, 30, -1);
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
		this.drawString(this.fontRenderer, output, 6, 40, -1);
		
	}
	private void AddTheButton(String text)
	{

		this.buttonList.clear();
		this.buttonList.add(new GuiButton(10000, 5, 5, 40, 20, text));
		/*
		this.field_146292_n.clear();
		this.field_146292_n.add(new GuiButton(10000, 5, 5, 40, 20, text));*/
	}
}
