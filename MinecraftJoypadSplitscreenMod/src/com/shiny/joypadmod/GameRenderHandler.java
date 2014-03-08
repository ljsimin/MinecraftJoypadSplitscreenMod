package com.shiny.joypadmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;

import org.lwjgl.input.Controllers;
import org.lwjgl.input.Keyboard;

import com.shiny.joypadmod.ControllerSettings.JoyBindingEnum;
import com.shiny.joypadmod.helpers.LogHelper;
import com.shiny.joypadmod.helpers.McGuiHelper;
import com.shiny.joypadmod.helpers.McObfuscationHelper;
import com.shiny.joypadmod.helpers.ModVersionHelper;
import com.shiny.joypadmod.inputevent.ControllerBinding;
import com.shiny.joypadmod.lwjglVirtualInput.VirtualKeyboard;
import com.shiny.joypadmod.lwjglVirtualInput.VirtualMouse;
import com.shiny.joypadmod.minecraftExtensions.JoypadConfigMenu;

import cpw.mods.fml.common.ObfuscationReflectionHelper;

public class GameRenderHandler
{
	private static Minecraft mc = Minecraft.getMinecraft();
	public static int reticalColor = 0xFFFFFFFF;
	// boolean to allow the original controls menu.
	// normally we override the controls menu when seen
	public static boolean allowOrigControlsMenu = false;
	private static long lastInGuiTick = 0;
	private static long lastInGameTick = 0;
	static int scrollDelay = 0;

	public static void HandlePreRender()
	{
		if (mc.currentScreen != null && !ControllerSettings.isSuspended())
		{
			try
			{
				if (mc.currentScreen instanceof GuiControls)
				{
					if (!allowOrigControlsMenu)
					{
						ReplaceControlScreen((GuiControls) mc.currentScreen);
					}
				}
				else if (!(mc.currentScreen instanceof JoypadConfigMenu))
				{
					allowOrigControlsMenu = false;
				}

				if (InGuiCheckNeeded())
				{
					// This call here re-points the mouse position that Minecraft picks
					// up to determine if it should do the Hover over button effect.
					VirtualMouse.setXY(JoypadMouse.getmcX(), JoypadMouse.getmcY());
					HandleDragAndScrolling();
				}
			}
			catch (Exception ex)
			{
				LogHelper.Fatal("Joypad mod unhandled exception caught! " + ex.toString());
			}
		}
	}

	public static void HandlePostRender()
	{
		if (ControllerSettings.isSuspended())
			return;

		try
		{
			if (InGuiCheckNeeded())
			{
				DrawRetical();
			}

			if (InGameCheckNeeded())
			{
				UpdateInGameCamera();
			}
		}
		catch (Exception ex)
		{
			LogHelper.Fatal("Joypad mod unhandled exception caught! " + ex.toString());
		}

	}

	public static void HandleClientStartTick()
	{

		if (ControllerSettings.isSuspended())
			return;

		if (InGameCheckNeeded())
		{
			HandleJoystickInGame();
			lastInGameTick = Minecraft.getSystemTime();
		}

		if (InGuiCheckNeeded())
		{
			HandleJoystickInGui();
			lastInGuiTick = Minecraft.getSystemTime();
		}
	}

	public static void HandleClientEndTick()
	{
		// does nothing currently
	}

	private static void DrawRetical()
	{

		if (mc.currentScreen == null || !JoypadMod.controllerSettings.isInputEnabled())
			return;

		JoypadMouse.updateXY();
		int x = JoypadMouse.getX();
		int y = JoypadMouse.getY();

		Gui.drawRect(x - 3, y, x + 4, y + 1, reticalColor);
		Gui.drawRect(x, y - 3, x + 1, y + 4, reticalColor);
	}

	private static void UpdateInGameCamera()
	{
		JoypadMouse.updateXY();
		mc.thePlayer.setAngles(JoypadMouse.AxisReader.deltaX,
				JoypadMouse.AxisReader.deltaY * (ControllerSettings.getInvertYAxis() ? 1.0f : -1.0f));
	}

	public static int lastScrollEvent = 0;

	private static void HandleDragAndScrolling()
	{

		if (VirtualMouse.isButtonDown(0) || VirtualMouse.isButtonDown(1))
		{
			// VirtualMouse.moveMouse(JoypadMouse.getmcX(), JoypadMouse.getmcY());
			McGuiHelper.guiMouseDrag(JoypadMouse.getX(), JoypadMouse.getY());
			VirtualMouse.setMouseButton(JoypadMouse.isLeftButtonDown() ? 0 : 1, true);
		}

		if (lastScrollEvent != 0)
		{
			VirtualMouse.scrollWheel(lastScrollEvent);
			scrollDelay = 2;
			lastScrollEvent = 0;
		}

		if (scrollDelay-- <= 0)
		{
			if (ControllerSettings.get(JoyBindingEnum.joyBindNextItem).isPressed(false))
			{
				VirtualMouse.scrollWheel(-1);
			}
			if (ControllerSettings.get(JoyBindingEnum.joyBindPrevItem).isPressed(false))
			{
				VirtualMouse.scrollWheel(1);
			}
			scrollDelay = 1;
		}
	}

	private static void HandleJoystickInGui()
	{
		// fixes issue with transitioning from inGame to Gui movement continuing
		if (Minecraft.getSystemTime() - lastInGameTick < 100)
		{
			System.out.println("Unpressing all buttons");
			ControllerSettings.unpressAll();
		}
		// update mouse coordinates
		// JoypadMouse.updateXY();
		VirtualMouse.setXY(JoypadMouse.getmcX(), JoypadMouse.getmcY());

		while (Controllers.next() && mc.currentScreen != null)
		{
			// ignore controller events in the milliseconds following in GAME
			// controlling
			if (Minecraft.getSystemTime() - lastInGameTick < 200)
				continue;

			// VirtualMouseNew.setXY(joypadMouse.mcX, joypadMouse.mcY);
			if (mc.currentScreen instanceof GuiContainer)
			{
				if (ControllerSettings.get(JoyBindingEnum.joyBindShiftClick).wasPressed())
				{
					LogHelper.Info("Shift Click");
					VirtualKeyboard.pressKey(Keyboard.KEY_LSHIFT);
					VirtualKeyboard.holdKey(Keyboard.KEY_LSHIFT, true);
					JoypadMouse.leftButtonDown();
					continue;
				}
			}

			if (ControllerSettings.get(JoyBindingEnum.joyBindInventory).wasPressed(false))
			{
				LogHelper.Info("Inventory dismiss pressed");

				if (mc.thePlayer != null)
					mc.thePlayer.closeScreen();
				else
				{
					// backup
					mc.displayGuiScreen(null);
				}
			}
			else if (ControllerSettings.get(JoyBindingEnum.joyBindGuiLeftClick).wasPressed())
			{
				JoypadMouse.leftButtonDown();
			}
			else if (ControllerSettings.get(JoyBindingEnum.joyBindGuiRightClick).wasPressed())
			{
				JoypadMouse.rightButtonDown();
			}
			else if (ControllerSettings.get(JoyBindingEnum.joyBindNextItem).wasPressed(false))
			{
				lastScrollEvent = -1;
			}
			else if (ControllerSettings.get(JoyBindingEnum.joyBindPrevItem).wasPressed(false))
			{
				lastScrollEvent = 1;
			}
			else
				ControllerSettings.get(JoyBindingEnum.joyBindMenu).wasPressed(); // auto handled
		}

		if (!ControllerSettings.get(JoyBindingEnum.joyBindGuiLeftClick).isPressed())
		{
			JoypadMouse.leftButtonUp();
		}

		if (!ControllerSettings.get(JoyBindingEnum.joyBindGuiRightClick).isPressed())
		{
			JoypadMouse.rightButtonUp();
		}

		if (mc.currentScreen == null)
		{
			JoypadMouse.UnpressButtons();
		}
	}

	// TODO: update this temporary hack to fix the attack/use from registering too many from single press (happens on triggers)
	private static boolean attackHeld = false;
	private static boolean useHeld = false;

	// does this have to be run in post render or pre? maybe doesn't
	// matter...but be wary if changing it around
	private static void HandleJoystickInGame()
	{
		if (Minecraft.getSystemTime() - lastInGuiTick > 200)
		{
			for (ControllerBinding binding : ControllerSettings.getAutoHandleBindings())
			{
				binding.isPressed();
			}
		}

		while (Controllers.next())
		{
			// ignore controller events in the milliseconds following in GUI
			// controlling
			if (Minecraft.getSystemTime() - lastInGuiTick < 200)
				continue;

			if (ControllerSettings.get(JoyBindingEnum.joyBindAttack).isPressed())
			{
				// this call ensures that you can break blocks in non-creative!
				mc.inGameHasFocus = true;
			}

			// hack in the drop more than 1 item for 172. normal keypresses work for this in 164.
			if (ModVersionHelper.getVersion() == 172)
			{
				if (ControllerSettings.get(JoyBindingEnum.joyBindDrop).wasPressed(false))
				{
					mc.thePlayer.dropOneItem(ControllerSettings.get(JoyBindingEnum.joyBindRun).isPressed(false));
					continue;
				}
			}

			// hack in sprint
			if (ModVersionHelper.getVersion() == 164)
			{
				if (ControllerSettings.get(JoyBindingEnum.joyBindRun).wasPressed())
				{
					mc.thePlayer.setSprinting(true);
					continue;
				}
			}

			boolean eventRead = false;
			for (ControllerBinding binding : ControllerSettings.getAutoHandleBindings())
			{
				if (eventRead = binding.wasPressed())
					break;
			}

			if (eventRead)
				continue;

			if (ControllerSettings.get(JoyBindingEnum.joyBindAttack).wasPressed())
			{
				if (!attackHeld)
				{
					LogHelper.Info("Initiating attack");
					VirtualMouse.holdMouseButton(0);
					// KeyBinding.onTick(attackKeyCode);
					attackHeld = true;
				}
			}
			else if (ControllerSettings.get(JoyBindingEnum.joyBindInteract).wasPressed()
					|| ControllerSettings.get(JoyBindingEnum.joyBindUseItem).wasPressed())
			{
				if (!useHeld)
				{
					LogHelper.Info("Initiating use ontick");
					VirtualMouse.holdMouseButton(1);
					// KeyBinding.onTick(useKeyCode);
					useHeld = true;
				}
			}
			else if (ControllerSettings.get(JoyBindingEnum.joyBindNextItem).wasPressed())
			{
				LogHelper.Info("NextItem pressed");
				VirtualMouse.scrollWheel(-1);
			}
			else if (ControllerSettings.get(JoyBindingEnum.joyBindPrevItem).wasPressed())
			{
				LogHelper.Info("PrevItem pressed");
				VirtualMouse.scrollWheel(1);
			}

			// these should go after the waspressed calls
			if (useHeld)
			{
				if (!ControllerSettings.get(JoyBindingEnum.joyBindUseItem).isPressed())
				{
					VirtualMouse.releaseMouseButton(1);
					useHeld = false;
				}
			}

			if (attackHeld)
			{
				if (!ControllerSettings.get(JoyBindingEnum.joyBindAttack).isPressed())
				{
					VirtualMouse.releaseMouseButton(0);
					attackHeld = false;
				}
			}
		}
	}

	private static void ReplaceControlScreen(GuiControls gui)
	{
		if (!(mc.currentScreen instanceof JoypadConfigMenu))
		{
			try
			{
				LogHelper.Debug("Replacing control screen");
				String[] names = McObfuscationHelper.getMcVarNames("parentScreen");
				GuiScreen parent = ObfuscationReflectionHelper.getPrivateValue(GuiControls.class, (GuiControls) gui,
						names[0], names[1]);
				mc.displayGuiScreen(new JoypadConfigMenu(parent, gui));
			}
			catch (Exception ex)
			{
				LogHelper.Debug("Failed to get parent of options gui.  aborting");
				return;
			}
		}
	}

	public static boolean InGameCheckNeeded()
	{
		if (!CheckIfModEnabled() || mc.currentScreen != null || mc.thePlayer == null)
		{
			return false;
		}

		return true;
	}

	public static boolean InGuiCheckNeeded()
	{
		if (!CheckIfModEnabled() || mc.currentScreen == null)
		{
			return false;
		}

		return true;
	}

	public static boolean CheckIfModEnabled()
	{
		if (mc == null || !JoypadMod.controllerSettings.isInputEnabled() || ControllerSettings.joystick == null)
		{
			return false;
		}

		return true;
	}
}
