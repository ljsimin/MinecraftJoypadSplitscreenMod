package com.shiny.joypadmod;

import java.io.File;
import org.lwjgl.input.Controllers;

import net.minecraft.client.Minecraft;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

import cpw.mods.fml.relauncher.Side;

/* 1.7.2
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent.ClientTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.RenderTickEvent;
import cpw.mods.fml.common.gameevent.TickEvent.WorldTickEvent;*/

// 1.6.4
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.registry.TickRegistry;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

@Mod(modid = JoypadMod.MODID, name = JoypadMod.NAME, version = JoypadMod.VERSION)
@NetworkMod(serverSideRequired = false)
public class JoypadMod
{
    public static final String MODID = "joypadmod";
    public static final String VERSION = "1.6.4";
    public static final String NAME = "Joypad / SplitScreen Mod"; 
        
    public static ControllerSettings controllerSettings;
    public int JoyNo = -1;    
    
    @EventHandler
    public void preInit(FMLPreInitializationEvent event) 
    {
		readConfigFile(event.getSuggestedConfigurationFile());			
	}
    
    @EventHandler
    public void init(FMLInitializationEvent event)
    {   
    	initialize();
    	if (ControllerSettings.inputEnabled)
    	{
    		TickRegistry.registerTickHandler(new RenderTickHandler(), Side.CLIENT);
    	}
    }
    
    public void initialize()
    {
    	controllerSettings = new ControllerSettings();
    	if (JoyNo < -1)
    	{
    		System.out.println("Controller disabled");
    		ControllerSettings.inputEnabled = false;
    	}
    	else
    	{
    		int nControllers = ControllerSettings.DetectControllers();    	
	    	if (nControllers > 0)
	    	{
	    		int selectedController = 0;
	    		if (JoyNo >= 0 && JoyNo < ControllerSettings.DetectControllers())
	    			selectedController = JoyNo;
	    		ControllerSettings.SetController(selectedController);
	    		Controllers.clearEvents();	    		
	    	}
    	}    	    	        	   
    	
    }
    
    public void readConfigFile(File configFile)
    {
    	Configuration config = new Configuration(configFile);
		
		config.load();
		
		String userName = "unknown";
		 
		if (Minecraft.getMinecraft() != null && Minecraft.getMinecraft().getSession() != null)
			userName = Minecraft.getMinecraft().getSession().getUsername();
		Property joyNo = config.get("Joypad-" + userName, "JoyNo", -1);
		System.out.println(userName + "'s JoyNo == " + joyNo.getInt());
		if (joyNo.getInt() != -1)
			JoyNo = joyNo.getInt();
		if (joyNo.getInt() == -2)
			JoyNo = -2;
	
		config.save();
    }
                          
      
}

