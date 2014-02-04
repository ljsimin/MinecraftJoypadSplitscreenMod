package com.shiny.joypadmod.minecraftExtensions;

import net.minecraft.util.MovementInput;

import com.shiny.joypadmod.ControllerSettings;

/**
 * Alternative MovementInput used to control the movement
 * with the game controller instead of a keyboard
 * @author shiny
 *
 */
public class ControllerMovementInput extends MovementInput {
	
	public boolean sneak = false;
    public boolean sprint = false;
	
	public ControllerMovementInput() {
	}

	@Override
	public void updatePlayerMoveState() 
	{
		moveStrafe = 0.0F;
		moveForward = 0.0F;

		if (ControllerSettings.inputEnabled && ControllerSettings.joystick != null)			
		{
			float xPlus = ControllerSettings.joyMovementXplus.getAnalogReading();
			float xMinus = ControllerSettings.joyMovementXminus.getAnalogReading();
			float xAxisValue = Math.abs(xPlus) > Math.abs(xMinus) ? xPlus : xMinus;
			
			float yPlus = ControllerSettings.joyMovementYplus.getAnalogReading();
			float yMinus = ControllerSettings.joyMovementYminus.getAnalogReading();
			float yAxisValue = Math.abs(yPlus) > Math.abs(yMinus) ? yPlus : yMinus;
			    		
    		moveStrafe =  -1*xAxisValue;
    		moveForward =  -1*yAxisValue;    
			
			jump = ControllerSettings.joyBindJump.isPressed();
			sneak = ControllerSettings.joyBindSneak.isPressed();
			if (sneak)
			{
				System.out.println("Sneak true in movementinput");
			}
			sprint = ControllerSettings.joyBindRun.isPressed();
		}
		else
		{
			super.updatePlayerMoveState();
		}		
	}
}