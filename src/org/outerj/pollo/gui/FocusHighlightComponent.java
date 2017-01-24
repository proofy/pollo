package org.outerj.pollo.gui;

import javax.swing.*;
import javax.swing.plaf.PanelUI;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;


/**
 * This class can be registered as a focuslistener to a component,
 * and will change the background color of an (other) component
 * when it the component receives the focus.
 */
public class FocusHighlightComponent implements FocusListener
{
    protected JComponent component;
    protected Color oldColor;
    protected static final Color highlightColor = new Color(203, 213, 229);

    /**
     * @param component the component of which the background will be changed
     */
    public FocusHighlightComponent(JComponent component)
    {
        this.component = component;
    }

    public void focusGained(FocusEvent event)
    {
        oldColor = component.getBackground();
        component.setBackground(highlightColor);
    }

    public void focusLost(FocusEvent event)
    {
        component.setBackground(oldColor);
    }
}
