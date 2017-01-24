package org.outerj.pollo.xmleditor.action;

import org.outerj.pollo.xmleditor.SelectionListener;
import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.util.ResourceManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * A Swing Action that places the currently selected node on the clipboard
 * (an internal clipboard, not the operating system clipboard).
 *
 * This action automatically enables/disables itself.
 *
 * @author Bruno Dumon
 */
public class CopyAction extends AbstractNodeAction
{
    protected static final ResourceManager resourceManager = ResourceManager.getManager(CopyAction.class);

    public CopyAction(XmlEditor xmlEditor)
    {
        super(xmlEditor);
        resourceManager.configureAction(this);
    }

    public void actionPerformed(ActionEvent e)
    {
        Node node = xmlEditor.getSelectedNode();
        if (!(node instanceof Document))
        {
            xmlEditor.putOnClipboard(xmlEditor.getSelectedNode());
        }
    }
}
