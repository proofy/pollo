package org.outerj.pollo.gui;

import javax.swing.*;

/**
 * A ToolButton is just like a normal JButton but:
 *  - it has no text on it
 *  - it can not get focus
 */
public class ToolButton extends JButton
{
    public ToolButton(Action action)
    {
        super(action);
        setText(null);
        setMnemonic('\0');
        setRequestFocusEnabled(false);
    }

    /**
     * Empty constructor for subclasses.
     */
    protected ToolButton()
    {
    }

    public void setText(String text)
    {
        // toolbuttons have no text

        // the reason for overriding this method is because when the text of the
        // corresponding Action changes (such as for undo), the text
        // would appear again on the button
    }
}
