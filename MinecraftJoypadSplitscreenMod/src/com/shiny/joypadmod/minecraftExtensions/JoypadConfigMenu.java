package com.shiny.joypadmod.minecraftExtensions;


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
import net.minecraft.client.settings.GameSettings;

public class JoypadConfigMenu extends GuiScreen {
	
	boolean nextClicked = false;
	long lastClick = 0;
	int currentJoyNo = 0;
	GuiScreen parentScr;
	Minecraft mc = Minecraft.getMinecraft();

	public JoypadConfigMenu(GuiScreen parent, GuiControls originalControlScreen) 
	{
		parentScr = parent;
		if (ControllerSettings.joyNo >= 0)
			currentJoyNo = ControllerSettings.joyNo;
	}
	

	@Override
	public void initGui()
	{
		int controllerButtonWidth = width - width / 5;
		AddButton(new GuiButton(100, width / 10, 60, controllerButtonWidth, 20, getJoystickInfo(currentJoyNo, JoyInfoEnum.name)));
		AddButton(new GuiButton(101, width / 10, 85, controllerButtonWidth / 2, 20, "PREV"));
		AddButton(new GuiButton(102, width / 2, 85, controllerButtonWidth / 2, 20, "NEXT"));
		AddButton(new GuiButton(105, width / 10 + controllerButtonWidth / 4, 110, controllerButtonWidth / 2, 20, "CALIBRATE"));
		AddButton(new GuiButton(2, width / 2 - 20, height - 25, 40, 20, "Exit"));
	}
		
	@Override
	protected void actionPerformed(GuiButton guiButton)
	{		
		System.out.println("Action performed on buttonID " + GetButtonId(guiButton));
		
		switch (GetButtonId(guiButton)) {
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
			case 2:
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
					ret += "Buttons: " + control.getButtonCount();
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
		int heightOffset = 10;
		this.drawCenteredString(GetFontRenderer(), "Controller Settings", width/2, heightOffset, -1);
		this.drawCenteredString(GetFontRenderer(), "Press SPACE at any time to toggle controller on/off", width/2, heightOffset + 11, -1);
		heightOffset += 29;
		
		// output TEXT buttons Axis, POV count here
		String joyStickInfoText = getJoystickInfo(currentJoyNo, JoyInfoEnum.buttonAxisInfo);		
		this.drawCenteredString(GetFontRenderer(), joyStickInfoText, width/2, heightOffset, -1);
		
		// CONTROLLER NAME BUTTON
		// PREV            NEXT
		//       CALIBRATE
			
		super.drawScreen(par1, par2, par3);		
	}
	
	/**
     * Fired when a key is typed. This is the equivalent of KeyListener.keyTyped(KeyEvent e).
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
    	Iterator it =  ControllerSettings.validControllers.entrySet().iterator();
    	
    	// find the current joyNo in the list of names
		while (it.hasNext())
		{	
			previous = current;
			current = (Map.Entry)it.next();
			if ((Integer)current.getKey() == currentJoyNo)
			{				
				if (offset < 0 && previous == null)
				{
					// at beginning of list and need to "wrap around" to end
					while (it.hasNext())
						previous = (Map.Entry)it.next();					
				}
				
				if (offset > 0)
				{
					if (!it.hasNext())
					{
						// at end of list and need to "wrap around" to beginning
						it = ControllerSettings.validControllers.entrySet().iterator();
					}
					current = (Map.Entry)it.next();
				}
					
				break;
			}			
		}
		
		if (current == null)
			return 0;  // something went wrong!
		
		if (offset < 0)
		{
			return (Integer)previous.getKey();
		}
		
		return (Integer)current.getKey();
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
		GuiButton button = (GuiButton)buttonList.get(0);
    	button.displayString = getJoystickInfo(currentJoyNo, JoyInfoEnum.name);
    }
	
	// Obfuscation & back porting helpers -- here and not in ObfuscationHelper because accessing protected methods
	// TODO think about extending the GuiButton class for this functionality
	
	@SuppressWarnings("unchecked")
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
	
	private FontRenderer GetFontRenderer()
	{
		//return this.field_146289_q;
		return this.fontRendererObj;
	}
}
