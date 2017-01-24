package org.outerj.pollo.gui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

public class SmallButton extends JButton implements MouseListener
{
    private static final Border emptyBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
    private static final Border hoverBorder = BorderFactory.createBevelBorder(BevelBorder.RAISED);

    public SmallButton(Icon icon)
    {
        super(icon);
        format();
    }

    public SmallButton(String text)
    {
        super(text);
        format();
    }

    private void format()
    {
        setRequestFocusEnabled(false);
        setMargin(new Insets(0, 0, 0, 0));
        setFocusPainted(false);
        setBorder(emptyBorder);
        addMouseListener(this);
    }

    public void mouseClicked(MouseEvent e)
    {
    }

    public void mousePressed(MouseEvent e)
    {
    }

    public void mouseReleased(MouseEvent e)
    {
    }

    public void mouseEntered(MouseEvent e)
    {
        if (isEnabled())
            setBorder(hoverBorder);
    }

    public void mouseExited(MouseEvent e)
    {
        setBorder(emptyBorder);
    }
}
