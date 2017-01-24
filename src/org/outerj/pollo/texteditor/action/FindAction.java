package org.outerj.pollo.texteditor.action;

import org.outerj.pollo.texteditor.XmlTextEditor;
import org.outerj.pollo.util.ResourceManager;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

public class FindAction extends AbstractAction
{
    protected XmlTextEditor xmlTextEditor;
    protected static final ResourceManager resourceManager = ResourceManager.getManager(FindAction.class);

    public FindAction(XmlTextEditor xmlTextEditor)
    {
        resourceManager.configureAction(this);
        this.xmlTextEditor = xmlTextEditor;
    }

    public void actionPerformed(ActionEvent e)
    {
        String searchString=javax.swing.JOptionPane.showInputDialog(xmlTextEditor, "Find...");
        xmlTextEditor.find(searchString);
    }

}
