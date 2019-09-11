package com.shiny.joypadmod;

import com.shiny.joypadmod.helpers.LogHelper;
import com.shiny.joypadmod.helpers.McObfuscationHelper;
import com.shiny.joypadmod.helpers.ModVersionHelper;
import com.shiny.joypadmod.inputevent.ControllerBinding;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;

public class ButtonScreenTips extends Gui {
	
	public static class HintString {
		
		public String bindingString;
		public String hintHolding;
		public String hintNormal;
		public Boolean isValid;
		private String hintOverride;
		private String hintOverrideHolding;
		public HintString(String bindString, String menuHintOverride, String menuHintOverrideHolding)
		{
			LogHelper.Info(String.format("HintString Constructor called with " +
					"binding: %s. MenuOverride: %s. menuHintOverrideHolding: %s",
					bindString, menuHintOverride, menuHintOverrideHolding));
			bindingString = bindString;
			hintOverride = menuHintOverride;
			hintOverrideHolding = menuHintOverrideHolding;
			UpdateHintString();
		}
		
		public HintString(String a)
		{
			this(a,null,null);
		}
		
		public void UpdateHintString()
		{
			try
			{
				ControllerBinding b = ControllerSettings.get(bindingString);
				isValid = b!= null && b.inputEvent != null && b.inputEvent.isValid();
				if (isValid)
				{
					hintNormal = b.getInputName() + " - ";
					if (hintOverride == null)
						hintNormal += b.getMenuItemName();
					else
						hintNormal += McObfuscationHelper.lookupString(hintOverride);
					if (hintOverrideHolding == null)
						hintHolding = null;
					else
						hintHolding = b.getInputName() + " - " + 
						McObfuscationHelper.lookupString(hintOverrideHolding);
				}
				LogHelper.Info(String.format("HintString: %s, HintStringHolding: %s", 
						hintNormal, hintHolding));
			} catch (Exception ex)
			{
				isValid = false;
				LogHelper.Error("Error updating HintString " + bindingString + " " + ex.getLocalizedMessage());
			}
		}
	}
	
	public static HintString[] blTipsGame = { new HintString("joy.inventory"), new HintString("joy.jump")};
	public static HintString[] brTipsGame = { new HintString("joy.attack"), new HintString("joy.use") };
	public static HintString[] blTipsMenu = { new HintString("joy.closeInventory"), 
			new HintString("joy.shiftClick","menuHint.quickmove",null) /* quick move */ };
	public static HintString[] brTipsMenu = { new HintString("joy.guiLeftClick","menuHint.takeall","menuHint.placeall"), 
			new HintString("joy.interact","menuHint.takehalf","menuHint.placeone") /* take half stack / drop 1 item */};
	
	Minecraft mc = Minecraft.getMinecraft();
	FontRenderer fr = mc.fontRenderer;
	int currentX = 5;
	int currentY = 20;
	
	public ButtonScreenTips()
	{
		if (ControllerSettings.isSuspended() 
				|| !ControllerSettings.isInputEnabled() 
				|| !ControllerSettings.displayHints)
			return;
        
		displayTips();
	}
	
	public static void UpdateHintString()
	{
		for (HintString hs : blTipsGame)
			hs.UpdateHintString();
		for (HintString hs : brTipsGame)
			hs.UpdateHintString();
		for (HintString hs : blTipsMenu)
			hs.UpdateHintString();
		for (HintString hs : brTipsMenu)
			hs.UpdateHintString();
	}
	
	private int findMaxStringLength(HintString[] hintStrings, boolean inMenu, boolean isHolding)
	{
		int max = 0;
		for (HintString hs : hintStrings)
    	{
    		if (hs.isValid)
        	{
    			String checkString;
    			if (isHolding &&  hs.hintHolding != null)
    				checkString = hs.hintHolding;
    			else
    				checkString = hs.hintNormal;
        		int len = fr.getStringWidth(checkString);
        		if (len > max)
        			max = len;         		
        	}
    	}
		return max;
	}
	
	private void displayTips()
	{
		ScaledResolution scaled = ModVersionHelper.GetScaledResolution();
        int width = scaled.getScaledWidth();
        int height = scaled.getScaledHeight();

        if (mc.currentScreen instanceof GuiContainer)
        {
        	boolean isHolding = mc.player != null &&
        			mc.player.inventory != null &&
        			mc.player.inventory.getItemStack() != null;
        	int maxLen = findMaxStringLength(blTipsMenu, true, isHolding);
        	currentX = width / 2 - 100 - maxLen;
            currentY = height - ( (fr.FONT_HEIGHT * blTipsMenu.length) + (blTipsMenu.length * 5) );
            for (HintString hs : blTipsMenu)
        	{
        		if (hs.isValid)
            	{
        			String outString = isHolding && hs.hintHolding != null ? hs.hintHolding :hs.hintNormal;
            		drawTip(outString, 0xFFFFFF);
            	}
        	}
            currentX = width / 2 + 100;
            currentY = height - ( (fr.FONT_HEIGHT * brTipsMenu.length) + (brTipsMenu.length * 5) );
            for (HintString hs : brTipsMenu)
        	{
        		if (hs.isValid)
            	{
        			String outString = isHolding && hs.hintHolding != null ? hs.hintHolding :hs.hintNormal;
            		drawTip(outString, 0xFFFFFF);
            	}
        	}
        }
        else if (mc.inGameHasFocus)
        {
        	boolean isHolding = false;
        	int maxLen = findMaxStringLength(blTipsGame, false, isHolding);
        	currentX = width / 2 - 95 - maxLen;        	
            currentY = height - ( (fr.FONT_HEIGHT * blTipsGame.length) + (blTipsGame.length * 5) );
            for (HintString hs : blTipsGame)
        	{
        		if (hs.isValid)
            	{
        			String outString = isHolding && hs.hintHolding != null ? hs.hintHolding :hs.hintNormal;
            		drawTip(outString, 0xFFFFFF);
            	}
        	}
            currentX = width / 2 + 95;
            currentY = height - ( (fr.FONT_HEIGHT * brTipsGame.length) + (brTipsGame.length * 5) );
            for (HintString hs : brTipsGame)
        	{
            	if (hs.isValid)
            	{
        			String outString = isHolding && hs.hintHolding != null ? hs.hintHolding :hs.hintNormal;
            		drawTip(outString, 0xFFFFFF);
            	}
        	}
        }	
	}
	
	private void drawTip(String text, int color)
	{
		this.drawString(fr, text, currentX, currentY, color);
		//fr.drawStringWithShadow(text, currentX, currentY, color);
		currentY += fr.FONT_HEIGHT + 5;
	}
}
