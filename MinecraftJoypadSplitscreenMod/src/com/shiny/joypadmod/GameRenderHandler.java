package com.shiny.joypadmod;

import org.lwjgl.input.Controllers;

import com.shiny.joypadmod.minecraftExtensions.ControllerMovementInput;
import com.shiny.joypadmod.minecraftExtensions.JoypadConfigMenu;

import cpw.mods.fml.common.ObfuscationReflectionHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;

public class GameRenderHandler {
	
	private static Minecraft mc = Minecraft.getMinecraft();
	public static int reticalColor = 0xFFFFFFFF;	
	public static VirtualMouse joypadMouse = new VirtualMouse();
	
	public static void HandlePreRender()
	{
		if (InGuiCheckNeeded() && (mc.currentScreen != null))
		{
			if (mc.currentScreen instanceof GuiControls && (!(mc.currentScreen instanceof JoypadConfigMenu)))
			{
				ReplaceControlScreen((GuiControls)mc.currentScreen);
			}

			HandleGuiPreRender();		
		}		
	}
	
	public static void HandlePostRender()
	{
		try
    	{
	    	if (InGuiCheckNeeded())
	    	{
	    		HandleGuiPostRender();	    		
	    	}
	    	    	
	    	if (InGameCheckNeeded())
	    	{ 
	    		ReplacePlayerMovement();
	    		
	    		HandleJoystickInGame();	    			    	
	    	}    	
    	}
    	catch (Exception ex)
    	{
    		System.out.println("Joypad mod unhandled exception caught! " + ex.toString());
    	}

	}
	
	private static void HandleGuiPreRender()
	{
		if (mc.currentScreen == null || ControllerSettings.inputEnabled == false)
    		return;	
    	
		// update mouse coordinates
		joypadMouse.getX();
    	joypadMouse.getY();
		
    	while (Controllers.next())
    	{
    		if (joypadMouse.leftButtonHeld && !ControllerSettings.joyBindAttack.isPressed())
    			joypadMouse.leftButtonUp();
    		
    		if (joypadMouse.rightButtonHeld && !ControllerSettings.joyBindUseItem.isPressed())
    			joypadMouse.rightButtonUp();
    		
    		if (ControllerSettings.joyBindInventory.wasPressed())
			 {
				 System.out.println("Inventory control pressed");
			 
				 if (mc.thePlayer != null)
					 mc.thePlayer.closeScreen();
				 else
				 {
					 // backup
					 JoypadMod.obfuscationHelper.DisplayGuiScreen(null);					 				
					 mc.setIngameFocus();
				 }
			 }
    		else if (ControllerSettings.joyBindAttack.wasPressed())
    		{    	
    			joypadMouse.leftButtonDown();

    		}
    		else if (ControllerSettings.joyBindUseItem.wasPressed())
    		{
    			joypadMouse.rightButtonDown();

    		}
    	}
    	
		// This call here re-points the mouse position that Minecraft picks up to 
		// determine if it should do the Hover over button effect.
		joypadMouse.hack_mouseXY(joypadMouse.mcX, joypadMouse.mcY);			
		
		if (joypadMouse.leftButtonHeld || joypadMouse.rightButtonHeld)
		{
			joypadMouse.gui_mouseDrag(joypadMouse.x,joypadMouse.y); 
			VirtualMouse.hack_mouseButton( joypadMouse.leftButtonHeld ? 0 : 1 );
		}
	}
	
	private static void HandleGuiPostRender()
    {      
    
    	if (mc.currentScreen == null || ControllerSettings.inputEnabled == false)
    		return; 
    	
		int x = joypadMouse.x;
		int y = joypadMouse.y;
    	
    	Gui.drawRect(x-3, y, x + 4, y+1, reticalColor); 
    	Gui.drawRect(x, y-3, x+1, y+4, reticalColor);    	    	   
    }
	
	private static boolean attacking = false; 
    private static boolean using = false;
    private static boolean sneaking = false;
    private static int attackKeyCode = JoypadMod.obfuscationHelper.KeyBindCodeHelper(mc.gameSettings.keyBindAttack);
	private static int useKeyCode =  JoypadMod.obfuscationHelper.KeyBindCodeHelper(mc.gameSettings.keyBindUseItem);
	private static int sneakKeyCode = JoypadMod.obfuscationHelper.KeyBindCodeHelper(mc.gameSettings.keyBindSneak);
	    
    private static void HandleJoystickInGame()
    {
		while (Controllers.next())
		{       	      			
			if (using && !ControllerSettings.joyBindUseItem.isPressed())
			{
				System.out.println("Setting using to false");
				using = false;
				KeyBinding.setKeyBindState(useKeyCode, false);				  			
			}
			
			if (attacking && !ControllerSettings.joyBindAttack.isPressed())
   			{
				 System.out.println("Setting attacking to false");
   				 attacking = false;
   				 KeyBinding.setKeyBindState(attackKeyCode, false);
   			}
			
			if (sneaking && !ControllerSettings.joyBindSneak.isPressed())
			{
				 System.out.println("Setting sneaking to false");
   				 sneaking = false;
   				 mc.thePlayer.movementInput.sneak = false;
   				 KeyBinding.setKeyBindState(sneakKeyCode, false);
			}
 			 
			 if (ControllerSettings.joyBindAttack.wasPressed())
			 {
				 System.out.println("Setting attacking to true");
				 VirtualMouse.leftClick();				
				 KeyBinding.setKeyBindState(attackKeyCode, true);
				 attacking = true;
			 }
			 else if (ControllerSettings.joyBindUseItem.wasPressed())
    		 {	    			
				System.out.println("Setting using to true");
				KeyBinding.setKeyBindState(useKeyCode, true);    			
    			using = true;
    		 }	        			     			     			    			 
			 else if (ControllerSettings.joyBindInventory.wasPressed())
    		 {
				 System.out.println("Inventory control pressed");
				 int code = JoypadMod.obfuscationHelper.KeyBindCodeInventory();
				 KeyBinding.onTick(code);				 				    
    		 } 
			 else if (ControllerSettings.joyBindNextItem.wasPressed())
			 {
				 System.out.println("NextItem pressed");
				 mc.thePlayer.inventory.changeCurrentItem(-1);
			 }
			 else if (ControllerSettings.joyBindPrevItem.wasPressed())
			 {
				 System.out.println("PrevItem pressed");
				 mc.thePlayer.inventory.changeCurrentItem(1);
			 }
			 else if (ControllerSettings.joyBindMenu.wasPressed())
			 {
				 if (mc.currentScreen != null)
				 {
					 JoypadMod.obfuscationHelper.DisplayGuiScreen(null);					 
					 mc.setIngameFocus();
				 }
				 else
					 mc.displayInGameMenu();
			 }
			 else if (ControllerSettings.joyBindDrop.wasPressed())
			 {
				 // TODO: add option to drop more than 1 item
				 mc.thePlayer.dropOneItem(true);
			 }    
			 else if (ControllerSettings.joyBindSneak.wasPressed())
			 {
				 System.out.println("Setting sneaking to true");
				 mc.thePlayer.movementInput.sneak = true;
				 KeyBinding.onTick(sneakKeyCode);
				 KeyBinding.setKeyBindState(sneakKeyCode, true);
				 sneaking = true;
			 } 
		}
		
		// Read joypad movements then rotate the player based on the movements
		VirtualMouse.updateCameraAxisReading();    		
        mc.thePlayer.setAngles(VirtualMouse.deltaX, VirtualMouse.deltaY);
    }
    
    private static boolean ReplacePlayerMovement()
    {
    	try
		{    		    	
    		// modify the in game player movement
    		EntityClientPlayerMP player = mc.thePlayer;
    		if (player != null && !(player.movementInput instanceof ControllerMovementInput))
	    	{	    			    			   
				ControllerMovementInput movementInput = new ControllerMovementInput();
				player.movementInput = movementInput; 
				System.out.println("Replaced Player Movement with mod movement");				    		
	    	}    		    	
    		return true;
		} catch (Exception ex)
		{
			System.out.println("Failed to replace Minecraft objects with modded objects. Error: " + ex.toString());
		}
    	return false;       	
    }
    
    private static void ReplaceControlScreen(GuiControls gui)
	{		
		if (!(mc.currentScreen instanceof JoypadConfigMenu))
		{
			try
			{				
				System.out.println("Replacing control screen");
				String[] names = JoypadMod.obfuscationHelper.GetMinecraftVarNames("parentScreen");
				GuiScreen parent = ObfuscationReflectionHelper.getPrivateValue(GuiControls.class, (GuiControls)gui, names[0], names[1]);
				JoypadMod.obfuscationHelper.DisplayGuiScreen(new JoypadConfigMenu(parent, mc.gameSettings));
			}
			catch (Exception ex)
			{
				System.out.println("Failed to get parent of options gui.  aborting");
				return;
			}					
		}
	}

	public static boolean InGameCheckNeeded()
    {
		if (!CheckIfModEnabled() || mc.currentScreen != null || mc.thePlayer == null)
		{
    		return false;
		}
    	
    	return true;
    }
    
    public static boolean InGuiCheckNeeded()
    {
    	if (!CheckIfModEnabled() || mc.currentScreen == null)
    	{
    		return false;
    	}
    	    	
    	return true;
    }
    
    public static boolean CheckIfModEnabled()
    {
    	if (mc == null || !ControllerSettings.inputEnabled || ControllerSettings.joystick == null)
    	{
    		return false;    		
    	}
    	
    	return true;    	
    }
}
