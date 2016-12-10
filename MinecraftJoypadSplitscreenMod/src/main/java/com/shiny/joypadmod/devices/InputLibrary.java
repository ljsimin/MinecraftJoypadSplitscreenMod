package com.shiny.joypadmod.devices;

import org.lwjgl.LWJGLException;

public abstract class InputLibrary {
	
	
	public abstract void create() throws Exception;
	public abstract Boolean isCreated();
	public abstract void clearEvents();
	public abstract InputDevice getController(int index);
	public abstract InputDevice getCurrentController();
	public abstract int getControllerCount();
	public abstract InputDevice getEventSource();
	public abstract int getEventControlIndex(); // todo change this name
	public abstract Boolean isEventButton();
	public abstract Boolean isEventAxis();
	public abstract Boolean isEventPovX();
	public abstract Boolean isEventPovY();
	public abstract Boolean next(); // returns true if there is still an event to process
	public abstract void poll();
	public abstract Boolean wasDisconnected();
	public abstract Boolean wasConnected();
	
		
}
