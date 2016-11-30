package com.ivan.xinput;

/**
 * Represents the delta (change) of states between two successive polls.
 *
 * @author Ivan "StrikerX3" Oliveira
 */
public class XInputComponentsDelta {
    private final XInputButtonsDelta buttonsDelta;
    private final XInputAxesDelta axesDelta;

    protected XInputComponentsDelta(final XInputComponents lastComps, final XInputComponents comps) {
        super();
        buttonsDelta = new XInputButtonsDelta(lastComps.getButtons(), comps.getButtons());
        axesDelta = new XInputAxesDelta(lastComps.getAxes(), comps.getAxes());
    }

    /**
     * Returns the delta of the buttons.
     *
     * @return the delta of the buttons.
     */
    public XInputButtonsDelta getButtons() {
        return buttonsDelta;
    }

    /**
     * Returns the delta of the axes.
     *
     * @return the delta of the axes.
     */
    public XInputAxesDelta getAxes() {
        return axesDelta;
    }
}
