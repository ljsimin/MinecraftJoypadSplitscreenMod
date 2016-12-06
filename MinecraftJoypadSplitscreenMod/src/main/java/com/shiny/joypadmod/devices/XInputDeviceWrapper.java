package com.shiny.joypadmod.devices;

import com.ivan.xinput.XInputDevice;
import com.ivan.xinput.XInputDevice14;
import com.ivan.xinput.enums.XInputAxis;
import com.ivan.xinput.enums.XInputButton;
import com.ivan.xinput.exceptions.XInputNotLoadedException;
import com.shiny.joypadmod.helpers.LogHelper;

public class XInputDeviceWrapper extends InputDevice {

	public XInputDevice theDevice;
	public Boolean xInput14 = false;
	
/*	public enum XInputButtonW {
		A =XInputButton.A, B, X, Y,
		BACK, START,
		LEFT_SHOULDER, RIGHT_SHOULDER,
		LEFT_THUMBSTICK, RIGHT_THUMBSTICK,
		DPAD_UP, DPAD_DOWN, DPAD_LEFT, DPAD_RIGHT,
		GUIDE_BUTTON, UNKNOWN;
	} */
	
	float[] deadZones = new float[] { 0.15f,0.15f,0.15f,0.15f,0.15f,0.15f };
	
	public XInputDeviceWrapper(int index) {
		super(index);

	}

	@Override
	public String getName() {
		String name = "XInput Device";
		if (!theDevice.isConnected())
			name += " [Disconnected]";
		return name;		
	}

	@Override
	public int getButtonCount() {
		return 15;
	}

	@Override
	public int getAxisCount() {
		return 6;			
	}

	@Override
	public float getAxisValue(int axisIndex) {
		float value = theDevice.getComponents().getAxes().get(XInputAxis.values()[axisIndex]); 		

		if (Math.abs(value) > deadZones[axisIndex])
		{
			XInputAxis axis = XInputAxis.values()[axisIndex];
			if (axis == XInputAxis.LEFT_THUMBSTICK_Y || axis == XInputAxis.RIGHT_THUMBSTICK_Y)
					value *= -1;
					
			return value;
		}
		
		return 0;	
	}

	@Override
	public String getAxisName(int index) {

		String name = XInputAxis.values()[index].toString();
		String ret;
		if (name.length() > 11)
		{
			ret = String.format("%s %s", name.substring(0, 9), name.charAt(name.length()-1));			
		}
		else
			ret = name;
		return ret;
	}

	@Override
	public float getDeadZone(int index) {
		return deadZones[index];
	}

	@Override
	public String getButtonName(int index) {
		return XInputButton.values()[index].toString();
	}

	@Override
	public Boolean isButtonPressed(int index) {
		
		return theDevice.getComponents().getButtons().isPressed(XInputButton.values()[index]);
	}

	@Override
	public Float getPovX() {
		
		if (theDevice.getDelta().getButtons().isPressed(XInputButton.DPAD_LEFT))
			return -1.0f;
		if (theDevice.getDelta().getButtons().isPressed(XInputButton.DPAD_RIGHT))
			return 1.0f;
		return 0f;
	}

	@Override
	public Float getPovY() {
		if (theDevice.getDelta().getButtons().isPressed(XInputButton.DPAD_UP))
			return 1.0f;
		if (theDevice.getDelta().getButtons().isPressed(XInputButton.DPAD_DOWN))
			return -1.0f;
		return 0f;
	}

	@Override
	public void setDeadZone(int axisIndex, float value) {
		deadZones[axisIndex] = value;
	}
	
	protected void setIndex(int index, Boolean useXInput14)
	{
		try {
						
			if (useXInput14)
			{
				xInput14 = true;
				theDevice = XInputDevice14.getDeviceFor(index);
			}
			else 
				theDevice = XInputDevice.getDeviceFor(index);
			
			theDevice.poll();
		} catch (XInputNotLoadedException e) { 
			LogHelper.Fatal("Failed calling setIndex on XInputDevice: " + e.toString());
		}
	}

	@Override
	public Boolean isConnected() {
		return theDevice.isConnected();		
	}

}
