package org.outerj.pollo.xmleditor.action;

import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.view.View;
import org.outerj.pollo.util.ResourceManager;
import org.w3c.dom.Node;

import java.awt.event.ActionEvent;

public class RemoveAction extends AbstractNodeAction
{
    protected static final ResourceManager resourceManager = ResourceManager.getManager(RemoveAction.class);

    public RemoveAction(XmlEditor xmlEditor)
    {
        super(xmlEditor);
        resourceManager.configureAction(this);
    }

    public void actionPerformed(ActionEvent e)
    {
        // delete the selected node and select the previous node
        Node node = xmlEditor.getSelectionInfo().getSelectedNode();

        // it is not allowed to delete the (visible) root node
        if (node != xmlEditor.getRootView().getNode())
        {
            View newSelectedView = xmlEditor.getSelectionInfo().getSelectedNodeView().getNextButNotChild();
            Node parent = node.getParentNode();
            parent.removeChild(node);

            if (newSelectedView != null)
            {
                int startV = newSelectedView.getVerticalPosition();
                int startH = newSelectedView.getHorizontalPosition();
                newSelectedView.markAsSelected(startH, startV);
                xmlEditor.scrollAlignTop(startV, newSelectedView.getHeight());
            }
        }
    }
}
