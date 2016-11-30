package com.shiny.joypadmod.devices;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Controllers;

public class LWJGL extends InputLibrary {
	
	LWJGLDevice theDevice;
	
	@Override
	public void create() throws LWJGLException {
		Controllers.create();
		theDevice = new LWJGLDevice(0);

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
		theDevice.setIndex(Controllers.getEventSource().getIndex());
		return theDevice;
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

}
