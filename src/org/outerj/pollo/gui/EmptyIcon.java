package org.outerj.pollo.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Invisible icon, size 16 x 16. Used to align menu items.
 */
public class EmptyIcon implements Icon
{
    protected static final Icon instance16 = new EmptyIcon(16, 16);
    protected static final Icon instance20 = new EmptyIcon(20, 20);
    protected int width;
    protected int height;

    public static Icon getInstance()
    {
        return instance16;
    }

    public static Icon get16Instance()
    {
        return instance16;
    }

    public static Icon get20Instance()
    {
        return instance20;
    }

    public EmptyIcon(int height, int width)
    {
        this.height = height;
        this.width = width;
    }

    public void paintIcon(Component c, Graphics g, int x, int y)
    {
    }

    public int getIconWidth()
    {
        return width;
    }

    public int getIconHeight()
    {
        return height;
    }
}
