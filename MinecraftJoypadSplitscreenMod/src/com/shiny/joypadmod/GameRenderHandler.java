package com.shiny.joypadmod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiControls;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Controllers;
import org.lwjgl.input.Keyboard;

import com.shiny.joypadmod.ControllerSettings.JoyBindingEnum;
import com.shiny.joypadmod.helpers.LogHelper;
import com.shiny.joypadmod.helpers.McObfuscationHelper;
import com.shiny.joypadmod.helpers.ModVersionHelper;
import com.shiny.joypadmod.inputevent.ControllerBinding;
import com.shiny.joypadmod.minecraftExtensions.JoypadConfigMenu;

import cpw.mods.fml.common.ObfuscationReflectionHelper;

public class GameRenderHandler
{
	private static Minecraft mc = Minecraft.getMinecraft();
	public static int reticalColor = 0xFFFFFFFF;
	public static VirtualMouse joypadMouse = new VirtualMouse();
	// boolean to allow the original controls menu.
	// normally we override the controls menu when seen
	public static boolean allowOrigControlsMenu = false;
	private static long lastInGuiTick = 0;
	private static long lastInGameTick = 0;

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
					HandleGuiMousePreRender();
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
				HandleGuiMousePostRender();
				lastInGuiTick = Minecraft.getSystemTime();
			}

			if (InGameCheckNeeded())
			{
				HandleJoystickInGame();
				lastInGameTick = Minecraft.getSystemTime();
			}
		}
		catch (Exception ex)
		{
			LogHelper.Fatal("Joypad mod unhandled exception caught! " + ex.toString());
		}

	}

	private static void HandleGuiMousePreRender()
	{
		if (mc.currentScreen == null || !JoypadMod.controllerSettings.isInputEnabled())
			return;

		// fixes issue with transitioning from inGame to Gui movement continuing
		if (Minecraft.getSystemTime() - lastInGameTick < 100)
		{
			ControllerSettings.unpressAll();
		}
		// update mouse coordinates
		joypadMouse.getX(true);
		joypadMouse.getY(true);

		while (Controllers.next() && mc.currentScreen != null)
		{
			// ignore controller events in the milliseconds following in GAME
			// controlling
			if (Minecraft.getSystemTime() - lastInGameTick < 200)
				continue;

			if (mc.currentScreen instanceof GuiContainer)
			{
				if (ControllerSettings.get(JoyBindingEnum.joyBindShiftClick).wasPressed())
				{
					LogHelper.Info("Shift Click");
					VirtualKeyboard.holdKey(Keyboard.KEY_LSHIFT, true);
					joypadMouse.leftButtonDown();
					continue;
				}
			}

			if (joypadMouse.leftButtonHeld && !ControllerSettings.get(JoyBindingEnum.joyBindGuiLeftClick).isPressed())
				joypadMouse.leftButtonUp();

			if (joypadMouse.rightButtonHeld && !ControllerSettings.get(JoyBindingEnum.joyBindGuiRightClick).isPressed())
				joypadMouse.rightButtonUp();

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
				joypadMouse.leftButtonDown();
			}
			else if (ControllerSettings.get(JoyBindingEnum.joyBindGuiRightClick).wasPressed())
			{
				joypadMouse.rightButtonDown();
			}
			else
				ControllerSettings.get(JoyBindingEnum.joyBindMenu).wasPressed(); // auto handled
		}

		if (mc.currentScreen != null)
		{

			// This call here re-points the mouse position that Minecraft picks
			// up to determine if it should do the Hover over button effect.
			joypadMouse.hack_mouseXY(joypadMouse.mcX, joypadMouse.mcY);

			if (joypadMouse.leftButtonHeld || joypadMouse.rightButtonHeld)
			{
				joypadMouse.gui_mouseDrag(joypadMouse.x, joypadMouse.y);
				VirtualMouse.hack_mouseButton(joypadMouse.leftButtonHeld ? 0 : 1);
			}
		}
		else
		{
			joypadMouse.UnpressButtons();
		}
	}

	private static void HandleGuiMousePostRender()
	{

		if (mc.currentScreen == null || !JoypadMod.controllerSettings.isInputEnabled())
			return;

		int x = joypadMouse.x;
		int y = joypadMouse.y;

		Gui.drawRect(x - 3, y, x + 4, y + 1, reticalColor);
		Gui.drawRect(x, y - 3, x + 1, y + 4, reticalColor);
	}

	private static int attackKeyCode = McObfuscationHelper.keyCode(mc.gameSettings.keyBindAttack);
	private static int useKeyCode = McObfuscationHelper.keyCode(mc.gameSettings.keyBindUseItem);

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
					LogHelper.Info("Initiating attack ontick");
					KeyBinding.onTick(attackKeyCode);
					attackHeld = true;
				}
			}
			else if (ControllerSettings.get(JoyBindingEnum.joyBindInteract).wasPressed()
					|| ControllerSettings.get(JoyBindingEnum.joyBindUseItem).wasPressed())
			{
				if (!useHeld)
				{
					LogHelper.Info("Initiating use ontick");
					KeyBinding.onTick(useKeyCode);
					useHeld = true;
				}
			}
			else if (ControllerSettings.get(JoyBindingEnum.joyBindNextItem).wasPressed())
			{
				LogHelper.Info("NextItem pressed");
				mc.thePlayer.inventory.changeCurrentItem(-1);
			}
			else if (ControllerSettings.get(JoyBindingEnum.joyBindPrevItem).wasPressed())
			{
				LogHelper.Info("PrevItem pressed");
				mc.thePlayer.inventory.changeCurrentItem(1);
			}

			// these should go after the waspressed calls
			if (useHeld)
			{
				if (!ControllerSettings.get(JoyBindingEnum.joyBindUseItem).isPressed())
				{
					KeyBinding.setKeyBindState(useKeyCode, false);
					useHeld = false;
				}
				else
				{
					KeyBinding.setKeyBindState(useKeyCode, true);
				}
			}

			if (attackHeld)
			{
				if (!ControllerSettings.get(JoyBindingEnum.joyBindAttack).isPressed())
				{
					KeyBinding.setKeyBindState(attackKeyCode, false);
					attackHeld = false;
				}
				else
				{
					KeyBinding.setKeyBindState(attackKeyCode, true);
				}
			}
		}

		// Read joypad movement
		VirtualMouse.updateCameraAxisReading(false);
		mc.thePlayer.setAngles(VirtualMouse.deltaX, VirtualMouse.deltaY
				* (ControllerSettings.getInvertYAxis() ? 1.0f : -1.0f));

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
