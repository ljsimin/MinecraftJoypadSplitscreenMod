package com.shiny.joypadmod.devices;

public abstract class InputDevice {
	
	protected int myIndex;
	
	public InputDevice(int index) { myIndex = index; }
	
	public abstract String getName();
	public int getIndex() { return myIndex; }
	public abstract int getButtonCount();
	public abstract int getAxisCount();
	public abstract float getAxisValue(int axisIndex);
	public abstract String getAxisName(int index);
	public abstract float getDeadZone(int index);
	public abstract String getButtonName(int index);
	public abstract Boolean isButtonPressed(int index);
	public abstract Float getPovX();
	public abstract Float getPovY();
	public abstract void setDeadZone(int axisIndex, float value);
	
}
