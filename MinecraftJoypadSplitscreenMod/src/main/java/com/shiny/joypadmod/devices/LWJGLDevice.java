package com.shiny.joypadmod.devices;

import org.lwjgl.input.Controllers;

public class LWJGLDevice extends InputDevice {

	public LWJGLDevice(int index) {
		super(index);		
	}

	@Override
	public String getName() {		
		return Controllers.getController(myIndex).getName();
	}

	@Override
	public int getButtonCount() {
		return Controllers.getController(myIndex).getButtonCount();
	}

	@Override
	public int getAxisCount() {
		return Controllers.getController(myIndex).getAxisCount();
	}

	@Override
	public float getAxisValue(int axisIndex) {
		return Controllers.getController(myIndex).getAxisValue(axisIndex);
	}

	@Override
	public String getAxisName(int index) {
		return Controllers.getController(myIndex).getAxisName(index);
	}

	@Override
	public float getDeadZone(int index) {
		return Controllers.getController(myIndex).getDeadZone(index);
	}

	@Override
	public String getButtonName(int index) {
		return Controllers.getController(myIndex).getButtonName(index);
	}

	@Override
	public Boolean isButtonPressed(int index) {
		return Controllers.getController(myIndex).isButtonPressed(index);
	}

	@Override
	public Float getPovX() {
		return Controllers.getController(myIndex).getPovX();
	}

	@Override
	public Float getPovY() {
		return Controllers.getController(myIndex).getPovY();
	}

	@Override
	public void setDeadZone(int axisIndex, float value) {
		Controllers.getController(myIndex).setDeadZone(axisIndex, value);
	}

}
