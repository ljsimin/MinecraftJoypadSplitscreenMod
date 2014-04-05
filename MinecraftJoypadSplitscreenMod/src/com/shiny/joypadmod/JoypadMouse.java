package com.shiny.joypadmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

import com.shiny.joypadmod.helpers.LogHelper;
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

	public static void updateXY()
	{
		AxisReader.updateXY();
	}

	public static void leftButtonDown()
	{
		if (!VirtualMouse.isButtonDown(0))
		{
			// McGuiHelper.guiMouseDown(AxisReader.x, AxisReader.y, 0);
			VirtualMouse.setXY(AxisReader.mcX, AxisReader.mcY);
			VirtualMouse.holdMouseButton(0, true);
		}
	}

	public static void leftButtonUp()
	{
		if (VirtualMouse.isButtonDown(0))
		{
			// McGuiHelper.guiMouseUp(AxisReader.x, AxisReader.y, 0);
			// VirtualMouse.setXY(AxisReader.mcX, AxisReader.mcY);
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
			// McGuiHelper.guiMouseDown(AxisReader.x, AxisReader.y, 1);
			VirtualMouse.setXY(AxisReader.mcX, AxisReader.mcY);
			VirtualMouse.holdMouseButton(1, true);
		}
	}

	public static void rightButtonUp()
	{
		if (VirtualMouse.isButtonDown(1))
		{
			// McGuiHelper.guiMouseUp(AxisReader.x, AxisReader.y, 1);
			// VirtualMouse.setXY(AxisReader.mcX, AxisReader.mcY);
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
		private static long readingTimeout = 10;
		private static long last0Reading = 0;

		// this is the equivalent of moving the mouse around on your joypad
		public static void pollAxis()
		{
			boolean inGui = mc.currentScreen != null;
			long readTimeout = inGui ? readingTimeout + ControllerSettings.inMenuSensitivity : readingTimeout
					+ ControllerSettings.inGameSensitivity;

			if (Minecraft.getSystemTime() - lastAxisReading < readTimeout)
				return;

			float xPlus = getReading(inGui ? "joy.guiX+" : "joy.cameraX+");
			float xMinus = getReading(inGui ? "joy.guiX-" : "joy.cameraX-");
			float horizontalMovement = Math.abs(xPlus) > Math.abs(xMinus) ? xPlus : xMinus;

			float yPlus = getReading(inGui ? "joy.guiY+" : "joy.cameraY+");
			float yMinus = getReading(inGui ? "joy.guiY-" : "joy.cameraY-");
			float verticalMovement = Math.abs(yPlus) > Math.abs(yMinus) ? yPlus : yMinus;

			deltaX = horizontalMovement;
			deltaY = verticalMovement;

			if (deltaX == 0 && deltaY == 0)
			{
				last0Reading = Minecraft.getSystemTime();
				return;
			}

			float cameraMultiplier = getCameraMultiplier(inGui);

			// minecrafts original crazy calculation has found its way here
			float magicNumber = mc.gameSettings.mouseSensitivity * 0.4F + 0.2F;

			deltaX *= cameraMultiplier * magicNumber;
			deltaY *= cameraMultiplier * magicNumber;

			if (ControllerSettings.loggingLevel > 2)
				LogHelper.Debug("Camera deltaX: " + deltaX + " Camera deltaY: " + deltaY);

			lastAxisReading = Minecraft.getSystemTime();
		}

		public static void updateXY()
		{
			if (Minecraft.getSystemTime() - lastAxisReading < readingTimeout)
				return;

			final ScaledResolution scaledResolution = new ScaledResolution(mc.gameSettings, mc.displayWidth,
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
			}
		}

		public static void centerCrosshairs()
		{
			final ScaledResolution scaledResolution = new ScaledResolution(mc.gameSettings, mc.displayWidth,
					mc.displayHeight);

			x = scaledResolution.getScaledWidth() / 2;
			y = scaledResolution.getScaledHeight() / 2;

			mcY = mc.displayHeight - (int) (y * scaledResolution.getScaleFactor());
			mcX = x * scaledResolution.getScaleFactor();
		}

		private static float getReading(String bindKey)
		{
			if (ControllerSettings.get(bindKey).inputEvent.pressedOnce())
				return ControllerSettings.get(bindKey).getAnalogReading();
			return 0;
		}

		private static float getCameraMultiplier(boolean inGui)
		{
			float cameraMultiplier = (inGui ? ControllerSettings.inMenuSensitivity
					: ControllerSettings.inGameSensitivity * 2);

			long elapsed = Minecraft.getSystemTime() - last0Reading;
			if (elapsed < 500)
			{
				float base = cameraMultiplier * 0.5f;

				// increase the multiplier by 10% every 100 ms
				cameraMultiplier = base + (base * elapsed) / 500;
				if (ControllerSettings.loggingLevel > 2)
					LogHelper.Info("CameraMultiplier " + cameraMultiplier);
			}

			return cameraMultiplier;

		}

	}

}
