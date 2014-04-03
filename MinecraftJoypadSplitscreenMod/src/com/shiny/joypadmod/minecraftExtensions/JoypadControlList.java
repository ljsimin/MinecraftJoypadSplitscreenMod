package com.shiny.joypadmod.minecraftExtensions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.settings.KeyBinding;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.input.Controllers;
import org.lwjgl.input.Mouse;

import com.shiny.joypadmod.ControllerSettings;
import com.shiny.joypadmod.helpers.LogHelper;
import com.shiny.joypadmod.helpers.McObfuscationHelper;
import com.shiny.joypadmod.helpers.ModVersionHelper;
import com.shiny.joypadmod.inputevent.ButtonInputEvent;
import com.shiny.joypadmod.inputevent.ControllerBinding;
import com.shiny.joypadmod.inputevent.ControllerBinding.BindingOptions;
import com.shiny.joypadmod.inputevent.ControllerInputEvent;
import com.shiny.joypadmod.inputevent.ControllerInputEvent.EventType;
import com.shiny.joypadmod.lwjglVirtualInput.VirtualMouse;

import cpw.mods.fml.client.GuiScrollingList;

public class JoypadControlList extends GuiScrollingList
{
	private FontRenderer fontRenderer;
	private int controllerInputTimeout = 5000;
	public long controllerTickStart = 0;

	private static final int buttonHeight = 20;

	public static int lastXClick = 0;
	public static int lastYClick = 0;

	private int selectedIndex = -1;
	public int bindingIndexToUpdate = -1;
	private JoypadConfigMenu parent;
	private int lastListSize = 0;

	public List<ControllerBinding> joyBindings;

	public JoypadControlList(JoypadConfigMenu parent, FontRenderer fontRenderer)
	{

		super(parent.mc, parent.controlListWidth, // width
				parent.height, // height
				parent.controlListYStart, // top start
				parent.controlListYStart + parent.controlListHeight, // bottom end
				parent.controlListXStart, // left start
				buttonHeight); // entryHeight

		LogHelper.Info("width:" + parent.controlListWidth + " height:" + parent.height + " yStart:"
				+ parent.controlListYStart + " bottomEnd:" + (parent.controlListYStart + parent.controlListHeight)
				+ "xStart: " + parent.controlListXStart);

		this.parent = parent;
		this.fontRenderer = fontRenderer;
		joyBindings = new ArrayList<ControllerBinding>();
		if (this.parent.currentJoyIndex != -1)
		{
			updateJoyBindings();
		}
	}

	public void updateJoyBindings()
	{
		joyBindings.clear();

		KeyBinding[] akeybinding = (KeyBinding[]) ArrayUtils.clone(parent.mc.gameSettings.keyBindings);

		if (ModVersionHelper.getVersion() >= 172)
			Arrays.sort(akeybinding);

		ControllerSettings.setDefaultJoyBindingMap(parent.getCurrentControllerId(), true);

		String category = "joy.categories.guicontrol";
		joyBindings.add(new ControllerBinding(null, null, null, null, 0, EnumSet.of(BindingOptions.CATEGORY_GUICONTROL)));

		for (KeyBinding key : akeybinding)
		{
			String target = McObfuscationHelper.getKeyDescription(key).replace("key.", "joy.");
			ControllerBinding b;
			if (ControllerSettings.joyBindingsMap.containsKey(target))
			{
				b = ControllerSettings.joyBindingsMap.get(target);
			}
			else
			{
				BindingOptions bindCategory = ControllerBinding.mapMinecraftCategory(McObfuscationHelper.getKeyCategory(key));
				b = new ControllerBinding(target, McObfuscationHelper.getKeyDescription(key), new ButtonInputEvent(
						this.parent.getCurrentControllerId(), -1, 1), new int[] { McObfuscationHelper.keyCode(key) },
						0, EnumSet.of(BindingOptions.GAME_BINDING, bindCategory));
			}
			if (b.getCategoryString() != category)
			{
				// get any other bindings that are of this category but not originating from Minecraft
				List<ControllerBinding> otherBindings = ControllerSettings.getBindingsWithCategory(ControllerBinding.mapMinecraftCategory(category));
				for (ControllerBinding binding : otherBindings)
				{
					if (!joyBindings.contains(binding))
						joyBindings.add(binding);
				}
				ControllerBinding catBinding = new ControllerBinding(null, null, null, null, 0, b.bindingOptions);
				joyBindings.add(catBinding);
				category = b.getCategoryString();
			}
			joyBindings.add(b);
		}
	}

	@Override
	protected int getSize()
	{
		return joyBindings.size();
	}

	@Override
	protected void elementClicked(int index, boolean doubleClick)
	{
		selectedIndex = index;
	}

	@Override
	protected boolean isSelected(int index)
	{
		return false;
	}

	@Override
	protected void drawBackground()
	{
		parent.drawBackground(0);
	}

	public boolean getControllerInput()
	{
		try
		{
			while (Controllers.next())
			{
				if (Minecraft.getSystemTime() - controllerTickStart < 200)
				{
					LogHelper.Info("Discarding events that occured too soon after last button click");
				}
				else
				{
					ControllerInputEvent inputEvent = ControllerSettings.controllerUtils.getLastEvent(
							Controllers.getController(parent.getCurrentControllerId()),
							Controllers.getEventControlIndex());
					if (inputEvent != null)
					{
						float threshold = inputEvent.getThreshold();
						LogHelper.Info("Received from controller: " + inputEvent.getName() + " threshold: " + threshold);

						if (inputEvent.getEventType() == EventType.AXIS)
						{
							threshold = ControllerSettings.defaultAxisThreshhold * (threshold > 0 ? 1 : -1);
						}
						else if (inputEvent.getEventType() == EventType.POV)
						{
							threshold = ControllerSettings.defaultPovThreshhold * (threshold > 0 ? 1 : -1);
						}
						else
						{
							threshold = 1;
						}
						inputEvent.setThreshold(threshold);
						ControllerBinding binding = joyBindings.get(bindingIndexToUpdate);
						binding.inputEvent = inputEvent;
						ControllerSettings.setControllerBinding(parent.getCurrentControllerId(), binding.inputString,
								binding);
						return true;
					}
				}
			}
		}
		catch (Exception ex)
		{
			LogHelper.Error("Caught exception while trying to set controller button! " + ex.toString());
		}
		return false;
	};

	int wheelDown = 0;

	@Override
	protected void drawSlot(int var1, int var2, int var3, int var4, Tessellator var5)
	{
		if (var1 >= joyBindings.size())
			return;

		if (wheelDown-- > 0)
			VirtualMouse.scrollWheel(-1);
		if (lastListSize != getSize())
		{
			if (lastListSize > 0 && getSize() > lastListSize)
			{
				wheelDown = 350;
				selectedIndex = getSize() - 1;
			}
			else
			{
				selectedIndex = Math.max(-1, selectedIndex - 1);
			}
			lastListSize = getSize();
		}

		if (joyBindings.get(var1).inputString == null)
		{
			// this is a new category
			String category = McObfuscationHelper.lookupString(joyBindings.get(var1).getCategoryString());

			this.fontRenderer.drawString(category,
					this.left + this.listWidth / 2 - this.fontRenderer.getStringWidth(category) / 2, var3 + 5, -1);
			return;
		}

		String controlDescription = McObfuscationHelper.lookupString(joyBindings.get(var1).inputString);

		this.fontRenderer.drawString(this.fontRenderer.trimStringToWidth(controlDescription, 110), this.left + 3, var3
				+ buttonHeight / 2 - this.fontRenderer.FONT_HEIGHT / 2, 0xFF2222);

		// default to showing the master binding
		ControllerBinding bindingToDisplay = ControllerSettings.joyBindingsMap.get(joyBindings.get(var1).inputString);
		if (bindingToDisplay == null)
		{
			// this case is true if the user hasn't bound this option to any joypad button and thus isn't in the master binding list
			bindingToDisplay = joyBindings.get(var1);
		}
		drawControlButtons(var1, this.left + 120, var3, bindingToDisplay, var1 == selectedIndex);

		if (bindingIndexToUpdate != -1)
		{
			if (getControllerInput() || Minecraft.getSystemTime() - controllerTickStart > controllerInputTimeout)
			{
				bindingIndexToUpdate = -1;
				ControllerSettings.suspendControllerInput(false, 0);
			}
		}
	}

	private void drawControlButtons(int id, int x, int y, ControllerBinding binding, boolean slotSelected)
	{
		if (binding == null)
			return;

		Minecraft mc = Minecraft.getMinecraft();
		final ScaledResolution scaledResolution = new ScaledResolution(mc.gameSettings, mc.displayWidth,
				mc.displayHeight);

		final int k = Mouse.getX() * scaledResolution.getScaledWidth() / mc.displayWidth;
		final int i1 = scaledResolution.getScaledHeight() - Mouse.getY() * scaledResolution.getScaledHeight()
				/ mc.displayHeight - 1;

		int controlButtonWidth = 60;
		int smallButtonWidth = 15;

		// check if any buttons need updating
		if (slotSelected && lastYClick >= y && lastYClick <= y + buttonHeight && lastXClick >= x)
		{
			lastYClick = 0;
			// we are in range of the buttons
			if (lastXClick <= x + controlButtonWidth)
			{
				bindingIndexToUpdate = id;
				controllerTickStart = Minecraft.getSystemTime();
				ControllerSettings.suspendControllerInput(true, 10000);
			}
			else
			{
				if (lastXClick <= x + controlButtonWidth + smallButtonWidth)
				{
					if (binding.inputEvent.isValid())
					{
						ControllerSettings.unsetControllerBinding(parent.getCurrentControllerId(), binding.inputString);
						binding.inputEvent = new ButtonInputEvent(-1, -1, 0);
					}
					else if (binding.inputString.contains("user"))
					{
						// delete this user binding
						ControllerSettings.delete(binding.inputString);
						joyBindings.remove(id);
						return;
					}

				}
				else if (lastXClick <= x + controlButtonWidth + smallButtonWidth * 2)
				{
					if (binding.inputEvent.getEventType() != EventType.AXIS)
					{
						if (binding.bindingOptions.contains(BindingOptions.IS_TOGGLE))
							binding.bindingOptions.remove(BindingOptions.IS_TOGGLE);
						else
							binding.bindingOptions.add(BindingOptions.IS_TOGGLE);
					}
				}
			}
		}

		String controlButtonStr = "NONE";
		if (this.parent.currentJoyIndex != -1)
		{
			if (bindingIndexToUpdate != -1
					&& binding.inputString.compareTo(joyBindings.get(bindingIndexToUpdate).inputString) == 0)
				controlButtonStr = "> ?? <";
			else
			{
				controlButtonStr = ControllerSettings.controllerUtils.getHumanReadableInputName(
						Controllers.getController(this.parent.currentJoyIndex), binding.inputEvent);
			}
		}

		controlButtonStr = this.fontRenderer.trimStringToWidth(controlButtonStr, controlButtonWidth - 2);

		GuiButton b = new GuiButton(10001, x, y, controlButtonWidth, buttonHeight, controlButtonStr);
		b.drawButton(mc, k, i1);

		// - or x
		// draw a minus if the button is currently valid
		char optionRemove = '-';
		boolean enable = true;
		if (!binding.inputEvent.isValid())
		{
			// else draw an X if the button is currently invalid and its a user binding
			if (binding.inputString.contains("user."))
				optionRemove = (char) 0x2716;
			else
				// disable the button if the input is currently invalid
				enable = false;
		}

		if (enable)
		{
			b = new GuiButton(10002, x + controlButtonWidth, y, smallButtonWidth, buttonHeight, "" + optionRemove);
			b.drawButton(mc, k, i1);

			// draw the toggle option button
			if (binding.inputEvent.getEventType() != EventType.AXIS)
			{
				char toggle = 9675;
				if (binding.bindingOptions.contains(BindingOptions.IS_TOGGLE))
					toggle = 9679;
				b = new GuiButton(10003, x + controlButtonWidth + smallButtonWidth, y, smallButtonWidth, buttonHeight,
						"" + toggle);
				b.drawButton(mc, k, i1);
			}
		}
	}
}
