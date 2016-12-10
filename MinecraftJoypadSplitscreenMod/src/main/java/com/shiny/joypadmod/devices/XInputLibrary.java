package com.shiny.joypadmod.devices;

import java.util.LinkedList;

import com.ivan.xinput.XInputAxesDelta;
import com.ivan.xinput.XInputButtonsDelta;
import com.ivan.xinput.XInputComponentsDelta;
import com.ivan.xinput.XInputDevice;
import com.ivan.xinput.XInputDevice14;
import com.ivan.xinput.enums.XInputAxis;
import com.ivan.xinput.enums.XInputButton;
import com.ivan.xinput.exceptions.XInputNotLoadedException;
import com.ivan.xinput.listener.SimpleXInputDeviceListener;
import com.ivan.xinput.listener.XInputDeviceListener;
import com.shiny.joypadmod.helpers.LogHelper;
import com.shiny.joypadmod.inputevent.AxisInputEvent;
import com.shiny.joypadmod.inputevent.ControllerInputEvent;

public class XInputLibrary extends InputLibrary {
	
	private Boolean created = false;
	float axisSignalThreshold = 0.5f; // the point at which we will trigger an axis event if it meets the threshold	
	protected Boolean xInput14 = false;
	
	public Boolean recentlyDisconnected = false;
	public Boolean recentlyConnected = false;
	
	XInputDeviceWrapper theDevice;
	
	static LinkedList events = new LinkedList();
	
	// used for event notification for polling
	int lastEvent = -1;
	int lastEventControlIndex = -1;
	
	public class InputEvent {
		int eventType = -1; // 0 = axis event 1 = button press
		int eventControlIndex = -1; // button or axis number
		public InputEvent(int event, int controlIndex)
		{
			eventType = event;
		    eventControlIndex = controlIndex;
		}
	};

	@Override
	public void create() throws Exception {
		if (XInputDevice14.isAvailable())
			xInput14 = true;
		else if (!XInputDevice.isAvailable())
			throw new Exception("XInput is not available on system.");
		
		theDevice = new XInputDeviceWrapper(0);
		theDevice.setIndex(0, xInput14);
		created = true;
	}

	@Override
	public Boolean isCreated() {
		return created;
	}


	@Override
	public void clearEvents() {
		events.clear();
	}

	@Override
	public InputDevice getController(int index) {
		if (index != theDevice.theDevice.getPlayerNum())
		{
			theDevice.theDevice.removeListener(listener);
			theDevice.setIndex(index, xInput14);
			theDevice.theDevice.addListener(listener);			
		}
		return theDevice;
	}

	@Override
	public int getControllerCount() {
		return 4;
	}

	@Override
	public InputDevice getEventSource() {
		return theDevice;
	}

	@Override
	public int getEventControlIndex() {
		return lastEventControlIndex;
	}

	@Override
	public Boolean isEventButton() {
		return lastEvent == 0;
	}

	@Override
	public Boolean isEventAxis() {
		return lastEvent == 1;
	}

	@Override
	public Boolean isEventPovX() {
		return false;
	}

	@Override
	public Boolean isEventPovY() {
		return false;
	}

	@Override
	public Boolean next() {
	
		if (events.isEmpty())
			return false;
		InputEvent event = (InputEvent)events.removeFirst();
		lastEvent = event.eventType;
		lastEventControlIndex = event.eventControlIndex;
		return true;
	}


	@Override
	public void poll() {
		theDevice.theDevice.poll();
				
		for (int i = 0; i < 6; i++)
		{
			if (Math.abs(theDevice.getAxisValue(i)) > axisSignalThreshold)
			{
				events.add(new InputEvent(1, i));
			}
		}			
		
	}
	
	XInputDeviceListener listener = new SimpleXInputDeviceListener() {
	    @Override
	    public void connected() {
	        LogHelper.Info("Connection message received");
	        recentlyConnected = true;
	    }

	    @Override
	    public void disconnected() {
	    	LogHelper.Info("Disconnection message received");
	    	recentlyDisconnected = true;
	    }

	    @Override
	    public void buttonChanged(final XInputButton button, final boolean pressed) {
	    	events.add(new InputEvent(0, button.ordinal()));
	    	LogHelper.Info(button.name() + (pressed ? " pressed" : " released"));
	        // the given button was just pressed (if pressed == true) or released (pressed == false)
	    }
	};
	
	@Override
	public Boolean wasDisconnected() {
		if (recentlyDisconnected)
		{
			recentlyDisconnected = false;
			return true;		
		}
		return false;
	}

	@Override
	public Boolean wasConnected() {
		if (recentlyConnected)
		{
			recentlyConnected = false;
			return true;		
		}
		return false;
	}

	@Override
	public InputDevice getCurrentController() {

		return theDevice;
	}

}
