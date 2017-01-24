package org.outerj.pollo.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

/**
 * ColorSlider is a Swing component to let a user select a color using the mouse.
 * The colorslider consists of 3 colored bars for selecting the red, green and blue
 * color components. Since the bars themselves are colored according to the color you
 * will get when you move the slider to a certain position, selecting colors becomes
 * much easier.
 *
 * <p>To get notified of the current color, you can add a PropertyChangeListener that
 * listens for the property ColorSlider.COLOR. The value of the property is a Color object.
 *
 * @author Bruno Dumon (bruno at outerthought dot org)
 */
public class ColorSlider extends JComponent
{
    protected int red, green, blue;
    protected int width, height, redStart, redEnd, greenStart, greenEnd, blueStart, blueEnd;
    protected boolean dragging = false;
    protected int draggingSlider;
    protected final int RED = 0, GREEN = 1, BLUE = 2;

    public static final String COLOR = "colorslider.color";

    public ColorSlider(int red, int green, int blue)
    {
        this.red = red;
        this.green = green;
        this.blue = blue;

        addMouseListener(new MouseAdapter()
        {
            public void mousePressed(MouseEvent e)
            {
                handleMouseEvent(e);
            }

            public void mouseReleased(MouseEvent e)
            {
                dragging = false;
            }
        });

        addMouseMotionListener(new MouseMotionAdapter()
        {
            public void mouseDragged(MouseEvent e)
            {
                if (dragging != true)
                {
                    dragging = true;
                    draggingSlider = getSlider(e.getY());
                }
                handleMouseEvent(e);
            }
        });

        Dimension wantedSize = new Dimension(200, 30);
        setPreferredSize(wantedSize);
        setMinimumSize(wantedSize);
    }

    public ColorSlider()
    {
        this(0, 0, 0);
    }

    public Color getColor()
    {
        return new Color(red, green, blue);
    }

    public void setColor(Color color)
    {
        Color oldColor = getColor();
        this.red = color.getRed();
        this.green = color.getGreen();
        this.blue = color.getBlue();
        firePropertyChange(COLOR, oldColor, color);
        repaint();
    }

    public void paint(Graphics g)
    {
        Dimension dimension = getSize();
        int width = (int)dimension.getWidth();
        calculateSizes();

        // draw red changer
        for (int i = 0; i < width; i++)
        {
            g.setColor(new Color(255 * i / width, green, blue));
            g.drawLine(i, redStart, i, redEnd);
        }
        drawCurrentValueIndicator(g, red, (redEnd - redStart) / 2);

        // draw green changer
        for (int i = 0; i < width; i++)
        {
            g.setColor(new Color(red, 255 * i / width, blue));
            g.drawLine(i, greenStart, i, greenEnd);
        }
        drawCurrentValueIndicator(g, green, greenStart + ((greenEnd - greenStart) / 2));

        // draw blue changer
        for (int i = 0; i < width; i++)
        {
            g.setColor(new Color(red, green, 255 * i / width));
            g.drawLine(i, blueStart, i, blueEnd);
        }
        drawCurrentValueIndicator(g, blue, blueStart + ((blueEnd - blueStart) / 2));
    }

    private void drawCurrentValueIndicator(Graphics g, int value, int yPos)
    {
        g.setXORMode(Color.white);
        g.fillOval(colorToPos(value), yPos, 4, 4);
        g.setPaintMode();
    }

    protected int colorToPos(int colorValue)
    {
        return (int)((float)colorValue / 255f * (float)width);
    }

    protected void calculateSizes()
    {
        Dimension dimension = getSize();

        width = (int)dimension.getWidth();
        height = (int)dimension.getHeight();

        redStart = 0;
        redEnd = height / 3;
        greenStart = redEnd + 1;
        greenEnd = redEnd * 2;
        blueStart = greenEnd + 1;
        blueEnd = height;
    }

    /**
     * Returns RED, GREEN or BLUE according to which slider the given y coordinate lies in.
     */
    protected int getSlider(int y)
    {
        if (y >= redStart && y <= redEnd)
            return RED;
        else if (y >= greenStart && y <= greenEnd)
            return GREEN;
        else if (y >= blueStart && y <= blueEnd)
            return BLUE;
        else
            throw new RuntimeException("[ColorSlider] Coordinate lies outside any color area?!");
    }

    /**
     * Changes the current color depending on a MouseEvent.
     */
    protected void handleMouseEvent(MouseEvent e)
    {
        calculateSizes();
        Color oldColor = new Color(red, green, blue);

        int y = e.getY();
        int x = (int)((float)e.getX() / (float)width * 255f);
        x = x > 255 ? 255 : x;
        x = x < 0 ? 0 : x;

        int slider = dragging == true ? draggingSlider : getSlider(y);
        switch (slider)
        {
            case RED:
                red = x;
                break;
            case GREEN:
                green = x;
                break;
            case BLUE:
                blue = x;
                break;
        }
        repaint();

        Color newColor = new Color(red, green, blue);
        firePropertyChange(COLOR, oldColor, newColor);
    }
}
