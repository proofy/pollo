package org.outerj.pollo.xmleditor.action;

import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.SelectionListener;
import org.w3c.dom.Node;

import javax.swing.*;

/**
 * Abstract base class for Action's that do something with the selected node.
 * This base class will take care of enabling/disabling the action.
 */
public abstract class AbstractNodeAction extends AbstractAction implements SelectionListener
{
    protected XmlEditor xmlEditor;

    public AbstractNodeAction(XmlEditor xmlEditor)
    {
        this.xmlEditor = xmlEditor;
        xmlEditor.getSelectionInfo().addListener(this);
        setEnabled(false);
    }

    public void nodeUnselected(Node node)
    {
        setEnabled(false);
    }

    public void nodeSelected(Node node)
    {
        setEnabled(true);
    }
}
