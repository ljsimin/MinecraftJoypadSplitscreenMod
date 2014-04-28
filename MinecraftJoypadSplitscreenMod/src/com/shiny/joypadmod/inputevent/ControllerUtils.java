package com.shiny.joypadmod.inputevent;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.input.Controller;
import org.lwjgl.input.Controllers;

import com.shiny.joypadmod.helpers.LogHelper;

public class ControllerUtils
{
	private static Map<String, String> xinputNamesMap;

	public ControllerUtils()
	{
		xinputNamesMap = new HashMap<String, String>();
		xinputNamesMap.put("Button 0", "A");
		xinputNamesMap.put("Button 1", "B");
		xinputNamesMap.put("Button 2", "X");
		xinputNamesMap.put("Button 3", "Y");
		xinputNamesMap.put("Button 4", "LB");
		xinputNamesMap.put("Button 5", "RB");
		xinputNamesMap.put("Button 6", "BACK");
		xinputNamesMap.put("Button 7", "START");
		xinputNamesMap.put("Button 8", "LS");
		xinputNamesMap.put("Button 9", "RS");
		xinputNamesMap.put("Z Axis -", "RT");
		xinputNamesMap.put("Z Axis +", "LT");
		xinputNamesMap.put("X Axis +", "LS Right");
		xinputNamesMap.put("X Axis -", "LS Left");
		xinputNamesMap.put("Y Axis +", "LS Down");
		xinputNamesMap.put("Y Axis -", "LS Up");
		xinputNamesMap.put("X Rotation +", "RS right");
		xinputNamesMap.put("X Rotation -", "RS left");
		xinputNamesMap.put("Y Rotation +", "RS down");
		xinputNamesMap.put("Y Rotation -", "RS up");
		xinputNamesMap.put("POV X +", "Dpad right");
		xinputNamesMap.put("POV X -", "Dpad left");
		xinputNamesMap.put("POV Y +", "Dpad down");
		xinputNamesMap.put("POV Y -", "Dpad up");
		xinputNamesMap.put("X Axis", "Left stick horizontal");
		xinputNamesMap.put("Y Axis", "Left stick vertical");
		xinputNamesMap.put("X Rotation", "Right stick horizontal");
		xinputNamesMap.put("Y Rotation", "Right stick vertical");
		xinputNamesMap.put("Z Axis", "Triggers");
		xinputNamesMap.put("POV X", "Dpad horizontal");
		xinputNamesMap.put("POV Y", "Dpad vertical");
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
			if (Math.abs(controller.getAxisValue(eventIndex)) > 0.75f)
			{
				return new AxisInputEvent(controller.getIndex(), eventIndex, controller.getAxisValue(eventIndex),
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
		if (controller == null)
		{
			return "NONE";
		}
		if (controller.getName().toLowerCase().contains("xinput")
				|| controller.getName().toLowerCase().contains("xusb")
				|| controller.getName().toLowerCase().contains("xbox"))
		{
			String result = xinputNamesMap.get(inputEvent.getDescription());
			if (result != null)
			{
				return result;
			}
		}
		return inputEvent.getDescription();
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
			if (controller.getAxisValue(i) == -1)
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
		float currentValue = Math.abs(controller.getAxisValue(axisId));
		LogHelper.Info("Axis: " + axisId + " currently has a value of: " + currentValue);
		float newValue = currentValue + 0.15f;
		controller.setDeadZone(axisId, newValue);
		LogHelper.Info("Auto set axis " + axisId + " deadzone to " + newValue);
	}

	public static int findYAxisIndex(int joyId)
	{
		Controller controller = Controllers.getController(joyId);
		for (int i = 0; i < controller.getAxisCount(); i++)
		{
			if (controller.getAxisName(i).contains("Y Axis"))
				return i;
		}

		return 0;
	}

	public static int findXAxisIndex(int joyId)
	{
		Controller controller = Controllers.getController(joyId);
		for (int i = 0; i < controller.getAxisCount(); i++)
		{
			if (controller.getAxisName(i).contains("X Axis"))
				return i;
		}

		return 1;
	}

}
