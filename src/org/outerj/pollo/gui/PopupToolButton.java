package org.outerj.pollo.gui;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * A ToolButton that shows a popupmenu when clicked
 * on it.
 */
public class PopupToolButton extends ToolButton
{
    protected JPopupMenu popupMenu;

    /**
     * @param title A title that will appear as first item in the jmenu,
     *              set to null if you don't want that.
     */
    public PopupToolButton(String title, String tooltip, Icon icon)
    {
        setIcon(icon);
        setToolTipText(tooltip);
        this.popupMenu = new JPopupMenu();
        if (title != null)
        {
            JMenuItem titleItem = new JMenuItem(title);
            titleItem.setEnabled(false);
            popupMenu.add(titleItem);
            popupMenu.addSeparator();
        }

        addActionListener(new PopupButtonActionListener());

        setRequestFocusEnabled(false);
    }

    public void addAction(Action action)
    {
        popupMenu.add(action);
    }

    public void addItem(JMenuItem menuItem)
    {
        popupMenu.add(menuItem);
    }

    public class PopupButtonActionListener implements ActionListener
    {
        public void actionPerformed(ActionEvent e)
        {
            popupMenu.show(PopupToolButton.this, 0, getHeight());
        }
    }
}
