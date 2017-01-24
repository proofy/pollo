package org.outerj.pollo.util;

import javax.swing.*;
import java.awt.*;

public class Utilities
{
    /**
     * Creates a JMenuItem based on an Action. The Swing implementation
     * in JDK 1.3 will not copy the accelerator from the action to the
     * JMenuItem, therefore this method will do this additionally.
     */
    public static JMenuItem createMenuItemFromAction(Action action)
    {
        JMenuItem menuItem = new JMenuItem();
        menuItem.setAction(action);
        menuItem.setAccelerator((KeyStroke)action.getValue(Action.ACCELERATOR_KEY));

        return menuItem;
    }

    /**
     * Removes actions from JMenuItems. This is necessary because JMenuItem's
     * add an event listener to the action, and if the action lives longer
     * then this menu, we would have a memory leak.
     */
    public static void destructMenus(Component [] components)
    {
        for (int i = 0; i < components.length; i++)
        {
            if (components[i] instanceof JMenu)
            {
                destructMenus(((JMenu)components[i]).getMenuComponents());
            }
            else if (components[i] instanceof JPopupMenu)
            {
                destructMenus(((JPopupMenu)components[i]).getComponents());
            }
            else if (components[i] instanceof JMenuItem)
            {
                JMenuItem item = (JMenuItem)components[i];
                item.setAction(null);
            }
            else if (components[i] instanceof JButton)
            {
                JButton item = (JButton)components[i];
                item.setAction(null);
            }
        }
    }

}
