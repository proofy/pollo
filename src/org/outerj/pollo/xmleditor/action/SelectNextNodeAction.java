package org.outerj.pollo.xmleditor.action;

import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.view.View;
import org.outerj.pollo.gui.EmptyIcon;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * An action that selects the next node. Usefull to
 * connect to the arrow down key event.
 *
 * @author Bruno Dumon
 */
public class SelectNextNodeAction extends AbstractAction
{
    protected XmlEditor xmlEditor;

    public SelectNextNodeAction(XmlEditor xmlEditor)
    {
        super("Select next node", EmptyIcon.getInstance());
        this.xmlEditor = xmlEditor;
    }

    public void actionPerformed(ActionEvent e)
    {
        View selectedView = xmlEditor.getSelectionInfo().getSelectedNodeView();
        if (selectedView != null)
        {
            View nextView = selectedView.getNext(true);
            int startV = nextView.getVerticalPosition();
            int startH = nextView.getHorizontalPosition();
            nextView.markAsSelected(startH, startV);
            xmlEditor.scrollAlignBottom(startV, nextView.getHeight());
        }
    }
}
