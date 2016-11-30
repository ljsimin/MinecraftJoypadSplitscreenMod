package com.ivan.xinput;

import com.ivan.xinput.enums.XInputButton;

/**
 * Represents the delta (change) of the buttons between two successive polls.
 *
 * @author Ivan "StrikerX3" Oliveira
 */
public class XInputButtonsDelta {
    private final XInputButtons lastButtons;
    private final XInputButtons buttons;

    protected XInputButtonsDelta(final XInputButtons lastButtons, final XInputButtons buttons) {
        this.lastButtons = lastButtons;
        this.buttons = buttons;
    }

    /**
     * Returns <code>true</code> if the button was pressed (i.e. changed from released to pressed between two consecutive polls).
     * 
     * @param button the button
     * @return <code>true</code> if the button was pressed, <code>false</code> otherwise
     */
    public boolean isPressed(final XInputButton button) {
        return delta(lastButtons, buttons, button);
    }

    /**
     * Returns <code>true</code> if the button was released (i.e. changed from pressed to released between two consecutive polls).
     * 
     * @param button the button
     * @return <code>true</code> if the button was released, <code>false</code> otherwise
     */
    public boolean isReleased(final XInputButton button) {
        return delta(buttons, lastButtons, button);
    }

    /**
     * Determines if the state of a button was changed from one poll to the following poll.
     * 
     * @param from the old state
     * @param to the new state
     * @param button the button
     * @return <code>true</code> if there was a change, <code>false</code> otherwise
     */
    private boolean delta(final XInputButtons from, final XInputButtons to, final XInputButton button) {
        switch (button) {
            case A:
                return !from.a && to.a;
            case B:
                return !from.b && to.b;
            case X:
                return !from.x && to.x;
            case Y:
                return !from.y && to.y;
            case BACK:
                return !from.back && to.back;
            case START:
                return !from.start && to.start;
            case LEFT_SHOULDER:
                return !from.lShoulder && to.lShoulder;
            case RIGHT_SHOULDER:
                return !from.rShoulder && to.rShoulder;
            case LEFT_THUMBSTICK:
                return !from.lThumb && to.lThumb;
            case RIGHT_THUMBSTICK:
                return !from.rThumb && to.rThumb;
            case DPAD_UP:
                return !from.up && to.up;
            case DPAD_DOWN:
                return !from.down && to.down;
            case DPAD_LEFT:
                return !from.left && to.left;
            case DPAD_RIGHT:
                return !from.right && to.right;
            case GUIDE_BUTTON:
                return !from.guide && to.guide;
            case UNKNOWN:
                return !from.unknown && to.unknown;
        }
        return false;
    }
}
