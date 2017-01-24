package org.outerj.pollo.xmleditor.action;

import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.util.ResourceManager;
import org.w3c.dom.Node;

import java.awt.event.ActionEvent;

/**
 * A Swing Action that places the currently selected node on the clipboard
 * (an internal clipboard, not the operating system clipboard) and then
 * deletes the node.
 *
 * This action automatically enables/disables itself.
 *
 * @author Bruno Dumon
 */
public class CutAction extends RemoveAction
{
    protected static final ResourceManager resourceManager = ResourceManager.getManager(CutAction.class);

    public CutAction(XmlEditor xmlEditor)
    {
        super(xmlEditor);
        resourceManager.configureAction(this);
    }

    public void actionPerformed(ActionEvent e)
    {
        Node node = xmlEditor.getSelectionInfo().getSelectedNode();
        if (node != xmlEditor.getRootView().getNode())
        {
            xmlEditor.putOnClipboard(node);
            super.actionPerformed(e);
        }
    }

}
