package org.outerj.pollo.texteditor.action;

import org.outerj.pollo.texteditor.XmlTextEditor;
import org.outerj.pollo.util.ResourceManager;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class CutAction extends AbstractAction
{
    protected XmlTextEditor xmlTextEditor;
    protected static final ResourceManager resourceManager = ResourceManager.getManager(CutAction.class);

    public CutAction(XmlTextEditor xmlTextEditor)
    {
        resourceManager.configureAction(this);
        this.xmlTextEditor = xmlTextEditor;
    }

    public void actionPerformed(ActionEvent e)
    {
        xmlTextEditor.cut();
    }

}
