package com.shiny.joypadmod.inputevent;

import org.lwjgl.input.Controllers;

public class AxisInputEvent implements ControllerInputEvent{

	int controllerNumber;
	int axisNumber;
	float threshold;
	float deadzone;
	
	boolean pressed = false; //used for input limiting
	
	public AxisInputEvent(int controllerId, int axisNumber, float threshold, float deadzone) 
	{
		this.controllerNumber = controllerId;
		this.axisNumber = axisNumber;
		this.threshold = threshold;
		this.deadzone = deadzone;
	}
	
	@Override
	public EventType getEventType() {
		return EventType.AXIS;
	}

	@Override
	public boolean isPressed() {
		boolean result = false;
		
		if (threshold > 0) 
		{
			result = getAnalogReading() >= threshold;
		} else 
		{
			result = getAnalogReading() <= threshold;
		}
		
		if (!result) 
			pressed = false;
		return result;
	}
	
	@Override
	public boolean wasPressed() {
		boolean isValidEventType = false;
		try {
			isValidEventType = Controllers.isEventAxis() && Controllers.getEventSource().getIndex() == controllerNumber;
		} catch (NullPointerException e) {
			//cant determine event type
		}
		if (isValidEventType && isPressed()) {
			if (!pressed) {
				pressed = true;
				return true;
			}
			return false;
		}
		isPressed();
		return false;
	}
		
	@Override
	public float getAnalogReading() 
	{
		if (axisNumber < Controllers.getController(controllerNumber).getAxisCount()) 
		{
			return Controllers.getController(controllerNumber).getAxisValue(axisNumber);
		}
		return 0.0f;
	}

	@Override
	public float getDeadZone() {		
		if (axisNumber < Controllers.getController(controllerNumber).getAxisCount()) {
			return Controllers.getController(controllerNumber).getDeadZone(axisNumber);
		}
		return 0.0f;
	}

	@Override
	public String getName() {
		if (axisNumber < Controllers.getController(controllerNumber).getAxisCount()) {
			return Controllers.getController(controllerNumber).getAxisName(axisNumber);
		}
		return "Unknown";
	}
	
	@Override 
	public String toString() {
		return new StringBuilder()
			.append("Event: ").append(getName())
			.append(" Type: ").append(getEventType())
			.append(" Threshold: ").append(threshold)
			.append(" Current value: ").append(getAnalogReading())
			.append(" Is pressed: ").append(isPressed())
			.toString();
	}
	
	@Override
	public int getEventIndex() {		
		return axisNumber;
	}
	
	@Override
	public float getThreshold() {		
		return threshold;
	}

	@Override
	public void setDeadZone() {
		if (axisNumber < Controllers.getController(controllerNumber).getAxisCount()) {
			Controllers.getController(controllerNumber).setDeadZone(this.axisNumber, this.deadzone);
		} 
	}

	@Override
	public String getDescription() {
		return new StringBuilder()		
		.append(getName())
		.append(" ").append(getDirection(getThreshold()))
		.toString();
	}
	
	private String getDirection(float reading) {
		if (reading > 0) {
			return "+";
		}
		return "-";
	}
}
