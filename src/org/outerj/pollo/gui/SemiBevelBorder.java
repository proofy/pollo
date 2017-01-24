package org.outerj.pollo.gui;

import javax.swing.border.AbstractBorder;
import java.awt.*;

/**
 * Single-pixel bevel border with no line on the right side.
 */
public class SemiBevelBorder extends AbstractBorder
{
    private static final Color shadow1Color = Color.white;
    private static final Color shadow2Color = new Color(132, 130, 132);
    private static final SemiBevelBorder instance = new SemiBevelBorder();

    public Insets getBorderInsets(Component c)
    {
        return new Insets(1, 1, 1, 0);
    }

    public Insets getBorderInsets(Component c, Insets insets)
    {
        insets.left = insets.top = insets.bottom = 1;
        insets.right = 0;
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

        g.setColor(shadow1Color);
        g.drawLine(0, 0, width - 1, 0);
        g.drawLine(0, 0, 0, height - 1);

        g.setColor(shadow2Color);
        g.drawLine(0, height - 1, width - 1, height - 1);

        g.translate(-x, -y);
        g.setColor(oldColor);
    }

    /**
     * Border instances may be reused, hence this method that always returns the
     * same instance
     */
    public static SemiBevelBorder getInstance()
    {
        return instance;
    }
}
