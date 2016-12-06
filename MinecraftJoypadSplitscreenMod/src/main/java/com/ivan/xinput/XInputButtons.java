package com.ivan.xinput;

import com.ivan.xinput.enums.XInputButton;

/**
 * Contains the states of all XInput buttons.
 *
 * @author Ivan "StrikerX3" Oliveira
 */
public class XInputButtons {
    public boolean a, b, x, y;
    public boolean back, start;
    public boolean lShoulder, rShoulder;
    public boolean lThumb, rThumb;
    public boolean up, down, left, right;
    public boolean guide, unknown;

    protected XInputButtons() {
        reset();
    }
    
    public Boolean isPressed(XInputButton button)
    {
    	switch(button)
    	{
    	case A:
    		return a;
    	case B:
    		return b;
    	case X:
    		return x;
    	case Y:
    		return y;
    	case BACK:
    		return back;
    	case START:
    		return start;
    	case LEFT_SHOULDER:
    		return lShoulder;
    	case RIGHT_SHOULDER:
    		return rShoulder;
    	case LEFT_THUMBSTICK:
    		return lThumb;
    	case RIGHT_THUMBSTICK:
    		return rThumb;
    	case DPAD_UP:
    		return up;
    	case DPAD_DOWN:
    		return down;
    	case DPAD_LEFT:
    		return left; 
    	case DPAD_RIGHT:
    		return right;
    	case GUIDE_BUTTON:
    		return guide;
    		
		default:
			return false;
    	
    	}
    }

    /**
     * Resets the state of all buttons.
     */
    protected void reset() {
        a = b = x = y = false;
        back = start = false;
        lShoulder = rShoulder = false;
        lThumb = rThumb = false;
        up = down = left = right = false;
        guide = unknown = false;
    }

    /**
     * Copies the state of all buttons from the specified state.
     *
     * @param buttons the state to copy from
     */
    protected void copy(final XInputButtons buttons) {
        a = buttons.a;
        b = buttons.b;
        x = buttons.x;
        y = buttons.y;

        back = buttons.back;
        start = buttons.start;

        lShoulder = buttons.lShoulder;
        rShoulder = buttons.rShoulder;

        lThumb = buttons.lThumb;
        rThumb = buttons.rThumb;

        up = buttons.up;
        down = buttons.down;
        left = buttons.left;
        right = buttons.right;

        guide = buttons.guide;
        unknown = buttons.unknown;
    }
}
