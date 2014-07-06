package com.shiny.joypadmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import com.shiny.joypadmod.helpers.LogHelper;
import com.shiny.joypadmod.inputevent.ControllerInputEvent.EventType;
import com.shiny.joypadmod.lwjglVirtualInput.VirtualMouse;

public class JoypadMouse
{
	public static int getX()
	{
		return AxisReader.x;
	}

	public static int getY()
	{
		return AxisReader.y;
	}

	public static int getmcX()
	{
		return AxisReader.mcX;
	}

	public static int getmcY()
	{
		return AxisReader.mcY;
	}

	public static void pollAxis()
	{
		AxisReader.pollAxis();
	}

	public static boolean pollAxis(boolean inGui)
	{
		AxisReader.pollAxis(inGui);
		if (AxisReader.deltaX == 0 && AxisReader.deltaY == 0)
			return false;
		return true;
	}

	public static void updateXY()
	{
		AxisReader.updateXY();
	}

	public static void leftButtonDown()
	{
		if (!VirtualMouse.isButtonDown(0))
		{
			VirtualMouse.setXY(AxisReader.mcX, AxisReader.mcY);
			VirtualMouse.holdMouseButton(0, true);
		}
	}

	public static void leftButtonUp()
	{
		if (VirtualMouse.isButtonDown(0))
		{
			VirtualMouse.releaseMouseButton(0, true);
		}
	}

	public static boolean isLeftButtonDown()
	{
		return VirtualMouse.isButtonDown(0);
	}

	public static void rightButtonDown()
	{
		if (!VirtualMouse.isButtonDown(1))
		{
			VirtualMouse.setXY(AxisReader.mcX, AxisReader.mcY);
			VirtualMouse.holdMouseButton(1, true);
		}
	}

	public static void rightButtonUp()
	{
		if (VirtualMouse.isButtonDown(1))
		{
			VirtualMouse.releaseMouseButton(1, true);
		}
	}

	public static boolean isRightButtonDown()
	{
		return VirtualMouse.isButtonDown(1);
	}

	public static void UnpressButtons()
	{
		for (int i = 0; i < 2; i++)
		{
			if (VirtualMouse.isButtonDown(i))
				VirtualMouse.releaseMouseButton(i, true);
		}
	}

	public static class AxisReader
	{
		private static Minecraft mc = Minecraft.getMinecraft();

		// last delta movement of axis
		public static float deltaX;
		public static float deltaY;

		// last virtual mouse position
		public static int x = 0;
		public static int y = 0;

		// values that Minecraft expects when reading the actual mouse
		public static int mcX = 0;
		public static int mcY = 0;

		private static long lastAxisReading = 0;
		private static long guiPollTimeout = 30;
		private static long gamePollTimeout = 10;
		public static long last0Reading = 0;
		public static long lastNon0Reading = 0;

		public static void pollAxis()
		{
			pollAxis(mc.currentScreen != null);
		}

		// pollAxis()
		// this is the equivalent of moving the mouse around on your joypad
		// the current algorithm as of 4/6/2014
		// 1) get reading of axis (will be between -1.0 && 1.0)
		// 2) if reading of both axis == 0 exit ( note: a 0 reading will be partly determined by the deadzones )
		// 3) sensitivityValue = current menu/game sensitivity value (set by user)
		// 4) if axisReading > axisThreshold, delta = axisReading * sensitivityValue
		// 5) if in game && axisReading < axisThreshold / 2, delta = axisReading * sensitivityValue * 0.3
		// 6) if axisReading < axisThreshold, delta = axisReading * sensitivityValue * 0.5
		// a second modifier will apply if the axisReading > axisThreshold and is currently within the first 500ms of being pressed
		// this will start by halving the sensitivityValue and over the course of 500ms add an additional 10% until it is sending the full sensitivity value
		public static void pollAxis(boolean inGui)
		{
			if (!pollNeeded(inGui))
				return;

			float xPlus = getReading(inGui ? "joy.guiX+" : "joy.cameraX+");
			float xMinus = getReading(inGui ? "joy.guiX-" : "joy.cameraX-");
			float horizontalMovement;
			float horizontalThreshold;
			if (Math.abs(xPlus) > Math.abs(xMinus))
			{
				horizontalMovement = xPlus;
				horizontalThreshold = ControllerSettings.get(inGui ? "joy.guiX+" : "joy.cameraX+").inputEvent.getThreshold();
			}
			else
			{
				horizontalMovement = xMinus;
				horizontalThreshold = ControllerSettings.get(inGui ? "joy.guiX-" : "joy.cameraX-").inputEvent.getThreshold();
			}

			float yPlus = getReading(inGui ? "joy.guiY+" : "joy.cameraY+");
			float yMinus = getReading(inGui ? "joy.guiY-" : "joy.cameraY-");
			float verticalMovement;
			float verticalThreshold;
			if (Math.abs(yPlus) > Math.abs(yMinus))
			{
				verticalMovement = yPlus;
				verticalThreshold = ControllerSettings.get(inGui ? "joy.guiY+" : "joy.cameraY+").inputEvent.getThreshold();
			}
			else
			{
				verticalMovement = yMinus;
				verticalThreshold = ControllerSettings.get(inGui ? "joy.guiY-" : "joy.cameraY-").inputEvent.getThreshold();
			}

			deltaX = horizontalMovement;
			deltaY = verticalMovement;

			if (deltaX == 0 && deltaY == 0)
			{
				last0Reading = Minecraft.getSystemTime();
				return;
			}
			lastNon0Reading = Minecraft.getSystemTime();

			deltaX = calculateFinalDelta(inGui, deltaX, horizontalThreshold);
			deltaY = calculateFinalDelta(inGui, deltaY, verticalThreshold);
			if (ControllerSettings.loggingLevel > 2)
				LogHelper.Info("Camera deltaX: " + deltaX + " Camera deltaY: " + deltaY);
			lastAxisReading = Minecraft.getSystemTime();
		}

		public static void updateXY()
		{
			if (!pollNeeded(mc.currentScreen != null))
				return;

			final ScaledResolution scaledResolution = new ScaledResolution(mc, mc.displayWidth,
					mc.displayHeight);

			pollAxis();

			if (mc.currentScreen != null)
			{
				x += (int) deltaX;
				y += (int) deltaY;

				if (x < 0)
					x = 0;
				if (y < 0)
					y = 0;
				if (x > scaledResolution.getScaledWidth())
					x = scaledResolution.getScaledWidth() - 5;
				if (y > scaledResolution.getScaledHeight())
					y = scaledResolution.getScaledHeight() - 5;

				if (ControllerSettings.loggingLevel > 2)
					LogHelper.Debug("Virtual Mouse x: " + x + " y: " + y);

				mcY = mc.displayHeight - (int) (y * scaledResolution.getScaleFactor());
				mcX = x * scaledResolution.getScaleFactor();
				deltaX = 0;
				deltaY = 0;
			}
		}

		public static void centerCrosshairs()
		{
			final ScaledResolution scaledResolution = new ScaledResolution(mc, mc.displayWidth,
					mc.displayHeight);

			x = scaledResolution.getScaledWidth() / 2;
			y = scaledResolution.getScaledHeight() / 2;

			mcY = mc.displayHeight - (int) (y * scaledResolution.getScaleFactor());
			mcX = x * scaledResolution.getScaleFactor();
		}

		public static boolean pollNeeded(boolean inGui)
		{
			if (Minecraft.getSystemTime() - lastAxisReading < (inGui ? guiPollTimeout : gamePollTimeout))
				return false;

			return true;
		}

		public static void setXY(int x, int y)
		{
			final ScaledResolution scaledResolution = new ScaledResolution(mc, mc.displayWidth,
					mc.displayHeight);

			AxisReader.x = x;
			AxisReader.y = y;
			AxisReader.mcX = x * scaledResolution.getScaleFactor();
			AxisReader.mcY = mc.displayHeight - (int) (y * scaledResolution.getScaleFactor());
		}

		private static float getReading(String bindKey)
		{
			boolean isButton = ControllerSettings.get(bindKey).inputEvent.getEventType() == EventType.BUTTON;

			if (isButton || ControllerSettings.get(bindKey).inputEvent.pressedOnce())
			{
				float f = ControllerSettings.get(bindKey).getAnalogReading();
				if (isButton && bindKey.contains("-"))
					f *= -1;
				return f;
			}

			return 0;
		}

		private static float getModifiedMultiplier(float currentMultiplier)
		{
			long elapsed = Minecraft.getSystemTime() - last0Reading;
			if (elapsed < 500)
			{
				float base = currentMultiplier * 0.5f;

				// increase the multiplier by 10% every 100 ms
				currentMultiplier = base + (base * elapsed) / 500;
				if (ControllerSettings.loggingLevel > 2)
					LogHelper.Info("CameraMultiplier " + currentMultiplier);
			}

			return currentMultiplier;
		}

		private static float calculateFinalDelta(boolean inGui, float currentDelta, float currentThreshold)
		{
			if (Math.abs(currentDelta) < 0.01)
				return 0;

			if (Math.abs(currentDelta) < Math.abs(currentThreshold / 3))
			{
				return (currentDelta < 0 ? -1 : 1);
			}

			float cameraMultiplier = (inGui ? ControllerSettings.inMenuSensitivity
					: ControllerSettings.inGameSensitivity);

			if (!inGui && Math.abs(currentDelta) < Math.abs(currentThreshold / 2))
				cameraMultiplier *= 0.3;
			else if (Math.abs(currentDelta) < Math.abs(currentThreshold))
				cameraMultiplier *= 0.5;
			else
				cameraMultiplier = getModifiedMultiplier(cameraMultiplier);

			float finalDelta = currentDelta * cameraMultiplier;

			// return at minimum a 1 or -1
			if (finalDelta < 1.0 && finalDelta > 0.0)
				finalDelta = 1.0f;
			else if (finalDelta < 0 && finalDelta > -1.0)
				finalDelta = -1.0f;

			return finalDelta;
		}
	}

}
