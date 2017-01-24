package org.outerj.pollo.xmleditor;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * A icon consisting of a colored box.
 *
 * @author Bruno Dumon
 */
public class ElementColorIcon implements Icon
{
    public static final int ICON_WIDTH = 16;
    public static final int ICON_HEIGHT = 16;

    protected Image image;

    public ElementColorIcon(Color color)
    {
        this.image = new BufferedImage(ICON_WIDTH, ICON_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D gr = (Graphics2D)image.getGraphics();

        gr.setColor(color);
        Polygon poly = new Polygon();
        poly.addPoint(3, 4);
        poly.addPoint(9, 1);
        poly.addPoint(15, 5);
        poly.addPoint(15, 12);
        poly.addPoint(9, 16);
        poly.addPoint(3, 12);
        gr.fillPolygon(poly);

        gr.setColor(Color.black);
        gr.drawPolygon(poly);

        gr.drawLine(3, 4, 9, 8);
        gr.drawLine(9, 8, 15, 5);
        gr.drawLine(9, 8, 9, 16);
    }

    public int getIconHeight()
    {
        return ICON_HEIGHT;
    }

    public int getIconWidth()
    {
        return ICON_WIDTH;
    }

    public void paintIcon(Component c, Graphics gr, int x, int y)
    {
        gr.drawImage(image, x, y, c);
    }
}
