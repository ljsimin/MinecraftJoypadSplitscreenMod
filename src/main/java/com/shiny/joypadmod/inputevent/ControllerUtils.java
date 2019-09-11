package com.shiny.joypadmod.inputevent;

import java.util.HashMap;
import java.util.Map;

import com.shiny.joypadmod.ControllerSettings;
import com.shiny.joypadmod.JoypadMod;
import com.shiny.joypadmod.devices.InputDevice;
import com.shiny.joypadmod.devices.XInputDeviceWrapper;

public class ControllerUtils
{
	private static Map<String, String> joypadNameMap;
	private int mappedControllerIndex;

	public ControllerUtils()
	{
		joypadNameMap = new HashMap<String, String>();
		mappedControllerIndex = -1;
	}

	public void printDeadZones(InputDevice joystick2)
	{
		if (joystick2 != null)
		{
			for (int axisNo = 0; axisNo < joystick2.getAxisCount(); axisNo++)
			{
				JoypadMod.logger.info("Axis " + axisNo + " deadzone: " + joystick2.getDeadZone(axisNo));
			}
		}
	}

	public void printAxisNames(InputDevice joystick2)
	{
		for (int axisNo = 0; axisNo < joystick2.getAxisCount(); axisNo++)
		{
			JoypadMod.logger.info("Axis " + axisNo + ", " + joystick2.getAxisName(axisNo));
		}
	}

	public void printButtonNames(InputDevice joystick2)
	{
		for (int buttonNo = 0; buttonNo < joystick2.getButtonCount(); buttonNo++)
		{			
			JoypadMod.logger.info("Button " + buttonNo + ", " + joystick2.getButtonName(buttonNo));
		}
	}

	public boolean checkJoypadRequirements(InputDevice controller, int requiredButtonCount, int requiredMinButtonCount,
			int requiredAxisCount)
	{
		boolean meetsRequirements = meetsInputRequirements(controller, requiredButtonCount, requiredMinButtonCount,
				requiredAxisCount);
		StringBuilder msg = new StringBuilder("");

		if (!meetsRequirements)
		{
			msg.append("Selected controller ").append(controller.getName()).append(
					" has less than required number of axes or buttons \n").append("Buttons required - ").append(
					requiredButtonCount).append(" , detected - ").append(controller.getButtonCount()).append("\n").append(
					"Axes required - ").append(requiredAxisCount).append(" , detected - ").append(
					controller.getAxisCount()).append("\n").append(
					"Check settings file named 'options.txt' for the correct value of 'joyNo' parameter\n").append(
					"Total number of controllers detected: ").append(ControllerSettings.JoypadModInputLibrary.getControllerCount());
			JoypadMod.logger.info(msg.toString());
		}
		return meetsRequirements;
	}

	public boolean meetsInputRequirements(InputDevice controller, int requiredButtonCount, int requiredMinButtonCount,
			int requiredAxisCount)
	{
		boolean meetsRequirements = true;
		if ((controller.getButtonCount() < requiredMinButtonCount)
				|| (controller.getButtonCount() < requiredButtonCount && controller.getAxisCount() < requiredAxisCount))
		{
			meetsRequirements = false;
		}
		return meetsRequirements;
	}

	public ControllerInputEvent getLastEvent(InputDevice inputDevice, int eventIndex)
	{
		if (ControllerSettings.JoypadModInputLibrary.isEventAxis())
		{
			if (Math.abs(ControllerUtils.getAxisValue(inputDevice, eventIndex)) > 0.75f)
			{
				return new AxisInputEvent(inputDevice.getIndex(), eventIndex, ControllerUtils.getAxisValue(inputDevice, eventIndex),
						inputDevice.getDeadZone(eventIndex));
			}
		}
		else if (ControllerSettings.JoypadModInputLibrary.isEventButton())
		{
			int id = ControllerSettings.JoypadModInputLibrary.getEventControlIndex();
			if (inputDevice.isButtonPressed(id))
			{
				return new ButtonInputEvent(inputDevice.getIndex(), id, 1);
			}
		}
		else if (ControllerSettings.JoypadModInputLibrary.isEventPovX())
		{
			if (Math.abs(inputDevice.getPovX()) > 0.5f)
			{
				return new PovInputEvent(inputDevice.getIndex(), 0, inputDevice.getPovX() / 2);
			}
		}
		else if (ControllerSettings.JoypadModInputLibrary.isEventPovY())
		{
			if (Math.abs(inputDevice.getPovY()) > 0.5f)
			{
				return new PovInputEvent(inputDevice.getIndex(), 1, inputDevice.getPovY() / 2);
			}
		}
		return null;
	}

	public String getHumanReadableInputName(InputDevice inputDevice, ControllerInputEvent inputEvent)
	{
		if (inputDevice == null || inputEvent == null)
		{
			return "NONE";
		}
		String result = null;
		
		try
		{
			setJoypadNameMap(inputDevice);
			result = joypadNameMap.get(inputEvent.getDescription());
			if (result == null && inputEvent.getDescription() != "NONE")
				joypadNameMap.put(inputEvent.getDescription(), inputEvent.getDescription());
		} catch (Exception ex)
		{
			JoypadMod.logger.error("Error in getHumanReadableInputName: " + ex.toString());
		}
	
		return result == null ? inputEvent.getDescription() : result;
	}

	/**
	 * Returns true if any two controller axes read "-1" at the same time. Used to work around the issue with LWJGL which initializes axes at -1 until the axes are moved by the player.
	 * 
	 * @param controller
	 * @return
	 */
	public boolean isDeadlocked(InputDevice controller)
	{
		Integer numberOfNegativeAxes = 0;
		if (controller.getAxisCount() < 1)
		{
			return false;
		}
		for (int i = 0; i < controller.getAxisCount(); i++)
		{
			if (ControllerUtils.getAxisValue(controller, i) == -1)
			{
				numberOfNegativeAxes++;
			}
		}
		return numberOfNegativeAxes > 1;
	}

	public static void autoCalibrateAxis(int joyId, int axisId)
	{
		InputDevice controller = ControllerSettings.JoypadModInputLibrary.getController(joyId);
		controller.setDeadZone(axisId, 0);
		float currentValue = Math.abs(getAxisValue(controller, axisId));
		JoypadMod.logger.info("Axis: " + axisId + " currently has a value of: " + currentValue);
		float newValue = currentValue + 0.15f;
		controller.setDeadZone(axisId, newValue);
		JoypadMod.logger.info("Auto set axis " + axisId + " deadzone to " + newValue);
	}

	public static float getAxisValue(InputDevice inputDevice, int axisNum)
	{
		float rawValue = inputDevice.getAxisValue(axisNum);
		if (ControllerSettings.isSingleDirectionAxis(inputDevice.getIndex(), axisNum))
		{
			return (rawValue + 1f) / 2f;
		}
		return rawValue;
	}

	public static int findYAxisIndex(int joyId)
	{
		InputDevice controller = ControllerSettings.JoypadModInputLibrary.getController(joyId);
		if (controller.getClass() == XInputDeviceWrapper.class)
			return 1;
		for (int i = 0; i < controller.getAxisCount(); i++)
		{
			String axisName = controller.getAxisName(i);
			if (axisName.equals("y") || axisName.contains("Y Axis"))
				return i;
		}

		return 0;
	}

	public static int findXAxisIndex(int joyId)
	{
		InputDevice controller = ControllerSettings.JoypadModInputLibrary.getController(joyId);
		if (controller.getClass() == XInputDeviceWrapper.class)
			return 0;
		for (int i = 0; i < controller.getAxisCount(); i++)
		{
			String axisName = controller.getAxisName(i);
			if (axisName.equals("x") || axisName.contains("X Axis"))
				return i;
		}

		return 1;
	}
	
	private Map<String, String> buildDefaultMap(InputDevice controller)
	{
		Map<String, String> retMap = new HashMap<String, String>();
		if (controller.getName().toLowerCase().contains("xinput")
				|| controller.getName().toLowerCase().contains("xusb")
				|| controller.getName().toLowerCase().contains("xbox"))
		{
			retMap = new HashMap<String, String>();
			retMap.put("Button 0", "A");
			retMap.put("Button 1", "B");
			retMap.put("Button 2", "X");
			retMap.put("Button 3", "Y");
			retMap.put("Button 4", "LB");
			retMap.put("Button 5", "RB");
			retMap.put("Button 6", "BACK");
			retMap.put("Button 7", "START");
			retMap.put("Button 8", "LS");
			retMap.put("Button 9", "RS");
			retMap.put("X Axis +", "LS Right");
			retMap.put("X Axis -", "LS Left");
			retMap.put("Y Axis +", "LS Down");
			retMap.put("Y Axis -", "LS Up");
			retMap.put("X Rotation +", "RS right");
			retMap.put("X Rotation -", "RS left");
			retMap.put("Y Rotation +", "RS down");
			retMap.put("Y Rotation -", "RS up");
			retMap.put("POV X +", "Dpad right");
			retMap.put("POV X -", "Dpad left");
			retMap.put("POV Y +", "Dpad down");
			retMap.put("POV Y -", "Dpad up");
			retMap.put("X Axis", "Left stick horizontal");
			retMap.put("Y Axis", "Left stick vertical");
			retMap.put("X Rotation", "Right stick horizontal");
			retMap.put("Y Rotation", "Right stick vertical");
			retMap.put("Z Axis", "Triggers");
			retMap.put("POV X", "Dpad horizontal");
			retMap.put("POV Y", "Dpad vertical");
			if (ControllerSettings.xbox6Axis.contains(controller.getIndex()))
			{
				retMap.put("Z Axis -", "LT");
				retMap.put("Z Axis +", "LT");
				retMap.put("Z Rotation -", "RT");
				retMap.put("Z Rotation +", "RT");
			}
			else
			{
				retMap.put("Z Axis -", "RT");
				retMap.put("Z Axis +", "LT");
			}
			
			//double check all axis / buttons in case an OS reports these differently
			for (int iTemp = 0; iTemp < controller.getAxisCount() 
					+ controller.getButtonCount(); iTemp++)
			{
				int i = iTemp >= controller.getAxisCount() ? 
						iTemp - controller.getAxisCount() : iTemp;
				String key = iTemp < controller.getAxisCount() ? 
						controller.getAxisName(i) : controller.getButtonName(i);
				if (!retMap.containsKey(key))
					retMap.put(key, key);
			}		
		}
		else // unknown joystick, so create a default map
		{
			for (int iTemp = 0; iTemp < controller.getAxisCount() 
					+ controller.getButtonCount(); iTemp++)
			{
				int i = iTemp >= controller.getAxisCount() ? 
						iTemp - controller.getAxisCount() : iTemp;
				String key = iTemp < controller.getAxisCount() ? 
						controller.getAxisName(i) : controller.getButtonName(i);
				retMap.put(key, key);
			}	
		}
		return retMap;
	}
	
	private void setJoypadNameMap(InputDevice controller)
	{
		if (controller.getIndex() != mappedControllerIndex)
		{
			joypadNameMap.clear();
			// check if config file has map
			Map<String, String> newMap = ControllerSettings.config.buildStringMapFromConfig
					("-ControllerNameMap-", controller.getName());
			if (newMap == null)
			{
				joypadNameMap = buildDefaultMap(controller);
			}
			else
			{
				joypadNameMap = newMap;
			}
			mappedControllerIndex = controller.getIndex();			
		}
	}
	
	public void updateCurrentJoypadMap(String key, String value)
	{
		if (joypadNameMap != null)
		{
			// save if the key is new
			if (value != joypadNameMap.put(key, value))
				saveCurrentJoypadMap();
		}
	}
	
	public boolean saveCurrentJoypadMap()
	{
		if (mappedControllerIndex >= 0)
		{
			try
			{
				ControllerSettings.config.saveStringMap
					("-ControllerNameMap-", ControllerSettings.JoypadModInputLibrary.getController(mappedControllerIndex).getName(), 
					joypadNameMap, "Map the controller button/axis to human readable names");
				return true;
			}
			catch (Exception ex)
			{
				JoypadMod.logger.error("Failed trying to save joypadMap" + ex.toString());
			}
		}
		return false;
	}

}
