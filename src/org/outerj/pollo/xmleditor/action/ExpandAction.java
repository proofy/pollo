package org.outerj.pollo.xmleditor.action;

import org.outerj.pollo.xmleditor.XmlEditor;
import org.outerj.pollo.xmleditor.view.View;
import org.outerj.pollo.util.ResourceManager;
import org.w3c.dom.Node;

import java.awt.event.ActionEvent;

public class ExpandAction extends AbstractNodeAction
{
    protected static final ResourceManager resourceManager = ResourceManager.getManager(ExpandAction.class);

    public ExpandAction(XmlEditor xmlEditor)
    {
        super(xmlEditor);
        resourceManager.configureAction(this);
    }

    public void actionPerformed(ActionEvent e)
    {
        View view = xmlEditor.getSelectionInfo().getSelectedNodeView();
        if (view.getNode().getNodeType() != Node.DOCUMENT_NODE)
            view.expand();
    }
}
