package com.shiny.joypadmod.devices;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Controllers;

public class LWJGLibrary extends InputLibrary {
	
	LWJGLDeviceWrapper theDevice;
	LWJGLDeviceWrapper tempDevice;
	
	@Override
	public void create() throws LWJGLException {
		Controllers.create();
		theDevice = new LWJGLDeviceWrapper(0);
		tempDevice = new LWJGLDeviceWrapper(0);
	}

	@Override
	public Boolean isCreated() {
		return Controllers.isCreated();		
	}

	@Override
	public void clearEvents() {
		Controllers.clearEvents();
	}

	@Override
	public InputDevice getController(int index) {
		theDevice.setIndex(index);
		return theDevice;
	}

	@Override
	public int getControllerCount() {
		return Controllers.getControllerCount();
	}

	@Override
	public InputDevice getEventSource() {
		tempDevice.setIndex(Controllers.getEventSource().getIndex());		
		return tempDevice;
	}

	@Override
	public int getEventControlIndex() {
		return Controllers.getEventControlIndex();
	}

	@Override
	public Boolean isEventButton() {
		return Controllers.isEventButton();
	}

	@Override
	public Boolean isEventAxis() {
		return Controllers.isEventAxis();
	}

	@Override
	public Boolean isEventPovX() {
		return Controllers.isEventPovX();
	}

	@Override
	public Boolean isEventPovY() {
		return Controllers.isEventPovY();
	}

	@Override
	public Boolean next() {
		return Controllers.next();
	}

	@Override
	public void poll() {
		return; // polling happens within Minecraft itself so no need to do our own		
	}
	
	@Override
	public Boolean wasDisconnected() {
		return false; // not supported in this library
	}

	@Override
	public Boolean wasConnected() {
		return false; // not supported in this library
	}

	@Override
	public InputDevice getCurrentController() {
		return theDevice;
	}

}
