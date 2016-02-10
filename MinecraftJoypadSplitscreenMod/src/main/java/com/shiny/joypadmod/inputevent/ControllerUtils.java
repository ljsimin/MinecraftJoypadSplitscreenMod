package com.shiny.joypadmod.inputevent;

import java.util.HashMap;
import java.util.Map;

import com.shiny.joypadmod.ControllerSettings;
import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;

import com.shiny.joypadmod.helpers.ConfigFile;
import com.shiny.joypadmod.helpers.LogHelper;

public class ControllerUtils
{
	private static Map<String, String> joypadNameMap;
	private Controller currentController;

	public ControllerUtils()
	{
		joypadNameMap = new HashMap<String, String>();
		currentController = null;
	}

	public void printDeadZones(Controller joystick2)
	{
		if (joystick2 != null)
		{
			for (int axisNo = 0; axisNo < joystick2.getAxisCount(); axisNo++)
			{
				LogHelper.Info("Axis " + axisNo + " deadzone: " + joystick2.getDeadZone(axisNo));
			}
		}
	}

	public void printAxisNames(Controller joystick2)
	{
		for (int axisNo = 0; axisNo < joystick2.getAxisCount(); axisNo++)
		{
			LogHelper.Info("Axis " + axisNo + ", " + joystick2.getAxisName(axisNo));
		}
	}

	public void printButtonNames(Controller joystick2)
	{
		for (int buttonNo = 0; buttonNo < joystick2.getButtonCount(); buttonNo++)
		{
			LogHelper.Info("Button " + buttonNo + ", " + joystick2.getButtonName(buttonNo));
		}
	}

	public boolean checkJoypadRequirements(Controller controller, int requiredButtonCount, int requiredMinButtonCount,
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
					"Total number of controllers detected: ").append(Controllers.getControllerCount());
			LogHelper.Info(msg.toString());
		}
		return meetsRequirements;
	}

	public boolean meetsInputRequirements(Controller controller, int requiredButtonCount, int requiredMinButtonCount,
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

	public ControllerInputEvent getLastEvent(Controller controller, int eventIndex)
	{
		if (Controllers.isEventAxis())
		{
			if (Math.abs(ControllerUtils.getAxisValue(controller, eventIndex)) > 0.75f)
			{
				return new AxisInputEvent(controller.getIndex(), eventIndex, ControllerUtils.getAxisValue(controller, eventIndex),
						controller.getDeadZone(eventIndex));
			}
		}
		else if (Controllers.isEventButton())
		{
			int id = Controllers.getEventControlIndex();
			if (controller.isButtonPressed(id))
			{
				return new ButtonInputEvent(controller.getIndex(), id, 1);
			}
		}
		else if (Controllers.isEventPovX())
		{
			if (Math.abs(controller.getPovX()) > 0.5f)
			{
				return new PovInputEvent(controller.getIndex(), 0, controller.getPovX() / 2);
			}
		}
		else if (Controllers.isEventPovY())
		{
			if (Math.abs(controller.getPovY()) > 0.5f)
			{
				return new PovInputEvent(controller.getIndex(), 1, controller.getPovY() / 2);
			}
		}
		return null;
	}

	public String getHumanReadableInputName(Controller controller, ControllerInputEvent inputEvent)
	{
		if (controller == null || inputEvent == null)
		{
			return "NONE";
		}
		String result = null;
		
		try
		{
			setJoypadNameMap(controller);
			result = joypadNameMap.get(inputEvent.getDescription());
			if (result == null && inputEvent.getDescription() != "NONE")
				joypadNameMap.put(inputEvent.getDescription(), inputEvent.getDescription());
		} catch (Exception ex)
		{
			LogHelper.Error("Error in getHumanReadableInputName: " + ex.toString());
		}
	
		return result == null ? inputEvent.getDescription() : result;
	}

	/**
	 * Returns true if any two controller axes read "-1" at the same time. Used to work around the issue with LWJGL which initializes axes at -1 until the axes are moved by the player.
	 * 
	 * @param controller
	 * @return
	 */
	public boolean isDeadlocked(Controller controller)
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
		Controller controller = Controllers.getController(joyId);
		controller.setDeadZone(axisId, 0);
		float currentValue = Math.abs(getAxisValue(controller, axisId));
		LogHelper.Info("Axis: " + axisId + " currently has a value of: " + currentValue);
		float newValue = currentValue + 0.15f;
		controller.setDeadZone(axisId, newValue);
		LogHelper.Info("Auto set axis " + axisId + " deadzone to " + newValue);
	}

	public static float getAxisValue(Controller controller, int axisNum)
	{
		float rawValue = controller.getAxisValue(axisNum);
		if (ControllerSettings.isSingleDirectionAxis(controller.getIndex(), axisNum))
		{
			return (rawValue + 1f) / 2f;
		}
		return rawValue;
	}

	public static int findYAxisIndex(int joyId)
	{
		Controller controller = Controllers.getController(joyId);
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
		Controller controller = Controllers.getController(joyId);
		for (int i = 0; i < controller.getAxisCount(); i++)
		{
			String axisName = controller.getAxisName(i);
			if (axisName.equals("x") || axisName.contains("X Axis"))
				return i;
		}

		return 1;
	}
	
	private Map<String, String> buildDefaultMap(Controller controller)
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
	
	private void setJoypadNameMap(Controller controller)
	{
		if (currentController == null 
				|| controller.getIndex() != currentController.getIndex())
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
			currentController = controller;
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
		if (currentController != null)
		{
			try
			{
				ControllerSettings.config.saveStringMap
					("-ControllerNameMap-", currentController.getName(), 
					joypadNameMap, "Map the controller button/axis to human readable names");
				return true;
			}
			catch (Exception ex)
			{
				LogHelper.Error("Failed trying to save joypadMap" + ex.toString());
			}
		}
		return false;
	}

}
