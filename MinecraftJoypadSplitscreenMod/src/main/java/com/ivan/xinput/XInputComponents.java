package com.ivan.xinput;

/**
 * Contains all components for an XInput controller.
 *
 * @author Ivan "StrikerX3" Oliveira
 */
public class XInputComponents {
    private final XInputButtons buttons;
    private final XInputAxes axes;

    protected XInputComponents() {
        buttons = new XInputButtons();
        axes = new XInputAxes();
    }

    /**
     * Returns the XInput button states.
     *
     * @return the XInput button states
     */
    public XInputButtons getButtons() {
        return buttons;
    }

    /**
     * Returns the XInput axis states.
     *
     * @return the XInput axis states
     */
    public XInputAxes getAxes() {
        return axes;
    }

    /**
     * Resets the components to their default values.
     */
    protected void reset() {
        buttons.reset();
        axes.reset();
    }

    /**
     * Copies the values from the specified components.
     *
     * @param components the components to copy the values from
     */
    protected void copy(final XInputComponents components) {
        buttons.copy(components.getButtons());
        axes.copy(components.getAxes());
    }
}
