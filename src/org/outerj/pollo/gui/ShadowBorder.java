package org.outerj.pollo.gui;

import javax.swing.border.AbstractBorder;
import java.awt.*;

public class ShadowBorder extends AbstractBorder
{
    private static final Color borderColor = new Color(132, 130, 132);
    private static final Color shadow1Color = new Color(143, 141, 138);
    private static final Color shadow2Color = new Color(171, 168, 165);
    private static final ShadowBorder instance = new ShadowBorder();

    public Insets getBorderInsets(Component c)
    {
        return new Insets(1, 1, 3, 3);
    }

    public Insets getBorderInsets(Component c, Insets insets)
    {
        insets.left = insets.top = 1;
        insets.right = insets.bottom = 3;
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

        g.setColor(borderColor);
        g.drawRect(0, 0, width - 3, height - 3);

        g.setColor(shadow1Color);
        g.drawLine(width - 2, 1, width - 2, height - 2);
        g.drawLine(1, height - 2, width - 2, height - 2);

        g.setColor(shadow2Color);
        g.drawLine(width - 1, 2, width - 1, height - 3);
        g.drawLine(2, height - 1, width - 3, height - 1);

        g.translate(-x, -y);
        g.setColor(oldColor);
    }

    /**
     * Border instances may be reused, hence this method that always returns the
     * same instance
     */
    public static ShadowBorder getInstance()
    {
        return instance;
    }
}
