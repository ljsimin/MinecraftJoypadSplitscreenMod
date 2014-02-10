package com.shiny.joypadmod;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;





//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Mouse;






import cpw.mods.fml.common.ObfuscationReflectionHelper;

public class VirtualMouse {
	
	// last delta movement of axis 
	public static float deltaX;
	public static float deltaY;	
	
	// last virtual mouse position
	public int x = 0;
	public int y = 0;
	
	// values that Minecraft expects when reading the actual mouse
	public int mcX = 0;
	public int mcY = 0;	
	
	public boolean leftButtonHeld = false;
	public boolean rightButtonHeld = false;
	
	public boolean usingAxisCoordinates = true;
	public static boolean debug = false;
	public float sensitivity = 0.07f;
	
	private static Minecraft mc = Minecraft.getMinecraft();	
	
	public VirtualMouse(){}	
	
	public int getX()
	{
		if (this.usingAxisCoordinates)
			this.setMouseCoordinatesWithController();		
		return this.x;
	}
	
	public int getY()
	{
		if (this.usingAxisCoordinates)
			this.setMouseCoordinatesWithController();
		return this.y;
	}
	
	public void leftButtonDown()
	{
		if (!this.leftButtonHeld)
		{
			this.gui_mouseDown(x, y, 0);
			VirtualMouse.hack_mouseButton(0);
			this.leftButtonHeld = true;
		}			
	}	
	
	public void leftButtonUp()
	{
		if (this.leftButtonHeld)
		{
			this.gui_mouseUp(x, y, 0);
			this.leftButtonHeld = false;
		}
	}
	
	public void rightButtonDown()
	{
		if (!this.rightButtonHeld)
		{
			this.gui_mouseDown(x, y, 1);
			VirtualMouse.hack_mouseButton(1);
			this.rightButtonHeld = true;
		}		
	}
	
	public void rightButtonUp()
	{
		if (this.rightButtonHeld)
		{
			this.gui_mouseUp(x, y, 1);
			this.rightButtonHeld = false;
		}
	}
	
	public void UnpressButtons()
	{
		leftButtonUp();
		rightButtonUp();
	}
	
	// used for in game clicking
	public static void leftClick()
	{		
		game_leftClick();
	}
	
	// this is the equivalent of moving the mouse around on your joypad
	public static void updateCameraAxisReading()
	{
		float var3 = mc.gameSettings.mouseSensitivity * 0.6F + 0.2F;
		float var4 = var3 * var3 * var3 * 8.0F;
		float var8 = Math.abs(ControllerSettings.joyCameraXplus.getAnalogReading()) 
				> Math.abs(ControllerSettings.joyCameraXminus.getAnalogReading()) 
				? ControllerSettings.joyCameraXplus.getAnalogReading() 
				: ControllerSettings.joyCameraXminus.getAnalogReading();
        float var9 = Math.abs(ControllerSettings.joyCameraYplus.getAnalogReading()) 
        		> Math.abs(ControllerSettings.joyCameraYminus.getAnalogReading()) 
        		? ControllerSettings.joyCameraYplus.getAnalogReading() 
        		: ControllerSettings.joyCameraYminus.getAnalogReading();
        deltaX = (float) (Math.round(var8 * (float)ControllerSettings.joyCameraSensitivity) * var4); //* var7;
        deltaY = (float) (Math.round(var9 * (float)ControllerSettings.joyCameraSensitivity) * var4 * -1.0F);// * var7; 
        if (debug)
        {
        	//logger.debug("Camera deltaX: " + deltaX + " Camera deltaY: " + deltaY);
        	System.out.println("Camera deltaX: " + deltaX + " Camera deltaY: " + deltaY);
        }	        
	}	
	
	private void setMouseCoordinatesWithController()
	{				
		final ScaledResolution scaledResolution = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
		
		//System.out.println(scaledResolution.getScaledWidth() + " " + scaledResolution.getScaledHeight() + " " + scaledResolution.getScaleFactor());
		
		updateCameraAxisReading();
		
		int dx = (int)(sensitivity * deltaX);
		int dy = (int)(sensitivity * deltaY) * (mc.gameSettings.invertMouse ? 1 : -1);
		x += dx;
		y += dy;
		
		if (x < 0)
			x = 0;
		if (y < 0)
			y = 0;
		if (x > scaledResolution.getScaledWidth())
			x = scaledResolution.getScaledWidth() - 5;
		if (y > scaledResolution.getScaledHeight())
			y = scaledResolution.getScaledHeight() - 5;
						
		if (debug)
		{
			//logger.debug("Virtual Mouse x: " + x + " y: " + y);    	
			System.out.println("Virtual Mouse x: " + x + " y: " + y);
		}
				
		mcY = mc.displayHeight - (int)(y * scaledResolution.getScaleFactor());		
		mcX = x * scaledResolution.getScaleFactor();		
	}
	
	String[] eventButtonNames = JoypadMod.obfuscationHelper.GetMinecraftVarNames("eventButton");
	String[] lastMouseEventNames = JoypadMod.obfuscationHelper.GetMinecraftVarNames("lastMouseEvent");
	
	// todo: look at this variable!!?!
	int gMdParam = 0;
	private void gui_mouseDown( int rawX, int rawY, int button)
	{	    
		// todo: this function is riddled with bad names 
		if (gMdParam  == -1)
		{
			System.out.println("gui_mouseDown disabled due to earlier error");
			return;
		}
		String[] names = JoypadMod.obfuscationHelper.GetMinecraftVarNames("mouseClicked");		
		Method mouseClicked = null;
		
		@SuppressWarnings("rawtypes")
		Class[] params = new Class[] { int.class, int.class, int.class };
	    
		//System.out.println("Calling mouseClicked");
		try
		{
		    ObfuscationReflectionHelper.setPrivateValue(GuiScreen.class, (GuiScreen)mc.currentScreen, button, eventButtonNames[0], eventButtonNames[1] );
		    ObfuscationReflectionHelper.setPrivateValue(GuiScreen.class, (GuiScreen)mc.currentScreen, Minecraft.getSystemTime(), lastMouseEventNames[0], lastMouseEventNames[1]);
		    
		    try
		    {
		    	mouseClicked = GuiScreen.class.getDeclaredMethod(names[gMdParam], params);
		    }catch (Exception ex)
		    { 
		    	mouseClicked = GuiScreen.class.getDeclaredMethod(names[1], params);
		    	gMdParam=1;
		    }
		
		    mouseClicked.setAccessible(true);
		    mouseClicked.invoke((Object)mc.currentScreen, rawX, rawY, button);	
		}
		catch (Exception ex)
		{
			System.out.println("Failed calling " + names[0] + "(" + names[1] + ") : " + ex.toString());
			gMdParam= -1;
		}
	}

	int muParam = 0;
	private void gui_mouseUp(int rawX, int rawY, int button) 
	{
		if (muParam == -1)
		{
			System.out.println("gui_mouseUp disabled due to earlier error");
			return;
		}
		
		String[] names = JoypadMod.obfuscationHelper.GetMinecraftVarNames("mouseMovedOrUp");

	    Method mouseMovedOrUp = null;
		@SuppressWarnings("rawtypes")
		Class[] params = new Class[] { int.class, int.class, int.class };
		//System.out.println("Calling mouseUp");
		
		try
		{
			try {
				mouseMovedOrUp = GuiScreen.class.getDeclaredMethod(names[muParam], params);
			}catch (Exception ex)
			{
				mouseMovedOrUp = GuiScreen.class.getDeclaredMethod(names[1], params);
				muParam = 1;
			}
			mouseMovedOrUp.setAccessible(true);
			mouseMovedOrUp.invoke((Object)mc.currentScreen, rawX, rawY, button);
		}
		catch (Exception ex)
		{
			System.out.println("Failed calling " + names[0] + "(" + names[1] + ") : " + ex.toString());
			muParam = -1;
		}
	}

	// todo: again!!
	int mdParam = 0;
	public void gui_mouseDrag(int rawX, int rawY) 
	{
		if (mdParam == -1)
		{
			System.out.println("gui_mouseDrag disabled due to earlier error");
			return;
		}
		
		long lastEvent = -1;
		int eventButton = -1;
		String[] names = JoypadMod.obfuscationHelper.GetMinecraftVarNames("mouseClickMove");
	    Method mouseButtonMove = null;
		@SuppressWarnings("rawtypes")
		Class[] params = new Class[] { int.class, int.class, int.class, long.class };	
		//System.out.println("Calling mouseDrag");
		
		try
		{			
			eventButton = ObfuscationReflectionHelper.getPrivateValue(GuiScreen.class, (GuiScreen)mc.currentScreen, eventButtonNames[0], eventButtonNames[1] );
			lastEvent = ObfuscationReflectionHelper.getPrivateValue(GuiScreen.class, (GuiScreen)mc.currentScreen, lastMouseEventNames[0], lastMouseEventNames[1]);								
		}
		catch (Exception ex)
		{
			System.out.println("Failed calling ObfuscationReflectionHelper" + ex.toString());
			if (lastEvent == -1)
				lastEvent = 100;
			eventButton = 0;
		}
		long var3 = Minecraft.getSystemTime() - lastEvent;
		
		try
		{
			try{
				mouseButtonMove = GuiScreen.class.getDeclaredMethod(names[mdParam], params);
			}catch (Exception ex)
			{
				mouseButtonMove = GuiScreen.class.getDeclaredMethod(names[1], params);
				mdParam = 1;				
			}
			mouseButtonMove.setAccessible(true);
			mouseButtonMove.invoke((Object)mc.currentScreen, rawX, rawY, eventButton, var3);
		}
		catch (Exception ex)
		{
			System.out.println("Failed calling " + names[0] + "(" + names[1] + ") : " + ex.toString());
			mdParam = -1;
		}
				
	}
		
	// oh minecraft why did you have to mess with the clicking function?
	private static int glcParam = 0;
	private static void game_leftClick()
	{		
		if (glcParam == -1)
		{
			System.out.println("leftClick disabled due to earlier error");
			return;
		}
			
		String[] names = { "clickMouse", "func_71402_c" };
		//String actualName = "clickMouse"; // this might be wrong
	   // String obscuredName = "func_147116_af";
	    System.out.println("Calling " + names[0] + "(" + names[1] + ")");
	    @SuppressWarnings("rawtypes")
		Class[] params = new Class[] { int.class };
		Method clickLeftMouse;
    	try
		{    	
    		try
    		{
    			clickLeftMouse = Minecraft.class.getDeclaredMethod(names[glcParam], params);
    		}catch (Exception ex)
        	{
        		clickLeftMouse = Minecraft.class.getDeclaredMethod(names[1], params);
        		glcParam = 1;
        	}		    	
			clickLeftMouse.setAccessible(true);
			clickLeftMouse.invoke((Object)mc, 0);
		}
    	catch (Exception ex)
    	{
    		System.out.println("Failed calling " + names[0] + "(" + names[1] + ") : " + ex.toString());
    		glcParam = -1;
    	}
	}
		
	public boolean hack_mouseXY(int x, int y)
	{	
		//System.out.println("Hacking mouse position to x:" + x + " y:" + y);
		try
		{			
			Field xField = Mouse.class.getDeclaredField("x");
			Field yField = Mouse.class.getDeclaredField("y");
			 			
			xField.setAccessible(true);
			yField.setAccessible(true);
			xField.setInt(null, x);
			yField.setInt(null, y);				
		}
		catch (Exception ex)
		{
			//logger.error("Failed calling Mouse fields: " + ex.toString());
			System.out.println("Failed calling Mouse fields: " + ex.toString());
			return false;
		}
		
		return true;
	}
	
	public static boolean hack_mouseButton(int button)
	{		
		System.out.println("Hacking mouse button: " + button);
		try
		{
			Field buttonField = Mouse.class.getDeclaredField("buttons");
			buttonField.setAccessible(true);
			
			// left Button
			if (button == 0)				
				((ByteBuffer)buttonField.get(null)).put(0,(byte) 1);
			//right Button
			else if (button == 1)
				((ByteBuffer)buttonField.get(null)).put(1,(byte) 1);				
		}
		catch (Exception ex)
		{
			//logger.error("Failed calling Mouse fields: " + ex.toString());
			System.out.println("Failed calling Mouse fields: " + ex.toString());
			return false;
		}
		
		return true;
	}

}
