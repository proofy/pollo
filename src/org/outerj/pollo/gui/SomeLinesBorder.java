package org.outerj.pollo.gui;

import javax.swing.border.AbstractBorder;
import java.awt.*;

/**
 * Simple line border that hasn't necesseraly borders on all sides.
 */
public class SomeLinesBorder extends AbstractBorder
{
    private static final Color lineColor = new Color(132, 130, 132);

    private final boolean top;
    private final boolean left;
    private final boolean bottom;
    private final boolean right;

    public SomeLinesBorder(boolean top, boolean left, boolean bottom, boolean right)
    {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }

    public Insets getBorderInsets(Component c)
    {
        return new Insets(top ? 1 : 0, left ? 1 : 0, bottom ? 1 : 0, right ? 1 : 0);
    }

    public Insets getBorderInsets(Component c, Insets insets)
    {
        insets.top = top ? 1 : 0;
        insets.left = left ? 1 :0;
        insets.bottom = bottom ? 1 : 0;
        insets.right = right ? 1 : 0;
        return insets;
    }

    public boolean isBorderOpaque()
    {
        return true;
    }

    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height)
    {
        Color oldColor = g.getColor();
        g.translate(x, y);

        g.setColor(lineColor);

        if (top)
            g.drawLine(0, 0, width - 1, 0);

        if (left)
            g.drawLine(0, 0, height - 1, 0);

        if (bottom)
            g.drawLine(0, height - 1, width - 1, height - 1);

        if (right)
            g.drawLine(width - 1, 0, width - 1, height - 1);

        g.translate(-x, -y);
        g.setColor(oldColor);
    }
}
