package org.outerj.pollo.texteditor.action;

import org.outerj.pollo.texteditor.XmlTextEditor;
import org.outerj.pollo.util.ResourceManager;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class CopyAction extends AbstractAction
{
    protected XmlTextEditor xmlTextEditor;
    protected static final ResourceManager resourceManager = ResourceManager.getManager(CopyAction.class);

    public CopyAction(XmlTextEditor xmlTextEditor)
    {
        resourceManager.configureAction(this);
        this.xmlTextEditor = xmlTextEditor;
    }

    public void actionPerformed(ActionEvent e)
    {
        xmlTextEditor.copy();
    }

}
